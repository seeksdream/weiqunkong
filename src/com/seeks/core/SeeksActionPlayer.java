package com.seeks.core;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class SeeksActionPlayer {
    private static SeeksActionPlayer instance = new SeeksActionPlayer();
    public static SeeksActionPlayer getInstance(){
        return instance;
    }
    private SeeksActionPlayer(){

    }

    private Map<String,Thread> threadMap = new HashMap();
    public void play(SeeksDevice device,SeeksAction action){
        System.out.println("在设备["+device.getName()+"]上启动任务："+action.getDesc());
        if(device.getCurrentAction()!=null){
            System.out.println("终止设备["+device.getName()+"]上的上一个任务："+device.getCurrentAction().getDesc());
            device.getCurrentAction().onKill("新的任务["+action.getDesc()+"]要开始！");
            threadMap.get(device.getName()).stop();

        }
        action.setDevice(device);
        device.setCurrentAction(action);
        Thread thread = new Thread(action);
        threadMap.put(device.getName(),thread);
        thread.start();
    }
}
