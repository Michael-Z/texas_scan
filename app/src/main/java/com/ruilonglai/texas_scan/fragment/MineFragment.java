package com.ruilonglai.texas_scan.fragment;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.ruilonglai.texas_scan.MainActivity;
import com.ruilonglai.texas_scan.R;
import com.ruilonglai.texas_scan.activity.LoginActivity;
import com.ruilonglai.texas_scan.adapter.AppAdapter;
import com.ruilonglai.texas_scan.adapter.MyAdapter;
import com.ruilonglai.texas_scan.entity.PlayerData;
import com.ruilonglai.texas_scan.entity.PlayerData1;
import com.ruilonglai.texas_scan.entity.PlayerData2;
import com.ruilonglai.texas_scan.entity.PlayerData3;
import com.ruilonglai.texas_scan.entity.QuerySelf;
import com.ruilonglai.texas_scan.entity.ReqData;
import com.ruilonglai.texas_scan.entity.Result;
import com.ruilonglai.texas_scan.util.ActionsTool;
import com.ruilonglai.texas_scan.util.Constant;
import com.ruilonglai.texas_scan.util.GsonUtil;
import com.ruilonglai.texas_scan.util.HttpUtil;
import com.ruilonglai.texas_scan.util.MyLog;
import com.ruilonglai.texas_scan.util.TimeUtil;

import org.litepal.crud.DataSupport;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

/**
 * 　　　　　　　　┏┓　　　┏┓
 * 　　　　　　　┏┛┻━━━┛┻┓
 * 　　　　　　　┃　　　　　　　┃
 * 　　　　　　　┃　　　━　　　┃
 * 　　　　　　 ████━████     ┃
 * 　　　　　　　┃　　　　　　　┃
 * 　　　　　　　┃　　　┻　　　┃
 * 　　　　　　　┃　　　　　　　┃
 * 　　　　　　　┗━┓　　　┏━┛
 * 　　　　　　　　　┃　　　┃
 * 　　　　　　　　　┃　　　┃
 * 　　　　　　　　　┃　　　┃
 * 　　　　　　　　　┃　　　┃
 * 　　　　　　　　　┃　　　┃
 * 　　　　　　　　　┃　　　┃
 * 　　　　　　　　　┃　 　 ┗━━━┓
 * 　　　　　　　　　┃ 神兽保佑　　 ┣┓
 * 　　　　　　　　　┃ 代码无BUG   ┏┛
 * 　　　　　　　　　┗┓┓┏━┳┓┏┛
 * 　　　　　　　　　　┃┫┫　┃┫┫
 * 　　　　　　　　　　┗┻┛　┗┻┛
 * Created by wangjian on 2016/9/9.
 */
public class MineFragment extends Fragment {
    private View view;
    private MyAdapter myAdapter;
    private List<PlayerData> seatList;
    List<PlayerData> dataList = new ArrayList<>();
    private MainActivity activity = null;
    public int winIndex = 0;
    private ViewHolder vh = null;

