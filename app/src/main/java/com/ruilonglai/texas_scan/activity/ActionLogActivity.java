package com.ruilonglai.texas_scan.activity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;

import com.ruilonglai.texas_scan.R;
import com.ruilonglai.texas_scan.adapter.HandLogAdapter;
import com.ruilonglai.texas_scan.entity.OneHandLog;
import com.ruilonglai.texas_scan.util.Constant;
import com.ruilonglai.texas_scan.util.TimeUtil;

import org.litepal.crud.DataSupport;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class ActionLogActivity extends AppCompatActivity {

    @BindView(R.id.back)
    TextView back;
    @BindView(R.id.title)
    TextView title;
    @BindView(R.id.right)
    TextView right;
    @BindView(R.id.logList)
    RecyclerView logList;

    Handler handler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            DataSupport.deleteAll(OneHandLog.class);
            finish();
        }
    };
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_action_log);
        ButterKnife.bind(this);
        title.setText("动作记录");
        back.setText("取消");
        back.setVisibility(View.VISIBLE);
        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        right.setText("清除");
        right.setVisibility(View.VISIBLE);
        right.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                handler.sendEmptyMessage(0);
            }
        });
        LinearLayoutManager manager = new LinearLayoutManager(this);
        logList.setLayoutManager(manager);
        logList.setItemAnimator(new DefaultItemAnimator());
        List<OneHandLog> all = DataSupport.findAll(OneHandLog.class);
        List<String> list = new ArrayList<>();
        final List<String> listContent = new ArrayList<>();
        for (int i = 0; i < all.size(); i++) {
            OneHandLog oneHandLog = all.get(i);
            list.add(TimeUtil.getCurrentDateToSecond(new Date(oneHandLog.getDate())));
            listContent.add(Constant.APPNAMES[oneHandLog.getPlattype()]+"#"+oneHandLog.getLog());
        }
        final HandLogAdapter adapter = new HandLogAdapter(ActionLogActivity.this,list);
        adapter.setOnItemListener(new HandLogAdapter.OnItemClickListener() {
            @Override
            public void onClick(int position) {
                Intent intent = new Intent(ActionLogActivity.this,ListActivity.class);
                intent.putExtra("list",listContent.get(position));
                startActivity(intent);
            }

            @Override
            public void onLongClick(int position) {

            }
        });
        logList.setAdapter(adapter);
    }
}
