package com.ruilonglai.texas_scan.newprocess;

import android.text.TextUtils;
import android.util.Log;

import com.google.gson.Gson;
import com.ruilonglai.texas_scan.entity.Boards;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by Administrator on 2017/8/11.
 */

public class JsonTool {
    public static final String TAG = "JsonTool";
    public static String getJsonName(String json,String key){
        String name = null;
        try {
            JSONObject jsonObject = new JSONObject(json);
            name = jsonObject.getString(key);
        } catch (JSONException e) {
            Log.e(TAG,"解析名字出错");
        }
        return name;
    }
    /*获取json中int类型数据 */
    public static int getJsonMes(String json,String key){
        int anInt = -1;
        if(json.contains(key)){
            try {
                JSONObject jsonObject = new JSONObject(json);
                anInt = jsonObject.getInt(key);
                Log.e(TAG,key+"-->"+anInt);
            } catch (JSONException e) {
                Log.e(TAG,key+"解析出错");
            }
        }
        return anInt;
    }

    public static Boards getBoards(String json){
        Gson gson = new Gson();
        if(TextUtils.isEmpty(json))
            return null;
        Boards boards = gson.fromJson(json, Boards.class);
        Log.e(TAG,json);
        return boards;
    }
}
