package com.ruilonglai.texas_scan.fragment;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.lcodecore.tkrefreshlayout.TwinklingRefreshLayout;
import com.ruilonglai.texas_scan.MainActivity;
import com.ruilonglai.texas_scan.R;
import com.ruilonglai.texas_scan.adapter.PlayerViewAdapter;
import com.ruilonglai.texas_scan.entity.PlayerData;
import com.ruilonglai.texas_scan.util.AssetsCopyUtil;
import com.ruilonglai.texas_scan.util.TimeUtil;

import org.litepal.crud.DataSupport;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

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
 * Created by wangshuai on 2016/9/9.
 */
public class PlayerFragment extends Fragment {
    private List<PlayerData> list;
    private Context context=null;
    private RecyclerView playerList;
    private PlayerViewAdapter adapter = null;
    private TwinklingRefreshLayout refreshLayout;
    private boolean fristOpen = true;
    private MainActivity activity = null;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_message, container, false);
    }
    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        activity = (MainActivity) getActivity();
        list = new ArrayList<PlayerData>();

        playerList = (RecyclerView) getActivity().findViewById(R.id.playerList);
        refreshLayout = (TwinklingRefreshLayout) getActivity().findViewById(R.id.refreshLayout);
        refreshLayout.setEnableRefresh(false);
        refreshLayout.setOnRefreshListener(new TwinklingRefreshLayout.OnRefreshListener(){
            @Override
            public void onRefresh(final TwinklingRefreshLayout refreshLayout) {
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        refreshLayout.finishRefreshing();
                    }
                },1000);
            }

            @Override
            public void onLoadMore(final TwinklingRefreshLayout refreshLayout) {
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        refreshLayout.finishLoadmore();
                    }
                },200);
            }
        });
        LinearLayoutManager manager = new LinearLayoutManager(context);
        playerList.setLayoutManager(manager);
        playerList.setItemAnimator(new DefaultItemAnimator());
        findTodayData(TimeUtil.getCurrentDateToDay(new Date()));
    }
    public void findTodayData(String date) {//在数据库获取当天赢的钱最多的玩家得信息
        if(list==null){
            list = new ArrayList<>();
        }else{
            list.clear();
        }
        List<PlayerData> playerDatas = DataSupport.where("not name=?","_self").find(PlayerData.class);
        for (PlayerData player : playerDatas) {
            if (!"_self".equals(player.getName())) {
                list.add(player);
            }
        }
        if(adapter==null){
            adapter = new PlayerViewAdapter(activity,list);
            adapter.setOnItemListener(new PlayerViewAdapter.OnItemClickListener() {
                @Override
                public void onClick(int position) {

                }
                @Override
                public void onLongClick(final int position) {
                    final PlayerData playerData = list.get(position);
                    AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                    builder.setMessage("是否删除这条记录?");
                    builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            DataSupport.deleteAll(PlayerData.class,"name=?",playerData.getName());
                            list.remove(position);
                            adapter.notifyDataSetChanged();
                            AssetsCopyUtil.copyDataBaseToSD(getActivity());
                            Toast.makeText(getActivity(),"删除成功",Toast.LENGTH_LONG).show();
                        }
                    });
                    builder.setNegativeButton("取消",null);
                    builder.show();
                }
            });
            playerList.setAdapter(adapter);
        }else{
            adapter.notifyDataSetChanged();
        }
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        context = getActivity();
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }

    public void notifyDataSetChaged(){
        findTodayData(TimeUtil.getCurrentDateToDay(new Date()));
    }

}
