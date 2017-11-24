package com.ruilonglai.texas_scan.fragment;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.ruilonglai.texas_scan.MainActivity;
import com.ruilonglai.texas_scan.R;
import com.ruilonglai.texas_scan.activity.ActionLogActivity;
import com.ruilonglai.texas_scan.activity.LoginActivity;
import com.ruilonglai.texas_scan.activity.SettingActivity;
import com.ruilonglai.texas_scan.activity.UseActivity;
import com.ruilonglai.texas_scan.entity.PlayerData;
import com.ruilonglai.texas_scan.entity.PlayerData1;
import com.ruilonglai.texas_scan.entity.PlayerData2;
import com.ruilonglai.texas_scan.entity.PlayerData3;
import com.ruilonglai.texas_scan.entity.PokerUser;
import com.ruilonglai.texas_scan.entity.ReqData;
import com.ruilonglai.texas_scan.entity.ReqDelUser;
import com.ruilonglai.texas_scan.entity.UserName;
import com.ruilonglai.texas_scan.util.ActionsTool;
import com.ruilonglai.texas_scan.util.Constant;
import com.ruilonglai.texas_scan.util.HttpUtil;
import com.ruilonglai.texas_scan.util.MyLog;
import com.ruilonglai.texas_scan.util.TimeUtil;

import org.json.JSONException;
import org.json.JSONObject;
import org.litepal.crud.DataSupport;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

import static org.litepal.crud.DataSupport.where;

/**
 * Created by Administrator on 2017/10/26.
 */

public class SettingFragment extends Fragment implements View.OnClickListener{

    private final static String TAG = "SettingFragment";
    ViewHolder vh;
    MainActivity context;
    int platType = 0;
    int flag = -1;

    Handler handler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            if(msg.arg1==0){
                platType =  msg.arg2;
            }else if(msg.arg1==1){
                List<String> names= new ArrayList<>();
                if(msg.arg2==0){
                    List<UserName> unames = DataSupport.where("plattype=?",platType+"").find(UserName.class);
                    for (int i = 0; i < unames.size(); i++) {
                        if(!unames.get(i).name.contains(context.phone)){
                            names.add(unames.get(i).name);
                        }
                    }
                    flag = Constant.DELETE_PLAYER;
                }else if(msg.arg2==1){
                    List<UserName> unames = DataSupport.where("plattype=?",platType+"").find(UserName.class);
                    for (int i = 0; i < unames.size(); i++) {
                        if(unames.get(i).name.contains(context.phone)){
                            names.add(unames.get(i).name);
                        }
                    }
                    flag = Constant.DELETE_SELF;
                }
                deleteNameData(names);
            }else if(msg.arg1 == 2){
                deleteCurrentData();
            }else if(msg.arg1 == 3){//出现异常
                String str = (String)msg.obj;
                Toast.makeText(context,str,Toast.LENGTH_SHORT).show();
            }
        }
    };
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
                final EditText text = new EditText(context);
                text.setHint("输入密码");
                AlertDialog.Builder dialog = new AlertDialog.Builder(context);
                dialog.setTitle("开发专用,猜对算我输");
                dialog.setView(text);
                dialog.setPositiveButton("进入", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String pwd = text.getText().toString();
                        if(pwd.equals("tianxia678")){
                            startActivity(new Intent(context, ActionLogActivity.class));
                        }
                        dialog.cancel();
                    }
                }).show();
//                Toast.makeText(context,"暂未开放此功能",Toast.LENGTH_SHORT).show();
                break;
            case 1:
                platType = 0;
                AlertDialog.Builder dialog1 = new AlertDialog.Builder(context);
                dialog1.setTitle("选择要删除的平台")
                    .setSingleChoiceItems(Constant.APPNAMES, 0, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            Message msg = new Message();
                            msg.arg1 = 0;
                            msg.arg2 = which;
                            handler.sendMessage(msg);
                        }
                    })
                    .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            Message msg = new Message();
                            msg.arg1 = 1;
                            msg.arg2 = 0;
                            handler.sendMessage(msg);
                            dialog.cancel();
                        }
                    })
                    .setNegativeButton("取消", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                }).show();
                break;
            case 2:
                platType = 0;
                AlertDialog.Builder dialog2 = new AlertDialog.Builder(context);
                dialog2.setTitle("选择要删除的平台")
                        .setSingleChoiceItems(Constant.APPNAMES, 0, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                Message msg = new Message();
                                msg.arg1 = 0;
                                msg.arg2 = which;
                                handler.sendMessage(msg);
                            }
                        })
                        .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                Message msg = new Message();
                                msg.arg1 = 1;
                                msg.arg2 = 1;
                                handler.sendMessage(msg);
                                dialog.cancel();
                            }
                        })
                        .setNegativeButton("取消", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.cancel();
                            }
                        }).show();
                break;
            case 3:
