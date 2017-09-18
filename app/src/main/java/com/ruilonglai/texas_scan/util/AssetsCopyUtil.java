package com.ruilonglai.texas_scan.util;

import android.content.Context;
import android.os.Environment;
import android.util.Log;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.channels.FileChannel;

import static android.content.ContentValues.TAG;

/**
 * Created by Administrator on 2017/4/12.
 */

public class AssetsCopyUtil {
    final String PATH_DB = "";
    private static String packageName = "com.ruilonglai.texas_scan-1";
    public static boolean copyAssetsFilesToData(Context context) {
        String inPath = "desk_scan";
        String outPath = "/mnt/sdcard/desk_scan";
        long begin = System.currentTimeMillis();
        boolean ret = copyFiles(context, inPath, outPath);
        long end = System.currentTimeMillis();
        Log.i(TAG, "copyAssetsFilesToData() elapsedTime:" + (end-begin));
        return ret;
    }
    public static boolean copyFiles(Context context, String inPath, String outPath) {
        Log.i(TAG, "copyFiles() inPath:" + inPath + ", outPath:" + outPath);
        String[] fileNames = null;
        try {// 获得Assets一共有几多文件
            fileNames = context.getAssets().list(inPath);
        } catch (IOException e1) {
            e1.printStackTrace();
            return false;
        }
        if (fileNames.length > 0) {//如果是目录
            File fileOutDir = new File(outPath);
            if(fileOutDir.isFile()){
                boolean ret = fileOutDir.delete();
                if(!ret){
                    Log.e(TAG, "delete() FAIL:" + fileOutDir.getAbsolutePath());
                }
            }
            if (!fileOutDir.exists()) { // 如果文件路径不存在
                if (!fileOutDir.mkdirs()) { // 创建文件夹
                    Log.e(TAG, "mkdirs() FAIL:" + fileOutDir.getAbsolutePath());
                    return false;
                }
            }
            for (String fileName : fileNames) { //递归调用复制文件夹
                String inDir = inPath;
                String outDir = outPath + File.separator;
                if(!inPath.equals("")) { //空目录特殊处理下
                    inDir = inDir + File.separator;
                }
                copyFiles(context,inDir + fileName, outDir + fileName);
            }
            return true;
        } else {//如果是文件
            try {
                File fileOut = new File(outPath);
                if(fileOut.exists()) {
                    boolean ret = fileOut.delete();
                    if(!ret){
                        Log.e(TAG, "delete() FAIL:" + fileOut.getAbsolutePath());
                    }
                }
                boolean ret = fileOut.createNewFile();
                if(!ret){
                    Log.e(TAG, "createNewFile() FAIL:" + fileOut.getAbsolutePath());
                }
                FileOutputStream fos = new FileOutputStream(fileOut);
                InputStream is = context.getAssets().open(inPath);
                byte[] buffer = new byte[1024];
                int byteCount=0;
                while((byteCount = is.read(buffer)) > 0) {
                    fos.write(buffer, 0, byteCount);
                }
                fos.flush();//刷新缓冲区
                is.close();
                fos.close();
                return true;
            } catch (Exception e) {
                e.printStackTrace();
                return false;
            }
        }
    }
    /*拷贝sd备份文件到应用数据库目录下*/
    public static boolean copySDCardToDataBase(Context context){
        if (!Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState())) {
            return false;
        }
        File file = new File(context.getDatabasePath("playersData")+".db");
        File dbFile  = new File(Environment.getExternalStorageDirectory()+File.separator+"desk_scan", "playersData.db");

        FileChannel inChannel = null,outChannel = null;

        try {
            if(file.exists()) {
                inChannel = new FileInputStream(dbFile).getChannel();
                outChannel = new FileOutputStream(file).getChannel();
                inChannel.transferTo(0, inChannel.size(), outChannel);
            }
        } catch (Exception e) {
            return  false;
        }finally{
            try {
                if (inChannel != null) {
                    inChannel.close();
                    inChannel = null;
                }
                if(outChannel != null){
                    outChannel.close();
                    outChannel = null;
                }
            } catch (IOException e) {
              return false;
            }
        }
        return true;
    }
    /*拷贝数据库文件到sd卡中*/
    public static void copyDataBaseToSD(Context context){
        if (!Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState())) {
            return ;
        }
        File dbFile = new File(context.getDatabasePath("playersData")+".db");
        File file  = new File(Environment.getExternalStorageDirectory()+File.separator+"desk_scan", "playersData.db");

        FileChannel inChannel = null,outChannel = null;

        try {
            if(file.exists()){
                file.delete();
            }
            file.createNewFile();
            inChannel = new FileInputStream(dbFile).getChannel();
            outChannel = new FileOutputStream(file).getChannel();
            inChannel.transferTo(0, inChannel.size(), outChannel);
        } catch (Exception e) {
            FileIOUtil.saveToFile("保存本地数据失败!");
        }finally{
            try {
                if (inChannel != null) {
                    inChannel.close();
                    inChannel = null;
                }
                if(outChannel != null){
                    outChannel.close();
                    outChannel = null;
                }
            } catch (IOException e) {
                FileIOUtil.saveToFile("保存本地数据失败!");
            }
        }
    }
    public static void copyDataBaseToSD(Context context,String path,String fileName){
        if (!Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState())) {
            return ;
        }
        File dbFile = new File(path);
        File file  = new File(Environment.getExternalStorageDirectory()+File.separator+"desk_scan", fileName);

        FileChannel inChannel = null,outChannel = null;

        try {
            if(file.exists()){
                file.delete();
            }
            file.createNewFile();
            inChannel = new FileInputStream(dbFile).getChannel();
            outChannel = new FileOutputStream(file).getChannel();
            inChannel.transferTo(0, inChannel.size(), outChannel);
        } catch (Exception e) {
            FileIOUtil.saveToFile("复制apk失败");
        }finally{
            try {
                if (inChannel != null) {
                    inChannel.close();
                    inChannel = null;
                }
                if(outChannel != null){
                    outChannel.close();
                    outChannel = null;
                }
            } catch (IOException e) {
                FileIOUtil.saveToFile("复制apk失败!");
            }
        }
    }
    /*获取解析的包名*/
    public static String getPackageName(Context context){
        String packageResourcePath = context.getApplicationContext().getPackageResourcePath();
        Log.e(TAG,"包名:"+packageResourcePath);
        return packageResourcePath;
    }
}
