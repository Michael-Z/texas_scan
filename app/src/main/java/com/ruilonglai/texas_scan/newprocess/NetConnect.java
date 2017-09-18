package com.ruilonglai.texas_scan.newprocess;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * Created by Administrator on 2017/8/12.
 */

public class NetConnect {
    public LinkedBlockingQueue sendQ;
    public LinkedBlockingQueue recQ;

    class Package{
        public int type;
        public String content;
    }
    public void start(int sendport,int recPort){

    }
    public void send(Package p) throws Exception{
       sendQ.put(p);
    }

    public Package receive(){
    return (Package) recQ.poll();
    }

    public void runRev(int recPort){
        try {
            ServerSocket ss = new ServerSocket(recPort, 3);
            Socket accept = ss.accept();
            InputStream is = accept.getInputStream();
            BufferedReader br = new BufferedReader(new InputStreamReader(is));
            while (true){

               /* String buf = null;
                StringBuilder sb = new StringBuilder();
                char[] bytes = new char[4];
                br.read(bytes,0,4);
                Integer integer = new Integer(bytes);
                byte[] content= new byte[integer.intValue()-4];
                br.read(content);
                recQ.put(new Package());
                Log.e(TAG,sb.toString());*/
            }
//            is.close();
//            accept.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    public void runSend(int sendPort){
        try {
            Socket socket = new Socket("localhost",sendPort);
            OutputStream outputStream = socket.getOutputStream();
            BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(outputStream));

            while (true){
                if(!sendQ.isEmpty()){
                    Package poll = (Package) sendQ.poll();
                    if(poll==null){
                        Thread.sleep(100);
                    }
                    int len = 4 + 4 +poll.content.length()*2;
                    bw.write(len);
                    bw.write(poll.type);
                    bw.write(poll.content);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
