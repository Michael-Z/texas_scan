package com.ruilonglai.texas_scan.activity;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.ruilonglai.texas_scan.R;
import com.ruilonglai.texas_scan.entity.JsonBean;
import com.ruilonglai.texas_scan.entity.PokerUser;
import com.ruilonglai.texas_scan.util.HttpUtil;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.HashMap;

import okhttp3.Call;
import okhttp3.Response;

import static com.ruilonglai.texas_scan.util.HttpUtil.networkConnected;

public class RegActivity extends AppCompatActivity {
    private EditText password, et_security, et_number;
    private Button btreg, btsecurity,bteye;
    public static final String mob_APPKEY = "154edf2c734e2";
    public static final String mob_APPSECRET = "9dbe04e99b9c24f73f4a2c631031f852";
    private String number, name, pwd;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reg);
        inti();
        Register();
    }
    public void inti() {
        password = (EditText) findViewById(R.id.etpwd);
        btreg = (Button) findViewById(R.id.btreg);
        et_number = (EditText) findViewById(R.id.phone);
    }
    /*  注册按钮 */
    public void Register() {
        if (networkConnected(this))
            btreg.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    number = et_number.getText().toString().trim();
                    pwd = password.getText().toString().trim();
                    if (pwd.isEmpty()) {
                        Toast.makeText(RegActivity.this, "密码不能为空", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    PokerUser pu = new PokerUser();
                    pu.id = number;
                    pu.nick = "";
                    pu.passwd = pwd;
                    Gson gson = new Gson();
                    String jsonstr = gson.toJson(pu);
                    HttpUtil.sendPostRequestData("register", jsonstr, new okhttp3.Callback() {
                        @Override
                        public void onFailure(Call call, IOException e) {
                            Log.d("失败：", "111" + e);
                            runOnUiThread(new Runnable() {
                                public void run() {
                                    Toast.makeText(getApplicationContext(), "登录失败,请联系管理员！", Toast.LENGTH_SHORT).show();
                                }
                            });
                        }
                        @Override
                        public void onResponse(Call call, Response response) throws IOException {
                            String responseDate = response.body().string();
                            Gson gson = new Gson();
                            Type type = new TypeToken<JsonBean>() {
                            }.getType();
                            final JsonBean jsonBean = gson.fromJson(responseDate, type);
                            String resp = jsonBean.result;
                            if (resp.equals("true")) {
                                runOnUiThread(new Runnable() {
                                    public void run() {
                                        Toast.makeText(getApplicationContext(), jsonBean.msg, Toast.LENGTH_SHORT).show();
                                    }
                                });
                                Intent intent = new Intent();
                                intent.setClass(RegActivity.this, LoginActivity.class);
                                startActivity(intent);
                                finish();
                            } else {
                                runOnUiThread(new Runnable() {
                                    public void run() {
                                        Toast.makeText(getApplicationContext(), jsonBean.msg, Toast.LENGTH_SHORT).show();
                                    }
                                });
                            }
                        }
                    });
            }
        });
        else {
                Toast.makeText(getApplicationContext(), "请检查网络！", Toast.LENGTH_SHORT).show();
        }
    }
    public void back(View view) {
        Intent intent = new Intent();
        intent.setClass(RegActivity.this, LoginActivity.class);
        startActivity(intent);
    }
}

