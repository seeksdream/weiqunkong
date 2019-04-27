package com.lenovo.ScreenCapture;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.awt.image.BufferedImage;
import java.io.File;

import javax.swing.*;

import com.android.ddmlib.AndroidDebugBridge;
import com.android.ddmlib.IDevice;
import com.seeks.core.SeeksDevice;
import com.seeks.utils.ImageUtils;

/**
 * 主页面,手机屏幕实时投射到PC端,PC端通过adb控制手机【反向控制】
 */
@SuppressWarnings("serial")
public class MainWindow extends JFrame {
    public static String DEFINED_ADB_PATH = "adb";

    static {
        String adbLocation = System.getenv("com.android.screenshot.bindir");

        if (adbLocation != null && adbLocation.length() != 0) {
            adbLocation += File.separator + "adb";
        } else {
            adbLocation = "adb";
        }
        DEFINED_ADB_PATH = adbLocation;
    }
    public static void main(String[] args) {
        new MainWindow();
    }

    /**
     * PC端应用页面
     */
    public MainWindow() {
        super();
        initWindow();

        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(200, 200);

        setVisible(true);
        setResizable(true);
    }

    private JLabel deviceScreenPanel;   //用来放置屏幕截图的组件
    private ImageIcon currentScreenImage;   //用来放置屏幕截图的组件
    private JPanel bottomPanel;   //用来放置屏幕截图的组件
    public static SeeksDevice currentDevice = null;
    public IDevice[] allDevices = null;
    private boolean isGetColoring = false;
    public Thread th = null;
    private int MAX_WIDTH = 700;
    private int MAX_HEIGHT = 700;
    public int width = 480;
    public int height = 800;
    public double zoom = 1;

