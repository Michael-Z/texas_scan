package com.ruilonglai.texas_scan.activity;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.ruilonglai.texas_scan.R;

import butterknife.BindView;
import butterknife.ButterKnife;


public class FindpwdActivity extends AppCompatActivity {
    @BindView(R.id.back)
    TextView back;
    @BindView(R.id.title)
    TextView title;
    @BindView(R.id.right)
    TextView right;
    @BindView(R.id.phone)
    EditText phone;
    @BindView(R.id.code)
    EditText ecode;
    @BindView(R.id.btgetcode)
    Button btgetcode;
    @BindView(R.id.etnewpwd)
    EditText etnewpwd;
    @BindView(R.id.btfind)
    Button btfind;
    String newpwd, number, code;
    public static final String mob_APPKEY = "154edf2c734e2";
    public static final String mob_APPSECRET = "9dbe04e99b9c24f73f4a2c631031f852";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_findpwd);
        ButterKnife.bind(this);
        Findpwd();
    }
    /* 确认修改按钮 */
    public void Findpwd() {
        btfind.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                code = ecode.getText().toString();
                number = phone.getText().toString().trim();
                newpwd = etnewpwd.getText().toString().trim();
                if (number.isEmpty()) {
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
}
