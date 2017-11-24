package com.ruilonglai.texas_scan.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.View;
import android.widget.TextView;

import com.ruilonglai.texas_scan.R;
import com.ruilonglai.texas_scan.adapter.HandLogAdapter;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class ListActivity extends AppCompatActivity {

    @BindView(R.id.back)
    TextView back;
    @BindView(R.id.title)
    TextView title;
    @BindView(R.id.right)
    TextView right;
    @BindView(R.id.logList)
    RecyclerView logList;
    @BindView(R.id.userList)
    RecyclerView userList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_action_log);
        ButterKnife.bind(this);
        title.setText("一手动作详情");
        back.setText("取消");
        back.setVisibility(View.VISIBLE);
        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        userList.setVisibility(View.VISIBLE);
        Intent intent = getIntent();
        String content = intent.getStringExtra("list");
        String[] split = content.split("#");
        LinearLayoutManager manager = new LinearLayoutManager(this);
        logList.setLayoutManager(manager);
        logList.setItemAnimator(new DefaultItemAnimator());
        LinearLayoutManager manager1 = new LinearLayoutManager(this);
        userList.setLayoutManager(manager1);
        userList.setItemAnimator(new DefaultItemAnimator());
        boolean isUserContent = false;
        List<String> list = new ArrayList<>();
        List<String> users = new ArrayList<>();
        for (int i = 0; i < split.length; i++) {
            String str = split[i];
            if(TextUtils.isEmpty(str)){
                isUserContent = true;
                continue;
            }
            if(!isUserContent)
                list.add(str);
            else
                users.add(str);
        }
        HandLogAdapter adapter = new HandLogAdapter(ListActivity.this,list);
        logList.setAdapter(adapter);
        HandLogAdapter adapter1 = new HandLogAdapter(ListActivity.this,users);
        userList.setAdapter(adapter1);
    }
}
