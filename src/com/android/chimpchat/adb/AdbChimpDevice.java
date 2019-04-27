//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package com.android.chimpchat.adb;

import com.android.chimpchat.ChimpManager;
import com.android.chimpchat.adb.LinearInterpolator.Callback;
import com.android.chimpchat.adb.LinearInterpolator.Point;
import com.android.chimpchat.core.IChimpDevice;
import com.android.chimpchat.core.IChimpImage;
import com.android.chimpchat.core.TouchPressType;
import com.android.chimpchat.hierarchyviewer.HierarchyViewer;
import com.android.ddmlib.AdbCommandRejectedException;
import com.android.ddmlib.IDevice;
import com.android.ddmlib.InstallException;
import com.android.ddmlib.ShellCommandUnresponsiveException;
import com.android.ddmlib.SyncException;
import com.android.ddmlib.TimeoutException;
import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.annotation.Nullable;

public class AdbChimpDevice implements IChimpDevice {
    private static final Logger LOG = Logger.getLogger(AdbChimpDevice.class.getName());
    private static final String[] ZERO_LENGTH_STRING_ARRAY = new String[0];
    private static final long MANAGER_CREATE_TIMEOUT_MS = 30000L;
    private static final long MANAGER_CREATE_WAIT_TIME_MS = 1000L;
    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private final IDevice device;
    private ChimpManager manager;

    public AdbChimpDevice(IDevice var1) {
        System.out.println("Seeks Monkey Device init for:"+var1.getName());
        this.device = var1;
        this.manager = this.createManager("127.0.0.1", 12345);
        this.manager.close();
        this.manager = this.createManager("127.0.0.1", 12345);
        Preconditions.checkNotNull(this.manager);
//        try {
//            this.manager.touch(100,100);
//        } catch (IOException e) {
//            this.manager = this.createManager("127.0.0.1", 12345);
//            e.printStackTrace();
//        }
    }

    public ChimpManager getManager() {
        return this.manager;
    }

    public void dispose() {
        try {
            this.manager.quit();
        } catch (IOException var2) {
            LOG.log(Level.SEVERE, "Error getting the manager to quit", var2);
        }

        this.manager.close();
        this.executor.shutdown();
        this.manager = null;
    }

    public HierarchyViewer getHierarchyViewer() {
        return new HierarchyViewer(this.device);
    }

    private void executeAsyncCommand(final String var1, final LoggingOutputReceiver var2) {
        this.executor.submit(new Runnable() {
            public void run() {
                try {
                    AdbChimpDevice.this.device.executeShellCommand(var1, var2);
                } catch (Exception var5) {
                    AdbChimpDevice.LOG.log(Level.INFO, "Error starting command: " + var1);
//                    throw new RuntimeException(var5);
                }
            }
        });
    }

    private ChimpManager createManager(String var1, int var2) {
        System.out.println("##########################Create manager##################################");
        try {
            this.device.createForward(var2, var2);
        } catch (TimeoutException var18) {
            LOG.log(Level.SEVERE, "Timeout creating adb port forwarding", var18);
            return null;
        } catch (AdbCommandRejectedException var19) {
            LOG.log(Level.SEVERE, "Adb rejected adb port forwarding command: " + var19.getMessage(), var19);
            return null;
        } catch (IOException var20) {
            LOG.log(Level.SEVERE, "Unable to create adb port forwarding: " + var20.getMessage(), var20);
            return null;
        }


        String var3 = "monkey --port " + var2;
        try {
            this.executeAsyncCommand(var3, new LoggingOutputReceiver(LOG, Level.FINE));
            Thread.sleep(1000L);
        } catch (Exception var17) {
            LOG.log(Level.SEVERE, "error command:"+var3, var17);
        }

        InetAddress var4;
        try {
            var4 = InetAddress.getByName(var1);
        } catch (UnknownHostException var16) {
            LOG.log(Level.SEVERE, "Unable to convert address into InetAddress: " + var1, var16);
            return null;
        }

        boolean var5 = false;
        ChimpManager var6 = null;
        long var7 = System.currentTimeMillis();

        while(!var5) {
            System.out.println("########## try socket #########");
            long var9 = System.currentTimeMillis();
            long var11 = var9 - var7;
            if (var11 > 30000L) {
                LOG.severe("Timeout while trying to create chimp mananger");
                return null;
            }

            try {
                Thread.sleep(1000L);
            } catch (InterruptedException var15) {
                LOG.log(Level.SEVERE, "Unable to sleep", var15);
            }

            Socket var13;
            try {
                var13 = new Socket(var4, var2);
            } catch (IOException var23) {
                LOG.log(Level.FINE, "Unable to connect socket", var23);
                var5 = false;
                continue;
            }

            try {
                var6 = new ChimpManager(var13);
            } catch (IOException var22) {
                LOG.log(Level.SEVERE, "Unable to open writer and reader to socket");
                continue;
            }

            try {
                var6.wake();
            } catch (IOException var21) {
                var21.printStackTrace();
                var5 = false;
                continue;
            }

            var5 = true;
        }

        return var6;
    }

