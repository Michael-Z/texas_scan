package com.ruilonglai.texas_scan.activity;

import android.app.ListActivity;
import android.content.Intent;
import android.os.Bundle;
import android.widget.ArrayAdapter;

import com.ruilonglai.texas_scan.entity.SerialInfo;
import com.ruilonglai.texas_scan.util.GsonUtil;

public class SerialActivity extends ListActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Intent intent = getIntent();
        if(intent!=null){
            String log = intent.getStringExtra("log");
            String[] strs = log.split("#");
            ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,android.R.layout.simple_expandable_list_item_1,strs);
            setListAdapter(adapter);
        }
    }
}
