package com.lenovo.test;

import com.android.ddmlib.AndroidDebugBridge;
import com.lenovo.ScreenCapture.MainWindow;
import com.seeks.utils.TailLogThread;

import java.net.Socket;
import java.util.concurrent.*;

public class MyChimpChat {

	public Socket socket;

	public MyChimpChat() {

        ExecutorService exec = Executors.newFixedThreadPool(1);

        Callable<String> call = new Callable<String>() {
            public String call() throws Exception {
                // 开始执行耗时操作
                try {
                    Process process1 = Runtime.getRuntime().exec(MainWindow.DEFINED_ADB_PATH+" forward tcp:12345 tcp:12345");
//                    new TailLogThread(process1.getErrorStream(),"err").start();
//                    new TailLogThread(process1.getInputStream(),"info").start();
                    Thread.sleep(1000);
                    Process process = Runtime.getRuntime().exec(MainWindow.DEFINED_ADB_PATH+" shell monkey --port 12345");
//                    new TailLogThread(process.getErrorStream(),"err").start();
//                    new TailLogThread(process.getInputStream(),"info").start();

                    Thread.sleep(1000);
                    socket = new Socket("127.0.0.1", 12345);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                return "Done";
            }
        };

        try {
            Future<String> future = exec.submit(call);
            String obj = future.get(60 * 1000 * 10, TimeUnit.MILLISECONDS);
            System.out.println("启动成功" + obj);
        } catch (Exception e) {
            System.out.println("遍历时出现卡顿,可能是端口映射出错,重新连接");
            AndroidDebugBridge.getBridge().restart();
//			CMDUtils.runCMD1("adb kill-server", "");
//			CMDUtils.runCMD1("adb start-server", "");
            System.out.println("启动monkey的设备失败");
            e.printStackTrace();
        }
        // 关闭线程池
        exec.shutdown();


	}

	public void touchDown(int x, int y) {
		try {
			socket.getOutputStream().write(
					new String("touch down " + x + " " + y + "\n").getBytes());
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void touchUp(int x, int y) {
		try {
			socket.getOutputStream().write(
					new String("touch up " + x + " " + y + "\n").getBytes());
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void Press() {
		try {
			socket.getOutputStream().write(
					new String("press KEYCODE_HOME\n").getBytes());
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void touchMove(int x, int y) {
		try {
			socket.getOutputStream().write(
					new String("touch move " + x + " " + y + "\n").getBytes());
		} catch (Exception e) {
			e.printStackTrace();
		}

	}
}
