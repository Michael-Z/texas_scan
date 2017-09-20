package com.ruilonglai.texas_scan.newprocess;

import android.graphics.Bitmap;
import android.os.Looper;
import android.util.Log;

import com.ruilonglai.texas_scan.ScanTool;
import com.ruilonglai.texas_scan.ScreenShotUtil.ScreentShotUtil;
import com.ruilonglai.texas_scan.util.Constant;
import com.ruilonglai.texas_scan.util.PokerAnalysisTool;

import java.io.ByteArrayOutputStream;
import java.net.Socket;


/**
 * Created by wgl on 2017/7/31.
 */

public class Main {
    public static boolean begin = true;
    static {
        System.load("/data/data/com.ruilonglai.texas_scan/lib/libleptonica.so");
        System.load("/data/data/com.ruilonglai.texas_scan/lib/liblibtesseract.so");
        System.load("/data/data/com.ruilonglai.texas_scan/lib/libopencv_info.so");
        System.load("/data/data/com.ruilonglai.texas_scan/lib/libnative-lib.so");
//        华为
//        System.load("/data/app/com.ruilonglai.texas_scan-1/lib/arm64/libleptonica.so");
//        System.load("/data/app/com.ruilonglai.texas_scan-1/lib/arm64/liblibtesseract.so");
//        System.load("/data/app/com.ruilonglai.texas_scan-1/lib/arm64/libopencv_info.so");
//        System.load("/data/app/com.ruilonglai.texas_scan-1/lib/arm64/libnative-lib.so");
    }

    public static void main(String[] args) {
        System.out.println("Andcast Main Entry!");
        int initRet = ScanTool.InitScan("/mnt/sdcard/desk_scan");
        ScanTool.SetTemplate("dpq");
        PokerAnalysisTool instance = PokerAnalysisTool.getInstance();
        boolean isConnect = false;
        while (begin){
            if(!isConnect){
                Connect.getInstance().connect(28838);
                Connect.getInstance().setCallback(new Connect.CallBack() {
                    @Override
                    public void action(int type) {
                        Log.e("Main","设置的模板"+type);
                        ScanTool.SetTemplate(Constant.PLATFORM[type-8]);
                    }
                    @Override
                    public void exit() {
                        begin = false;
                    }
                });
                Package pkg = new Package();
                pkg.setType(Constant.SOCKET_GET_TEMPLATE);//获取平台
                Connect.send(pkg);
                isConnect = true;
            }
            long beginTime = System.currentTimeMillis();
            Bitmap bitmap = ScreentShotUtil.screenShot(720, 1280);
            Log.e("Main","获取bitmap时间 "+(System.currentTimeMillis()-beginTime) + "ms");
            ScanTool.ScanSeatCount(bitmap);
            if(bitmap!=null){
                long beginTime2 = System.currentTimeMillis();
                instance.analysisBitmap(bitmap, 0);
                long time = System.currentTimeMillis() - beginTime;
                Log.e("Main","解析时间 "+ time + "ms");
                Log.e("Main","----------------------------------------------");
                try {
                    if(1000-time>0)
                        Thread.sleep(1000-time);
                }catch (Exception e){
                    Log.e("Main","线程出错"+e.toString());
                }
            }else{
                Log.e("MainActivity","bitmap为空");
            }
        }
        Log.e("Main","main进程结束");
        System.exit(0);
    }

    public static byte[] getBitmapData(Bitmap photo) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        photo.compress(Bitmap.CompressFormat.PNG, 100, baos);
        return baos.toByteArray();
    }
}
