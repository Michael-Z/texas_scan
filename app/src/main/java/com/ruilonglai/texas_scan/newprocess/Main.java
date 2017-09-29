package com.ruilonglai.texas_scan.newprocess;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Looper;
import android.text.TextUtils;
import android.util.Log;

import com.ruilonglai.texas_scan.ScanTool;
import com.ruilonglai.texas_scan.ScreenShotUtil.ScreentShotUtil;
import com.ruilonglai.texas_scan.util.AssetsCopyUtil;
import com.ruilonglai.texas_scan.util.Constant;
import com.ruilonglai.texas_scan.util.PokerAnalysisTool;

import java.io.ByteArrayOutputStream;
import java.net.Socket;


/**
 * Created by wgl on 2017/7/31.
 */

public class Main {

    public static boolean begin = true;
    public static int flag;
    public static void main(String[] args) {
        System.out.println("Andcast Main Entry!");
        String str =  args[0];
        String[] split = str.split("#");
        String packagename = split[0];
        if(!TextUtils.isEmpty(packagename)&& Boolean.valueOf(split[1])){
            Log.e("main",packagename);
            System.load(packagename+"/lib/arm/libleptonica.so");
            System.load(packagename+"/lib/arm/liblibtesseract.so");
            System.load(packagename+"/lib/arm/libopencv_info.so");
            System.load(packagename+"/lib/arm/libnative-lib.so");
        }else{
            System.load("/data/data/com.ruilonglai.texas_scan/lib/libleptonica.so");
            System.load("/data/data/com.ruilonglai.texas_scan/lib/liblibtesseract.so");
            System.load("/data/data/com.ruilonglai.texas_scan/lib/libopencv_info.so");
            System.load("/data/data/com.ruilonglai.texas_scan/lib/libnative-lib.so");
        }
        int initRet = ScanTool.InitScan("/mnt/sdcard/desk_scan");
        ScanTool.SetTemplate("dpq");
        PokerAnalysisTool instance = PokerAnalysisTool.getInstance();
        boolean isConnect = false;
        BitmapFactory.Options opts = new BitmapFactory.Options();
        while (begin){
            if(!isConnect){
                Connect.getInstance().connect(28838);
                Connect.getInstance().setCallback(new Connect.CallBack() {
                    @Override
                    public void action(int type) {
                        Log.e("Main","设置的模板"+type);
                        ScanTool.SetTemplate(Constant.PLATFORM[type-8]);
                        flag = type-8;
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
            if(Boolean.valueOf(split[1])){//手机上运

            }
//            byte[] bytes = getBitmapData(bitmap);
//            if(bitmap!=null){
//                bitmap.recycle();
//                bitmap = null;
//                System.gc();
//            }
//            Log.e("Main","获取bitmap时间 "+(System.currentTimeMillis()-beginTime) + "ms"+"bitmap字节数:"+bytes.length);
//            try {
//                bitmap = BitmapFactory.decodeByteArray(bytes,0,bytes.length, opts);
//            } catch (OutOfMemoryError e) {
//                if(bitmap!=null){
//                    bitmap.recycle();
//                    bitmap = null;
//                    System.gc();
//                }
//            }
            if(bitmap!=null){
                long beginTime2 = System.currentTimeMillis();
                instance.analysisBitmap(bitmap, flag);
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

    public static byte[] getBitmapData(Bitmap bmp) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bmp.compress(Bitmap.CompressFormat.PNG, 100, baos);
        return baos.toByteArray();
    }
}
