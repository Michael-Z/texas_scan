package com.ruilonglai.texas_scan.fragment;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.ruilonglai.texas_scan.MainActivity;
import com.ruilonglai.texas_scan.R;
import com.ruilonglai.texas_scan.activity.LoginActivity;
import com.ruilonglai.texas_scan.activity.SettingActivity;
import com.ruilonglai.texas_scan.activity.UseActivity;
import com.ruilonglai.texas_scan.entity.MyData;
import com.ruilonglai.texas_scan.entity.OneHand;
import com.ruilonglai.texas_scan.entity.OneHandLog;
import com.ruilonglai.texas_scan.entity.PlayerData;
import com.ruilonglai.texas_scan.entity.PokerUser;
import com.ruilonglai.texas_scan.util.Constant;
import com.ruilonglai.texas_scan.util.HttpUtil;
import com.ruilonglai.texas_scan.util.TimeUtil;

import org.litepal.crud.DataSupport;

import java.io.IOException;
import java.util.Date;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import okhttp3.Call;
import okhttp3.Response;

/**
 * Created by Administrator on 2017/10/26.
 */

public class SettingFragment extends Fragment implements View.OnClickListener{

    private final static String TAG = "SettingFragment";
    ViewHolder vh;
    MainActivity context;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        this.context = (MainActivity) context;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_setting, container, false);
        vh = new ViewHolder(view);
        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        initView();
    }
    public void initView(){
        vh.morePageRow0.setOnClickListener(this);
        vh.morePageRow1.setOnClickListener(this);
        vh.morePageRow2.setOnClickListener(this);
        vh.morePageRow3.setOnClickListener(this);
        vh.morePageRow4.setOnClickListener(this);
        vh.morePageRow5.setOnClickListener(this);
        vh.morePageRow6.setOnClickListener(this);
        vh.morePageRow7.setOnClickListener(this);
    }
    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.more_page_row0:
                setttingAction(0);
                break;
            case R.id.more_page_row1:
                setttingAction(1);
                    break;
            case R.id.more_page_row2:
                setttingAction(2);
                    break;
            case R.id.more_page_row3:
                setttingAction(3);
                    break;
            case R.id.more_page_row4:
                setttingAction(4);
                    break;
            case R.id.more_page_row5:
                setttingAction(5);
                    break;
            case R.id.more_page_row6:
                setttingAction(6);
                    break;
            case R.id.more_page_row7:
                setttingAction(7);
                    break;
        }
    }
    public void setttingAction(int position){
        switch (position){
            case 0:
                Toast.makeText(context,"暂未开放此功能",Toast.LENGTH_SHORT).show();
                break;
            case 1:
                DataSupport.deleteAll(PlayerData.class, "name=?","self");
                DataSupport.deleteAll(MyData.class);
                for (int i = 0; i < 9; i++) {
                    PlayerData player = new PlayerData();
                    player.setSeatFlag(Constant.seatFlags[i]);
                    player.setName("self");
                    List<PlayerData> self = DataSupport.where("name=? and seatFlag=?", "self", Constant.seatFlags[i]).find(PlayerData.class);
                    if (self.size() == 0) {//不存在则创建一个
                        player.setDate(TimeUtil.getCurrentDateToDay(new Date()));
                        player.save();
                    }
                }
                Toast.makeText(context,"暂未开放此功能",Toast.LENGTH_SHORT).show();
                break;
            case 2:
                DataSupport.deleteAll(PlayerData.class);
                DataSupport.deleteAll(MyData.class);
                DataSupport.deleteAll(OneHand.class);
                DataSupport.deleteAll(OneHandLog.class);
                Toast.makeText(context,"暂未开放此功能",Toast.LENGTH_SHORT).show();
                break;
            case 3:
//                boolean save = AssetsCopyUtil.copyDataBaseToSD(context);
//                if(save)
                Toast.makeText(context,"暂未开放此功能",Toast.LENGTH_SHORT).show();
                break;
            case 4:
                Toast.makeText(context,"暂未开放此功能",Toast.LENGTH_SHORT).show();
                break;
            case 5:
                Intent intent = new Intent(context, SettingActivity.class);
                startActivityForResult(intent,0);
                break;
            case 6:
                Intent intent1 = new Intent(context, UseActivity.class);
                startActivity(intent1);
                break;
            case 7:
                PokerUser pu = new PokerUser();
                pu.id = context.phone;
                pu.nick = "";
                pu.passwd = context.password;
                pu.license = 10;
                Gson gson = new Gson();
                String jsonstr = gson.toJson(pu);
                HttpUtil.sendPostRequestData("logout",jsonstr,new okhttp3.Callback(){

                    @Override
                    public void onFailure(Call call, IOException e) {
                        Log.e(TAG,e.toString());
                    }

                    @Override
                    public void onResponse(Call call, Response response) throws IOException {
                        Log.e(TAG,response.toString());
                        Intent in = new Intent(context,LoginActivity.class);
                        startActivity(in);
                        context.finish();
                    }
                });
                break;
        }
    }
    static class ViewHolder {
        @BindView(R.id.titleText)
        TextView titleText;
        @BindView(R.id.top_relative)
        RelativeLayout topRelative;
        @BindView(R.id.more_page_row0)
        TableRow morePageRow0;
        @BindView(R.id.MorePageTableLayout_Favorite)
        TableLayout MorePageTableLayoutFavorite;
        @BindView(R.id.more_page_row1)
        TableRow morePageRow1;
        @BindView(R.id.more_page_row2)
        TableRow morePageRow2;
        @BindView(R.id.textView4)
        TextView textView4;
        @BindView(R.id.more_page_row3)
        TableRow morePageRow3;
        @BindView(R.id.MorePageTableLayout_Follow)
        TableLayout MorePageTableLayoutFollow;
        @BindView(R.id.more_page_row4)
        TableRow morePageRow4;
        @BindView(R.id.more_page_row5)
        TableRow morePageRow5;
        @BindView(R.id.more_page_row6)
        TableRow morePageRow6;
        @BindView(R.id.more_page_row7)
        TableRow morePageRow7;
        @BindView(R.id.MorePageTableLayout_Client)
        TableLayout MorePageTableLayoutClient;
        @BindView(R.id.empty_cart_view)
        RelativeLayout emptyCartView;

        ViewHolder(View view) {
            ButterKnife.bind(this, view);
        }
    }
}
