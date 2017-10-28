package com.ruilonglai.texas_scan.fragment;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.lcodecore.tkrefreshlayout.TwinklingRefreshLayout;
import com.ruilonglai.texas_scan.MainActivity;
import com.ruilonglai.texas_scan.R;
import com.ruilonglai.texas_scan.activity.LoginActivity;
import com.ruilonglai.texas_scan.adapter.AppAdapter;
import com.ruilonglai.texas_scan.adapter.PlayerViewAdapter;
import com.ruilonglai.texas_scan.entity.PlayerData;
import com.ruilonglai.texas_scan.entity.QueryUser;
import com.ruilonglai.texas_scan.entity.ReqData;
import com.ruilonglai.texas_scan.entity.Result;
import com.ruilonglai.texas_scan.entity.UserName;
import com.ruilonglai.texas_scan.util.ActionsTool;
import com.ruilonglai.texas_scan.util.Constant;
import com.ruilonglai.texas_scan.util.GsonUtil;
import com.ruilonglai.texas_scan.util.HttpUtil;
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
 * Created by wangshuai on 2016/9/9.
 */
public class PlayerFragment extends Fragment {
    private List<PlayerData> list;
    private Context context = null;
    private PlayerViewAdapter adapter = null;
    private boolean fristOpen = true;
    private MainActivity activity = null;
    private ViewHolder vh;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        activity = (MainActivity) context;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_message, container, false);
        vh = new ViewHolder(view);
        return view;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        initView();
        findData(TimeUtil.getCurrentDateToDay(new Date()));
    }

    public void initView(){
        LinearLayoutManager layoutManager1 = new LinearLayoutManager(activity);
        layoutManager1.setOrientation(LinearLayoutManager.HORIZONTAL);
        vh.appList.setLayoutManager(layoutManager1);
        vh.appList.setItemAnimator(new DefaultItemAnimator());
        final AppAdapter appAdapter = new AppAdapter(activity, Constant.APPNAMES);
        appAdapter.setOnItemListener(new AppAdapter.OnItemClickListener() {
            @Override
            public void onClick(int position) {
                Toast.makeText(activity, Constant.APPNAMES[position], Toast.LENGTH_SHORT).show();
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
            }

            @Override
            public void onLongClick(int position) {
                //Toast.makeText(activity,appNames[position]+"长按", Toast.LENGTH_SHORT).show();
            }
        });
        vh.appList.setAdapter(appAdapter);
        list = new ArrayList<PlayerData>();
        vh.title.setText("玩家");
        vh.refreshLayout.setEnableRefresh(false);
        vh.refreshLayout.setOnRefreshListener(new TwinklingRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh(final TwinklingRefreshLayout refreshLayout) {
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        refreshLayout.finishRefreshing();
                    }
                }, 1000);
            }

            @Override
            public void onLoadMore(final TwinklingRefreshLayout refreshLayout) {
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        refreshLayout.finishLoadmore();
                    }
                }, 200);
            }
        });
        LinearLayoutManager manager = new LinearLayoutManager(context);
        vh.playerList.setLayoutManager(manager);
        vh.playerList.setItemAnimator(new DefaultItemAnimator());
    }

    public void queryUserData(){
        List<UserName> names = DataSupport.findAll(UserName.class);
        if(names.size()==0)
            return;
        List<String> usernames = new ArrayList<>();
        for (int i = 0; i < names.size(); i++) {
            usernames.add(names.get(i).name);
        }
        ReqData data = new ReqData();
        QueryUser user = new QueryUser();
        user.setUsernames(usernames);
        user.setPlatType(activity.winIndex);
        data.setParam(new Gson().toJson(user));
        data.setReqno(TimeUtil.getCurrentDateToMinutes(new Date()) + ActionsTool.disposeNumber());
        data.setReqid(context.getSharedPreferences(LoginActivity.PREF_FILE, Context.MODE_PRIVATE).getString("name", ""));
        HttpUtil.sendPostRequestData("queryuser", new Gson().toJson(data), new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.e("WindowTool", "response:(error)" + e.toString());
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String json = response.body().string();
                Log.e("WindowTool", "response:" + json);
                Result result = GsonUtil.parseJsonWithGson(json, Result.class);
                Map<String, String> map = result.getRets();
                String players = map.get("listuser");
                List<PlayerData> playerDatas = new ArrayList<PlayerData>();
                Type listType = new TypeToken<List<PlayerData>>() {}.getType();
                playerDatas = new Gson().fromJson(players, listType);
                if (playerDatas != null) {
                    for (int i = 0; i < playerDatas.size(); i++) {
                        PlayerData playerData = playerDatas.get(i);
                        if (playerData != null) {
                            List<PlayerData> datas = DataSupport.where("name=?", playerData.getName()).find(PlayerData.class);
                            if (datas.size() > 0) {
                                DataSupport.deleteAll(PlayerData.class, "name=?", playerData.getName());
                            }
                            playerData.save();
                        }
                    }
                }
                Message msg = new Message();
                msg.arg1 = 1;
                handler.sendMessage(msg);
            }
        });
    }
    public void findData(String date) {//在数据库获取当天赢的钱最多的玩家得信息
        if (list == null) {
            list = new ArrayList<>();
        } else {
            list.clear();
        }
        List<PlayerData> playerDatas = DataSupport.where("not name=?", "self").find(PlayerData.class);
        for (PlayerData player : playerDatas) {
            if (!"self".contains(player.getName())) {
                list.add(player);
            }
        }
        if (adapter == null) {
            adapter = new PlayerViewAdapter(activity, list);
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
                            DataSupport.deleteAll(PlayerData.class, "name=?", playerData.getName());
                            list.remove(position);
                            adapter.notifyDataSetChanged();
//                            AssetsCopyUtil.copyDataBaseToSD(getActivity());
                            Toast.makeText(getActivity(), "删除成功", Toast.LENGTH_LONG).show();
                        }
                    });
                    builder.setNegativeButton("取消", null);
                    builder.show();
                }
            });
            vh.playerList.setAdapter(adapter);
        } else {
            adapter.notifyDataSetChanged();
        }
    }
    Handler handler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            if(msg.arg1==1){
                findData(TimeUtil.getCurrentDateToDay(new Date()));
            }
        }
    };
    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        context = getActivity();
    }

    public void notifyDataSetChaged() {
        queryUserData();
    }

    static class ViewHolder {
        @BindView(R.id.title)
        TextView title;
        @BindView(R.id.playerList)
        RecyclerView playerList;
        @BindView(R.id.playerAppList)
        RecyclerView appList;
        @BindView(R.id.refreshLayout)
        TwinklingRefreshLayout refreshLayout;

        ViewHolder(View view) {
            ButterKnife.bind(this, view);
        }
    }
}
