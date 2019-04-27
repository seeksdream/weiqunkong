package com.seeks.app;

import com.android.ddmlib.AndroidDebugBridge;
import com.android.ddmlib.IDevice;
import com.seeks.actions.Action4WaBao;
import com.seeks.core.SeeksAction;
import com.seeks.core.SeeksActionPlayer;
import com.seeks.core.SeeksDevice;

public class AppMain {
    public static String DEFINED_ADB_PATH = "/Users/gaokui/Library/Nemu/adb";
    public static void main(String[] args){
        AndroidDebugBridge.init(false);
        AndroidDebugBridge bridge = AndroidDebugBridge.createBridge(DEFINED_ADB_PATH, true);


        System.out.println("使用的ADB:" + DEFINED_ADB_PATH);
        System.out.println("是否存在设备:" + bridge.hasInitialDeviceList());

        int count = 0;
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
        final IDevice[] devices = bridge.getDevices();
        //获取连接的设备列表,然后动态生成到设备列表中
        for (IDevice d : devices) {
            System.out.println("Add currentDevice:" + d.getName());
            SeeksDevice device = new SeeksDevice(d);
            SeeksAction action = new Action4WaBao();
            SeeksActionPlayer.getInstance().play(device,action);
        }
    }
}
