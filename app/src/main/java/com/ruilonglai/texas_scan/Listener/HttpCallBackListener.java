package com.ruilonglai.texas_scan.Listener;

/**
 * Created by Administrator on 2017/6/2.
 */

public interface HttpCallBackListener {

    void finish(String response);

    void error(Exception e);
}
