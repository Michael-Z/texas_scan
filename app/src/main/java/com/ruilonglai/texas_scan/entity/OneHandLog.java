package com.ruilonglai.texas_scan.entity;

import org.litepal.crud.DataSupport;

/**
 * Created by wangshuai on 2017/5/16.
 */

public class OneHandLog extends DataSupport{
    private long date;
    private String log;

    public long getDate() {
        return date;
    }

    public void setDate(long date) {
        this.date = date;
    }

    public String getLog() {
        return log;
    }

    public void setLog(String log) {
        this.log = log;
    }
}
