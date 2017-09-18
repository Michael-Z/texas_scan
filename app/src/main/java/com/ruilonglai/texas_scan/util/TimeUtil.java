package com.ruilonglai.texas_scan.util;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by Administrator on 2017/5/5.
 */

public class TimeUtil {
    final static String path = "yyyyMMddHHmm";
    final static String path2 = "yyyyMMddHHmmss";
    final static String path1 = "yyyyMMdd";

    public static String changeDateToShow(String str){
        Long date = Long.valueOf(str);
        return (int)(date/10000)+"/"+(int)(date%10000/100)+"/"+(int)(date%10000%100);
    }
    public static String changeDateToShow2(Date date1){
        String str = getCurrentDateToDay(date1);
        Long date = Long.valueOf(str);
        return (int)(date/10000)+"/"+(int)(date%10000/100)+"/"+(int)(date%10000%100);
    }
    //取小数点后两位
    public static String changeShow(float f) {
        DecimalFormat df = new DecimalFormat("#.00");
        return df.format(f);
    }
    public static String getCurrentDateToMinutes(Date date){
        SimpleDateFormat sdf = new SimpleDateFormat(path);
        String format = sdf.format(date);
        return format;
    }
    public static String getCurrentDateToDay(Date date){
        SimpleDateFormat sdf = new SimpleDateFormat(path1);
        String format = sdf.format(date);
        return format;
    }
    public static String getCurrentDateToSecond(Date date){
        SimpleDateFormat sdf = new SimpleDateFormat(path2);
        String format = sdf.format(date);
        return format;
    }
    public static String getDate(int year,int month,int day){
        StringBuffer sb = new StringBuffer();
        sb.append(year);
        if(month<9){
            sb.append("0");
        }
        sb.append(month+1);
        if(day<10){
            sb.append("0");
        }
        sb.append(day);
        return sb.toString();
    }
}