    public IChimpImage takeSnapshot() {
        try {
            return new AdbChimpImage(this.device.getScreenshot());
        } catch (TimeoutException var2) {
            LOG.log(Level.SEVERE, "Unable to take snapshot", var2);
            return null;
        } catch (AdbCommandRejectedException var3) {
            LOG.log(Level.SEVERE, "Unable to take snapshot", var3);
            return null;
        } catch (IOException var4) {
            LOG.log(Level.SEVERE, "Unable to take snapshot", var4);
            return null;
        }
    }

    public String getSystemProperty(String var1) {
        return this.device.getProperty(var1);
    }

    public String getProperty(String var1) {
        try {
            return this.manager.getVariable(var1);
        } catch (IOException var3) {
            LOG.log(Level.SEVERE, "Unable to get variable: " + var1, var3);
            return null;
        }
    }

    public Collection<String> getPropertyList() {
        try {
            return this.manager.listVariable();
        } catch (IOException var2) {
            LOG.log(Level.SEVERE, "Unable to get variable list", var2);
            return null;
        }
    }

    public void wake() {
        try {
            this.manager.wake();
        } catch (IOException var2) {
            LOG.log(Level.SEVERE, "Unable to wake currentDevice (too sleepy?)", var2);
        }

    }

    private String shell(String... var1) {
        StringBuilder var2 = new StringBuilder();
        String[] var3 = var1;
        int var4 = var1.length;

        for(int var5 = 0; var5 < var4; ++var5) {
            String var6 = var3[var5];
            var2.append(var6).append(" ");
        }

        return this.shell(var2.toString());
    }

    public String shell(String var1) {
        CommandOutputCapture var2 = new CommandOutputCapture();

        try {
            this.device.executeShellCommand(var1, var2);
        } catch (TimeoutException var4) {
            LOG.log(Level.SEVERE, "Error executing command: " + var1, var4);
            return null;
        } catch (ShellCommandUnresponsiveException var5) {
            LOG.log(Level.SEVERE, "Error executing command: " + var1, var5);
            return null;
        } catch (AdbCommandRejectedException var6) {
            LOG.log(Level.SEVERE, "Error executing command: " + var1, var6);
            return null;
        } catch (IOException var7) {
            LOG.log(Level.SEVERE, "Error executing command: " + var1, var7);
            return null;
        }

        return var2.toString();
    }

    public boolean pushFile(String var1, String var2) {
        try {
            this.device.pushFile(var1, var2);
            return true;
        } catch (SyncException var4) {
            LOG.log(Level.SEVERE, "Error pushing file: " + var1, var4);
            return false;
        } catch (AdbCommandRejectedException var5) {
            LOG.log(Level.SEVERE, "Error pushing file: " + var1, var5);
            return false;
        } catch (TimeoutException var6) {
            LOG.log(Level.SEVERE, "Error pushing file: " + var1, var6);
            return false;
        } catch (IOException var7) {
            LOG.log(Level.SEVERE, "Error pushing file: " + var1, var7);
            return false;
        }
    }

    public boolean pullFile(String var1, String var2) {
        try {
            this.device.pullFile(var1, var2);
            return true;
        } catch (SyncException var4) {
            LOG.log(Level.SEVERE, "Error pulling file: " + var1, var4);
            return false;
        } catch (AdbCommandRejectedException var5) {
            LOG.log(Level.SEVERE, "Error pulling file: " + var1, var5);
            return false;
        } catch (TimeoutException var6) {
            LOG.log(Level.SEVERE, "Error pulling file: " + var1, var6);
            return false;
        } catch (IOException var7) {
            LOG.log(Level.SEVERE, "Error pulling file: " + var1, var7);
            return false;
        }
    }