    /**
     * PC端展示设备屏幕截图的容器大小
     */
    public void resizeDeviceScreenSize() {
//        width = 480;  // oa.getScreenWidth();
//        height = 800;  // oa.getScreenHeight();
        //width = oa.getScreenWidth() / scale;
        //height = oa.getScreenHeight() / scale;
        deviceScreenPanel.setSize(width,height);
        this.setSize(width,height+300);
    }
    /**
     * 初始化窗口
     * 根据ADB获取链接到设备上的设备列表,展示在设备列表的菜单下
     */
    public void initWindow() {
        deviceScreenPanel = new JLabel();
        bottomPanel = new JPanel();
        this.setLayout(new GridLayout(2,1));
        this.add(deviceScreenPanel);
        this.add(bottomPanel);
        bottomPanel.setSize(300,300);
        bottomPanel.setBackground(new Color(255,255,255));
        deviceScreenPanel.setHorizontalAlignment(SwingConstants.CENTER);


        AndroidDebugBridge.init(false);
        AndroidDebugBridge bridge = AndroidDebugBridge.createBridge(DEFINED_ADB_PATH, true);
        int count = 0;

        System.out.println("使用的ADB:" + DEFINED_ADB_PATH);
        System.out.println("是否存在设备:" + bridge.hasInitialDeviceList());

        /**
         * 循环100次,每次间隔0.1s, 去获取是否有设备连接到电脑上
         */
        while (bridge.hasInitialDeviceList() == false) {
            try {
                Thread.sleep(100);
                count++;
            } catch (InterruptedException e) {
            }

            if (count > 100) {
                System.err.println("获取设备超时,请检查是否把设备用USB连接到PC上");
                return;
            }
        }

        //android机器列表
        allDevices = bridge.getDevices();

        JMenu fileMenu = new JMenu("设备列表");
        JMenu functionMenu = new JMenu("功能键");
        JMenu functionZoom = new JMenu("缩放");
        JCheckBoxMenuItem luzhi = new JCheckBoxMenuItem("录制脚本");
        JCheckBoxMenuItem quse = new JCheckBoxMenuItem("取色");

        JMenuItem ItemHome = new JMenuItem("HOME");
        JMenuItem ItemBack = new JMenuItem("BACK");
        JMenuItem ItemMenu = new JMenuItem("MENU");
        //JMenuItem ItemPower = new JMenuItem("POWER");

        functionMenu.add(ItemHome);
        functionMenu.add(ItemBack);
        functionMenu.add(ItemMenu);
        //functionMenu.add(ItemPower);

        JMenuItem Item100 = new JMenuItem("100%");
        JMenuItem Item80 = new JMenuItem("80%");
        JMenuItem Item50 = new JMenuItem("50%");

        functionZoom.add(Item100);
        functionZoom.add(Item80);
        functionZoom.add(Item50);

        JMenuBar menuBar = new JMenuBar();
        menuBar.add(fileMenu);
        menuBar.add(functionMenu);
        menuBar.add(functionZoom);
        menuBar.add(luzhi);
        menuBar.add(quse);

        luzhi.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent arg0) {
                LuZhiHelper.switchStatus();
                luzhi.setText(LuZhiHelper.isLuZhi_ing?"停止录制":"开始录制");
                LuZhiHelper.onCommandExe("#Script#");
            }
        });
        quse.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent arg0) {
                isGetColoring = !isGetColoring;
                quse.setText(isGetColoring?"停止取色":"取色");
            }
        });

        JMenuItem newMenuItem = null;

        //获取连接的设备列表,然后动态生成到设备列表中
        for (IDevice d : allDevices) {
            System.out.println("Add currentDevice:"+d.getName());
            newMenuItem = new JMenuItem(d.getName());
            newMenuItem.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent arg0) {
                    MainWindow.this.selectDevice(arg0.getActionCommand());
                }
            });
            fileMenu.add(newMenuItem);

        }



        ItemHome.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent arg0) {
                currentDevice.press(SeeksDevice.HOME);
            }
        });
        ItemBack.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent arg0) {
                currentDevice.press(SeeksDevice.BACK);
            }
        });
        ItemMenu.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent arg0) {
                currentDevice.press(SeeksDevice.MENU);
            }
        });

        // ItemPower.addActionListener(new ActionListener() {
        //
        // @Override
        // public void actionPerformed(ActionEvent arg0) {
        // oa.press(OperateAndroid.POWER);
        // }
        // });

        Item100.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent arg0) {
                MAX_WIDTH = 1400;
                MAX_HEIGHT = 1000;
            }
        });

        Item80.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent arg0) {
                MAX_WIDTH = 1000;
                MAX_HEIGHT = 1000;
            }
        });
        Item50.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent arg0) {
                MAX_WIDTH = 700;
                MAX_HEIGHT = 700;
            }
        });

        setJMenuBar(menuBar);

        th = new Thread(new Runnable() {

            @Override
            public void run() {
                ImageIcon image = null;
                while (true) {

                    image = new ImageIcon("no.gif");
                    ImageIcon realScreenImage = currentDevice.getImageIcon();

                    int _width = realScreenImage.getIconWidth();
                    int _height = realScreenImage.getIconHeight();

                    boolean isV = true;

                    if (_height > _width) {
                        width = Integer.valueOf(_width * MAX_HEIGHT / _height);
                        height = MAX_HEIGHT;
                        zoom = _height / MAX_HEIGHT;
                    } else {
                        isV = false;
                        width = MAX_WIDTH;
                        height = Integer.valueOf(_height * MAX_WIDTH / _width);
                    }

                    zoom = Double.valueOf(width) / Double.valueOf(_width);
//                    System.out.println("[zoom:" + zoom + "]w:" + _width + ",h:" + _height + "(FINAL:W:" + width + ",H:" + height + ")");
                    currentScreenImage = realScreenImage;

                    image.setImage(realScreenImage.getImage().getScaledInstance(width, height, Image.SCALE_DEFAULT));
                    deviceScreenPanel.setIcon(image);
                    resizeDeviceScreenSize();
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        });
        if (allDevices.length < 1) {
            fileMenu.setText("没有设备");
            fileMenu.setEnabled(false);
            functionMenu.setEnabled(false);
        }else{
            this.selectDevice(allDevices[0].getName());
        }
        deviceScreenPanel.addMouseListener(new MouseListener() {

            @Override
            public void mouseReleased(MouseEvent e) {
                try {
                    if(isGetColoring==false){
                        currentDevice.touchUp((int) (e.getX() / zoom), (int) (e.getY() / zoom));
                    }
//                    System.out.printf("mouseReleased,x=%d,y=%d", e.getX(), e.getY());
                } catch (Exception e1) {
                    e1.printStackTrace();
                }
            }

            @Override
            public void mousePressed(MouseEvent e) {
                try {
                    int real_x = (int) (e.getX() / zoom);
                    int real_y = (int) (e.getY() / zoom);
                    Color _color = new Color(((BufferedImage)currentScreenImage.getImage()).getRGB(real_x,real_y));
                    bottomPanel.setBackground(_color);
                    String color = ImageUtils.convertRGBToHex(_color.getRed(),_color.getGreen(),_color.getBlue());
                    System.out.println("["+isGetColoring+"]" + color+":"+real_x+","+real_y);
                    if(isGetColoring==false){
                        currentDevice.touchDown(real_x, real_y);
                    }
//                    System.out.printf("mousePressed,x=%d,y=%d", e.getX(), e.getY());
                } catch (Exception e1) {
                    e1.printStackTrace();
                }
            }

            @Override
            public void mouseExited(MouseEvent e) {
            }

            @Override
            public void mouseEntered(MouseEvent e) {
            }

            @Override
            public void mouseClicked(MouseEvent e) {
            }
        });

        deviceScreenPanel.addMouseMotionListener(new MouseMotionListener() {

            @Override
            public void mouseMoved(MouseEvent e) {
            }

            @Override
            public void mouseDragged(MouseEvent e) {
                try {
                    currentDevice.touchMove((int) (e.getX() / zoom),
                            (int) (e.getY() / zoom));
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        });

        deviceScreenPanel.addMouseWheelListener(new MouseWheelListener() {

            @Override
            public void mouseWheelMoved(MouseWheelEvent e) {
                if (e.getWheelRotation() == 1) {
                    currentDevice.press("KEYCODE_DPAD_DOWN");
                } else if (e.getWheelRotation() == -1) {
                    currentDevice.press("KEYCODE_DPAD_UP");
                }
            }
        });

        this.addKeyListener(new KeyListener() {

            @Override
            public void keyTyped(KeyEvent e) {

            }

            @Override
            public void keyReleased(KeyEvent e) {
            }

            @Override
            public void keyPressed(KeyEvent e) {
                int code = e.getKeyCode();
                switch (code) {

                    case KeyEvent.VK_BACK_SPACE:
                        currentDevice.press("KEYCODE_DEL");
                        break;
                    case KeyEvent.VK_SPACE:
                        currentDevice.press("KEYCODE_SPACE");
                        break;
                    case KeyEvent.VK_DELETE:
                        currentDevice.press("KEYCODE_FORWARD_DEL");
                        break;
                    case KeyEvent.VK_UP:
                        currentDevice.press("KEYCODE_DPAD_UP");
                        break;
                    case KeyEvent.VK_DOWN:
                        currentDevice.press("KEYCODE_DPAD_DOWN");
                        break;
                    case KeyEvent.VK_LEFT:
                        currentDevice.press("KEYCODE_DPAD_LEFT");
                        break;
                    case KeyEvent.VK_RIGHT:
                        currentDevice.press("KEYCODE_DPAD_RIGHT");
                        break;
                    case KeyEvent.VK_ENTER:
                        currentDevice.press("KEYCODE_ENTER");
                        break;
                    case KeyEvent.VK_CONTROL:
                        break;
                    case KeyEvent.VK_ALT:
                        break;
                    case KeyEvent.VK_SHIFT:
                        break;
                    default:
                        currentDevice.type(e.getKeyChar());
                }

            }
        });
    }

    private void selectDevice(String serialNumber){
        System.out.println("select device serialNumber:" + serialNumber);
        for (IDevice d : allDevices) {
            System.out.println(d.getSerialNumber() + "==" + serialNumber);
            if (d.getSerialNumber().equals(serialNumber)) {
                currentDevice = new SeeksDevice(d);
                if (!th.isAlive()) {
                    th.start();
                }
            }
        }
    }
}
