package com.ruilonglai.texas_scan.util;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.NotificationCompat;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.util.SparseArray;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.ruilonglai.texas_scan.R;
import com.ruilonglai.texas_scan.activity.LoginActivity;
import com.ruilonglai.texas_scan.entity.PercentType;
import com.ruilonglai.texas_scan.entity.PlayerData;
import com.ruilonglai.texas_scan.entity.QuerySelf;
import com.ruilonglai.texas_scan.entity.QueryUser;
import com.ruilonglai.texas_scan.entity.ReqData;
import com.ruilonglai.texas_scan.entity.Result;
import com.ruilonglai.texas_scan.entity.UserName;
import com.ruilonglai.texas_scan.newprocess.MainProcessUtil;
import com.ruilonglai.texas_scan.newprocess.MainServer;
import com.ruilonglai.texas_scan.newprocess.Package;

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

import static com.ruilonglai.texas_scan.util.Constant.winPos;
import static com.ruilonglai.texas_scan.util.Constant.winPos_1080;
import static org.litepal.crud.DataSupport.where;

/**
 * Created by wgl on 2017/8/17.
 */

public class WindowTool {

    private View view;
    private View settingView;
    private View openCloseView;//开启，结束,重启
    private OpenViewHolder ovh;
    private boolean isOpen = false;
    private boolean isClose = true;
    private Intent intent1;
    private int winIndex = 0;
    private TextView textView;
    private boolean canSelect = true;
    private WindowManager windowManager = null;
    private WindowManager.LayoutParams params;
    private WindowManager.LayoutParams nameParams;
    private NotificationCompat.Builder builder;
    public boolean havePercentWindow = false;//是否创建当前手牌胜率悬浮框
    public boolean haveSeatsWindow = false;//是否创建当前位置悬浮框
    private boolean isInit = false;
    private boolean haveSettingView;
    public int playCount = 0;
    public int isWatch = 0;
    private Activity context;
    private List<TextView> seats;
    private SparseArray<String> names;
    private SparseArray<String> seatContents;
    private List<Integer> percents;
    private String userId;
    private int appCount;
    private int widthIdx;//默认是720,当widthIdx=1时,适配1080*1920
    List<Double> vpips; //当前牌桌各玩家得入池率
    List<Double> flops; //当前牌桌各玩家得翻牌率

