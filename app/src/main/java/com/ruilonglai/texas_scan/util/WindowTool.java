package com.ruilonglai.texas_scan.util;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.os.Handler;
import android.support.v4.app.NotificationCompat;
import android.text.TextUtils;
import android.util.Log;
import android.util.SparseArray;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.gson.Gson;
import com.ruilonglai.texas_scan.R;
import com.ruilonglai.texas_scan.activity.LoginActivity;
import com.ruilonglai.texas_scan.entity.PercentType;
import com.ruilonglai.texas_scan.entity.PlayerData;
import com.ruilonglai.texas_scan.entity.QuerySelf;
import com.ruilonglai.texas_scan.entity.QueryUser;
import com.ruilonglai.texas_scan.entity.ReqData;

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

import static com.ruilonglai.texas_scan.util.Constant.winPos;
import static org.litepal.crud.DataSupport.where;

/**
 * Created by Administrator on 2017/8/17.
 */

public class WindowTool {

    private View view;
    private Intent intent1;
    private int winIndex = 0;
    private TextView textView;
    private boolean canSelect = true;
    private WindowManager windowManager = null;
    private WindowManager.LayoutParams params;
    private NotificationCompat.Builder builder;
    public boolean havePercentWindow = false;//是否创建当前手牌胜率悬浮框
    public boolean haveSeatsWindow = false;//是否创建当前位置悬浮框
    private boolean isInit = false;
    public int playCount = 0;
    public int isWatch = 0;
    private Activity context;
    private List<TextView> seats;
    private SparseArray<String> names;
    private SparseArray<String> seatContents;
    private List<Integer> percents;
    private String userId;

    private volatile static WindowTool instance = null;

    private WindowTool(){
        seats = new ArrayList<>();
        seatContents = new SparseArray<>();
    }

    public static WindowTool getInstance(){
        if(instance == null){
            synchronized (WindowTool.class){
                if(instance == null){
                    instance = new WindowTool();
                }
            }
        }
        return instance;
    }

