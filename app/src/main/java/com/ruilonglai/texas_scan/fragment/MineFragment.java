package com.ruilonglai.texas_scan.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.ruilonglai.texas_scan.MainActivity;
import com.ruilonglai.texas_scan.R;
import com.ruilonglai.texas_scan.activity.PokerDetailActivity;
import com.ruilonglai.texas_scan.adapter.AppAdapter;
import com.ruilonglai.texas_scan.adapter.MyAdapter;
import com.ruilonglai.texas_scan.entity.PlayerData;
import com.ruilonglai.texas_scan.util.TimeUtil;
import com.ruilonglai.texas_scan.util.ToastUtil;

import org.litepal.crud.DataSupport;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static org.litepal.crud.DataSupport.where;

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
public class MineFragment extends Fragment implements View.OnClickListener{
    private View view;
    private MyAdapter myAdapter;
    private RecyclerView appList;
    private List<PlayerData> seatList;
    private RecyclerView seatFlagsList;
    private MainActivity activity= null;
    public int winIndex = 2;
    private Button btn_poker_detail;
    private int selfTotalWinCount;
    private int selfTotalLoseCount;
    private int selfTotalPlayCount;
    private int selfTotalJoinCount;
    private int selfTotalBet3Count;
    private int selfTotalStlCount;
    private int selfTotalFoldStlCount;
    private int selfTotalPfrCount;
    private double selfTotalBBCount;
    private TextView winLoseCount;
    private TextView totalBBCount;
    private TextView totalPlayCount;
    private TextView totalBet3Count;
    private TextView totalVpipCount;
    private TextView totalPfrCount;
    private TextView totalStlCount;
    private TextView totalFoldStlCount;
    private String[] appNames = {"德扑圈","扑克部落MTT","德友圈"};

