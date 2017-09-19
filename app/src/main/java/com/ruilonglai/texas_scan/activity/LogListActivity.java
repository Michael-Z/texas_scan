package com.ruilonglai.texas_scan.activity;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.Toast;

import com.ruilonglai.texas_scan.R;
import com.ruilonglai.texas_scan.adapter.HandLogAdapter;
import com.ruilonglai.texas_scan.entity.OneHandLog;

import org.litepal.crud.DataSupport;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class LogListActivity extends AppCompatActivity {
    RecyclerView logList;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_log_list);
        logList = (RecyclerView) findViewById(R.id.logList);
        final List<OneHandLog> oneHandLogs = DataSupport.order("date desc").find(OneHandLog.class);
        LinearLayoutManager manager = new LinearLayoutManager(this);
        logList.setLayoutManager(manager);
        logList.setItemAnimator(new DefaultItemAnimator());
        final List<String> list = new ArrayList<>();
        for (int i = 0; i < oneHandLogs.size(); i++) {
            long date = oneHandLogs.get(i).getDate();
            Calendar instance = Calendar.getInstance();
            instance.setTime(new Date(date*1000));
            list.add(instance.get(Calendar.YEAR)+"/"+(instance.get(Calendar.MONTH)+1)+"/"
                    +instance.get(Calendar.DAY_OF_MONTH)+" "+instance.get(Calendar.HOUR_OF_DAY)+":"+instance.get(Calendar.MINUTE));
        }
        final HandLogAdapter adapter = new HandLogAdapter(this, list);
        adapter.setOnItemListener(new HandLogAdapter.OnItemClickListener() {
            @Override
            public void onClick(int position) {
                Intent intent = new Intent(LogListActivity.this,SerialActivity.class);
                intent.putExtra("log",oneHandLogs.get(position).getLog());
                startActivity(intent);
            }

            @Override
           public void onLongClick(final int position) {
                AlertDialog.Builder builder = new AlertDialog.Builder(LogListActivity.this);
                builder.setMessage("是否删除这条记录?");
                builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        DataSupport.deleteAll(OneHandLog.class,"date=?",oneHandLogs.get(position).getDate()+"");
                        oneHandLogs.remove(position);
                        list.remove(position);
                        adapter.notifyDataSetChanged();
                        Toast.makeText(LogListActivity.this,"删除成功",Toast.LENGTH_LONG).show();
                    }
                });
                builder.setNegativeButton("取消",null);
                builder.show();
            }
        });
        logList.setAdapter(adapter);
        findViewById(R.id.clear).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DataSupport.deleteAll(OneHandLog.class);
                oneHandLogs.clear();
                list.clear();
                adapter.notifyDataSetChanged();
            }
        });
    }
}
