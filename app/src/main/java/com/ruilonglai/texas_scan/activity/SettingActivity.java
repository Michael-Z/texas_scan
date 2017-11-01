package com.ruilonglai.texas_scan.activity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.SparseArray;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.ruilonglai.texas_scan.R;
import com.ruilonglai.texas_scan.adapter.MesShowAdapter;
import com.ruilonglai.texas_scan.entity.PercentType;
import com.ruilonglai.texas_scan.entity.ShowMes;
import com.ruilonglai.texas_scan.util.Constant;
import com.ruilonglai.texas_scan.util.GsonUtil;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class SettingActivity extends AppCompatActivity implements OnClickListener {


    @BindView(R.id.back)
    TextView back;
    @BindView(R.id.title)
    TextView title;
    @BindView(R.id.right)
    TextView sure;
    @BindView(R.id.selects)
    RecyclerView selects;
    @BindView(R.id.selectNum)
    TextView selectNum;
    @BindView(R.id.sureSelect)
    Button sureSelect;
    private SparseArray<ShowMes> list;

    private int percentCount;

    private List<Integer> keys;

    private Handler handler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            percentCount = 0;
            for (int i = 0; i < list.size(); i++) {
                int key = list.keyAt(i);
                ShowMes sm = list.get(key);
                if (sm.getIsSelect() == 1) {
                    percentCount++;
                    keys.add(key);
                }
            }
            selectNum.setText("已选择"+percentCount+"项");
        }
    };
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting);
        ButterKnife.bind(this);
        title.setText("显示设置");
        back.setVisibility(View.VISIBLE);
        sure.setVisibility(View.VISIBLE);
        back.setBackgroundColor(getResources().getColor(R.color.title_bg_color));
        back.setText("取消");
        back.setTextColor(getResources().getColor(R.color.white));
        sureSelect.setOnClickListener(this);
        back.setOnClickListener(this);
        boolean hideWP = getSharedPreferences(LoginActivity.PREF_FILE, MODE_PRIVATE).getBoolean("hidewinpercent", false);
        LinearLayoutManager manager = new LinearLayoutManager(this);
        selects.setLayoutManager(manager);
        selects.setItemAnimator(new DefaultItemAnimator());
        list = new SparseArray<>();
        keys = new ArrayList<>();
        String json = getSharedPreferences(LoginActivity.PREF_FILE, MODE_PRIVATE).getString("percentTypeArray", "");
        for (int i = 0; i < Constant.percentTypes.length; i++) {
            ShowMes msg = new ShowMes();
            msg.setPercentType(Constant.percentTypes[i]);
            msg.setPercentContent(Constant.percentContents[i]);
            list.put(i, msg);
        }
        if (!TextUtils.isEmpty(json)) {
            List<Integer> idxs = GsonUtil.parseJsonWithGson(json, PercentType.class).getPercents();
            for (int i = 0; i < idxs.size(); i++) {
                Integer integer = idxs.get(i);
                list.get(integer.intValue()).setIsSelect(1);
            }
        }
        final MesShowAdapter adapter = new MesShowAdapter(this, list);
        adapter.setOnItemListener(new MesShowAdapter.OnItemListener() {
            @Override
            public void onClick(int position, boolean isSelect) {
                percentCount = 0;
                for (int i = 0; i < list.size(); i++) {
                    int key = list.keyAt(i);
                    ShowMes msg = list.get(key);
                    if (msg.getIsSelect() == 1) {
                        percentCount++;
                        keys.add(key);
                    }
                }
                if (isSelect) {
                    if (percentCount < 6) {
                        ShowMes showMes = list.valueAt(position);
                        showMes.setIsSelect(1);
                        adapter.notifyDataSetChanged();
                    } else {
                        Toast.makeText(SettingActivity.this, "选项不能超过6项,您可取消一些选项,再重新选择此项！", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    ShowMes showMes = list.valueAt(position);
                    showMes.setIsSelect(0);
                    adapter.notifyDataSetChanged();
                }
                handler.sendEmptyMessage(0);
            }
        });
        selects.setAdapter(adapter);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.sureSelect:
                Intent intent = new Intent();
                Gson gson = new Gson();
                keys.clear();
                for (int i = 0; i < list.size(); i++) {
                    int key = list.keyAt(i);
                    ShowMes msg = list.get(key);
                    if (msg.getIsSelect() == 1) {
                        keys.add(key);
                    }
                }
                if (keys.size() == 6) {
                    PercentType percent = new PercentType();
                    percent.setPercents(keys);
                    String json = gson.toJson(percent);
                    intent.putExtra("result", json);
                    saveKeysToPreference(json);
                    setResult(1, intent);
                    Toast.makeText(SettingActivity.this, "设置成功!", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(SettingActivity.this, "选项少于6项，设置失败", Toast.LENGTH_SHORT).show();
                }
                break;
            case R.id.back:
                finish();
                break;
        }
    }

    public void saveKeysToPreference(String json) {
        SharedPreferences preferences = getSharedPreferences(LoginActivity.PREF_FILE, MODE_PRIVATE);
        preferences.edit().putString("percentTypeArray", json).apply();
    }
}