    private String[] seatFlags = {"BTN", "SB", "BB", "UTG", "UTG+1", "MP", "MP+1", "HJ", "CO"};

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_mine, container, false);
        vh = new ViewHolder(view);
        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        activity = (MainActivity) getActivity();
        initView();
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    public void initView() {
        vh.title.setText("个人数据");
        LinearLayoutManager layoutManager = new LinearLayoutManager(activity);

        vh.selfDataRecyclerView.setLayoutManager(layoutManager);
        vh.selfDataRecyclerView.setItemAnimator(new DefaultItemAnimator());

        LinearLayoutManager layoutManager1 = new LinearLayoutManager(activity);
        layoutManager1.setOrientation(LinearLayoutManager.HORIZONTAL);
        vh.appList.setLayoutManager(layoutManager1);
        vh.appList.setItemAnimator(new DefaultItemAnimator());
        final AppAdapter appAdapter = new AppAdapter(activity, Constant.APPNAMES);
        appAdapter.setOnItemListener(new AppAdapter.OnItemClickListener() {
            @Override
            public void onClick(int position) {
                winIndex = position;
                int count = appAdapter.getItemCount();
                for (int i = 0; i < count; i++) {
                    View child = vh.appList.getChildAt(i);
                    if (child != null) {
                        TextView item = (TextView) child.findViewById(R.id.appName);
                        if (i == position) {
                            item.setTextColor(activity.getResources().getColor(R.color.red));
                        } else {
                            item.setTextColor(activity.getResources().getColor(R.color.black_overlay));
                        }
                    }
                }
                 querySelfData();
            }

            @Override
            public void onLongClick(int position) {
                //Toast.makeText(activity,appNames[position]+"长按", Toast.LENGTH_SHORT).show();
            }
        });
        vh.appList.setAdapter(appAdapter);
        seatList = new ArrayList<>();
        querySelfData();
    }
    public void querySelfData(){
        ReqData data = new ReqData();
        QuerySelf queryself = new QuerySelf();
        queryself.setUserid(activity.phone);
        queryself.setPlatType(winIndex);
        String param = new Gson().toJson(queryself);
        data.setParam(param);
        data.setReqno(TimeUtil.getCurrentDateToMinutes(new Date()) + ActionsTool.disposeNumber());
        data.setReqid(activity.getSharedPreferences(LoginActivity.PREF_FILE, Context.MODE_PRIVATE).getString("name", ""));
        HttpUtil.sendPostRequestData("queryself", new Gson().toJson(data), new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                MyLog.e("WindowTool", "response:(error)" + e.toString());
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String json = response.body().string();
                MyLog.e("MineFragment", "response:" + json);
                Result result = GsonUtil.parseJsonWithGson(json, Result.class);
                Map<String, String> map = result.getRets();
                String listself = map.get("listself");
                saveSelfData(listself);
                Message msg = new Message();
                msg.arg1 = 1;
                handler.sendMessage(msg);
            }
        });
    }

    public void saveSelfData(String json){
        seatList.clear();
        if(winIndex==0){//德扑圈
            List<PlayerData> playerDatas = new ArrayList<PlayerData>();
            Type listType = new TypeToken<List<PlayerData>>() {
            }.getType();
            playerDatas = new Gson().fromJson(json, listType);
            if (playerDatas != null) {
                if(playerDatas.size()!=0){
                    for (int i = 0; i < playerDatas.size(); i++) {
                        PlayerData playerData = playerDatas.get(i);
                        if (playerData != null) {
                            seatList.add(playerData);
                        }
                    }
                }else{
                    DataSupport.deleteAll(PlayerData.class,"name=?","self");
                    for (int i = 0; i < 9; i++) {
                        PlayerData player = new PlayerData();
                        player.setSeatFlag(seatFlags[i]);
                        player.setName("self");
                        player.setDate(TimeUtil.getCurrentDateToDay(new Date()));
                        seatList.add(player);
                    }
                }
            }
        }else if(winIndex==1){//德友圈
            List<PlayerData1> playerDatas = new ArrayList<PlayerData1>();
            Type listType = new TypeToken<List<PlayerData1>>() {
            }.getType();
            playerDatas = new Gson().fromJson(json, listType);
            if (playerDatas != null) {
                if(playerDatas.size()!=0){
                    for (int i = 0; i < playerDatas.size(); i++) {
                        PlayerData1 playerData = playerDatas.get(i);
                        seatList.add(playerData);
                    }
                }else{
                    DataSupport.deleteAll(PlayerData1.class,"name=?","self");
                    for (int i = 0; i < 9; i++) {
                        PlayerData1 player = new PlayerData1();
                        player.setSeatFlag(seatFlags[i]);
                        player.setName("self");
                        player.setDate(TimeUtil.getCurrentDateToDay(new Date()));
                        seatList.add(player);
                    }
                }
            }
        }else if(winIndex==2){//扑克部落MTT
            List<PlayerData2> playerDatas = new ArrayList<PlayerData2>();
            Type listType = new TypeToken<List<PlayerData2>>() {
            }.getType();
            playerDatas = new Gson().fromJson(json, listType);
            if (playerDatas != null) {
                if(playerDatas.size()!=0){
                    for (int i = 0; i < playerDatas.size(); i++) {
                        PlayerData2 playerData = playerDatas.get(i);
                        seatList.add(playerData);
                    }
                }else{
                    DataSupport.deleteAll(PlayerData2.class,"name=?","self");
                    for (int i = 0; i < 9; i++) {
                        PlayerData2 player = new PlayerData2();
                        player.setSeatFlag(seatFlags[i]);
                        player.setName("self");
                        player.setDate(TimeUtil.getCurrentDateToDay(new Date()));
                        seatList.add(player);
                    }
                }
            }
        }else if(winIndex==3){//扑克部落SNG
            List<PlayerData3> playerDatas = new ArrayList<PlayerData3>();
            Type listType = new TypeToken<List<PlayerData3>>() {
            }.getType();
            playerDatas = new Gson().fromJson(json, listType);
            if (playerDatas != null) {
                if(playerDatas.size()!=0){
                    for (int i = 0; i < playerDatas.size(); i++) {
                        PlayerData3 playerData = playerDatas.get(i);
                        seatList.add(playerData);
                    }
                }else {
                    DataSupport.deleteAll(PlayerData3.class,"name=?","self");
                    for (int i = 0; i < 9; i++) {
                        PlayerData3 player = new PlayerData3();
                        player.setSeatFlag(seatFlags[i]);
                        player.setName("self");
                        player.setDate(TimeUtil.getCurrentDateToDay(new Date()));
                        seatList.add(player);
                    }
                }
            }
        }

    }
    Handler handler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            if(msg.arg1==1){
                getSelfSeatsData();
            }
        }
    };
    public void getSelfSeatsData() {//获取个人在每个位置的玩家数据
        if(dataList.size()==0){
            for (int i = 0; i < 9; i++) {
                PlayerData playerData = new PlayerData();
                playerData.setSeatFlag(seatFlags[i]);
                playerData.setName("self");
                playerData.setDate(TimeUtil.getCurrentDateToDay(new Date()));
                dataList.add(i,playerData);
            }
        }
        for (int i = 0; i < seatList.size(); i++) {
            PlayerData playerData = seatList.get(i);
            switch (playerData.getSeatFlag()) {
                case "BTN":
                    dataList.set(0, playerData);
                    break;
                case "SB":
                    dataList.set(1, playerData);
                    break;
                case "BB":
                    dataList.set(2, playerData);
                    break;
                case "HJ":
                    dataList.set(3, playerData);
                    break;
                case "CO":
                    dataList.set(4, playerData);
                    break;
                case "UTG":
                    dataList.set(5, playerData);
                    break;
                case "UTG+1":
                    dataList.set(6, playerData);
                    break;
                case "MP":
                    dataList.set(7, playerData);
                    break;
                case "MP+1":
                    dataList.set(8, playerData);
                    break;
                default:
                    break;
            }
        }
        if (myAdapter == null) {
            myAdapter = new MyAdapter(activity, dataList);
            vh.selfDataRecyclerView.setAdapter(myAdapter);
        } else {
            myAdapter.notifyDataSetChanged();
        }
        PlayerData self = new PlayerData();
        for (int i = 0; i < dataList.size(); i++) {
            PlayerData playerData = dataList.get(i);
            self.setPlayCount(self.getPlayCount()+playerData.getPlayCount());
            self.setLoseCount(self.getLoseCount()+playerData.getLoseCount());
            self.setWinCount(self.getWinCount()+playerData.getWinCount());
            self.setBbCount(self.getBbCount()+playerData.getBbCount());
            self.setPfrCount(self.getPfrCount()+playerData.getPfrCount());
            self.setJoinCount(self.getJoinCount()+playerData.getJoinCount());
            self.setBet3Count(self.getBet3Count()+playerData.getBet3Count());
            self.setFoldCount(self.getFoldCount()+playerData.getFoldCount());
            self.setCallCount(self.getCallCount()+playerData.getCallCount());
            self.setRaiseCount(self.getRaiseCount()+playerData.getRaiseCount());
            self.setStlCount(self.getStlCount()+playerData.getStlCount());
            self.setFoldStlCount(self.getFoldStlCount()+playerData.getFoldStlCount());
            self.setFaceOpenCount(self.getFaceOpenCount()+playerData.getFaceOpenCount());
            self.setFaceCbCount(self.getFaceCbCount()+playerData.getFaceCbCount());
            self.setFaceStlCount(self.getFaceStlCount()+playerData.getFaceStlCount());
            self.setFace3BetCount(self.getFace3BetCount()+playerData.getFace3BetCount());
            self.setLastRaiseCount(self.getLastRaiseCount()+playerData.getLastRaiseCount());
            self.setCbCount(self.getCbCount()+playerData.getCbCount());
            self.setStlPosCount(self.getStlPosCount()+playerData.getStlPosCount());
            self.setFlopCount(self.getFlopCount()+playerData.getFlopCount());
            self.setFoldFlopCount(self.getFoldFlopCount()+playerData.getFoldFlopCount());
            self.setTurnCount(self.getTurnCount()+playerData.getTurnCount());
            self.setFoldTurnCount(self.getFoldTurnCount()+playerData.getFoldTurnCount());
            self.setRiverCount(self.getRiverCount()+playerData.getRiverCount());
            self.setFoldRiverCount(self.getFoldRiverCount()+playerData.getFoldRiverCount());
        }
        vh.totalBBCount.setText("总战绩:" + String.format("%.2f", self.getBbCount()) + "bb");
        vh.winLoseCount.setText("胜负比例:" + self.getWinCount() + "/" + self.getLoseCount());
        vh.totalPlayCount.setText("总手数:" + self.getPlayCount());
        vh.selfVpipPercent.setText(Constant.percentTypes[1] + "(" + Constant.getPercent(self, Constant.TYPE_VPIP) + "%)");
        vh.selfPfrPercent.setText(Constant.percentTypes[2] + "(" + Constant.getPercent(self, Constant.TYPE_PFR) + "%)");
        vh.selfBet3Percent.setText(Constant.percentTypes[3] + "(" + Constant.getPercent(self, Constant.TYPE_3BET) + "%)");
        vh.totalCBPercent.setText(Constant.percentTypes[4] + "(" + Constant.getPercent(self, Constant.TYPE_CB) + "%)");
        vh.totalAFPercent.setText(Constant.percentTypes[5] + "(" + Constant.getPercent(self, Constant.TYPE_AF) + ")");
        vh.totalF3BetPercent.setText(Constant.percentTypes[6] + "(" + Constant.getPercent(self, Constant.TYPE_F3BET) + "%)");
        vh.totalStlPercent.setText(Constant.percentTypes[7] + "(" + Constant.getPercent(self, Constant.TYPE_STL) + "%)");
        vh.totalFStlPercent.setText(Constant.percentTypes[8] + "(" + Constant.getPercent(self, Constant.TYPE_FSTL) + "%)");
        vh.totalFCBPercent.setText(Constant.percentTypes[9] + "(" + Constant.getPercent(self, Constant.TYPE_FCB) + "%)");
        vh.totalFFlopPercent.setText(Constant.percentTypes[10] + "(" + Constant.getPercent(self, Constant.TYPE_FFLOP) + "%)");
        vh.totalFTurnPercent.setText(Constant.percentTypes[11] + "(" + Constant.getPercent(self, Constant.TYPE_FTURN) + "%)");
        vh.totalFRiverPercent.setText(Constant.percentTypes[12] + "(" + Constant.getPercent(self, Constant.TYPE_FRIVER) + "%)");
    }


    static class ViewHolder {
        @BindView(R.id.back)
        TextView back;
        @BindView(R.id.title)
        TextView title;
        @BindView(R.id.right)
        TextView right;
        @BindView(R.id.totalPlayCount)
        TextView totalPlayCount;
        @BindView(R.id.winLoseCount)
        TextView winLoseCount;
        @BindView(R.id.totalBBCount)
        TextView totalBBCount;
        @BindView(R.id.self_vpipPercent)
        TextView selfVpipPercent;
        @BindView(R.id.self_pfrPercent)
        TextView selfPfrPercent;
        @BindView(R.id.self_bet3Percent)
        TextView selfBet3Percent;
        @BindView(R.id.totalStlPercent)
        TextView totalStlPercent;
        @BindView(R.id.totalFStlPercent)
        TextView totalFStlPercent;
        @BindView(R.id.totalF3BetPercent)
        TextView totalF3BetPercent;
        @BindView(R.id.totalCBPercent)
        TextView totalCBPercent;
        @BindView(R.id.totalFCBPercent)
        TextView totalFCBPercent;
        @BindView(R.id.totalAFPercent)
        TextView totalAFPercent;
        @BindView(R.id.totalFFlopPercent)
        TextView totalFFlopPercent;
        @BindView(R.id.totalFTurnPercent)
        TextView totalFTurnPercent;
        @BindView(R.id.totalFRiverPercent)
        TextView totalFRiverPercent;
        @BindView(R.id.appList)
        RecyclerView appList;
        @BindView(R.id.textView2)
        TextView textView2;
        @BindView(R.id.selfDataRecyclerView)
        RecyclerView selfDataRecyclerView;

        ViewHolder(View view) {
            ButterKnife.bind(this, view);
        }
    }
}
