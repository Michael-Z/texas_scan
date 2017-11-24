package com.ruilonglai.texas_scan.activity;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.ruilonglai.texas_scan.R;

import butterknife.BindView;
import butterknife.ButterKnife;

public class UseActivity extends AppCompatActivity implements View.OnClickListener{

    @BindView(R.id.back)
    TextView back;
    @BindView(R.id.title)
    TextView title;
    @BindView(R.id.right)
    TextView right;
    @BindView(R.id.userIntrduce)
    ImageView userIntrduce;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_use);
        ButterKnife.bind(this);
        back.setVisibility(View.VISIBLE);
        back.setText("返回");
        right.setVisibility(View.VISIBLE);
        back.setOnClickListener(this);
        title.setText("使用说明");
        SharedPreferences spf = getSharedPreferences(LoginActivity.PREF_FILE, Context.MODE_PRIVATE);
        Bitmap bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.use);
        int width = bitmap.getWidth();
        int newWidth = spf.getInt("width", 0);
        int newheight = bitmap.getHeight()* newWidth / width;
        userIntrduce.setImageBitmap(Bitmap.createScaledBitmap(bitmap,newWidth,newheight,false));
    }

    @Override
    public void onClick(View v) {
         finish();
    }
}
