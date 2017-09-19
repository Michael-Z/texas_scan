package com.ruilonglai.texas_scan.data;

import com.ruilonglai.texas_scan.entity.MyData;
import com.ruilonglai.texas_scan.util.TimeUtil;

import org.litepal.crud.DataSupport;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by Administrator on 2017/7/26.
 */

public class MyDataUtil {


    public static List<MyData> getAllMyData(){

        return  DataSupport.findAll(MyData.class);
    }

    public static List<MyData> getSomeDayMyData(int daySize){
        List<MyData> list = new ArrayList<MyData>();
        for (int i = 0; i < daySize; i++) {
            Date date = new Date(System.currentTimeMillis() - i * 24 * 3600 * 1000);
            List<MyData> myDatas = DataSupport.where("date=?", TimeUtil.getCurrentDateToDay(date)).find(MyData.class);
            list.addAll(myDatas);
        }
        return list;
    }

    public static List<MyData> getOneDayMyData(Date date){

        return DataSupport.where("date=?", TimeUtil.getCurrentDateToDay(date)).find(MyData.class);
    }
}
