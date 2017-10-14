package com.ruilonglai.texas_scan.activity;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.DisplayMetrics;
import android.view.KeyEvent;
import android.widget.TextView;
import android.widget.Toast;

import com.ruilonglai.texas_scan.MainActivity;
import com.ruilonglai.texas_scan.R;
import com.ruilonglai.texas_scan.config.SystemParams;
import com.ruilonglai.texas_scan.download.DownLoadUtils;
import com.ruilonglai.texas_scan.download.DownloadApk;
import com.ruilonglai.texas_scan.newprocess.MainProcessUtil;
import com.ruilonglai.texas_scan.util.AssetsCopyUtil;
import com.ruilonglai.texas_scan.util.SystemInfoUtil;
import com.ruilonglai.texas_scan.view.CustomDialog;
import com.tendcloud.tenddata.TCAgent;

import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class LaunchActivity extends AppCompatActivity {
    final int REQUEST_SDCARD = 1;
    ProgressDialog progressDialog = null;
    boolean isSave = false;
    @BindView(R.id.versionName)
    TextView versionName;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_launch);
        MainProcessUtil.getInstance().exit(LaunchActivity.this);
        Map<String,String> map = SystemInfoUtil.collectDeviceInfo(this);
        String cpu_abi = map.get("CPU_ABI");
        if(cpu_abi.contains("arm")){
            getSharedPreferences(LoginActivity.PREF_FILE,MODE_PRIVATE).edit().putBoolean("isPhone",true).apply();
        }
        //1.注册下载广播接收器
        DownloadApk.registerBroadcast(this);
        //2.删除已存在的Apk
        DownloadApk.removeFile(this);
        ButterKnife.bind(this);
        try {
            PackageManager manager = this.getPackageManager();
            PackageInfo packageInfo = manager.getPackageInfo(this.getPackageName(), 0);
            versionName.setText(packageInfo.versionName+"");
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        initAppAnalytics();
        checkVersion(this);
        saveScreenWidthAndHeight();
    }

    public void applyPermission() {
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission_group.STORAGE) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE}, REQUEST_SDCARD);
            }
        } else {
            updateFiles();
        }
    }

    public void initAppAnalytics() {
        TCAgent.LOG_ON = true;
        // App ID: 在TalkingData创建应用后，进入数据报表页中，在“系统设置”-“编辑应用”页面里查看App ID。
        // 渠道 ID: 是渠道标识符，可通过不同渠道单独追踪数据。
        TCAgent.init(this, "A2A17B8DEA844ECE911145791C78AD18", "E0493A651AFC116CD24E3B5955E297ED");
        // 如果已经在AndroidManifest.xml配置了App ID和渠道ID，调用TCAgent.init(this)即可；或与AndroidManifest.xml中的对应参数保持一致。
        TCAgent.setReportUncaughtExceptions(true);
    }
    public void updateFiles() {
        SharedPreferences share = getSharedPreferences("data", MODE_PRIVATE);
        isSave = share.getBoolean("isSave", false);
        progressDialog = new ProgressDialog(LaunchActivity.this);
        progressDialog.setTitle("提示");
        progressDialog.setMessage("更新数据中，请稍后。。。");
        progressDialog.setCancelable(true);
        if (!isSave) {
            progressDialog.show();//需要更新资源，显示等待提示
        }
        new Thread(new Runnable() {
            @Override
            public void run() {
               if (!isSave) {
                    boolean copySuccess = AssetsCopyUtil.copyAssetsFilesToData(LaunchActivity.this);
                    SharedPreferences.Editor editor = getSharedPreferences("data", MODE_PRIVATE).edit();
                    editor.putBoolean("isSave", copySuccess);
                    editor.apply();
                    progressDialog.cancel();
                } else {
                    try {
                        Thread.sleep(1000);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        //跳转至 MainActivity
                        Intent intent = new Intent(LaunchActivity.this, LoginActivity.class);
                        startActivity(intent);
                        LaunchActivity.this.finish();
                    }
                });
            }
        }).start();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case REQUEST_SDCARD://请求sd卡权限回调
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    updateFiles();
                }
                break;
        }
    }
    public boolean checkVersion(final Context context) {//
        try {
            PackageManager manager = context.getPackageManager();
            PackageInfo packageInfo = manager.getPackageInfo(context.getPackageName(), 0);
            final int versionCode = packageInfo.versionCode;//当前版本号
            int oldVersionCode = SystemParams.getInstance().getInt("versionCode");
            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        OkHttpClient client = new OkHttpClient();
                        Request request = new Request.Builder()
                                .url("http://ruilonglai.com:30000/check_version.json")
                                .build();
                        Response response = client.newCall(request).execute();
                        String string = response.body().string();
                        JSONObject json = new JSONObject(string);
                        final int newVersionCode = json.getInt("versionCode");
                        final String versionName = json.getString("versionName");
                        Message msg = new Message();
                        if(newVersionCode>versionCode){
                            msg.obj = versionName;
                            msg.arg1 = 2;
                            SharedPreferences.Editor editor = getSharedPreferences("data", MODE_PRIVATE).edit();
                            editor.putBoolean("isSave", false);
                            editor.apply();
                            handler.sendMessage(msg);
                        }else{
                            msg.arg1 = 1;
                            handler.sendMessage(msg);
                        }
                    } catch (Exception e) {
                        Message msg = new Message();
                        msg.arg1 = 0;
                        handler.sendMessage(msg);
                    }
                }
            }).start();
        } catch (Exception e) {
            return true;
        }
        return false;
    }

    Handler handler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            final String  vName = (String) msg.obj;
            switch (msg.arg1){
                case 2:
                    final CustomDialog.Builder builder = new CustomDialog.Builder(LaunchActivity.this);
                    builder.setTitle("更新");
                    builder.setMessage("软件有新版本，是否更新?");
                    builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            if (DownLoadUtils.getInstance(LaunchActivity.this).canDownload()) {
                                DownloadApk.downloadApk(LaunchActivity.this, "http://ruilonglai.com:30000/texas_scan_"+vName+".apk", "德扑数据大师更新", "texas_scan_"+vName);
                            } else {
                                DownLoadUtils.getInstance(LaunchActivity.this).skipToDownloadManager();
                            }
                            dialog.dismiss();
                            ProgressDialog progress = new ProgressDialog(LaunchActivity.this);
                            progress.setMessage("新版本正在下载，请稍后...");
                            progress.setCancelable(false);
                            progress.show();
                        }
                    });
                    builder.setNegativeButton("取消", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            applyPermission();
                            dialog.dismiss();
                        }
                    });
                    builder.create().show();
                    break;
                case 1:
                    try {
                        File file = new File("/mnt/sdcard/desk_scan/update.txt");
                        if(!file.exists()){
                            SharedPreferences.Editor editor = getSharedPreferences("data", MODE_PRIVATE).edit();
                            editor.putBoolean("isSave", false);
                            editor.apply();
                            file.createNewFile();
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    applyPermission();
                    break;
                case 0:
                    Toast.makeText(LaunchActivity.this,"更新失败", Toast.LENGTH_SHORT).show();
                    startActivity(new Intent(LaunchActivity.this,LoginActivity.class));
                    break;
            }
        }
    };
    /*返回键退出*/
    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        if (event.getKeyCode() == KeyEvent.KEYCODE_BACK && event.getRepeatCount() == 0 && event.getAction() == KeyEvent.ACTION_DOWN) {
            System.exit(0);
        }
        return super.dispatchKeyEvent(event);
    }
    @Override
    protected void onDestroy() {
        super.onDestroy();
        DownloadApk.unregisterBroadcast(LaunchActivity.this);
    }


    public void saveScreenWidthAndHeight(){
        DisplayMetrics metrics = new DisplayMetrics();
        this.getWindowManager().getDefaultDisplay().getMetrics(metrics);
        SharedPreferences spf = getSharedPreferences(LoginActivity.PREF_FILE, Context.MODE_PRIVATE);
             spf.edit()
                     .putFloat("density",metrics.density)
                     .putInt("width",metrics.widthPixels)
                     .putInt("height",metrics.heightPixels)
                     .putFloat("xdpi",metrics.xdpi)
                     .putFloat("ydpi",metrics.ydpi)
                     .apply();
    }
}
