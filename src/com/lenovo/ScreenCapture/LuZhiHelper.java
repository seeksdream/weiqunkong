package com.lenovo.ScreenCapture;

public class LuZhiHelper {
    public static boolean isLuZhi_ing = false;
    private static boolean isAllowLuzhi = false;
    private static StringBuffer commandSB = new StringBuffer();
    private static long prevCommandTime = -1;

    public static void switchStatus() {
        isLuZhi_ing = !isLuZhi_ing;
    }

    public static void onCommandExe(String command) {
        if(LuZhiHelper.isLuZhi_ing){
            isAllowLuzhi = true;
        }else{
            if(isAllowLuzhi){
                isAllowLuzhi = false;
                prevCommandTime = -1;
                System.out.println("----------------------------------------------");
                System.out.println(commandSB.toString());
                System.out.println("----------------------------------------------");
                commandSB = new StringBuffer();
            }
        }
        if(isAllowLuzhi){
            long _time = 0;
            if(prevCommandTime!=-1){
                _time = System.currentTimeMillis()-prevCommandTime;
            }
            prevCommandTime = System.currentTimeMillis();
            commandSB.append(_time+":"+command+"\n");
        }
        System.out.println("["+LuZhiHelper.isLuZhi_ing+"/"+ isAllowLuzhi +"]Monkey Command info: " + command);
    }
}