    private volatile static WindowTool instance = null;

    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            if (msg.arg1 == 0) {
                getUpdateSeatContents(names);
                updateNineWindow();
            } else if (msg.arg1 == 1) {
                params.width = 100;
                params.height = WindowManager.LayoutParams.WRAP_CONTENT;
                params.x = 20;
                params.y = 250;
                if (isClose) {
                    MainProcessUtil.getInstance().exit(context);
                    boolean isphone = context.getSharedPreferences(LoginActivity.PREF_FILE, Context.MODE_PRIVATE).getBoolean("isPhone", false);
                    MainProcessUtil.getInstance().createMainProcess(AssetsCopyUtil.getPackageName(context), isphone, true);
                    ovh.open.setText("暂停");
                    isClose = false;
                } else {
                    try {
                        Package pkg = new Package();
                        pkg.setType(Constant.SOCKET_EXIT);
                        pkg.setContent("exit");
                        MainServer.newInstance().send(pkg, context);
                        ovh.open.setText("开启");
                        ovh.avgVpip.setText("");
                        isClose = true;
                        deleteWindow(false);
                        names.clear();
                        vpips.clear();
                        flops.clear();
                    } catch (Exception e) {
                        Log.e("error", "异常退出");
                        MainProcessUtil.getInstance().exit(context);
                    }
                }
                windowManager.updateViewLayout(openCloseView, params);
            }

        }
    };

    private WindowTool() {
        seats = new ArrayList<>();
        seatContents = new SparseArray<>();
    }

    public static WindowTool getInstance() {
        if (instance == null) {
            synchronized (WindowTool.class) {
                if (instance == null) {
                    instance = new WindowTool();
                }
            }
        }
        return instance;
    }

    public void init(Activity context, int winIndex, String userId) {
        this.userId = userId;
        Log.e("isInit", isInit + "");
        this.winIndex = winIndex;
        if (vpips == null)
            vpips = new ArrayList<>();
        if(flops == null)
            flops = new ArrayList<>();
        if (!isInit) {
            this.context = context;
            if (windowManager == null) {
                windowManager = (WindowManager) context.getApplication().getSystemService(context.getApplication().WINDOW_SERVICE);
            }
            //类型是TYPE_TOAST，像一个普通的Android Toast一样。这样就不需要申请悬浮窗权限了。
            if (params == null)
                params = new WindowManager.LayoutParams(WindowManager.LayoutParams.TYPE_TOAST);
            //初始化后不首先获得窗口焦点。不妨碍设备上其他部件的点击、触摸事件。
            params.flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE;
            params.format = PixelFormat.RGBA_8888;
            params.gravity = Gravity.BOTTOM | Gravity.LEFT;
            if (nameParams == null)
                nameParams = new WindowManager.LayoutParams(WindowManager.LayoutParams.TYPE_TOAST);
            nameParams.flags = WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL;
            nameParams.height = WindowManager.LayoutParams.WRAP_CONTENT;
            nameParams.width = 200;
            nameParams.x = 0;
            nameParams.y = 0;
            nameParams.format = PixelFormat.RGBA_8888;
            nameParams.gravity = Gravity.CENTER;
            if (textView == null)
                textView = new TextView(context);
            isInit = true;
            int width = context.getSharedPreferences(LoginActivity.PREF_FILE, Context.MODE_PRIVATE).getInt("width", 720);
            if (width == 720) {
                widthIdx = 0;
            } else if (width == 1080) {
                widthIdx = 1;
            }
        }
    }

    public boolean createOpenView(Activity context) {//开启和关闭mian进程
        this.context = context;
        if (!isOpen) {
            if (openCloseView == null) {
                openCloseView = LayoutInflater.from(context).inflate(R.layout.window_open, null, false);
                ovh = new OpenViewHolder(openCloseView);
                ovh.open.setText("开启");
                ovh.avgVpip.setText("");
                openCloseView.setBackgroundColor(context.getResources().getColor(R.color.hui));
                ovh.open.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Message msg = new Message();
                        msg.arg1 = 1;
                        handler.sendMessage(msg);
                    }
                });
            }
            params.width = 100;
            params.height = WindowManager.LayoutParams.WRAP_CONTENT;
            params.x = 20;
            params.y = 250;
            windowManager.addView(openCloseView, params);
            isOpen = true;
        } else {
            return false;
        }
        return true;
    }
    /*更新平均入池率*/
    public void updateOpenViewVPIP(){
        if(openCloseView!=null && ovh != null){
            params.width = 100;
            params.height = WindowManager.LayoutParams.WRAP_CONTENT;
            params.x = 20;
            params.y = 250;
            ovh.avgVpip.setText(getAvgVpip());
            windowManager.updateViewLayout(openCloseView,params);
        }
    }
    public boolean createWindow(String percent) {
        if (TextUtils.isEmpty(percent)) {
            return false;
        }
        if (textView == null)
            textView = new TextView(context);
        textView.setGravity(Gravity.CENTER);
        textView.setBackgroundColor(Color.WHITE);
        textView.setText(percent);
        textView.setTextColor(Color.RED);
        if (widthIdx == 0) {
            params.width = winPos[winIndex][0][0][2];
            params.height = winPos[winIndex][0][0][3];
            params.x = winPos[winIndex][0][0][0];
            params.y = winPos[winIndex][0][0][1];
            textView.setTextSize(16);
        } else if (widthIdx == 1) {
            params.width = winPos_1080[winIndex][0][0][2];
            params.height = winPos_1080[winIndex][0][0][3];
            params.x = winPos_1080[winIndex][0][0][0];
            params.y = winPos_1080[winIndex][0][0][1];
            textView.setTextSize(8);
        }
        textView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });
        windowManager.addView(textView, params);
        havePercentWindow = true;
        return true;
    }

    public boolean createSettingView(final int seatIdx) {
        if(haveSettingView){
            return false;
        }
        if (settingView == null) {
            settingView = LayoutInflater.from(context).inflate(R.layout.window_setting, null, false);
        }
        final SettingViewHolder vh = new SettingViewHolder(settingView);
        vh.seatIdx.setText(seatIdx + "号位");
        vh.name.setText("");
        vh.close.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String name = vh.name.getText().toString();
                if (!TextUtils.isEmpty(name)) {
                    Package pkg = new Package();
                    pkg.setType(Constant.SOCKET_UPDATE_NAME);
                    pkg.setContent("{\"seatIdx\":" + seatIdx + ",\"name\":" + name + "}");
                    MainServer.newInstance().send(pkg, context);
                }
                windowManager.removeViewImmediate(settingView);
                haveSettingView = false;
            }
        });
        vh.nameList.setAdapter(null);
        vh.name.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                Log.e("WindowTool", s.toString());
                String searchName = s.toString();
                List<String> list = new ArrayList<String>();
                if (!TextUtils.isEmpty(searchName)) {
                    List<UserName> userNames = DataSupport.where("name like ?", "%" + searchName + "%").find(UserName.class);
                    for (int i = 0; i < userNames.size(); i++) {
                        UserName userName = userNames.get(i);
                        if (userName != null) {
                            String name = userName.name;
                            if (!TextUtils.isEmpty(name) && !name.contains("self"))
                                list.add(name);
                        }
                    }
                }
                vh.nameList.setAdapter(new ArrayAdapter<String>(context, android.R.layout.simple_list_item_1, list));
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
        vh.nameList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String name = vh.nameList.getItemAtPosition(position).toString();
                vh.name.setText(name);
            }
        });
        windowManager.addView(settingView, nameParams);
        return true;
    }

    public boolean createNinePointWindow(int appCount, int playCount, SparseArray<String> seatNames, int isWatch) {
        this.isWatch = isWatch;
        this.appCount = appCount;
        if (playCount > 0) {
            this.playCount = playCount;
        }
        if (seatNames != null) {
            this.names = seatNames;
            getSeatContents(names);
        }
        return true;
    }

    public void updateNineWindow() {
        int arr3Idx = 0;
        if (playCount == 9) {
            arr3Idx = 0;
        } else if (playCount == 8) {
            arr3Idx = 1;
        } else if (playCount == 7) {
            arr3Idx = 2;
        } else if (playCount == 6) {
            arr3Idx = 3;
        } else if (playCount == 2) {
            arr3Idx = 4;
        }
        if (haveSeatsWindow && windowManager != null) {
            for (int j = 0; j < seats.size(); j++) {
                TextView text = seats.get(j);
                windowManager.removeViewImmediate(text);
            }
            haveSeatsWindow = false;
        }
        seats.clear();
        for (int j = 2 + isWatch; j < playCount + 2 + isWatch; j++) {
            TextView btn = new TextView(context);
            btn.setTag(j - 2);
            if (names != null && !TextUtils.isEmpty(names.get(j - 2))) {
                btn.setText(seatContents.get(j - 2));
                if (widthIdx == 0) {
                    params.width = WindowManager.LayoutParams.WRAP_CONTENT;
                    params.height = winPos[appCount][arr3Idx][j][3];
                    params.x = winPos[appCount][arr3Idx][j][0];
                    params.y = winPos[appCount][arr3Idx][j][1];
                    btn.setTextSize(16);
                } else if (widthIdx == 1) {
                    btn.setTextSize(8);
                    params.width = WindowManager.LayoutParams.WRAP_CONTENT;
                    params.height = winPos_1080[appCount][arr3Idx][j][3];
                    params.x = winPos_1080[appCount][arr3Idx][j][0];
                    params.y = winPos_1080[appCount][arr3Idx][j][1];
                }
            } else {
                btn.setText("－|－|－\n－|－|－");
                if (widthIdx == 0) {
                    params.width = WindowManager.LayoutParams.WRAP_CONTENT;
                    params.height = winPos[appCount][arr3Idx][j][3];
                    params.x = winPos[appCount][arr3Idx][j][0];
                    params.y = winPos[appCount][arr3Idx][j][1];
                    btn.setTextSize(16);
                } else if (widthIdx == 1) {
                    btn.setTextSize(8);
                    params.width = WindowManager.LayoutParams.WRAP_CONTENT;
                    params.height = winPos_1080[appCount][arr3Idx][j][3];
                    params.x = winPos_1080[appCount][arr3Idx][j][0];
                    params.y = winPos_1080[appCount][arr3Idx][j][1];
                }
            }
            btn.setGravity(Gravity.CENTER);
            btn.setTextColor(Color.WHITE);
            btn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int seatIdx = (int) v.getTag();
                    String name = names.get(seatIdx);
                    if(!TextUtils.isEmpty(name)){
                        createPlayerMessage(seatIdx);
                    }else{
                        if (!haveSettingView) {
                            haveSettingView = createSettingView(seatIdx);
                        }
                    }
                }
            });
            seats.add(btn);
            windowManager.addView(btn, params);
        }
        haveSeatsWindow = true;
    }

    public String getAvgVpip() {//获取平均入池率
        double sum = 0.0;
        double sumFlop = 0.0;
        String str = "";
        if (vpips.size() == 0){
            str += "0.0";
        }else{
            for (int i = 0; i < vpips.size(); i++) {
                Double aDouble = vpips.get(i);
                sum += aDouble;
            }
            String avgVpip= String.format("%.1f", sum / vpips.size());
            if(Double.valueOf(avgVpip)>100)
                avgVpip = "100.0";
            str += avgVpip;
        }
        
        if (flops.size() == 0){
            str += "|0.0";
        }else{
            for (int i = 0; i < flops.size(); i++) {
                Double aDouble = flops.get(i);
                sumFlop += aDouble;
            }
            String avgFlop = String.format("%.1f", sumFlop / flops.size());
            if(Double.valueOf(avgFlop)>100)
                avgFlop = "100.0";
            str += "|"+avgFlop;
        }
        return str;
    }
    public void getSeatContents(final SparseArray<String> names) {
        if (names == null || names.size() == 0)
            return;
        Gson gson = new Gson();
        ReqData data = new ReqData();
        final List<String> usernames = new ArrayList<>();
        for (int i = isWatch; i < playCount + isWatch; i++) {
            String name = names.get(i);
            if (!TextUtils.isEmpty(name)) {
                if (name.contains("self")) {
                    QuerySelf queryself = new QuerySelf();
                    queryself.setUserid(userId);
                    queryself.setPlatType(winIndex);
                    String param = gson.toJson(queryself);
                    data.setParam(param);
                    data.setReqno(TimeUtil.getCurrentDateToMinutes(new Date()) + ActionsTool.disposeNumber());
                    data.setReqid(context.getSharedPreferences(LoginActivity.PREF_FILE, Context.MODE_PRIVATE).getString("name", ""));
                    HttpUtil.sendPostRequestData("queryself", gson.toJson(data), new Callback() {
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
                            String players = map.get("listself");
                            List<PlayerData> playerDatas = new ArrayList<PlayerData>();
                            Type listType = new TypeToken<List<PlayerData>>() {
                            }.getType();
                            playerDatas = new Gson().fromJson(players, listType);
                            if (playerDatas != null) {
                                for (int i = 0; i < playerDatas.size(); i++) {
                                    PlayerData playerData = playerDatas.get(i);
                                    if (playerData != null) {
                                        List<PlayerData> datas = DataSupport.where("name=? and seatFlag=?", playerData.getName(), playerData.getSeatFlag()).find(PlayerData.class);
                                        if (datas.size() > 0) {
                                            DataSupport.deleteAll(PlayerData.class, "name=? and seatFlag=?", playerData.getName(), playerData.getSeatFlag());
                                        }
                                        playerData.save();
                                    }
                                }
                            }
                        }
                    });
                } else {
                    usernames.add(name);
                }
            }
        }
        QueryUser user = new QueryUser();
        user.setUsernames(usernames);
        user.setPlatType(winIndex);
        data.setParam(gson.toJson(user));
        data.setReqno(TimeUtil.getCurrentDateToMinutes(new Date()) + ActionsTool.disposeNumber());
        data.setReqid(context.getSharedPreferences(LoginActivity.PREF_FILE, Context.MODE_PRIVATE).getString("name", ""));
        HttpUtil.sendPostRequestData("queryuser", gson.toJson(data), new Callback() {
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
                            List<UserName> userNames1 = DataSupport.where("name=?", playerData.getName()).find(UserName.class);
                            if(userNames1.size()==0){
                                UserName un = new UserName();
                                un.name = playerData.getName();
                                un.save();
                            }
                        }
                    }
                }
                Message msg = new Message();
                msg.arg1 = 0;
                handler.sendMessage(msg);
            }
        });
    }

    public void getUpdateSeatContents(SparseArray<String> names) {
        if (names == null || names.size() == 0)
            return;
        vpips.clear();
        flops.clear();
        for (int i = isWatch; i < playCount + isWatch; i++) {
            String name = names.get(i);
            if (!TextUtils.isEmpty(name)) {
                seatContents.put(i, getPlayerMessage(name));
            }
        }
        updateOpenViewVPIP();
    }

    /*改变显示设置的时候重新获取显示列表*/
    public void clearPercents() {
        percents = null;
    }

    public String getPlayerMessage(String name) {
        double vpip = 0;
        if (TextUtils.isEmpty(name))
            return "";
        StringBuilder sb = new StringBuilder();
        if (name.equals("self")) {
            name = "self";
        }
        if (percents == null) {
            String json = context.getSharedPreferences(LoginActivity.PREF_FILE, Context.MODE_PRIVATE).getString("percentTypeArray", "");
            if (TextUtils.isEmpty(json)) {
                percents = new ArrayList<>();
                for (int i = 0; i < 6; i++) {
                    percents.add(i);
                }
            } else {
                percents = GsonUtil.parseJsonWithGson(json, PercentType.class).getPercents();
            }
        }
        List<PlayerData> playerDatas = DataSupport.where("name=?", name).find(PlayerData.class);
        if (playerDatas.size() == 0 || "E".equals(name) || "玲".equals(name) || "玟".equals(name) || "C".equals(name) || "c".equals(name)
                || "5".equals(name) || "2".equals(name) || "河".equals(name) || "招".equals(name) || "[".equals(name)) {
            sb.append("－|－|－\n－|－|－");
        } else {
            PlayerData playerData = null;
            if (name.contains("self")) {
                playerData = getSelfPlayerData();
            } else {
                playerData = playerDatas.get(0);
            }
            int playCount = playerData.getPlayCount();
            if (playCount == 0) {
                sb.append("－|－|－\n－|－|－");
            } else {
                for (int i = 0; i < percents.size(); i++) {
                    int value = percents.get(i).intValue();
                    sb.append(Constant.getPercent(playerData, value));
                    if (i == 2) {
                        sb.append("\n");
                    } else if (i < 5) {
                        sb.append("|");
                    }
                }
                vpips.add(Double.valueOf(Constant.getPercent(playerData, Constant.TYPE_VPIP)));
                flops.add(Double.valueOf(playerData.getFlopCount()*100/playCount));
            }
        }
        return sb.toString();
    }

    //个人的总类
    public PlayerData getSelfPlayerData() {
        List<PlayerData> dataList = where("name=?", "self").find(PlayerData.class);
        PlayerData player = new PlayerData();
        for (int i = 0; i < dataList.size(); i++) {
            PlayerData playerData = dataList.get(i);
            player.setBbCount(player.getBbCount() + playerData.getBbCount());
            player.setBet3Count(player.getBet3Count() + playerData.getBet3Count());
            player.setCallCount(playerData.getCallCount() + player.getCallCount());
            player.setPlayCount(playerData.getPlayCount() + player.getPlayCount());
            player.setFace3BetCount(playerData.getFace3BetCount() + player.getFace3BetCount());
            player.setFaceOpenCount(playerData.getFaceOpenCount() + player.getFaceOpenCount());
            player.setWinCount(playerData.getWinCount() + player.getWinCount());
            player.setLoseCount(playerData.getLoseCount() + player.getLoseCount());
            player.setFold3BetCount(playerData.getFold3BetCount() + player.getFold3BetCount());
            player.setFoldStlCount(playerData.getFoldStlCount() + player.getFoldStlCount());
            player.setJoinCount(playerData.getJoinCount() + player.getJoinCount());
            player.setRaiseCount(playerData.getRaiseCount() + player.getRaiseCount());
            player.setLastRaiseCount(playerData.getLastRaiseCount() + player.getLastRaiseCount());
            player.setCbCount(playerData.getCbCount() + player.getCbCount());
            player.setPfrCount(playerData.getPfrCount() + player.getPfrCount());
            player.setStlPosCount(playerData.getStlPosCount() + player.getStlPosCount());
            player.setFaceStlCount(playerData.getFaceStlCount() + player.getFaceStlCount());
            player.setStlCount(playerData.getStlCount() + player.getStlCount());
            player.setFaceCbCount(playerData.getFaceCbCount() + player.getFaceCbCount());
            player.setFoldCbCount(playerData.getFoldCbCount() + player.getFoldCbCount());
            player.setFlopCount(playerData.getFlopCount() + player.getFlopCount());
            player.setFoldFlopCount(playerData.getFoldFlopCount() + player.getFoldFlopCount());
            player.setTurnCount(playerData.getTurnCount() + player.getTurnCount());
            player.setFoldTurnCount(playerData.getFoldTurnCount() + player.getFoldTurnCount());
            player.setRiverCount(playerData.getRiverCount() + player.getRiverCount());
            player.setFoldRiverCount(playerData.getFoldRiverCount() + player.getFoldRiverCount());
        }
        return player;
    }

    Handler handlerClose = new Handler();
    Runnable callBack = new Runnable() {
        @Override
        public void run() {
            if (!canSelect) {
                windowManager.removeViewImmediate(view);
            }
            canSelect = true;
        }
    };

    /*玩家得详细信息*/
    public void createPlayerMessage(final int seatIdx) {
        if (!canSelect) {
            windowManager.removeViewImmediate(view);
            handlerClose.removeCallbacks(callBack);
            canSelect = true;
            return;
        }
        canSelect = false;
        String playerName = "";
        if (names != null && !TextUtils.isEmpty(names.get(seatIdx))) {
            playerName = names.get(seatIdx);
        }
        if (view == null) {
            view = LayoutInflater.from(context).inflate(R.layout.layout_window, null, false);
        }
        if (widthIdx == 1) {
            params.width = WindowManager.LayoutParams.WRAP_CONTENT;
            params.height = winPos_1080[winIndex][0][1][3];
            params.x = winPos_1080[winIndex][0][1][0];
            params.y = winPos_1080[winIndex][0][1][1];
        } else if (widthIdx == 0) {
            params.width = winPos[winIndex][0][1][2];
            params.height = winPos[winIndex][0][1][3];
            params.x = winPos[winIndex][0][1][0];
            params.y = winPos[winIndex][0][1][1];
        }
        PlayerData player = null;
        ViewHolder vh = new ViewHolder(view);
        if (!TextUtils.isEmpty(playerName) || seatIdx == 0) {
            if (playerName.equals("self") || seatIdx == 0) {
                List<PlayerData> playerDatas = where("name=?", "self").find(PlayerData.class);
                player = getSelfPlayerData();
            } else {
                if ("E".equals(playerName) || "玲".equals(playerName) || "玟".equals(playerName) || "C".equals(playerName) || "c".equals(playerName)
                        || "5".equals(playerName) || "2".equals(playerName) || "河".equals(playerName) || "招".equals(playerName) || "[".equals(playerName)) {
                    player = new PlayerData();
                } else {
                    List<PlayerData> playerDatas = where("name=?", playerName).find(PlayerData.class);
                    if (playerDatas.size() > 0) {
                        player = playerDatas.get(0);
                    }
                }
            }
        }
        if (widthIdx == 1) {
            vh.pos1.setTextSize(8);
            vh.pos2.setTextSize(8);
            vh.pos3.setTextSize(8);
            vh.pos4.setTextSize(8);
            vh.pos5.setTextSize(8);
            vh.pos6.setTextSize(8);
            vh.pos7.setTextSize(8);
            vh.pos8.setTextSize(8);
            vh.pos9.setTextSize(8);
            vh.pos10.setTextSize(8);
            vh.pos11.setTextSize(8);
            vh.pos12.setTextSize(8);
            vh.pos13.setTextSize(8);
        }
        if (player != null) {
            vh.pos1.setText(playerName + Constant.getPercent(player, Constant.TYPE_HAND));
            vh.pos2.setText(Constant.percentTypes[1] + "(" + Constant.getPercent(player, Constant.TYPE_VPIP) + "%)");
            vh.pos3.setText(Constant.percentTypes[2] + "(" + Constant.getPercent(player, Constant.TYPE_PFR) + "%)");
            vh.pos4.setText(Constant.percentTypes[3] + "(" + Constant.getPercent(player, Constant.TYPE_3BET) + "%)");
            vh.pos5.setText(Constant.percentTypes[4] + "(" + Constant.getPercent(player, Constant.TYPE_CB) + "%)");
            vh.pos6.setText(Constant.percentTypes[5] + "(" + Constant.getPercent(player, Constant.TYPE_AF) + ")");
            vh.pos7.setText(Constant.percentTypes[6] + "(" + Constant.getPercent(player, Constant.TYPE_F3BET) + "%)");
            vh.pos8.setText(Constant.percentTypes[7] + "(" + Constant.getPercent(player, Constant.TYPE_STL) + "%)");
            vh.pos9.setText(Constant.percentTypes[8] + "(" + Constant.getPercent(player, Constant.TYPE_FSTL) + "%)");
            vh.pos10.setText(Constant.percentTypes[9] + "(" + Constant.getPercent(player, Constant.TYPE_FCB) + "%)");
            vh.pos11.setText(Constant.percentTypes[10] + "(" + Constant.getPercent(player, Constant.TYPE_FFLOP) + "%)");
            vh.pos12.setText(Constant.percentTypes[11] + "(" + Constant.getPercent(player, Constant.TYPE_FTURN) + "%)");
            vh.pos13.setText(Constant.percentTypes[12] + "(" + Constant.getPercent(player, Constant.TYPE_FRIVER) + "%)");
        } else {
            vh.pos1.setText(playerName + "(-)");
            vh.pos2.setText(Constant.percentTypes[1] + "(-%)");
            vh.pos3.setText(Constant.percentTypes[2] + "(-%)");
            vh.pos4.setText(Constant.percentTypes[3] + "(-%)");
            vh.pos5.setText(Constant.percentTypes[4] + "(-%)");
            vh.pos6.setText(Constant.percentTypes[5] + "(-)");
            vh.pos7.setText(Constant.percentTypes[6] + "(-%)");
            vh.pos8.setText(Constant.percentTypes[7] + "(-%)");
            vh.pos9.setText(Constant.percentTypes[8] + "(-%)");
            vh.pos10.setText(Constant.percentTypes[9] + "(-%)");
            vh.pos11.setText(Constant.percentTypes[10] + "(-%)");
            vh.pos12.setText(Constant.percentTypes[11] + "(-%)");
            vh.pos13.setText(Constant.percentTypes[12] + "(-%)");
        }
        vh.item.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!haveSettingView) {
                    haveSettingView = createSettingView(seatIdx);
                }
            }
        });
        windowManager.addView(view, params);
        handlerClose.postDelayed(callBack, 5000);
    }

    public void updateWindow(String percent) {
        if (textView == null) {
            createWindow(percent);
        } else {
            params.flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE;
            params.format = PixelFormat.RGBA_8888;
            params.width = winPos[winIndex][0][0][2];
            params.height = winPos[winIndex][0][0][3];
            params.gravity = Gravity.BOTTOM | Gravity.LEFT;
            params.x = winPos[winIndex][0][0][0];
            params.y = winPos[winIndex][0][0][1];
            textView.setText(percent);
            windowManager.updateViewLayout(textView, params);
        }
    }

    public void deleteWindow(boolean close) {
        if (havePercentWindow && windowManager != null && textView != null) {
            windowManager.removeViewImmediate(textView);//删除手牌胜率悬浮框
            havePercentWindow = false;
            textView = null;
        }
        if (haveSeatsWindow && windowManager != null) {
            for (int j = 0; j < seats.size(); j++) {
                TextView text = seats.get(j);
                windowManager.removeViewImmediate(text);
            }
            seats.clear();
            haveSeatsWindow = false;
        }
        if (close && openCloseView != null) {
            windowManager.removeViewImmediate(openCloseView);
            openCloseView = null;
            isOpen = false;
        }
    }

    static class ViewHolder {
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

        ViewHolder(View view) {
            ButterKnife.bind(this, view);
        }
    }

    class SettingViewHolder {
        @BindView(R.id.close)
        Button close;
        @BindView(R.id.seatIdx)
        TextView seatIdx;
        @BindView(R.id.name)
        EditText name;
        @BindView(R.id.nameList)
        ListView nameList;

        SettingViewHolder(View view) {
            ButterKnife.bind(this, view);
        }
    }

    static class OpenViewHolder {
        @BindView(R.id.open)
        TextView open;
        @BindView(R.id.avgVpip)
        TextView avgVpip;

        OpenViewHolder(View view) {
            ButterKnife.bind(this, view);
        }
    }
}
