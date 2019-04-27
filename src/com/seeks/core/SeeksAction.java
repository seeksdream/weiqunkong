package com.seeks.core;

public abstract class SeeksAction implements Runnable{
    protected SeeksDevice device;
    @Override
    public void run() {
        try {
            this.start();
            this.device.setCurrentAction(null);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    public abstract String getDesc();
    public abstract void start() throws Exception;
    public void onKill(String reason){

    }

    public void setDevice(SeeksDevice device2){
        this.device = device2;
    }


}