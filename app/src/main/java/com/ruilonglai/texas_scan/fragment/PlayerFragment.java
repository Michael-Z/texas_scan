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
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.lcodecore.tkrefreshlayout.TwinklingRefreshLayout;
import com.ruilonglai.texas_scan.MainActivity;
import com.ruilonglai.texas_scan.R;
import com.ruilonglai.texas_scan.activity.LoginActivity;
import com.ruilonglai.texas_scan.adapter.AppAdapter;
import com.ruilonglai.texas_scan.adapter.PlayerViewAdapter;
import com.ruilonglai.texas_scan.entity.PlayerData;
import com.ruilonglai.texas_scan.entity.PlayerData1;
import com.ruilonglai.texas_scan.entity.PlayerData2;
import com.ruilonglai.texas_scan.entity.PlayerData3;
import com.ruilonglai.texas_scan.entity.QueryUser;
import com.ruilonglai.texas_scan.entity.ReqData;
import com.ruilonglai.texas_scan.entity.ReqDelUser;
import com.ruilonglai.texas_scan.entity.Result;
import com.ruilonglai.texas_scan.entity.UserName;
import com.ruilonglai.texas_scan.util.ActionsTool;
import com.ruilonglai.texas_scan.util.Constant;
import com.ruilonglai.texas_scan.util.GsonUtil;
import com.ruilonglai.texas_scan.util.HttpUtil;
import com.ruilonglai.texas_scan.util.MyLog;
import com.ruilonglai.texas_scan.util.TimeUtil;

import org.litepal.crud.DataSupport;

