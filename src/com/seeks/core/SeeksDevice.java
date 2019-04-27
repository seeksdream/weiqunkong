package com.seeks.core;

import com.android.chimpchat.ChimpManager;
import com.android.chimpchat.adb.AdbChimpDevice;
import com.android.chimpchat.core.IChimpDevice;
import com.android.chimpchat.core.TouchPressType;
import com.android.ddmlib.IDevice;
import com.android.ddmlib.RawImage;
import com.seeks.utils.BaiduAIUtils;
import com.seeks.utils.ImageUtils;
import com.seeks.utils.orc.OcrText;
import org.apache.commons.io.FileUtils;
import org.json.JSONArray;
import org.json.JSONObject;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;

public class SeeksDevice {
    private SeeksAction currentAction;
    private String name;
    public SeeksDevice(String name){
        this.name = name;
    }
    public void setCurrentAction(SeeksAction action) {
        this.currentAction = action;
    }
    public SeeksAction getCurrentAction(){
        return this.currentAction;
    }

    public String getName() {
        return this.name;
    }



    public static String HOME = "KEYCODE_HOME";
    public static String BACK = "KEYCODE_BACK";
    public static String MENU = "KEYCODE_MENU";
    public static String POWER = "KEYCODE_POWER";
    //    private static OperateAndroid oa; //操作移动设备的类
    private AdbChimpDevice device; //adb移动设备
    private IDevice iDevice; //adb移动设备

