package com.ruilonglai.texas_scan.util;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Administrator on 2017/4/13.
 */

public class GsonUtil {
    // 将Json数据解析成相应的映射对象
    public static <T> T parseJsonWithGson(String jsonData, Class<T> type) {
        Gson gson = new Gson();
        T result = gson.fromJson(jsonData, type);
        return result;
    }
    // 将Json数组解析成相应的映射对象列表
    public static <T> List<T> parseJsonArrayWithGson(String jsonData, Class<T> type) {
        Gson gson = new Gson();
        List<T> result = new ArrayList<T>();
        result = gson.fromJson(jsonData, new TypeToken<List<T>>() {}.getType());
        return result;
    }
    /*获取json对象map中一个key对应的值*/
    public static String getContent(String json,String key){
        JSONObject object = null;
        String string = "";
        try {
            object = new JSONObject(json);
            string = object.getString(key);
        } catch (JSONException e) {
           return string;
        }
        return string;
    }
}
