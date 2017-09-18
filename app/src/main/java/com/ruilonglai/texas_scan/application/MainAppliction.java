package com.ruilonglai.texas_scan.application;

import com.ruilonglai.texas_scan.config.SystemParams;
import com.ruilonglai.texas_scan.log.CrashHandler;

import org.litepal.LitePalApplication;
/**
 * Created by wgl on 2017/6/28.
 */

public class MainAppliction extends LitePalApplication {
    @Override
    public void onCreate() {
        super.onCreate();
        CrashHandler.getInstance().init(this);
        SystemParams.init(this);
    }
}
