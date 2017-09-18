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

import com.ruilonglai.texas_scan.R;

import java.util.HashMap;


public class FindpwdActivity extends AppCompatActivity {
    private EditText etphonenum,etnewpwd,etcode;
    private Button btFind,btgetcode;
    String newpwd,number,code;
    public static final String mob_APPKEY = "154edf2c734e2";
    public static final String mob_APPSECRET = "9dbe04e99b9c24f73f4a2c631031f852";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_findpwd);
        inti();
        Findpwd();
    }
    public void inti(){
        etphonenum=(EditText)findViewById(R.id.phone);
        etnewpwd=(EditText)findViewById(R.id.etnewpwd);
        etcode=(EditText)findViewById(R.id.code) ;
        btgetcode=(Button)findViewById(R.id.btgetcode);
        btFind=(Button)findViewById(R.id.btfind);
    }
    /* 确认修改按钮 */
    public void Findpwd() {
        btFind.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                code = etcode.getText().toString();
                number = etphonenum.getText().toString().trim();
                newpwd = etnewpwd.getText().toString().trim();
                if (number.isEmpty()){
                    Toast.makeText(FindpwdActivity.this, "手机号不能为空", Toast.LENGTH_SHORT).show();
                    return;
                }
                if (newpwd.isEmpty()) {
                    Toast.makeText(FindpwdActivity.this, "新密码不能为空", Toast.LENGTH_SHORT).show();
                    return;
                }
            }
        });
    }
    public void back(View view){
        Intent intent = new Intent();
        intent.setClass(FindpwdActivity.this, LoginActivity.class);
        startActivity(intent);
    }
}
