package com.ruilonglai.texas_scan.activity;

import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.ruilonglai.texas_scan.R;
import com.ruilonglai.texas_scan.entity.JsonBean;
import com.ruilonglai.texas_scan.entity.PokerUser;
import com.ruilonglai.texas_scan.util.HttpUtil;

import java.io.IOException;
import java.lang.reflect.Type;

import butterknife.BindView;
import butterknife.ButterKnife;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

public class JihuoActivity extends AppCompatActivity {
    @BindView(R.id.back)
    TextView back;
    @BindView(R.id.title)
    TextView title;
    @BindView(R.id.right)
    TextView right;
    @BindView(R.id.text)
    TextView text;
    @BindView(R.id.phone)
    EditText ephone;
    @BindView(R.id.etname)
    TextView etname;
    @BindView(R.id.code)
    EditText ecode;
    @BindView(R.id.btregjihuo)
    Button btregjihuo;
    private String phone, code;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_jihuo);
        ButterKnife.bind(this);
        back.setVisibility(View.VISIBLE);
        back.setText("取消");
        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        back.setTextColor(Color.WHITE);
        title.setText("软件激活");
    }

    public void jihuo(View view) {
        phone = ephone.getText().toString().trim();
        code = ecode.getText().toString().trim();
        if (phone.length() < 11) {
            Toast.makeText(getApplicationContext(), "手机号错误，请重新输入", Toast.LENGTH_SHORT).show();
            return;
        }
        if (code.length() < 16) {
            Toast.makeText(getApplicationContext(), "激活码是16位，请重新输入", Toast.LENGTH_SHORT).show();
            return;
        }
        PokerUser pu = new PokerUser();
        pu.id = phone;
        pu.serialno = code;
        Gson gson = new Gson();
        String jsonstr = gson.toJson(pu);
        HttpUtil.sendPostRequestData("serialno", jsonstr, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                //  Log.d("失败：", e);
                runOnUiThread(new Runnable() {
                    public void run() {
                        Toast.makeText(getApplicationContext(), "激活失败！请联系管理员！", Toast.LENGTH_SHORT).show();
                    }
                });
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String responseDate = response.body().string();
                Log.d("返回的数据：", responseDate);
                Gson gson = new Gson();
                Type type = new TypeToken<JsonBean>() {
                }.getType();
                final JsonBean jsonBean = gson.fromJson(responseDate, type);
                //  String resp = jsonBean.result;
                runOnUiThread(new Runnable() {
                    public void run() {
                        Toast.makeText(getApplicationContext(), jsonBean.result, Toast.LENGTH_SHORT).show();
                        if(jsonBean.result.equals("true")){
                            finish();
                        }
                    }
                });
            }
        });
    }
}