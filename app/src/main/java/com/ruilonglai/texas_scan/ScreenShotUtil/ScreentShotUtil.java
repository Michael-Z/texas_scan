package com.ruilonglai.texas_scan.ScreenShotUtil;


import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.util.Log;
import android.view.Surface;

import com.ruilonglai.texas_scan.util.FileIOUtil;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class ScreentShotUtil {

	private static final String TAG = "ScreentShotUtil";

	private static final String CLASS1_NAME = "android.view.SurfaceControl";

	private static final String CLASS2_NAME = "android.view.Surface";

	private static final String METHOD_NAME = "screenshot";

	private static ScreentShotUtil instance;

	private ShellUtils shell;

	private ScreentShotUtil() {
		shell = new ShellUtils();
		shell.Init(true);
	}

	public static ScreentShotUtil getInstance() {
		synchronized (ScreentShotUtil.class) {
			if (instance == null) {
				instance = new ScreentShotUtil();
			}
		}
		return instance;
	}
	public void initOrRefresh(){
		shell = new ShellUtils();
		shell.Init(true);
	}
	public static Bitmap screenShot(int width, int height) {
		Log.i(TAG, "android.os.Build.VERSION.SDK : "
				+ android.os.Build.VERSION.SDK_INT);
		Class<?> surfaceClass = null;
		Method method = null;
		try {
			Log.i(TAG, "width : " + width);
			Log.i(TAG, "height : " + height);
			if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.JELLY_BEAN_MR2) {

				surfaceClass = Class.forName(CLASS1_NAME);
			} else {
				surfaceClass = Class.forName(CLASS2_NAME);
			}
			method = surfaceClass.getDeclaredMethod(METHOD_NAME, int.class,
					int.class);
			method.setAccessible(true);
			Bitmap bitmap = (Bitmap)method.invoke(null, width, height);
			return  bitmap;
		} catch (NoSuchMethodException e) {
			Log.e(TAG, e.toString());
		} catch (IllegalArgumentException e) {
			Log.e(TAG, e.toString());
		} catch (IllegalAccessException e) {
			Log.e(TAG, e.toString());
		} catch (InvocationTargetException e) {
			Log.e(TAG, e.toString());
		} catch (ClassNotFoundException e) {
			Log.e(TAG, e.toString());
		}
		return null;
	}

	/**
	 * Takes a screenshot of the current display and shows an animation.
	 */
	@SuppressLint("NewApi")
	public String takeScreenshot(Context context,int index) {
		    String fileFullPath = "/mnt/sdcard/desk_scan/cap"+index+".png";
		    if(index==0){
				fileFullPath = "/mnt/sdcard/desk_scan/cap.png";
			}
			if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
				FileIOUtil.saveToFile("截图指令执行!");
				shell.execCommand("rm /mnt/sdcard/desk_scan/tmp*.png;/system/bin/screencap /mnt/sdcard/desk_scan/tmp.png;mv /mnt/sdcard/desk_scan/tmp*.png "+fileFullPath);
				FileIOUtil.saveToFile("截图指令执行完毕!");
			}
			return fileFullPath;
	}
	public void takeScreenshotTmp(Context context){
		if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
			shell.execCommand("/system/bin/screencap /mnt/sdcard/desk_scan/error.png");
		}
	}
	public void removeAllCapImg(Context context){
		if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
			shell.execCommand("rm /mnt/sdcard/desk_scan/cap*.png;");
		}
	}
	//
	public void saveBitmap2file(Context context, Bitmap bmp, String fileName) {
		int quality = 100;
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		bmp.compress(Bitmap.CompressFormat.PNG, quality, baos);
		InputStream is = new ByteArrayInputStream(baos.toByteArray());
		byte[] buffer = new byte[1024];
		int len = 0;
		File file = new File(fileName);
		if (!file.exists()) {
			try {
				file.getParentFile().mkdir();
				file.getParentFile().createNewFile();
			} catch (IOException e) {
				Log.e(TAG, e.toString());
			}
		} else {
			try {
				file.getParentFile().delete();
				file.getParentFile().createNewFile();
			} catch (IOException e) {
				Log.e(TAG, e.toString());
			}
		}
		FileOutputStream stream = null;
		try {
			stream = new FileOutputStream(file);
			while ((len = is.read(buffer)) != -1) {
				stream.write(buffer, 0, len);
			}
			stream.flush();
		} catch (FileNotFoundException e) {
			Log.i(TAG, e.toString());
		} catch (IOException e) {
			Log.i(TAG, e.toString());
		} finally {
			if (is != null) {
				try {
					is.close();
				} catch (IOException e) {
					Log.i(TAG, e.toString());
				}
			}
			if (stream != null) {
				try {
					stream.close();
				} catch (IOException e) {
					Log.i(TAG, e.toString());
				}
			}
		}
		if (bmp != null && !bmp.isRecycled()) {
			bmp.recycle();
		}
	}

	/**
	 * @return the current display rotation in degrees
	 */
	private float getDegreesForRotation(int value) {
		switch (value) {
		case Surface.ROTATION_90:
			return 360f - 90f;
		case Surface.ROTATION_180:
			return 360f - 180f;
		case Surface.ROTATION_270:
			return 360f - 270f;
		}
		return 0f;
	}

}
