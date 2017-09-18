package com.ruilonglai.texas_scan.util;

import android.graphics.Bitmap;
import android.util.Log;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * Created by wgl on 2017/8/4.
 */

public class StreamTool {

    public final static String TAG = "StreamTool";

    public static byte[] getNetImage(InputStream inStream) throws Exception {

        ByteArrayOutputStream outStream = new ByteArrayOutputStream();
        byte[] buffer = new byte[1024];
        int len = 0;

        // 读取到流的末尾，返回-1
        // 这里为只要还没有读到流的末尾，就一直往内存中输出二进制数据
        while ((len = inStream.read(buffer)) != -1) {
            outStream.write(buffer, 0, len);
        }
        inStream.close();
        return outStream.toByteArray();
    }

    /** 保存方法 */
    public static void saveBitmap(Bitmap bm,String picName) {
        Log.e(TAG, "保存图片");
        File f = new File("/sdcard/desk_scan/", picName);
        if (f.exists()) {
            f.delete();
        }
        try {
            FileOutputStream out = new FileOutputStream(f);
            bm.compress(Bitmap.CompressFormat.PNG, 90, out);
            out.flush();
            out.close();
            Log.i(TAG, "已经保存");
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
