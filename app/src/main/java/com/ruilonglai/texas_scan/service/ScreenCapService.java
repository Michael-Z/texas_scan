package com.ruilonglai.texas_scan.service;

import android.app.ActivityManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Binder;
import android.os.Build;
import android.os.IBinder;
import android.os.Looper;
import android.util.Log;

import com.ruilonglai.texas_scan.ScreenShotUtil.ScreentShotUtil;
import com.ruilonglai.texas_scan.util.FileIOUtil;

import java.io.File;

public class ScreenCapService extends Service {
    private  PendingIntent pi;
	private  Callback callback;
    boolean isStop = false;
	boolean canCap = false;
	static Looper looper;
	private  ChangeMsgBinder changeMsgBinder = new ChangeMsgBinder();
	ScreentShotUtil instance = ScreentShotUtil.getInstance();
	@Override
	public IBinder onBind(Intent intent) {
		return changeMsgBinder;
	}

	@Override
	public void onCreate() {
		super.onCreate();
		Log.d("Service", "onCreate executed");

	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		// TODO Auto-generated method stub
       return super.onStartCommand(intent, flags, startId);
	}
	@Override
	public void onDestroy() {
		super.onDestroy();
		Log.d("Service", "onDestroy executed");
        isStop = true;
	}
	public void setCallBack(Callback callBack){
		this.callback = callBack;
	}
    public class ChangeMsgBinder extends Binder{
		public ScreenCapService getService() {
			return ScreenCapService.this;
		}
	}
	public interface Callback {
		void getPath(String str);
	}
}