//                boolean save = AssetsCopyUtil.copyDataBaseToSD(context);
//                if(save)
//                Toast.makeText(context,"暂未开放此功能",Toast.LENGTH_SHORT).show();
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
                        MyLog.e(TAG,e.toString());
                    }

                    @Override
                    public void onResponse(Call call, Response response) throws IOException {
                        MyLog.e(TAG,response.toString());
                        Intent in = new Intent(context,LoginActivity.class);
                        startActivity(in);
                        context.finish();
                    }
                });
                break;
        }
    } /*删除某平台下的数据*/
    public void deleteNameData( List<String> names){
        ReqData data = new ReqData();
        ReqDelUser rdu = new ReqDelUser();
        rdu.setPlattype(platType);
        rdu.setUsernames(names);
        data.setReqno(TimeUtil.getCurrentDateToMinutes(new Date()) + ActionsTool.disposeNumber());
        data.setReqid(context.getSharedPreferences(LoginActivity.PREF_FILE, Context.MODE_PRIVATE).getString("name", ""));
        data.setParam(new Gson().toJson(rdu));
        HttpUtil.sendPostRequestData("reqdeluser", new Gson().toJson(data), new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
//                Toast.makeText(context,"服务器未响应",Toast.LENGTH_SHORT).show();
                Message msg = new Message();
                msg.arg1 = 3;
                msg.obj = "服务器请求失败";
                handler.sendMessage(msg);
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String string = response.body().string();
                MyLog.e(getClass().getName()+"response(reqdeluser)", string);
                boolean ifOk = false;
                String errMsg = "";
                try {
                    JSONObject object = new JSONObject(string);
                    ifOk = object.getBoolean("result");
                    errMsg = object.getString("msg");
                    Message msg = new Message();
                    msg.arg1 = 3;
                    msg.obj = errMsg;
                    handler.sendMessage(msg);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                Message msg = new Message();
                msg.arg1 = 2;
                handler.sendMessage(msg);
            }
        });
    }
    public void deleteCurrentData(){
     if(flag==Constant.DELETE_PLAYER){
         switch (platType){
             case 0:
                 DataSupport.deleteAll(PlayerData.class,"not name=?","self");
                 break;
             case 1:
                 DataSupport.deleteAll(PlayerData1.class,"not name=?","self");
                 break;
             case 2:
                 DataSupport.deleteAll(PlayerData2.class,"not name=?","self");
                 break;
             case 3:
                 DataSupport.deleteAll(PlayerData3.class,"not name=?","self");
                 break;
         }
     }else if(flag==Constant.DELETE_SELF){
         switch (platType){
             case 0:
                 DataSupport.deleteAll(PlayerData.class,"name=?","self");
                 for (int i = 0; i < 9; i++) {
                     PlayerData player = new PlayerData();
                     player.setSeatFlag(Constant.seatFlags[i]);
                     player.setName("self");
                     List<PlayerData> self = where("name=? and seatFlag=?", "self", Constant.seatFlags[i]).find(PlayerData.class);
                     if (self.size() == 0) {//不存在则创建一个
                         player.setDate(TimeUtil.getCurrentDateToDay(new Date()));
                         player.save();
                     }
                 }
                 break;
             case 1:
                 DataSupport.deleteAll(PlayerData1.class,"name=?","self");
                 for (int i = 0; i < 9; i++) {
                     PlayerData1 player = new PlayerData1();
                     player.setSeatFlag(Constant.seatFlags[i]);
                     player.setName("self");
                     List<PlayerData1> self = where("name=? and seatFlag=?", "self", Constant.seatFlags[i]).find(PlayerData1.class);
                     if (self.size() == 0) {//不存在则创建一个
                         player.setDate(TimeUtil.getCurrentDateToDay(new Date()));
                         player.save();
                     }
                 }
                 break;
             case 2:
                 DataSupport.deleteAll(PlayerData2.class,"name=?","self");
                 for (int i = 0; i < 9; i++) {
                     PlayerData2 player = new PlayerData2();
                     player.setSeatFlag(Constant.seatFlags[i]);
                     player.setName("self");
                     List<PlayerData2> self = where("name=? and seatFlag=?", "self", Constant.seatFlags[i]).find(PlayerData2.class);
                     if (self.size() == 0) {//不存在则创建一个
                         player.setDate(TimeUtil.getCurrentDateToDay(new Date()));
                         player.save();
                     }
                 }
                 break;
             case 3:
                 DataSupport.deleteAll(PlayerData3.class,"name=?","self");
                 for (int i = 0; i < 9; i++) {
                     PlayerData3 player = new PlayerData3();
                     player.setSeatFlag(Constant.seatFlags[i]);
                     player.setName("self");
                     List<PlayerData3> self = where("name=? and seatFlag=?", "self", Constant.seatFlags[i]).find(PlayerData3.class);
                     if (self.size() == 0) {//不存在则创建一个
                         player.setDate(TimeUtil.getCurrentDateToDay(new Date()));
                         player.save();
                     }
                 }
                 break;
         }
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