    public boolean installPackage(String var1) {
        try {
            String var2 = this.device.installPackage(var1, true);
            if (var2 != null) {
                LOG.log(Level.SEVERE, "Got error installing package: " + var2);
                return false;
            } else {
                return true;
            }
        } catch (InstallException var3) {
            LOG.log(Level.SEVERE, "Error installing package: " + var1, var3);
            return false;
        }
    }

    public boolean removePackage(String var1) {
        try {
            String var2 = this.device.uninstallPackage(var1);
            if (var2 != null) {
                LOG.log(Level.SEVERE, "Got error uninstalling package " + var1 + ": " + var2);
                return false;
            } else {
                return true;
            }
        } catch (InstallException var3) {
            LOG.log(Level.SEVERE, "Error installing package: " + var1, var3);
            return false;
        }
    }

    public void press(String var1, TouchPressType var2) {
        try {
            switch(var2) {
                case DOWN_AND_UP:
                    this.manager.press(var1);
                    break;
                case DOWN:
                    this.manager.keyDown(var1);
                    break;
                case UP:
                    this.manager.keyUp(var1);
            }
        } catch (IOException var4) {
            LOG.log(Level.SEVERE, "Error sending press event: " + var1 + " " + var2, var4);
        }

    }

    public void type(String var1) {
        try {
            this.manager.type(var1);
        } catch (IOException var3) {
            LOG.log(Level.SEVERE, "Error Typing: " + var1, var3);
        }

    }

    public void touch(int var1, int var2, TouchPressType var3) {
        try {
            switch(var3) {
                case DOWN_AND_UP:
                    this.manager.tap(var1, var2);
                    break;
                case DOWN:
                    this.manager.touchDown(var1, var2);
                    break;
                case UP:
                    this.manager.touchUp(var1, var2);
            }
        } catch (IOException var5) {
            LOG.log(Level.SEVERE, "Error sending touch event: " + var1 + " " + var2 + " " + var3, var5);
        }

    }

    public void reboot(String var1) {
        try {
            this.device.reboot(var1);
        } catch (TimeoutException var3) {
            LOG.log(Level.SEVERE, "Unable to reboot currentDevice", var3);
        } catch (AdbCommandRejectedException var4) {
            LOG.log(Level.SEVERE, "Unable to reboot currentDevice", var4);
        } catch (IOException var5) {
            LOG.log(Level.SEVERE, "Unable to reboot currentDevice", var5);
        }

    }

    public void startActivity(String var1, String var2, String var3, String var4, Collection<String> var5, Map<String, Object> var6, String var7, int var8) {
        List var9 = this.buildIntentArgString(var1, var2, var3, var4, var5, var6, var7, var8);
        this.shell((String[])Lists.asList("am", "start", var9.toArray(ZERO_LENGTH_STRING_ARRAY)).toArray(ZERO_LENGTH_STRING_ARRAY));
    }

    public void broadcastIntent(String var1, String var2, String var3, String var4, Collection<String> var5, Map<String, Object> var6, String var7, int var8) {
        List var9 = this.buildIntentArgString(var1, var2, var3, var4, var5, var6, var7, var8);
        this.shell((String[])Lists.asList("am", "broadcast", var9.toArray(ZERO_LENGTH_STRING_ARRAY)).toArray(ZERO_LENGTH_STRING_ARRAY));
    }

    private static boolean isNullOrEmpty(@Nullable String var0) {
        return var0 == null || var0.length() == 0;
    }

