package com.ruilonglai.texas_scan.data;

import com.ruilonglai.texas_scan.entity.MyData;
import com.ruilonglai.texas_scan.util.TimeUtil;

import org.litepal.crud.DataSupport;

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
            List<MyData> myDatas = DataSupport.findAll(MyData.class);
        return myDatas;
    }

    public static List<MyData> getOneDayMyData(Date date){

        List<MyData> myDatas = DataSupport.where("date=?", TimeUtil.getCurrentDateToDay(date)).find(MyData.class);
        return myDatas;
    }
}
