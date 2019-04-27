package com.lenovo.Form;

import com.android.ddmlib.AndroidDebugBridge;
import com.android.ddmlib.IDevice;
import com.lenovo.ScreenCapture.MainWindow;
import com.seeks.core.SeeksDevice;

import javax.swing.*;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.util.ArrayList;

/**
 * Created by zhongxia on 10/24/16.
 */
public class App {
    private JPanel pApp;
    private JButton btnWX;
    private JButton btnConnect;
    private JButton btnHome;
    private JButton btnBack;
    private JButton btnMenu;
    private JPanel panelDevices;
    private JPanel pTop;
    private JScrollPane spMain;
    private JButton btnControl;
    private JPanel panelTable;
    private JButton btnCommand;
    private JTextField txtCommand;

    private static IDevice[] devices = null;  //所有的设备数组
    private static ArrayList<SeeksDevice> oas = null; //保存所有的Android操作对象[创建这个对象挺耗时间,因此只创建一次]
    private ControlPage cp = null;

    //设备屏幕截图的宽高
    final int imgWidth = 270;
    final int imgHeight = 480;


    public static void main(String[] args) {
        JFrame frame = new JFrame("App");
        frame.setContentPane(new App().pApp);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.pack();
        frame.setSize(800, 600);
        frame.setVisible(true);
    }

    /**
     * 构造函数
     */
    public App() {
        bindEvent();
        devices = getDevices();
        oas = getOperateAndroid(devices);
        initList(devices);
    }

    /**
     * 初始化设备列表
     */
    public void initList(IDevice[] devices) {
        if (devices != null) {
            System.out.println("devices size:"+devices.length);
            ArrayList<Object> listData = new ArrayList<>();
            for (int i = 0; i < devices.length; i++) {
                System.out.println("Add currentDevice:" + devices[i].getName());
                listData.add(devices[i].getName());
            }
            JList<Object> list = new JList<>();
            list.setListData(listData.toArray());
            panelTable.add(list);
        }else{
            System.out.println("devices is null!");
        }
    }

    /**
     * 绑定事件
     */
    public void bindEvent() {
        //打开微信
        btnCommand.addMouseListener(new ZxMouseListener() {
            @Override
            public void mousePressed(MouseEvent e) {
                String shell = txtCommand.getText();
                for (SeeksDevice oa : oas) {
                    oa.shell(shell.trim());
                }
            }
        });

        //打开微信
        btnWX.addMouseListener(new ZxMouseListener() {
            @Override
            public void mousePressed(MouseEvent e) {
                for (SeeksDevice oa : oas) {
                    oa.openWeiXin();
                }
            }
        });

        //群控微信,弹出操作面板
        btnControl.addMouseListener(new ZxMouseListener() {
            @Override
            public void mousePressed(MouseEvent e) {
                if (cp == null) {
                    JFrame frame = new JFrame("操作页面");
                    cp = new ControlPage(devices[0], oas.get(0));
                    frame.setContentPane(cp.panel1);
                    frame.setSize(cp.lblImage.getWidth() + 300, cp.lblImage.getHeight() + 20);
                    frame.setVisible(true);
                    frame.addWindowListener(new WindowAdapter() {
                        public void windowClosing(WindowEvent e) {
                            System.out.println("关闭操作设备的窗口");
                            cp.stopThread();
                            cp = null;
                        }
                    });
                }
            }
        });

        //显示屏幕
        btnConnect.addMouseListener(new ZxMouseListener() {
            @Override
            public void mousePressed(MouseEvent e) {
                btnConnect.setEnabled(false);
                if (devices != null) {
                    if (devices.length > 0) {
                        initDevices(devices);
                    } else {
                        JOptionPane.showMessageDialog(pApp, "没有检测到设备连接该电脑");
                    }
                }
            }
        });

        //返回首页
        btnHome.addMouseListener(new ZxMouseListener() {
            @Override
            public void mousePressed(MouseEvent e) {
                for (SeeksDevice oa : oas) {
                    oa.press(SeeksDevice.HOME);
                }
            }
        });

        //返回键
        btnBack.addMouseListener(new ZxMouseListener() {
            @Override
            public void mousePressed(MouseEvent e) {
                for (SeeksDevice oa : oas) {
                    oa.press(SeeksDevice.BACK);
                }
            }
        });

        //菜单键
        btnMenu.addMouseListener(new ZxMouseListener() {
            @Override
            public void mousePressed(MouseEvent e) {
                for (SeeksDevice oa : oas) {
                    oa.press(SeeksDevice.MENU);
                }
            }
        });
    }

    /**
     * 获取设备列表
     *
     * @return
     */
    public IDevice[] getDevices() {

        AndroidDebugBridge.init(false);
        AndroidDebugBridge bridge = AndroidDebugBridge.createBridge(MainWindow.DEFINED_ADB_PATH, true);

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
                JOptionPane.showMessageDialog(pApp, "获取设备超时,请检查是否把设备用USB连接到PC上");
                break;
            }
        }
        //android机器列表
        IDevice[] devices = bridge.getDevices();
        return devices;
    }

    /**
     * 把设备列表的设备展示到面板上
     *
     * @param devices
     */
    public void initDevices(IDevice[] devices) {
        //获取连接的设备列表,然后动态生成到设备列表中
        for (IDevice d : devices) {

            JLabel label = new JLabel();
            label.setSize(imgWidth, imgHeight);
            label.setToolTipText(d.getName());

            panelDevices.add(label);
            initDeviceImage(label, d);
        }
    }

    /**
     * 初始化设备屏幕截图到应用上
     *
     * @param label  放置屏幕截图的组件
     * @param device 设备对象
     */
    public void initDeviceImage(JLabel label, IDevice device) {
        ImageThread it = new ImageThread(label, device);
        if (!it.isAlive()) {
            it.start();
        }
    }

    /**
     * 获取操作Android设备的对象
     *
     * @param devices
     * @return
     */
    public ArrayList<SeeksDevice> getOperateAndroid(IDevice[] devices) {
        long start = System.currentTimeMillis();
        ArrayList<SeeksDevice> oas = new ArrayList<>();
        for (IDevice d : devices) {
            oas.add(new SeeksDevice(d));
        }
        long end = System.currentTimeMillis();
        System.out.println("构建OperatorAndroid对象耗时:" + (end - start) + "毫秒");
        return oas;
    }
}
