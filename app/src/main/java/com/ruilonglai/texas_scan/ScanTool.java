package com.ruilonglai.texas_scan;

import android.graphics.Bitmap;

/**
 * Created by sjt on 2017/8/6.
 */

public class ScanTool {
    public static native int InitScan(String path);
    public static native void EndScan();
    public static native int SetTemplate(String temp);
    public static native int CalcPower(int pokerCount, int[] pokers);
    public static native float CalcProbablyWinRate(int playerCount,int[] pokers,int[] boards);
    public static native int IsImgOK(Bitmap img);
    public static native String ScanBtn(Bitmap img);
    public static native String ScanSeatCount(Bitmap img);
    public static native String ScanBlinds(Bitmap img);
    public static native String ScanAnte(Bitmap img);
    public static native String ScanStraddle(Bitmap img);
    public static native String ScanBoardCards(Bitmap img);
    public static native String ScanDichi(Bitmap img);
    public static native String ScanCurDichi(Bitmap img);
    public static native int InitScanName(String path,String temp);
    public static native void EndNameScan();
    public static native String ScanName(Bitmap img,int pos,int force);
    public static native String ScanSeatName(Bitmap img,int pos,int force);
    public static native String ScanSeat(Bitmap img,int pos);
}