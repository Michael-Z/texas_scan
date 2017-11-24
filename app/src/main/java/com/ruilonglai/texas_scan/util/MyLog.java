package com.ruilonglai.texas_scan.util;

import android.util.Log;

/**
 * Created by wgl on 2017/11/21.
 */

public class MyLog {
    private static boolean isShow = true;
    public static void e(String tag,String msg){
        if(isShow){
            Log.e(tag,msg);
        }
    }
    public static void e(String tag,String msg,Throwable ro){
        if(isShow){
            Log.e(tag,msg,ro);
        }
    }
    public static void i(String tag,String msg){
        if(isShow){
            Log.i(tag,msg);
        }
    }
    public static void d(String tag,String msg){
        if(isShow){
            Log.d(tag,msg);
        }
    }
}