    private List<String> buildIntentArgString(String var1, String var2, String var3, String var4, Collection<String> var5, Map<String, Object> var6, String var7, int var8) {
        ArrayList var9 = Lists.newArrayList();
        if (!isNullOrEmpty(var2)) {
            var9.add("-a");
            var9.add(var2);
        }

        if (!isNullOrEmpty(var3)) {
            var9.add("-d");
            var9.add(var3);
        }

        if (!isNullOrEmpty(var4)) {
            var9.add("-t");
            var9.add(var4);
        }

        Iterator var10 = var5.iterator();

        while(var10.hasNext()) {
            String var11 = (String)var10.next();
            var9.add("-c");
            var9.add(var11);
        }

        var10 = var6.entrySet().iterator();

        while(var10.hasNext()) {
            Entry var15 = (Entry)var10.next();
            Object var12 = var15.getValue();
            String var13;
            String var14;
            if (var12 instanceof Integer) {
                var13 = Integer.toString((Integer)var12);
                var14 = "--ei";
            } else if (var12 instanceof Boolean) {
                var13 = Boolean.toString((Boolean)var12);
                var14 = "--ez";
            } else {
                var13 = var12.toString();
                var14 = "--es";
            }

            var9.add(var14);
            var9.add(var15.getKey());
            var9.add(var13);
        }

        if (!isNullOrEmpty(var7)) {
            var9.add("-n");
            var9.add(var7);
        }

        if (var8 != 0) {
            var9.add("-f");
            var9.add(Integer.toString(var8));
        }

        if (!isNullOrEmpty(var1)) {
            var9.add(var1);
        }

        return var9;
    }

    public Map<String, Object> instrument(String var1, Map<String, Object> var2) {
        ArrayList var3 = Lists.newArrayList(new String[]{"am", "instrument", "-w", "-r", var1});
        String var4 = this.shell((String[])var3.toArray(ZERO_LENGTH_STRING_ARRAY));
        return convertInstrumentResult(var4);
    }

    @VisibleForTesting
    static Map<String, Object> convertInstrumentResult(String var0) {
        HashMap var1 = Maps.newHashMap();
        Pattern var2 = Pattern.compile("^INSTRUMENTATION_(\\w+): ", 8);
        Matcher var3 = var2.matcher(var0);
        int var4 = 0;

        String var5;
        String var6;
        int var7;
        String var8;
        String var9;
        for(var5 = null; var3.find(); var5 = var3.group(1)) {
            if ("RESULT".equals(var5)) {
                var6 = var0.substring(var4, var3.start()).trim();
                var7 = var6.indexOf("=");
                var8 = var6.substring(0, var7);
                var9 = var6.substring(var7 + 1);
                var1.put(var8, var9);
            }

            var4 = var3.end();
        }

        if ("RESULT".equals(var5)) {
            var6 = var0.substring(var4, var3.start()).trim();
            var7 = var6.indexOf("=");
            var8 = var6.substring(0, var7);
            var9 = var6.substring(var7 + 1);
            var1.put(var8, var9);
        }

        return var1;
    }

    public void drag(int var1, int var2, int var3, int var4, int var5, long var6) {
        final long var8 = var6 / (long)var5;
        LinearInterpolator var10 = new LinearInterpolator(var5);
        Point var11 = new Point(var1, var2);
        Point var12 = new Point(var3, var4);
        var10.interpolate(var11, var12, new Callback() {
            public void step(Point var1) {
                try {
                    AdbChimpDevice.this.manager.touchMove(var1.getX(), var1.getY());
                } catch (IOException var4) {
                    AdbChimpDevice.LOG.log(Level.SEVERE, "Error sending drag start event", var4);
                }

                try {
                    Thread.sleep(var8);
                } catch (InterruptedException var3) {
                    AdbChimpDevice.LOG.log(Level.SEVERE, "Error sleeping", var3);
                }

            }

            public void start(Point var1) {
                try {
                    AdbChimpDevice.this.manager.touchDown(var1.getX(), var1.getY());
                    AdbChimpDevice.this.manager.touchMove(var1.getX(), var1.getY());
                } catch (IOException var4) {
                    AdbChimpDevice.LOG.log(Level.SEVERE, "Error sending drag start event", var4);
                }

                try {
                    Thread.sleep(var8);
                } catch (InterruptedException var3) {
                    AdbChimpDevice.LOG.log(Level.SEVERE, "Error sleeping", var3);
                }

            }

            public void end(Point var1) {
                try {
                    AdbChimpDevice.this.manager.touchMove(var1.getX(), var1.getY());
                    AdbChimpDevice.this.manager.touchUp(var1.getX(), var1.getY());
                } catch (IOException var3) {
                    AdbChimpDevice.LOG.log(Level.SEVERE, "Error sending drag end event", var3);
                }

            }
        });
    }
}