import java.io.IOException;
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
    public static List<PlayerData> list;
    private Context context = null;
    private PlayerViewAdapter adapter = null;
    private boolean fristOpen = true;
    private MainActivity activity = null;
    private ViewHolder vh;
    private int winIdx;
    private int offsetIdx;

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
    }

    public void initView() {
        LinearLayoutManager layoutManager1 = new LinearLayoutManager(activity);
        layoutManager1.setOrientation(LinearLayoutManager.HORIZONTAL);
        vh.appList.setLayoutManager(layoutManager1);
        vh.appList.setItemAnimator(new DefaultItemAnimator());
        final AppAdapter appAdapter = new AppAdapter(activity, Constant.APPNAMES);
        appAdapter.setOnItemListener(new AppAdapter.OnItemClickListener() {
            @Override
            public void onClick(int position) {
//                Toast.makeText(activity, Constant.APPNAMES[position], Toast.LENGTH_SHORT).show();
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
                if(winIdx!=position){
                    offsetIdx=0;
                    list.clear();
                }
                winIdx = position;
                queryUserData(false);
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
                        queryUserData(false);
                    }
                }, 1000);
            }
        });
        LinearLayoutManager manager = new LinearLayoutManager(context);
        vh.playerList.setLayoutManager(manager);
        vh.playerList.setItemAnimator(new DefaultItemAnimator());
    }

    public void queryUserData(boolean isClear) {
        if(isClear){
            offsetIdx = 0;
            list.clear();
        }
        List<UserName> names = DataSupport.where("not name=? and platType=?","self",winIdx+"").order("id desc").limit(20).offset(offsetIdx).find(UserName.class);
        offsetIdx+=names.size();
        for (int i = names.size()-1; i >=0 ; i--) {
            UserName name = names.get(i);
            if(name.name.contains(activity.phone)){
                names.remove(i);
            }
        }
        if (names.size() == 0){
            findData(isClear);
            return;
        }
        List<String> usernames = new ArrayList<>();
        for (int i = 0; i < names.size(); i++) {
            usernames.add(names.get(i).name);
        }
        ReqData data = new ReqData();
        QueryUser user = new QueryUser();
        user.setUsernames(usernames);
        user.setPlatType(winIdx);
        data.setParam(new Gson().toJson(user));
        data.setReqno(TimeUtil.getCurrentDateToMinutes(new Date()) + ActionsTool.disposeNumber());
        data.setReqid(context.getSharedPreferences(LoginActivity.PREF_FILE, Context.MODE_PRIVATE).getString("name", ""));
        HttpUtil.sendPostRequestData("queryuser", new Gson().toJson(data), new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                MyLog.e("WindowTool", "response:(error)" + e.toString());
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String json = response.body().string();
                MyLog.e("WindowTool", "response:" + json);
                Result result = GsonUtil.parseJsonWithGson(json, Result.class);
                Map<String, String> map = result.getRets();
                String players = map.get("listuser");
                saveUserData(players);
                Message msg = new Message();
                msg.arg1 = 1;
                handler.sendMessage(msg);
            }
        });
    }
    public void saveUserData(String json) {
        Gson gson = new Gson();
        if (winIdx == 0) {
            List<PlayerData> playerDatas = new ArrayList<PlayerData>();
            playerDatas = gson.fromJson(json, new TypeToken<List<PlayerData>>() {
            }.getType());
            if (playerDatas != null) {
                for (int i = 0; i < playerDatas.size(); i++) {
                    PlayerData playerData = playerDatas.get(i);
                    if (playerData != null) {
                        List<PlayerData> datas = DataSupport.where("name=?", playerData.getName()).find(PlayerData.class);
                        if (datas.size() > 0) {
                            DataSupport.deleteAll(PlayerData.class, "name=?", playerData.getName());
                        }
                        if(!playerData.getName().equals("self"))
                        list.add(playerData);
                        playerData.save();
                        List<UserName> userNames1 = DataSupport.where("name=?", playerData.getName()).find(UserName.class);
                        if (userNames1.size() == 0) {
                            UserName un = new UserName();
                            un.name = playerData.getName();
                            un.save();
                        }
                    }
                }
            }
        } else if (winIdx == 1) {
            List<PlayerData1> playerDatas = new ArrayList<PlayerData1>();
            playerDatas = gson.fromJson(json, new TypeToken<List<PlayerData1>>() {
            }.getType());
            if (playerDatas != null) {
                for (int i = 0; i < playerDatas.size(); i++) {
                    PlayerData1 playerData = playerDatas.get(i);
                    if (playerData != null) {
                        List<PlayerData1> datas = DataSupport.where("name=?", playerData.getName()).find(PlayerData1.class);
                        if (datas.size() > 0) {
                            DataSupport.deleteAll(PlayerData1.class, "name=?", playerData.getName());
                        }
                        playerData.save();
                        if(!playerData.getName().equals("self"))
                        list.add(playerData);
                        List<UserName> userNames1 = DataSupport.where("name=?", playerData.getName()).find(UserName.class);
                        if (userNames1.size() == 0) {
                            UserName un = new UserName();
                            un.name = playerData.getName();
                            un.save();
                        }
                    }
                }
            }
        } else if (winIdx == 2) {
            List<PlayerData2> playerDatas = new ArrayList<PlayerData2>();
            playerDatas = gson.fromJson(json, new TypeToken<List<PlayerData2>>() {
            }.getType());
            if (playerDatas != null) {
                for (int i = 0; i < playerDatas.size(); i++) {
                    PlayerData2 playerData = playerDatas.get(i);
                    if (playerData != null) {
                        List<PlayerData2> datas = DataSupport.where("name=?", playerData.getName()).find(PlayerData2.class);
                        if (datas.size() > 0) {
                            DataSupport.deleteAll(PlayerData2.class, "name=?", playerData.getName());
                        }
                        playerData.save();
                        if(!playerData.getName().equals("self"))
                        list.add(playerData);
                        List<UserName> userNames1 = DataSupport.where("name=?", playerData.getName()).find(UserName.class);
                        if (userNames1.size() == 0) {
                            UserName un = new UserName();
                            un.name = playerData.getName();
                            un.save();
                        }
                    }
                }
            }
        } else if (winIdx == 3) {
            List<PlayerData3> playerDatas = new ArrayList<PlayerData3>();
            playerDatas = gson.fromJson(json, new TypeToken<List<PlayerData3>>() {
            }.getType());
            if (playerDatas != null) {
                for (int i = 0; i < playerDatas.size(); i++) {
                    PlayerData3 playerData = playerDatas.get(i);
                    if (playerData != null) {
                        List<PlayerData3> datas = DataSupport.where("name=?", playerData.getName()).find(PlayerData3.class);
                        if (datas.size() > 0) {
                            DataSupport.deleteAll(PlayerData3.class, "name=?", playerData.getName());
                        }
                        playerData.save();
                        if(!playerData.getName().equals("self"))
                        list.add(playerData);
                        List<UserName> userNames1 = DataSupport.where("name=?", playerData.getName()).find(UserName.class);
                        if (userNames1.size() == 0) {
                            UserName un = new UserName();
                            un.name = playerData.getName();
                            un.save();
                        }
                    }
                }
            }
        }

    }
    public void findData(boolean isClear) {//在数据库获取当天赢的钱最多的玩家得信息
        if (list == null) {
            list = new ArrayList<>();
        }
        if(isClear){
            list.clear();
        }
        if (adapter == null) {
            adapter = new PlayerViewAdapter(activity, list);
            adapter.setOnItemListener(new PlayerViewAdapter.OnItemClickListener() {
                @Override
                public void onClick(final int position) {
                    View view = LayoutInflater.from(context).inflate(R.layout.layout_window, null, false);
                    PlayerViewHolder pvh = new PlayerViewHolder(view);
                    PlayerData player = list.get(position);
                    pvh.note.setVisibility(View.GONE);
                    pvh.item.setBackgroundColor(context.getResources().getColor(R.color.white));
                    pvh.pos1.setText(player.getName() + Constant.getPercent(player, Constant.TYPE_HAND));
                    pvh.pos2.setText(Constant.percentTypes[1] + "(" + Constant.getPercent(player, Constant.TYPE_VPIP) + "%)");
                    pvh.pos3.setText(Constant.percentTypes[2] + "(" + Constant.getPercent(player, Constant.TYPE_PFR) + "%)");
                    pvh.pos4.setText(Constant.percentTypes[3] + "(" + Constant.getPercent(player, Constant.TYPE_3BET) + "%)");
                    pvh.pos5.setText(Constant.percentTypes[4] + "(" + Constant.getPercent(player, Constant.TYPE_CB) + "%)");
                    pvh.pos6.setText(Constant.percentTypes[5] + "(" + Constant.getPercent(player, Constant.TYPE_AF) + ")");
                    pvh.pos7.setText(Constant.percentTypes[6] + "(" + Constant.getPercent(player, Constant.TYPE_F3BET) + "%)");
                    pvh.pos8.setText(Constant.percentTypes[7] + "(" + Constant.getPercent(player, Constant.TYPE_STL) + "%)");
                    pvh.pos9.setText(Constant.percentTypes[8] + "(" + Constant.getPercent(player, Constant.TYPE_FSTL) + "%)");
                    pvh.pos10.setText(Constant.percentTypes[9] + "(" + Constant.getPercent(player, Constant.TYPE_FCB) + "%)");
                    pvh.pos11.setText(Constant.percentTypes[10] + "(" + Constant.getPercent(player, Constant.TYPE_FFLOP) + "%)");
                    pvh.pos12.setText(Constant.percentTypes[11] + "(" + Constant.getPercent(player, Constant.TYPE_FTURN) + "%)");
                    pvh.pos13.setText(Constant.percentTypes[12] + "(" + Constant.getPercent(player, Constant.TYPE_FRIVER) + "%)");
                    pvh.pos14.setText(Constant.percentTypes[13] + "(" + Constant.getPercent(player, Constant.TYPE_WTSD) + "%)");
                    pvh.pos15.setText(Constant.percentTypes[14] + "(" + Constant.getPercent(player, Constant.TYPE_WWSD) + "%)");
                    final AlertDialog.Builder builder = new AlertDialog.Builder(context);
                    builder .setView(view)
                            .setCancelable(false)
                            .setNegativeButton("返回", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    dialog.cancel();
                                }
                            })
                            .setPositiveButton("删除", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    final PlayerData playerData = list.get(position);
                                    AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                                    builder.setMessage("是否删除这条记录?");
                                    builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            if(winIdx==0){
                                                DataSupport.deleteAll(PlayerData.class, "name=?", playerData.getName());
                                            }else if(winIdx==1){
                                                DataSupport.deleteAll(PlayerData1.class, "name=?", playerData.getName());
                                            }else if(winIdx==2){
                                                DataSupport.deleteAll(PlayerData2.class, "name=?", playerData.getName());
                                            }else if(winIdx==3){
                                                DataSupport.deleteAll(PlayerData3.class, "name=?", playerData.getName());
                                            }
                                            DataSupport.deleteAll(UserName.class,"name=? and plattype=?",playerData.getName(),winIdx+"");
                                            list.remove(position);
                                            adapter.notifyDataSetChanged();
                                            Message msg = new Message();
                                            msg.arg1 = 2;
                                            msg.obj = playerData.getName();
                                            handler.sendMessage(msg);
                                        }
                                    });
                                    builder.setNegativeButton("取消", null);
                                    builder.show();
                                }
                            })
                            .show();
                }

                @Override
                public void onLongClick(final int position) {

                }
            });
            vh.playerList.setAdapter(adapter);
        } else {
            adapter.notifyDataSetChanged();
        }
    }
     /*删除某平台下的数据*/
    public void deleteNameData(List<String> names){
        ReqData data = new ReqData();
        ReqDelUser rdu = new ReqDelUser();
        rdu.setPlattype(winIdx);
        rdu.setUsernames(names);
        data.setReqno(TimeUtil.getCurrentDateToMinutes(new Date()) + ActionsTool.disposeNumber());
        data.setReqid(context.getSharedPreferences(LoginActivity.PREF_FILE, Context.MODE_PRIVATE).getString("name", ""));
        data.setParam(new Gson().toJson(rdu));
        HttpUtil.sendPostRequestData("reqdeluser", new Gson().toJson(data), new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
//                Toast.makeText(context,"服务器未响应",Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                MyLog.e(getClass().getName()+"response(reqdeluser)",response.toString());
            }
        });
    }
    Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            if (msg.arg1 == 1) {
                findData(false);
            }else if(msg.arg1 == 2){/*删除服务器某玩家的数据*/
                String name  = (String)msg.obj;
               List<String> list = new  ArrayList<>();
                list.add(name);
                deleteNameData(list);
            }
        }
    };

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        context = getActivity();
    }

    public void notifyDataSetChaged(boolean isClear) {
        queryUserData(isClear);
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

    static class PlayerViewHolder {
        @BindView(R.id.pos1)
        TextView pos1;
        @BindView(R.id.pos2)
        TextView pos2;
        @BindView(R.id.pos3)
        TextView pos3;
        @BindView(R.id.pos4)
        TextView pos4;
        @BindView(R.id.pos5)
        TextView pos5;
        @BindView(R.id.pos6)
        TextView pos6;
        @BindView(R.id.pos7)
        TextView pos7;
        @BindView(R.id.pos8)
        TextView pos8;
        @BindView(R.id.pos9)
        TextView pos9;
        @BindView(R.id.pos10)
        TextView pos10;
        @BindView(R.id.pos11)
        TextView pos11;
        @BindView(R.id.pos12)
        TextView pos12;
        @BindView(R.id.pos13)
        TextView pos13;
        @BindView(R.id.pos14)
        TextView pos14;
        @BindView(R.id.pos15)
        TextView pos15;
        @BindView(R.id.item)
        LinearLayout item;
        @BindView(R.id.note)
        TextView note;

        PlayerViewHolder(View view) {
            ButterKnife.bind(this, view);
        }
    }
}
