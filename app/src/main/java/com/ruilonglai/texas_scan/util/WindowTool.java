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
import com.ruilonglai.texas_scan.MainActivity;
import com.ruilonglai.texas_scan.R;
import com.ruilonglai.texas_scan.activity.LoginActivity;
import com.ruilonglai.texas_scan.entity.JsonBean;
import com.ruilonglai.texas_scan.entity.PercentType;
import com.ruilonglai.texas_scan.entity.PlayerData;
import com.ruilonglai.texas_scan.entity.QuerySelf;
import com.ruilonglai.texas_scan.entity.QueryUser;
import com.ruilonglai.texas_scan.entity.ReqData;
import com.ruilonglai.texas_scan.entity.Result;
import com.ruilonglai.texas_scan.newprocess.Connect;
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
 * Created by Administrator on 2017/8/17.
 */

public class WindowTool {

    private View view;
    private View settingView;
    private TextView openCloseView;//开启，结束,重启
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

    private volatile static WindowTool instance = null;

    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            if(msg.arg1==0){
                getUpdateSeatContents(names);
                updateNineWindow();
            }else if(msg.arg1 == 1){
                params.width = 60;
                params.height =40;
                params.x = 20;
                params.y = 270;
                 if(isClose){
                     MainProcessUtil.getInstance().exit(context);
                     boolean isphone = context.getSharedPreferences(LoginActivity.PREF_FILE, Context.MODE_PRIVATE).getBoolean("isPhone",false);
                     MainProcessUtil.getInstance().createMainProcess(AssetsCopyUtil.getPackageName(context),isphone,true);
                     openCloseView.setText("暂停");
                     isClose = false;
                 }else{
                     try {
                         Package pkg = new Package();
                         pkg.setType( Constant.SOCKET_EXIT);
                         pkg.setContent("exit");
                         MainServer.newInstance().send(pkg,context);
                         openCloseView.setText("开启");
                         isClose = true;
                         deleteWindow(false);
                         names.clear();
                     } catch (Exception e) {
                         Log.e("error","异常退出");
                         MainProcessUtil.getInstance().exit(context);
                     }
                 }
                windowManager.updateViewLayout(openCloseView,params);
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
        if (!isInit) {
            this.context = context;
            if (windowManager == null) {
                windowManager = (WindowManager) context.getApplication().getSystemService(context.getApplication().WINDOW_SERVICE);
            }
            //类型是TYPE_TOAST，像一个普通的Android Toast一样。这样就不需要申请悬浮窗权限了。
            if (params == null)
                params = new WindowManager.LayoutParams(WindowManager.LayoutParams.TYPE_TOAST);
            //初始化后不首先获得窗口焦点。不妨碍设备上其他部件的点击、触摸事件。
            params.flags =WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE;
            params.format = PixelFormat.RGBA_8888;
            params.gravity = Gravity.BOTTOM | Gravity.LEFT;
            if(nameParams == null)
                nameParams = new WindowManager.LayoutParams(WindowManager.LayoutParams.TYPE_TOAST);
            nameParams.flags =WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL;
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
    public boolean createOpenView(Activity context){//开启和关闭mian进程
        this.context = context;
        if(!isOpen){
            if(openCloseView==null){
                openCloseView = new TextView(context);
                openCloseView.setText("开启");
                openCloseView.setGravity(Gravity.CENTER);
                openCloseView.setBackgroundColor(context.getResources().getColor(R.color.hui));
                openCloseView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Message msg = new Message();
                        msg.arg1 = 1;
                        handler.sendMessage(msg);
                    }
                });
            }
            params.width = 60;
            params.height =40;
            params.x = 20;
            params.y = 270;
            windowManager.addView(openCloseView,params);
            isOpen = true;
        }else{
            return false;
        }
     return true;
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
        if (settingView == null) {
            settingView = LayoutInflater.from(context).inflate(R.layout.window_setting, null, false);
        }
         final SettingViewHolder vh = new SettingViewHolder(settingView);
         vh.seatIdx.setText(seatIdx+"号位");
         vh.name.setText("");
         vh.close.setOnClickListener(new View.OnClickListener() {
             @Override
             public void onClick(View v) {
                 String name = vh.name.getText().toString();
                 if(!TextUtils.isEmpty(name) ){
                     Package pkg = new Package();
                     pkg.setType(Constant.SOCKET_UPDATE_NAME);
                     pkg.setContent("{\"seatIdx\":"+seatIdx+",\"name\":"+name+"}");
                     MainServer.newInstance().send(pkg,context);
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
                Log.e("WindowTool",s.toString());
                String searchName = s.toString();
                List<String> list = new ArrayList<String>();
                if(!TextUtils.isEmpty(searchName)){
                    List<PlayerData> playerDatas = DataSupport.where("name like ?","%" + searchName + "%").find(PlayerData.class);
                    for (int i = 0; i < playerDatas.size(); i++) {
                        PlayerData playerData = playerDatas.get(i);
                        if(playerData!=null){
                            String name = playerData.getName();
                            if(!TextUtils.isEmpty(name) && !name.contains("self"))
                                list.add(name);
                        }
                    }
                }
                vh.nameList.setAdapter(new ArrayAdapter<String>(context,android.R.layout.simple_list_item_1,list));
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
        windowManager.addView(settingView,nameParams);
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
                    createPlayerMessage(seatIdx);
                }
            });
            seats.add(btn);
            windowManager.addView(btn, params);
        }
//        if(settingView!=null && haveSettingView){//在更新9个位置的时候同时更新修改名字的窗口
//            windowManager.removeViewImmediate(settingView);
//            haveSettingView = false;
//        }
        haveSeatsWindow = true;
    }

    public void getSeatContents(final SparseArray<String> names) {
        if (names == null || names.size() == 0)
            return;
        Gson gson = new Gson();
        ReqData data = new ReqData();
        QueryUser user = new QueryUser();
        List<String> usernames = new ArrayList<>();
        for (int i = isWatch; i < playCount + isWatch; i++) {
            String name = names.get(i);
            if (!TextUtils.isEmpty(name)) {
                if (name.contains("self")) {
                    QuerySelf queryself = new QuerySelf();
                    queryself.setUserid(userId);
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
                                    if(playerData!=null){
                                        List<PlayerData> datas = DataSupport.where("name=? and seatFlag=?", playerData.getName(),playerData.getSeatFlag()).find(PlayerData.class);
                                        if (datas.size() > 0) {
                                            DataSupport.deleteAll(PlayerData.class,"name=? and seatFlag=?", playerData.getName(),playerData.getSeatFlag());
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
        user.setUsernames(usernames);
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
                Type listType = new TypeToken<List<PlayerData>>() {
                }.getType();
                playerDatas = new Gson().fromJson(players, listType);
                if (playerDatas != null) {
                    for (int i = 0; i < playerDatas.size(); i++) {
                        PlayerData playerData = playerDatas.get(i);
                        if(playerData!=null){
                            List<PlayerData> datas = DataSupport.where("name=?", playerData.getName()).find(PlayerData.class);
                            if (datas.size() > 0) {
                                DataSupport.deleteAll(PlayerData.class,"name=?", playerData.getName());
                            }
                            playerData.save();
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
        for (int i = isWatch; i < playCount + isWatch; i++) {
            String name = names.get(i);
            if (!TextUtils.isEmpty(name)) {
                seatContents.put(i, getPlayerMessage(name));
            }
        }
    }

    /*改变显示设置的时候重新获取显示列表*/
    public void clearPercents() {
        percents = null;
    }

    public String getPlayerMessage(String name) {
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
                    sb.append(getPercent(playerData, value));
                    if (i == 2) {
                        sb.append("\n");
                    } else if (i < 5) {
                        sb.append("|");
                    }
                }
            }
        }
        return sb.toString();
    }

    /*获取相应类型的概率*/
    public String getPercent(PlayerData player, int type) {
        String percent = "－";
        int playCount = player.getPlayCount();
        switch (type) {
            case Constant.TYPE_HAND:
                if (playCount >= 1000) {
                    percent = playCount / 1000 + "K+";
                } else {
                    percent = "(" + playCount + ")";
                }
                break;
            case Constant.TYPE_VPIP:
                if (playCount != 0)
                    percent = player.getJoinCount() * 100 / playCount + "";
                break;
            case Constant.TYPE_PFR:
                if (playCount != 0)
                    percent = player.getPfrCount() * 100 / playCount + "";
                break;
            case Constant.TYPE_3BET:
                if (player.getFaceOpenCount() != 0)
                    percent = player.getBet3Count() * 100 / player.getFaceOpenCount() + "";
                break;
            case Constant.TYPE_CB:
                if (player.getLastRaiseCount() != 0)
                    percent = player.getCbCount() * 100 / player.getLastRaiseCount() + "";
                break;
            case Constant.TYPE_AF:
                if (player.getCallCount() != 0) {
                    double af = player.getRaiseCount() / Double.valueOf(player.getCallCount());
                    if (af > 10) {
                        af = af / 2;
                    }
                    percent = String.format("%.1f", af);
                }

                break;
            case Constant.TYPE_F3BET:
                if (player.getFace3BetCount() != 0)
                    percent = player.getFold3BetCount() * 100 / player.getFace3BetCount() + "";
                break;
            case Constant.TYPE_STL:
                if (player.getStlPosCount() != 0)
                    percent = player.getStlCount() * 100 / player.getStlPosCount() + "";
                break;
            case Constant.TYPE_FSTL:
                if (player.getFaceStlCount() != 0)
                    percent = player.getFoldStlCount() * 100 / player.getFaceStlCount() + "";
                break;
            case Constant.TYPE_FCB:
                if (player.getFaceCbCount() != 0)
                    percent = player.getFoldCbCount() * 100 / player.getFaceCbCount() + "";
                break;
            case Constant.TYPE_FFLOP:
                if (player.getFlopCount() != 0 && playCount != 0)
                    percent = player.getFoldFlopCount() * 100 / player.getFlopCount() + "";
                break;
            case Constant.TYPE_FTURN:
                if (player.getTurnCount() != 0 && playCount != 0)
                    percent = player.getFoldTurnCount() * 100 / player.getTurnCount() + "";
                break;
            case Constant.TYPE_FRIVER:
                if (player.getRiverCount() != 0 && playCount != 0)
                    percent = player.getFoldRiverCount() * 100 / player.getRiverCount() + "";
                break;
        }
        return percent;
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
            vh.pos1.setText(playerName + getPercent(player, Constant.TYPE_HAND));
            vh.pos2.setText(Constant.percentTypes[1] + "(" + getPercent(player, Constant.TYPE_VPIP) + "%)");
            vh.pos3.setText(Constant.percentTypes[2] + "(" + getPercent(player, Constant.TYPE_PFR) + "%)");
            vh.pos4.setText(Constant.percentTypes[3] + "(" + getPercent(player, Constant.TYPE_3BET) + "%)");
            vh.pos5.setText(Constant.percentTypes[4] + "(" + getPercent(player, Constant.TYPE_CB) + "%)");
            vh.pos6.setText(Constant.percentTypes[5] + "(" + getPercent(player, Constant.TYPE_AF) + ")");
            vh.pos7.setText(Constant.percentTypes[6] + "(" + getPercent(player, Constant.TYPE_F3BET) + "%)");
            vh.pos8.setText(Constant.percentTypes[7] + "(" + getPercent(player, Constant.TYPE_STL) + "%)");
            vh.pos9.setText(Constant.percentTypes[8] + "(" + getPercent(player, Constant.TYPE_FSTL) + "%)");
            vh.pos10.setText(Constant.percentTypes[9] + "(" + getPercent(player, Constant.TYPE_FCB) + "%)");
            vh.pos11.setText(Constant.percentTypes[10] + "(" + getPercent(player, Constant.TYPE_FFLOP) + "%)");
            vh.pos12.setText(Constant.percentTypes[11] + "(" + getPercent(player, Constant.TYPE_FTURN) + "%)");
            vh.pos13.setText(Constant.percentTypes[12] + "(" + getPercent(player, Constant.TYPE_FRIVER) + "%)");
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
                if(!haveSettingView){
                    haveSettingView =   createSettingView(seatIdx);
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
        if(close && openCloseView != null){
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
}
