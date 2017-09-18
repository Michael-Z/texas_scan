package com.ruilonglai.texas_scan.util;

import android.content.Context;
import android.widget.Toast;

/**
 * Created by Administrator on 2017/7/18.
 */

public class ToastUtil {
    public static void showError(Context context,String errMsg){
        Toast.makeText(context,errMsg,Toast.LENGTH_SHORT).show();
    }
}