    public void init(Activity context,int winIndex,String userId){
        this.userId = userId;
        Log.e("isInit",isInit+"");
        this.winIndex = winIndex;
        if(!isInit){
            this.context = context;
            if(windowManager==null){
                windowManager = (WindowManager) context.getApplication().getSystemService(context.getApplication().WINDOW_SERVICE);
            }
            //类型是TYPE_TOAST，像一个普通的Android Toast一样。这样就不需要申请悬浮窗权限了。
            if(params==null)
            params = new WindowManager.LayoutParams(WindowManager.LayoutParams.TYPE_TOAST);
            //初始化后不首先获得窗口焦点。不妨碍设备上其他部件的点击、触摸事件。
            params.flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE;
            params.format = PixelFormat.RGBA_8888;
            params.gravity = Gravity.BOTTOM | Gravity.LEFT;
            if(textView == null)
            textView = new TextView(context);
            isInit = true;
        }
    }
    public boolean createWindow(String percent) {
        if (TextUtils.isEmpty(percent)) {
            return false;
        }
        if(textView == null)
        textView = new TextView(context);
        textView.setGravity(Gravity.CENTER);
        textView.setBackgroundColor(Color.WHITE);
        textView.setText(percent);
        textView.setTextSize(TypedValue.COMPLEX_UNIT_SP,18);
        textView.setTextColor(Color.RED);

        params.width = winPos[winIndex][0][0][2];
        params.height = winPos[winIndex][0][0][3];
        params.x = winPos[winIndex][0][0][0];
        params.y = winPos[winIndex][0][0][1];
        textView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });
        windowManager.addView(textView, params);
        havePercentWindow = true;
        return true;
    }

    public boolean createNinePointWindow(int appCount, int playCount,SparseArray<String> seatNames,int isWatch) {
        this.isWatch = isWatch;
        if(playCount>0){
            this.playCount = playCount;
        }
        if(seatNames !=null){
            this.names = seatNames;
            getSeatContents(names);
        }
        int arr3Idx = 0;
        if (playCount == 9) {
            arr3Idx = 0;
        } else if (playCount == 8) {
            arr3Idx = 1;
        } else if (playCount == 7) {
            arr3Idx = 2;
        }else if(playCount == 6){
            arr3Idx = 3;
        }else if(playCount == 2){
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
        for (int j = 2+isWatch; j < playCount + 2+isWatch; j++) {
            TextView btn = new TextView(context);
            btn.setTag(j - 2);
            if(names!=null && !TextUtils.isEmpty(names.get(j-2))){
                btn.setText(seatContents.get(j-2));
                params.width = winPos[appCount][arr3Idx][j][2];
                params.height = winPos[appCount][arr3Idx][j][3];
                params.x = winPos[appCount][arr3Idx][j][0];
                params.y = winPos[appCount][arr3Idx][j][1];
            }else{
                btn.setText("");
                params.width = WindowManager.LayoutParams.WRAP_CONTENT;
                params.height =  WindowManager.LayoutParams.WRAP_CONTENT;
                params.x =  WindowManager.LayoutParams.WRAP_CONTENT;
                params.y =  WindowManager.LayoutParams.WRAP_CONTENT;
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
        haveSeatsWindow = true;
        return true;
    }
    public void updateNineWindow(int appCount, int playCount,SparseArray<String> seatNames){
         if(names==null)
             return;
        if(seats.size()==0){
            createNinePointWindow(appCount,playCount,seatNames,isWatch);
        }else{
            int arr3Idx = 0;
            if (playCount == 9) {
                arr3Idx = 0;
            } else if (playCount == 8) {
                arr3Idx = 1;
            }else if(playCount == 6){
                arr3Idx = 2;
            }
            getSeatContents(names);
            for (int i = 2; i < playCount+2; i++) {
                String name = names.get(i-2);
                if(i-2==seats.size()){
                    continue;
                }
                TextView tv = seats.get(i-2);
                tv.setGravity(Gravity.CENTER);
                if(!TextUtils.isEmpty(name)){
                    params.width = winPos[appCount][arr3Idx][i][2];
                    params.height = winPos[appCount][arr3Idx][i][3];
                    params.x = winPos[appCount][arr3Idx][i][0];
                    params.y = winPos[appCount][arr3Idx][i][1];
                    String text = seatContents.get(i - 2);
                    Log.e("WindowTool","nineupdate "+text);
                    tv.setText(text);
                }else{
                    params.width = WindowManager.LayoutParams.WRAP_CONTENT;
                    params.height =  WindowManager.LayoutParams.WRAP_CONTENT;
                    params.x =  WindowManager.LayoutParams.WRAP_CONTENT;
                    params.y =  WindowManager.LayoutParams.WRAP_CONTENT;
                    tv.setText("");
                }
                windowManager.updateViewLayout(tv,params);
            }
        }
    }

    public void getSeatContents(SparseArray<String> names){
        if(names==null || names.size()==0)
            return;
        Gson gson = new Gson();
        ReqData data = new ReqData();
        QueryUser user = new QueryUser();
        List<String> usernames = new ArrayList<>();
        for (int i = isWatch; i < playCount+isWatch; i++) {
            String name = names.get(i);
            if(!TextUtils.isEmpty(name)){
                seatContents.put(i,getPlayerMessage(name));
                if(name.contains("self")){
                    QuerySelf queryself = new QuerySelf();
                    queryself.setUserid(userId);
                    String param = gson.toJson(queryself);
                    data.setParam(param);
                    data.setReqno(TimeUtil.getCurrentDateToMinutes(new Date())+ActionsTool.disposeNumber());
                    data.setReqid(context.getSharedPreferences(LoginActivity.PREF_FILE, Context.MODE_PRIVATE).getString("name", ""));
                    HttpUtil.sendPostRequestData("queryself", gson.toJson(data), new Callback() {
                        @Override
                        public void onFailure(Call call, IOException e) {
                            Log.e("WindowTool","response:(error)"+e.toString());
                        }

                        @Override
                        public void onResponse(Call call, Response response) throws IOException {
                            Log.e("WindowTool","response:"+response.body().string());
                        }
                    });
                }else{
                    usernames.add(name);
                }
            }
        }
        user.setUsernames(usernames);
        data.setParam(gson.toJson(user));
        data.setReqno(TimeUtil.getCurrentDateToMinutes(new Date())+ActionsTool.disposeNumber());
        data.setReqid(context.getSharedPreferences(LoginActivity.PREF_FILE, Context.MODE_PRIVATE).getString("name", ""));
        HttpUtil.sendPostRequestData("queryuser", gson.toJson(data), new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.e("WindowTool","response:(error)"+e.toString());
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                Log.e("WindowTool","response:"+response.body().string());
            }
        });
    }
    /*改变显示设置的时候重新获取显示列表*/
    public void clearPercents(){
        percents = null;
    }
    public String getPlayerMessage(String name){
        if(TextUtils.isEmpty(name))
            return "";
        StringBuilder sb = new StringBuilder();
        if(name.equals("self")){
            name = "_self";
        }
        if(percents==null){
            String json = context.getSharedPreferences(LoginActivity.PREF_FILE, Context.MODE_PRIVATE).getString("percentTypeArray", "");
            if(TextUtils.isEmpty(json)){
                percents = new ArrayList<>();
                for (int i = 0; i < 6; i++) {
                    percents.add(i);
                }
            }else{
                percents = GsonUtil.parseJsonWithGson(json, PercentType.class).getPercents();
            }
        }
        List<PlayerData> playerDatas = DataSupport.where("name=?",name).find(PlayerData.class);
        if(playerDatas.size()==0){
            sb.append("－|－|－\n－|－|－");
        }else{
            PlayerData playerData = null;
            if(name.contains("self")){
                playerData = getSelfPlayerData();
            }else{
                playerData = playerDatas.get(0);
            }
            int playCount = playerData.getPlayCount();
            if(playCount == 0){
                sb.append("－|－|－\n－|－|－");
            }else{
                for (int i = 0; i < percents.size(); i++) {
                    int value = percents.get(i).intValue();
                    sb.append(getPercent(playerData,value));
                    if(i==2){
                        sb.append("\n");
                    }else if(i<5){
                        sb.append("|");
                    }
                }
            }
        }
        return sb.toString();
    }
    /*获取相应类型的概率*/
    public String getPercent(PlayerData player,int type){
        String percent = "－";
        int playCount = player.getPlayCount();
        switch (type){
            case Constant.TYPE_HAND:
                if(playCount>=1000){
                    percent = playCount/1000+"K+";
                }else{
                    percent = "("+ playCount +")";
                }
                break;
            case Constant.TYPE_VPIP:
                if(playCount!=0)
                    percent = player.getJoinCount()*100/playCount+"";
                break;
            case Constant.TYPE_PFR:
                if(playCount!=0)
                    percent = player.getPfrCount()*100/playCount+"";
                break;
            case Constant.TYPE_3BET:
                if(player.getFaceOpenCount()!=0)
                    percent = player.getBet3Count()*100/player.getFaceOpenCount()+"";
                break;
            case Constant.TYPE_CB:
                if(player.getLastRaiseCount()!=0)
                    percent = player.getCbCount()*100/player.getLastRaiseCount()+"";
                break;
            case Constant.TYPE_AF:
                if(player.getCallCount()!=0)
                    percent = String.format("%.1f",player.getRaiseCount()/Double.valueOf(player.getCallCount()));
                break;
            case Constant.TYPE_F3BET:
                if(player.getFace3BetCount()!=0)
                    percent = player.getFold3BetCount()*100/player.getFace3BetCount()+"";
                break;
            case Constant.TYPE_STL:
                if(player.getStlPosCount()!=0)
                    percent = player.getStlCount()*100/player.getStlPosCount()+"";
                break;
            case Constant.TYPE_FSTL:
                if(player.getFaceStlCount()!=0)
                    percent = player.getFaceStlCount()*100/player.getFaceStlCount()+"";
                break;
        }
        return percent;
    }
    //个人的总类
    public PlayerData getSelfPlayerData(){
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
        List<PlayerData> dataList = where("name=?", "_self").find(PlayerData.class);
        for (int i = 0; i < dataList.size(); i++) {
            PlayerData playerData = dataList.get(i);
            selfTotalBBCount += playerData.getBbCount();
            selfTotalWinCount += playerData.getWinCount();
            selfTotalLoseCount += playerData.getLoseCount();
            selfTotalPlayCount += playerData.getPlayCount();
            selfTotalJoinCount += playerData.getJoinCount();
            selfTotalBet3Count += playerData.getBet3Count();
            selfTotalPfrCount += playerData.getPfrCount();
            selfTotalStlCount += playerData.getStlCount();
            selfTotalFoldStlCount += playerData.getFoldStlCount();
            selfTotalFold3BetCount += playerData.getFold3BetCount();
            selfTotalFace3BetCount += playerData.getFace3BetCount();
            selfTotalFaceOpenCount += playerData.getFaceOpenCount();
            selfTotalCallCount += playerData.getCallCount();
            selfTotalRaiseCount += playerData.getRaiseCount();
            selfLastRaiseCount += playerData.getLastRaiseCount();
            selfTotalCbCount += playerData.getCbCount();
            selfTotalSTLPositionCount += playerData.getStlPosCount();
            selfTotalFaceSTLCount += playerData.getFaceStlCount();
        }
        PlayerData player = new PlayerData();
        player.setBbCount(selfTotalBBCount);
        player.setBet3Count(selfTotalBet3Count);
        player.setCallCount(selfTotalCallCount);
        player.setCbCount(selfTotalCbCount);
        player.setFace3BetCount(selfTotalFace3BetCount);
        player.setFaceOpenCount(selfTotalFaceOpenCount);
        player.setWinCount(selfTotalWinCount);
        player.setLoseCount(selfTotalLoseCount);
        player.setFold3BetCount(selfTotalFold3BetCount);
        player.setFoldStlCount(selfTotalFoldStlCount);
        player.setJoinCount(selfTotalJoinCount);
        player.setRaiseCount(selfTotalRaiseCount);
        player.setLastRaiseCount(selfLastRaiseCount);
        player.setPlayCount(selfTotalPlayCount);
        player.setPfrCount(selfTotalPfrCount);
        player.setStlPosCount(selfTotalSTLPositionCount);
        player.setFaceStlCount(selfTotalFaceSTLCount);
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
    public void createPlayerMessage(int seatIdx) {
        if (!canSelect) {
            windowManager.removeViewImmediate(view);
            handlerClose.removeCallbacks(callBack);
            canSelect = true;
            return;
        }
        canSelect = false;
        PlayerData player = null;
        String playerName = "";
        if(names!=null && !TextUtils.isEmpty(names.get(seatIdx))){
            playerName = names.get(seatIdx);
        }
        if (view == null) {
            view = LayoutInflater.from(context).inflate(R.layout.layout_window, null, false);
            view.setBackgroundColor(context.getResources().getColor(R.color.blue));
        }
        params.width = winPos[winIndex][0][1][2];
        params.height = winPos[winIndex][0][1][3];
        params.x = winPos[winIndex][0][1][0];
        params.y = winPos[winIndex][0][1][1];
        ViewHolder vh = new ViewHolder(view);
        if (!TextUtils.isEmpty(playerName) || seatIdx == 0) {
            if (playerName.equals("_self") || seatIdx == 0) {
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
                List<PlayerData> dataList = where("name=?", "_self").find(PlayerData.class);
                for (int i = 0; i < dataList.size(); i++) {
                    PlayerData playerData = dataList.get(i);
                    selfTotalBBCount += playerData.getBbCount();
                    selfTotalWinCount += playerData.getWinCount();
                    selfTotalLoseCount += playerData.getLoseCount();
                    selfTotalPlayCount += playerData.getPlayCount();
                    selfTotalJoinCount += playerData.getJoinCount();
                    selfTotalBet3Count += playerData.getBet3Count();
                    selfTotalPfrCount += playerData.getPfrCount();
                    selfTotalStlCount += playerData.getStlCount();
                    selfTotalFoldStlCount += playerData.getFoldStlCount();
                    selfTotalFold3BetCount += playerData.getFold3BetCount();
                    selfTotalFace3BetCount += playerData.getFace3BetCount();
                    selfTotalFaceOpenCount += playerData.getFaceOpenCount();
                    selfTotalCallCount += playerData.getCallCount();
                    selfTotalRaiseCount += playerData.getRaiseCount();
                    selfLastRaiseCount += playerData.getLastRaiseCount();
                    selfTotalCbCount += playerData.getCbCount();
                    selfTotalSTLPositionCount += playerData.getStlPosCount();
                    selfTotalFaceSTLCount += playerData.getFaceStlCount();
                }
                vh.totalPlayCount.setText("自己(" + selfTotalPlayCount + ")");
                if (selfTotalPlayCount != 0) {
                    if (selfTotalFaceOpenCount > 0) {
                        vh.bet3Percent.setText("3Bet(" + selfTotalBet3Count * 100 / selfTotalFaceOpenCount + "%)");
                    } else {
                        vh.bet3Percent.setText("3Bet(0%)");
                    }
                    vh.vpipPercent.setText("VPIP(" + selfTotalJoinCount * 100 / selfTotalPlayCount + "%)");
                    vh.pfrPercent.setText("PFR(" + selfTotalPfrCount * 100 / selfTotalPlayCount + "%)");
                    if(selfTotalSTLPositionCount>0) {
                        vh.stlPercent.setText("STL(" + selfTotalStlCount * 100 / selfTotalSTLPositionCount + "%)");
                    }else{
                        vh.stlPercent.setText("STL(-%)");
                    }
                    if(selfTotalFaceSTLCount>0){
                        vh.foldStlPercent.setText("FSTL(" + selfTotalFoldStlCount * 100 / selfTotalFaceSTLCount + "%)");
                    }else{
                        vh.foldStlPercent.setText("FSTL(-%)");
                    }
                    if (selfTotalFace3BetCount > 0) {
                        vh.fold3BetPercent.setText("F3Bet(" + selfTotalFold3BetCount * 100 / selfTotalFace3BetCount + "%)");
                    }else{
                        vh.fold3BetPercent.setText("F3Bet(-%)");
                    }
                    if (selfTotalCallCount > 0) {
                        double d = selfTotalRaiseCount / Double.valueOf(selfTotalCallCount);
                        vh.afPercent.setText("AF(" + String.format("%.1f",d) + ")");
                    }else{
                        vh.afPercent.setText("AF(-)");
                    }
                    if (selfLastRaiseCount > 0) {
                        vh.cbPercent.setText("CB(" + selfTotalCbCount * 100 / selfLastRaiseCount + "%)");
                    }else{
                        vh.cbPercent.setText("CB(-%)");
                    }
                }
            } else {
                List<PlayerData> playerDatas = where("name=?", playerName).find(PlayerData.class);
                if (playerDatas.size() > 0) {
                    player = playerDatas.get(0);
                }
                if (player != null) {
                    int playCount = player.getPlayCount();
                    vh.totalPlayCount.setText(player.getName() + "(" + playCount + ")");
                    if (playCount != 0) {
                        if (player.getFaceOpenCount() > 0) {
                            vh.bet3Percent.setText("3Bet(" + player.getBet3Count() * 100 / player.getFaceOpenCount() + "%)");
                        } else {
                            vh.bet3Percent.setText("3Bet(-%)");
                        }
                        vh.vpipPercent.setText("VPIP(" + player.getJoinCount() * 100 / playCount + "%)");
                        vh.pfrPercent.setText("PFR(" + player.getPfrCount() * 100 / playCount + "%)");
                        if(player.getStlPosCount()>0){
                            vh.stlPercent.setText("STL(" + player.getStlCount() * 100 / player.getStlPosCount() + "%)");
                        }else{
                            vh.stlPercent.setText("STL(-%)");
                        }
                        if(player.getFaceStlCount()>0){
                            vh.foldStlPercent.setText("FSTL(" + player.getFoldStlCount() * 100 / player.getFaceStlCount() + "%)");
                        }else{
                            vh.foldStlPercent.setText("FSTL(-%)");
                        }
                        if (player.getFace3BetCount() > 0) {
                            vh.fold3BetPercent.setText("F3Bet(" + player.getFold3BetCount() * 100 / player.getFace3BetCount() + "%)");
                        }else{
                            vh.fold3BetPercent.setText("F3Bet(-%)");
                        }
                        if (player.getCallCount() > 0) {
                            double i = player.getRaiseCount() / Double.valueOf(player.getCallCount());
                            vh.afPercent.setText("AF(" + String.format("%.1f",i) + ")");
                        }else{
                            vh.afPercent.setText("AF(-)");
                        }
                        if (player.getLastRaiseCount() > 0) {
                            vh.cbPercent.setText("CB(" + player.getCbCount() * 100 / player.getLastRaiseCount() + "%)");
                        }else{
                            vh.cbPercent.setText("CB(-%)");
                        }
                    }
                }else{
                    vh.totalPlayCount.setText("(-)");
                    vh.bet3Percent.setText("3Bet(-%)");
                    vh.vpipPercent.setText("VPIP(-%)");
                    vh.pfrPercent.setText("PFR(-%)");
                    vh.stlPercent.setText("STL(-%)");
                    vh.foldStlPercent.setText("FSTL(-%)");
                    vh.fold3BetPercent.setText("F3Bet(-%)");
                    vh.afPercent.setText("AF(-)");
                    vh.cbPercent.setText("CB(-%)");
                }
            }
        } else {
            view = LayoutInflater.from(context).inflate(R.layout.layout_window, null, false);
            view.setBackgroundColor(context.getResources().getColor(R.color.blue));
        }
        windowManager.addView(view, params);
        handlerClose.postDelayed(callBack, 5000);
    }

    public void updateWindow(String percent) {
        if(textView==null){
             createWindow(percent);
        }else{
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

    public void deleteWindow() {
        if (havePercentWindow && windowManager != null && textView!=null) {
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
    }

    static class ViewHolder {
        @BindView(R.id.totalPlayCount)
        TextView totalPlayCount;
        @BindView(R.id.vpipPercent)
        TextView vpipPercent;
        @BindView(R.id.pfrPercent)
        TextView pfrPercent;
        @BindView(R.id.bet3Percent)
        TextView bet3Percent;
        @BindView(R.id.foldBet3Percent)
        TextView fold3BetPercent;
        @BindView(R.id.cbPercent)
        TextView cbPercent;
        @BindView(R.id.afPercent)
        TextView afPercent;
        @BindView(R.id.stlPercent)
        TextView stlPercent;
        @BindView(R.id.foldStlPercent)
        TextView foldStlPercent;
        @BindView(R.id.item)
        LinearLayout item;

        ViewHolder(View view) {
            ButterKnife.bind(this, view);
        }
    }
}
