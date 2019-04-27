package com.seeks.actions;

import com.seeks.core.SeeksAction;
import com.seeks.utils.orc.OcrText;

import java.util.List;

public class Action4WaBao extends SeeksAction {
    @Override
    public String getDesc() {
        return "WaBao";
    }

    @Override
    public void start() throws Exception {
        System.out.println("开始挖宝.....");
        s(1000);
        if(device.getColor(821,36).equals("FDECBA")){
            log("点击活动");
            device.touch(821,36);
            s(2000);

        }
//        this.device.touch(100,100);
//        this.device.drag(742,533,725,168,30,3000);
//        this.device.getScreenText();
//        this.device.getText(340,168,563,555);
        List<OcrText> textlist= this.device.getText(339,170,563,545);
        textlist.addAll(this.device.getText(808,167,1039,546));
        for(OcrText o:textlist){
            System.out.println("检查："+o.toXString());
            if(o.text.contains("经验喜刷刷")){
//                813,201，1098,226
//                o.x:808,o.y196
//                1104,228
                this.device.touch(o.x+290,o.y+32);
            }
        }
        /**
         0:#Script#
         1641:touch down 958 594
         72:touch up 958 594
         2700:touch down 848 387
         37:touch move 848 385
         29:touch up 848 385
         4271:touch down 853 365
         69:touch up 853 365
         2871:touch down 259 248
         78:touch up 259 248
         4078:touch down 294 266
         15:touch move 294 266
         78:touch up 294 266
         4699:press KEYCODE_BACK
         3612:press KEYCODE_HOME
         */
        System.out.println("开始完成");
    }

    public void log(String msg){
        System.out.println(msg);
    }
    public void s(long t){
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