    private String[] seatFlags = {"BTN", "SB", "BB", "UTG", "UTG+1", "MP", "MP+1", "HJ", "CO"};
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_mine, container, false);
        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        activity = (MainActivity) getActivity();
        initView();
        getSelfSeatsData();
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    public void initView() {
        totalBBCount = (TextView) view.findViewById(R.id.totalBBCount);
        totalPlayCount = (TextView) view.findViewById(R.id.totalPlayCount);
        winLoseCount = (TextView) view.findViewById(R.id.winLoseCount);
        totalBet3Count = (TextView) view.findViewById(R.id.self_bet3Percent);
        totalVpipCount = (TextView) view.findViewById(R.id.self_vpipPercent);
        totalPfrCount = (TextView) view.findViewById(R.id.self_pfrPercent);
        totalStlCount = (TextView) view.findViewById(R.id.self_stlPercent);
        totalFoldStlCount = (TextView) view.findViewById(R.id.self_foldStlPercent);
        btn_poker_detail = (Button) view.findViewById(R.id.btn_poker_details);
        btn_poker_detail.setOnClickListener(this);
        LinearLayoutManager layoutManager = new LinearLayoutManager(activity);
        seatFlagsList = (RecyclerView) view.findViewById(R.id.selfDataRecyclerView);
        seatFlagsList.setLayoutManager(layoutManager);
        seatFlagsList.setItemAnimator(new DefaultItemAnimator());

        appList = (RecyclerView) view.findViewById(R.id.appList);
        LinearLayoutManager layoutManager1 = new LinearLayoutManager(activity);
        layoutManager1.setOrientation(LinearLayoutManager.HORIZONTAL);
        appList.setLayoutManager(layoutManager1);
        appList.setItemAnimator(new DefaultItemAnimator());
        final AppAdapter appAdapter = new AppAdapter(activity,appNames);
        appAdapter.setOnItemListener(new AppAdapter.OnItemClickListener() {
            @Override
            public void onClick(int position) {
                winIndex = position;
                Toast.makeText(activity,"开启"+appNames[position], Toast.LENGTH_SHORT).show();
                int count = appAdapter.getItemCount();
                for (int i = 0; i < count; i++) {
                    TextView item = (TextView) appList.getChildAt(i).findViewById(R.id.appName);
                    if(i==position){
                        item.setTextColor(activity.getResources().getColor(R.color.red));
                    }else{
                        item.setTextColor(activity.getResources().getColor(R.color.black_overlay));
                    }
                }
                activity.setTemplate(position+8);
            }
            @Override
            public void onLongClick(int position) {
                //Toast.makeText(activity,appNames[position]+"长按", Toast.LENGTH_SHORT).show();
            }
        });
        appList.setAdapter(appAdapter);
        seatList = new ArrayList<>();
        for (int i = 0; i < 9; i++) {
            PlayerData player = new PlayerData();
            player.setSeatFlag(seatFlags[i]);
            player.setName("_self");
            List<PlayerData> self = DataSupport.where("name=? and seatFlag=?", "_self", seatFlags[i]).find(PlayerData.class);
            if (self.size() == 0) {//不存在则创建一个
                player.setDate(TimeUtil.getCurrentDateToDay(new Date()));
                player.save();
            }
            seatList.add(player);
        }

    }
    public void getSelfSeatsData() {//获取个人在每个位置的玩家数据
        double selfTotalBBCount = 0;
        int selfTotalWinCount = 0;
        int selfTotalLoseCount = 0;
        int selfTotalPlayCount = 0;
        int selfTotalJoinCount = 0;
        int selfTotalBet3Count = 0;
        int selfTotalPfrCount = 0;
        int selfTotalStlCount = 0;
        int selfTotalFoldStlCount = 0;
        int selfTotalFold3BetCount = 0;
        int selfTotalFace3BetCount = 0;
        int selfTotalFaceOpenCount = 0;
        int selfTotalCallCount = 0;
        int selfTotalRaiseCount = 0;
        int selfTotalCbCount = 0;
        int selfLastRaiseCount = 0;
        int selfTotalSTLPositionCount = 0;
        int selfTotalFaceSTLCount = 0;
        List<PlayerData> dataList = where("name=?","_self").find(PlayerData.class);
        for (int i = 0; i < dataList.size(); i++) {
            PlayerData playerData = dataList.get(i);
            switch (playerData.getSeatFlag()) {
                case "BTN":
                    seatList.set(0, playerData);
                    break;
                case "SB":
                    seatList.set(1, playerData);
                    break;
                case "BB":
                    seatList.set(2, playerData);
                    break;
                case "HJ":
                    seatList.set(3, playerData);
                    break;
                case "CO":
                    seatList.set(4, playerData);
                    break;
                case "UTG":
                    seatList.set(5, playerData);
                    break;
                case "UTG+1":
                    seatList.set(6, playerData);
                    break;
                case "MP":
                    seatList.set(7, playerData);
                    break;
                case "MP+1":
                    seatList.set(8, playerData);
                    break;
                default:
                    break;
            }
        }
        if(myAdapter==null){
            myAdapter = new MyAdapter(activity, seatList);
            seatFlagsList.setAdapter(myAdapter);
        }else{
            myAdapter.notifyDataSetChanged();
        }
        for (int i = 0; i < seatList.size(); i++) {
            PlayerData playerData = seatList.get(i);
            selfTotalBBCount += playerData.getBbCount();
            selfTotalWinCount += playerData.getWinCount();
            selfTotalLoseCount += playerData.getLoseCount();
            selfTotalPlayCount += playerData.getPlayCount();
            selfTotalJoinCount += playerData.getJoinCount();
            selfTotalBet3Count += playerData.getBet3Count();
            selfTotalPfrCount += playerData.getPfrCount();
            selfTotalStlCount += playerData.getStlCount();
            selfTotalFoldStlCount += playerData.getFoldStlCount();
            selfTotalFace3BetCount += playerData.getFace3BetCount();
            selfTotalFaceSTLCount += playerData.getFaceStlCount();
            selfTotalSTLPositionCount += playerData.getStlPosCount();
            selfTotalFaceOpenCount += playerData.getFaceOpenCount();
        }
        totalBBCount.setText("总战绩:"+String.format("%.2f",selfTotalBBCount)+"bb");
        winLoseCount.setText("胜负比例:"+selfTotalWinCount+"/"+selfTotalLoseCount);
        totalPlayCount.setText("总手数:"+selfTotalPlayCount);
        if(selfTotalFaceOpenCount!=0){
            totalBet3Count.setText("3Bet("+selfTotalBet3Count*100/selfTotalFaceOpenCount+"%)");
        }else{
            totalBet3Count.setText("3Bet(0.0%)");
        }
       if(selfTotalPlayCount>0){
           totalVpipCount.setText("VPIP("+selfTotalJoinCount*100/selfTotalPlayCount+"%)");
           totalPfrCount.setText("PFR("+selfTotalPfrCount*100/selfTotalPlayCount+"%)");
           if(selfTotalSTLPositionCount>0){
               totalStlCount.setText("STL("+selfTotalStlCount*100/selfTotalSTLPositionCount+"%)");
           }else{
               totalStlCount.setText("STL(-%)");
           }
           if(selfTotalFaceSTLCount>0){
               totalFoldStlCount.setText("FSTL("+selfTotalFoldStlCount*100/selfTotalFaceSTLCount+"%)");
           }else{
               totalFoldStlCount.setText("FSTL(-%)");
           }
       }else{
           totalVpipCount.setText("VPIP(-%)");
           totalPfrCount.setText("PFR(-%)");
           totalStlCount.setText("STL(-%)");
           totalFoldStlCount.setText("FSTL(-%)");
       }
    }

    @Override
    public void onClick(View v) {
       if(v.getId()==R.id.btn_poker_details){
           Intent intent = new Intent(activity, PokerDetailActivity.class);
           startActivity(intent);
       }
    }

}
