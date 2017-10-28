package com.ruilonglai.texas_scan.activity;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.ruilonglai.texas_scan.R;

import butterknife.BindView;
import butterknife.ButterKnife;

public class UseActivity extends AppCompatActivity implements View.OnClickListener{

    @BindView(R.id.back)
    Button back;
    @BindView(R.id.title)
    TextView title;
    @BindView(R.id.right)
    Button right;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_use);
        ButterKnife.bind(this);
        back.setVisibility(View.VISIBLE);
        right.setVisibility(View.VISIBLE);
        back.setOnClickListener(this);
        title.setText("使用说明");
    }

    @Override
    public void onClick(View v) {
         finish();
    }
}