    /**
     * 初始化操作类
     *
     * @param dev 通过adb返回的device对象
     */
    public SeeksDevice(IDevice dev) {
        this.iDevice = dev;
        this.name = dev.getName();
        long start = System.currentTimeMillis();
        if (device == null) {
            device = new AdbChimpDevice(this.iDevice);
        }
    }
    /**
     * 获取屏幕截图
     *
     * @return
     */
    public ImageIcon getImageIcon() {
        try {
            long start = System.currentTimeMillis();
            RawImage rawImage = this.iDevice.getScreenshot();
            long end = System.currentTimeMillis();
//            System.out.println("获取设备屏幕的时间" + (end - start) + "毫秒");
//      System.out.println("w:"+rawImage.width+",h:"+rawImage.height);

            BufferedImage image = new BufferedImage(rawImage.width, rawImage.height, BufferedImage.TYPE_INT_RGB);

            int index = 0;
            int IndexInc = rawImage.bpp >> 3;
            for (int y = 0; y < rawImage.height; y++) {
                for (int x = 0; x < rawImage.width; x++) {
                    int value = rawImage.getARGB(index);
                    index += IndexInc;
                    image.setRGB(x, y, value);
                }
            }
            return new ImageIcon(image);

        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
    public String getColor(int x,int y){
        String _color_hex = "null";
        try{
            Color _color = new Color(((BufferedImage)this.getImageIcon().getImage()).getRGB(x,y));
            _color_hex = ImageUtils.convertRGBToHex(_color.getRed(),_color.getGreen(),_color.getBlue());
        }catch (Exception e){
            e.printStackTrace();
            System.out.println("Get Color error:"+e.getMessage());
        }
        System.out.println("Get Color:"+x+","+y+":"+_color_hex);
        return _color_hex;
    }
    public List<OcrText> getScreenText(){
        return this.getText(0,0,3000,3000);
    }
    public List<OcrText> getText(int fx, int fy, int tx, int ty){
        try {
            long start = System.currentTimeMillis();
            RawImage rawImage = this.iDevice.getScreenshot();
            long end = System.currentTimeMillis();
//            System.out.println("获取设备屏幕的时间" + (end - start) + "毫秒");
            int _w = tx-fx;
            int _h = ty-fy;
            BufferedImage image = new BufferedImage(_w, _h, BufferedImage.TYPE_INT_RGB);

            int index = 0;
            int IndexInc = rawImage.bpp >> 3;
            for (int y = 0; y < rawImage.height; y++) {
                for (int x = 0; x < rawImage.width; x++) {
                    if((fy<=y&&y<ty)&&(fx<=x&&x<tx)){
                        int value = rawImage.getARGB(index);
                        image.setRGB(x-fx, y-fy, value);
                    }
                    index += IndexInc;
                }
            }
            File imageFile = new File("/Users/gaokui/weiqunkong/temp/"+System.currentTimeMillis()+".png");
            ImageIO.write(image, "png", imageFile);
            final ByteArrayOutputStream os = new ByteArrayOutputStream();
            ImageIO.write(image, "png", os);
            JSONObject res = BaiduAIUtils.getInstance().imageToText(os.toByteArray());

            List<OcrText> _result = new ArrayList<OcrText>();
            JSONArray words = res.getJSONArray("words_result");
            for(int i=0;i<words.length();i++){
                JSONObject thisWordObject = words.getJSONObject(i);
                int _rx = thisWordObject.getJSONObject("location").getInt("left");
                int _ry = thisWordObject.getJSONObject("location").getInt("top");
                _result.add(new OcrText(thisWordObject.getString("words"),_rx+fx,_ry+fy));
            }
            FileUtils.writeStringToFile(new File(imageFile.getParentFile(),imageFile.getName()+".txt"),res.toString(2),"UTF-8");
            return _result;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
    /**
     * 执行命令
     *
     * @param str
     */
    public void shell(String str) {
        try {
            device.getManager().sendMonkeyEvent(str);
        } catch (IOException e) {
            System.out.println("执行命令:" + str + " ,异常!");
        }

    }
    /**
     * Android touch 事件
     *
     * @param x 坐标
     * @param y 坐标
     */
    public void touch(int x, int y) {
        device.touch(x, y, com.android.chimpchat.core.TouchPressType.DOWN_AND_UP);
    }

    /**
     * Android touch 事件
     *
     * @param x    坐标
     * @param y    坐标
     * @param type touch 类型
     */
    public void touch(int x, int y, TouchPressType type) {
        device.touch(x, y, type);
    }

    /**
     * press 触摸事件
     *
     * @param str
     */
    public void press(String str) {
        device.press(str, com.android.chimpchat.core.TouchPressType.DOWN_AND_UP);
    }

    /**
     * press 触摸按下事件
     *
     * @param str
     */
    public void press_DOWN(String str) {
        device.press(str, com.android.chimpchat.core.TouchPressType.DOWN);
    }

    /**
     * press 触摸放开事件
     *
     * @param str
     */
    public void press_UP(String str) {
        device.press(str, com.android.chimpchat.core.TouchPressType.UP);
    }

    /**
     * 移动 事件
     *
     * @param startX 开始位置X
     * @param startY 开始位置Y
     * @param endX   结束位置X
     * @param endY   结束位置Y
     * @param time   时间
     * @param step   步骤
     */
    public void drag(int startX, int startY, int endX, int endY, int step, int time) {
        device.drag(startX, startY, endX, endY, step, time);
    }

    /**
     * 输入字符
     *
     * @param c 字符
     */
    public void type(char c) {
        device.type(Character.toString(c));
    }

    /**
     * touch按下
     *
     * @param x 坐标
     * @param y 坐标
     * @throws Exception
     */
    public void touchDown(int x, int y) throws Exception {
        device.getManager().touchDown(x, y);
    }

    /**
     * touch 放开
     *
     * @param x 坐标
     * @param y 坐标
     * @throws Exception
     */
    public void touchUp(int x, int y) throws Exception {
        device.getManager().touchUp(x, y);
    }

    /**
     * touch 移动
     *
     * @param x 坐标
     * @param y 坐标
     * @throws Exception
     */
    public void touchMove(int x, int y) throws Exception {
        device.getManager().touchMove(x, y);
    }

    /**
     * 获取屏幕宽度
     *
     * @return
     */
    public int getScreenWidth() {
        return Integer.parseInt(device.getProperty("display.width"));
    }

    /**
     * 获取屏幕高度
     *
     * @return
     */
    public int getScreenHeight() {
        return Integer.parseInt(device.getProperty("display.height"));
    }

    /**
     * 安装软件
     *
     * @param path
     */
    public void installPackage(String path) {
        device.installPackage(path);
    }

    /**
     * 打开应用
     *
     * @param activityPath apk包名/主界面  eg: cn.com.fetion/.android.ui.activities.StartActivity
     */
    public void startActivity(String activityPath) {
        String action = "android.intent.action.MAIN";
        Collection<String> categories = new ArrayList();
        categories.add("android.intent.category.LAUNCHER");
        try {
            device.startActivity(null, action, null, null, categories, new HashMap<String, Object>(), activityPath, 0);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 打开微信
     */
    public void openWeiXin() {
        startActivity("com.tencent.mm/com.tencent.mm.ui.LauncherUI");
    }
}
