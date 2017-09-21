package com.ruilonglai.texas_scan.newprocess;

import android.util.Log;

import com.google.gson.Gson;
import com.ruilonglai.texas_scan.util.Constant;
import com.ruilonglai.texas_scan.util.TimeUtil;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Date;

import static com.ruilonglai.texas_scan.newprocess.Connect.changeTo;

/**
 * Created by wgl on 2017/8/16.
 */

public class MainServer {

    private final static String TAG = "MainServer";

    private final static int port = 28838;

    private final static int socketNum = 1;

    private boolean isAcceptClient = true;

    private boolean closeSocket = false;

    private byte[] head = new byte[8];

    private static ServerSocket ss = null;

    private static Socket accept = null;

    private static InputStream inStr = null;

    private static OutputStream outStr = null;

    private CallBack callBack;

    private Thread recv = new Thread(new RecvServer());

    private Thread open = new Thread(new OpenServer());

    private volatile static MainServer instance;

    private MainServer(){open.start();}

    public static MainServer newInstance(){
        if(instance==null){
            synchronized (MainServer.class){
                if(instance == null)
                    instance = new MainServer();
            }
        }
        return instance;
    }
    public void close() throws IOException{
        inStr.close();
        outStr.close();
        accept.close();
        ss.close();
    }
    public void send(Package pkg){
        try {
            String msg = new Gson().toJson(pkg,Package.class);
            byte[] bytes = msg.getBytes();
            String s = changeTo(bytes.length);
            if(outStr==null)
                return;
            outStr.write(s.getBytes());
            outStr.write(bytes);
        } catch (IOException e) {
            Log.e(TAG,"发送数据失败");
        }
    }
    class RecvServer implements Runnable{
        @Override
        public void run() {
            try {
                long beginTime = System.currentTimeMillis();
                while (isAcceptClient){
                    if(inStr.read(head)>-1){
                        String lenStr = new String(head);
                        Integer len = Integer.valueOf(lenStr);
                        byte[] bytes = new byte[len];
                        inStr.read(bytes);
                        if(len>50000){//大文件

                        }else{
                            String s = new String(bytes);
                            if("heart!".equals(s)){
                                Log.e(TAG,"收到心跳！！！！！！！");
                                beginTime=System.currentTimeMillis();
                            }else{
                                callBack.recMsg(s);
                            }
                        }
                    }else{
                        try {
                            Thread.sleep(1000);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                   /* if(System.currentTimeMillis()-beginTime>10000){//超过10s重新开启Main进程
                        Package pkg = new Package();
                        pkg.setType(Constant.SOCKET_RESTART_MAIN_PROCESS);
                       callBack.recMsg(new Gson().toJson(pkg));
                    }*/
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
    class OpenServer implements Runnable
    {
        @Override
        public void run()
        {
            try
            {
                ss = new ServerSocket(port,socketNum);
                while (!closeSocket)
                {
                    accept = ss.accept();
                    inStr = accept.getInputStream();
                    outStr = accept.getOutputStream();
                    if(!recv.isAlive())
                    recv.start();
                    //send.start();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
    public void setCallBack(CallBack callBack){

        this.callBack = callBack;

    }
    public interface CallBack{

        void recMsg(String msg);
    }
}
