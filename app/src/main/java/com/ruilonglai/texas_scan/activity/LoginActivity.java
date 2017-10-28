package com.ruilonglai.texas_scan.activity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.InputType;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.ruilonglai.texas_scan.MainActivity;
import com.ruilonglai.texas_scan.R;
import com.ruilonglai.texas_scan.entity.JsonBean;
import com.ruilonglai.texas_scan.entity.PokerUser;
import com.ruilonglai.texas_scan.util.HttpUtil;
import com.ruilonglai.texas_scan.util.TimeUtil;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.Date;

import okhttp3.Call;
import okhttp3.Response;


public class LoginActivity extends AppCompatActivity {
    private Button btregister, btfindpwd,btlogin,btjihuo;
    private CheckBox cbeyes,cbremember;
    private EditText userpwd;
    private AutoCompleteTextView userphone;
    private boolean rember; // 判断是否选择记住密码
    private String phone,password,szImei;
    public  final static String PREF_FILE = "preference";
    private boolean isMember;
    private  SharedPreferences preferences;
    private  ArrayAdapter<String> arrayAdapter;
    private  String historyphone;
    private  String [] arrays; //保存历史手机号
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        inti();
        getAccount();
        Login();
        Register();
        findpwd();
        checkpwd();
        jihuo();
    }
    /*  读取账号、判断是否记住密码 */
    public void getAccount(){
        rember = getSharedPreferences(PREF_FILE, MODE_PRIVATE).getBoolean("remember",false);
        userphone.setText(""+getSharedPreferences(PREF_FILE, MODE_PRIVATE).getString("name", ""));
        if (rember){
            userphone.setText(getSharedPreferences(PREF_FILE, MODE_PRIVATE).getString("name", ""));
            userpwd.setText(getSharedPreferences(PREF_FILE, MODE_PRIVATE).getString("password", ""));
            cbremember.setChecked(true);
        }else{
            userphone.setText(getSharedPreferences(PREF_FILE, MODE_PRIVATE).getString("name", ""));
            cbremember.setChecked(false);
        }
        //搜索历史手机号
        historyphone= getSharedPreferences(PREF_FILE, MODE_PRIVATE).getString("phone", "");
        arrays =historyphone.split(",");
        userphone.setThreshold(1);   // 从第一个字串开始搜索
        arrayAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1,arrays);
        userphone.setAdapter(arrayAdapter);
    }
    /*  初始化控件 */
    public void inti(){
        btlogin=(Button)findViewById(R.id.btlogin) ;
        userphone=(AutoCompleteTextView)findViewById(R.id.etusername);
        userpwd=(EditText)findViewById(R.id.etpassword);
        btregister = (Button) findViewById(R.id.btzhuce);
        btfindpwd = (Button) findViewById(R.id.btfindmima);
        cbeyes=(CheckBox)findViewById(R.id.Imgeyes);
        cbremember=(CheckBox)findViewById(R.id.rember);
        btjihuo=(Button)findViewById(R.id.btjihuo);
        // 获取手机IMEI
//        TelephonyManager TelephonyMgr = (TelephonyManager) getSystemService(TELEPHONY_SERVICE);
//        szImei = TelephonyMgr.getDeviceId();
    }
    /* 注册按钮 */
    public void Register() {
        btregister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.setClass(LoginActivity.this, RegActivity.class);
                startActivity(intent);
            }
        });
    }
    /* 找回密码监听 */
    public void findpwd() {
        btfindpwd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                Intent intent = new Intent();
//                intent.setClass(LoginActivity.this, FindpwdActivity.class);
//                startActivity(intent);
                Toast.makeText(LoginActivity.this, "暂不支持修改密码", Toast.LENGTH_SHORT).show();
            }
        });
    }
    /* 激活软件 */
    public void jihuo() {
        btjihuo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.setClass(LoginActivity.this, JihuoActivity.class);
                startActivity(intent);
            }
        });
    }
    /* 登录按钮监听*/
    public void Login(){

            btlogin.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (HttpUtil.networkConnected(LoginActivity.this)) {
                        phone = userphone.getText().toString().trim();
                        password = userpwd.getText().toString().trim();
                        if (phone.isEmpty()) {
                            Toast.makeText(LoginActivity.this, "手机号不能为空", Toast.LENGTH_SHORT).show();
                            return;
                        }
                        if (password.isEmpty()) {
                            Toast.makeText(LoginActivity.this, "密码不能为空", Toast.LENGTH_SHORT).show();
                            return;
                        }
                        PokerUser pu = new PokerUser();
                        pu.id = phone;
                        pu.passwd = password;
                        pu.curmachine = szImei;
                        pu.setSendtime(TimeUtil.getCurrentDateToSecond(new Date()));
                        Gson gson = new Gson();
                        String jsonstr = gson.toJson(pu);
                        HttpUtil.sendPostRequestData("login", jsonstr, new okhttp3.Callback() {
                            @Override
                            public void onFailure(Call call, IOException e) {
                                 Log.d("失败：", e.toString());
                                isMember = false;
                                runOnUiThread(new Runnable() {
                                    public void run() {
                                        Toast.makeText(getApplicationContext(), "登录失败！请联系管理员！", Toast.LENGTH_SHORT).show();
                                    }
                                });
                            }
                            @Override
                            public void onResponse(Call call, Response response) throws IOException {
                                String responseDate = response.body().string();
                                Log.d("返回的数据：", responseDate);
                                Gson gson = new Gson();
                                Type type = new TypeToken<JsonBean>() {}.getType();
                                final JsonBean jsonBean = gson.fromJson(responseDate, type);
                                String resp = jsonBean.result;
                                if (resp.equals("true")) {
                                    runOnUiThread(new Runnable() {
                                        public void run() {
                                            Toast.makeText(getApplicationContext(), "登录成功!", Toast.LENGTH_SHORT).show();
                                            if (cbremember.isChecked()) { //判断是否有选择记住密码
                                                SharedPreferences preferences = getSharedPreferences(PREF_FILE, MODE_PRIVATE);
                                                preferences.edit().putBoolean("login", true)
                                                        .putString("name", phone)
                                                        .putString("password", password)
                                                        .putBoolean("remember", true).apply();
                                                setResult(RESULT_OK);
                                                finish();
                                            } else {
                                                SharedPreferences preferences = getSharedPreferences(PREF_FILE, MODE_PRIVATE);
                                                preferences.edit().putBoolean("login", true)
                                                        .putString("name", phone)
                                                        .putBoolean("remember", false).apply();
                                                setResult(RESULT_OK);
                                                finish();
                                            }
                                            savehistory(phone);
                                            Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                                            Bundle bundle = new Bundle();
                                            bundle.putStringArray("user",new String[]{phone,password});
                                            intent.putExtra("user",bundle);
                                            startActivity(intent);
                                        }
                                    });
                                } else {
                                    runOnUiThread(new Runnable() {
                                        public void run() {
                                            Toast.makeText(getApplicationContext(), "登录失败，请重新登录"+jsonBean.msg, Toast.LENGTH_SHORT).show();
                                        }
                                    });
                                }
                            }
                        });
                    }
                    else{
                        Toast.makeText(getApplicationContext(), "网络未连接,请检查网络！", Toast.LENGTH_SHORT).show();
                    }
                }
            });
    }
    /*查看输入框 隐藏的密码*/
    public  void  checkpwd( ){
        cbeyes.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(isChecked){
         //选择状态 显示明文--设置为可见的密码
                    userpwd.setInputType(InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
                    cbeyes.setBackgroundDrawable(getResources().getDrawable(R.drawable.eyeflase));
                }else {
        //默认状态显示密码--设置文本 要一起写才能起作用 InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD
                    userpwd.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
                    cbeyes.setBackgroundDrawable(getResources().getDrawable(R.drawable.eyestrue));
                }
            }
        });
    }
    /*  保存历史输入的手机 */
    public  void savehistory(String phone){
        int num=0;
        historyphone= getSharedPreferences(PREF_FILE, MODE_PRIVATE).getString("phone", "");
        arrays =historyphone.split(",");
        for (int i=0;i<arrays.length;i++){
            if (phone.equals(arrays[i])){
                num=1;
                break;
            }
            else{}
        }
        if (num!=1){
            String  newphone= phone+","+historyphone;
            preferences = getSharedPreferences(PREF_FILE, MODE_PRIVATE);
            preferences.edit().putBoolean("user", true)
                    .putString("phone",newphone).apply();
            setResult(RESULT_OK);
            finish();
        }
    }
}