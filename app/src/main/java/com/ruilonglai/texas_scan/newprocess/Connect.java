package com.ruilonglai.texas_scan.newprocess;

import android.util.Log;

import com.google.gson.Gson;
import com.ruilonglai.texas_scan.util.Constant;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.net.UnknownHostException;

public class Connect {

     public interface CallBack{

        void exit();

        void action(int type);

     }

    private final static String TAG = "Connect";

    private static final ThreadLocal<Socket> threadConnect = new ThreadLocal<Socket>();

    private static final String HOST = "localhost";

    private static final int PORT = 8888;

    private static Socket client;

    private static OutputStream outStr = null;

    private static InputStream inStr = null;

    private static Thread tRecv = new Thread(new RecvThread());

    private static Thread tKeep = new Thread(new KeepThread());

    private static CallBack callback;

    private static volatile Connect instance;

    private Connect(){}

    public static Connect getInstance(){
        if(instance==null){
            synchronized (Connect.class){
                if(instance==null){
                    instance = new Connect();
                }
            }
        }
        return instance;
    }
    public void connect(int port) {
        try {
            client = threadConnect.get();
            if(client == null){
                client = new Socket(HOST, port);
                threadConnect.set(client);
                tKeep.start();
            }
            outStr = client.getOutputStream();
            inStr = client.getInputStream();
            tRecv.start();
            Thread.sleep(2000);
        } catch (UnknownHostException e) {
            Log.e(TAG,"未知端口异常");
        }catch (Exception e){
            Log.e(TAG,e.toString());
        }
    }

    public static void disconnect() {
        try {
            outStr.close();
            inStr.close();
            client.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    public static void send(Package pkg){
        try {
            String msg = new Gson().toJson(pkg,Package.class);
            byte[] bytes = msg.getBytes();
            send(bytes);
        } catch (Exception e) {
           Log.e(TAG,"发送数据失败");
        }
    }
    public static void send(int type,String content){
        Package pkg = new Package();
        pkg.setType(type);
        pkg.setContent(content);
        send(pkg);
    }
    public static synchronized void send(byte[] bytes){
        try {
            String s = changeTo(bytes.length);
            outStr.write(s.getBytes());
            outStr.write(bytes);
        } catch (IOException e) {
            Log.e(TAG,"发送数据失败");
        }
    }
    public void setCallback(CallBack callback){
        this.callback = callback;
    }
    private static class KeepThread implements Runnable {
        public void run() {
            //System.out.println("=====================开始发送心跳包==============");
            while (true) {
                try {
                    Thread.sleep(2000);
                } catch (InterruptedException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
                byte[] bytes = "heart!".getBytes();
                send(bytes);
            }
        }
    }
    public static String changeTo(long num){
        String value = String.valueOf(num);
        StringBuilder sb = new StringBuilder();
        for (int i = value.length(); i < 8; i++) {
            sb.append("0");
        }
        return sb.toString()+value;
    }
    private static class RecvThread implements Runnable {
        public void run() {
            try {
                Log.e("begin","==============开始接收数据===============");
                while (true) {
                    byte[] b = new byte[8];
                    int r = inStr.read(b);
                    if(r>-1){
                        String str = new String(b);
                        Integer valueOf = Integer.valueOf(str);
                        byte[] b1 = new byte[valueOf];
                        inStr.read(b1);
                        String ss = new String(b1);
                        Package aPackage = new Gson().fromJson(ss, Package.class);
                        switch (aPackage.getType()){
                            case Constant.SOCKET_EXIT:
                                disconnect();
                                callback.exit();
                                break;
                            case Constant.SOCKET_PLATFORM_NUTSPOKER:
                            case Constant.SOCKET_PLATFORM_POKERFISHS:
                            case Constant.SOCKET_PLATFORM_TEXASPOKER:
                                callback.action(aPackage.getType());
                                break;
                        }
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}