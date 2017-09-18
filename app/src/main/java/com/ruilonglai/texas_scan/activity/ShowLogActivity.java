package com.ruilonglai.texas_scan.activity;

import android.app.ListActivity;
import android.content.Intent;
import android.os.Bundle;
import android.widget.ArrayAdapter;

public class ShowLogActivity extends ListActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Intent intent = getIntent();
        if(intent!=null){
            String log = intent.getStringExtra("log");
            String[] strs = log.split("#");
            int handIdx = -1;
            int turnIdx = -1;
            for (int i = 0; i < strs.length; i++) {
                if(strs[i].contains("手牌")){
                    handIdx = i;
                }
                if(strs[i].contains("翻牌前")){
                    turnIdx = i;
                }
            }
            if(handIdx!=-1 && turnIdx !=-1 && handIdx>turnIdx){
                String str = strs[handIdx];
                for (int i = handIdx; i > turnIdx; i--) {
                   strs[i] = strs[i-1];
                }
                strs[turnIdx]=str;
            }
            ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,android.R.layout.simple_expandable_list_item_1,strs);
            setListAdapter(adapter);
        }
    }
}
