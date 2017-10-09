package com.ruilonglai.texas_scan.util;

import android.app.Activity;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;

/**
 * Created by wgl on 2017/6/2.
 */

public class HttpUtil {
//    public final static String url = "http://ruilonglai.com:40001/console/";
    public final static String url = "http://ruilonglai.com:40003/console/";
//    public final static String url = "http://192.168.2.105:40003/console/";
    public static void sendPostRequestData(String action ,final String reqData, Callback callback){//发送数据到服务器
        OkHttpClient client = new OkHttpClient();
        RequestBody body = new FormBody.Builder().add("param",reqData).build();
        Request request = new Request.Builder().url(url+action+".do").post(body).build();
        client.newCall(request).enqueue(callback);
    }
    //是否有网络
    public static boolean networkConnected(Activity activity) {
        ConnectivityManager conManager = (ConnectivityManager) activity.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = conManager.getActiveNetworkInfo();
        return networkInfo != null && networkInfo.isConnected();
    }
}
