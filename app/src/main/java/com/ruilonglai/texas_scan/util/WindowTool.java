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
import android.util.SparseArray;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.ruilonglai.texas_scan.R;
import com.ruilonglai.texas_scan.activity.LoginActivity;
import com.ruilonglai.texas_scan.entity.PercentType;
import com.ruilonglai.texas_scan.entity.PlayerData;
import com.ruilonglai.texas_scan.entity.PlayerData1;
import com.ruilonglai.texas_scan.entity.PlayerData2;
import com.ruilonglai.texas_scan.entity.PlayerData3;
import com.ruilonglai.texas_scan.entity.PlayerPoker;
import com.ruilonglai.texas_scan.entity.QueryPlayerPoker;
import com.ruilonglai.texas_scan.entity.QuerySelf;
import com.ruilonglai.texas_scan.entity.QueryUser;
import com.ruilonglai.texas_scan.entity.ReqData;
import com.ruilonglai.texas_scan.entity.ReqUpdateUser;
import com.ruilonglai.texas_scan.entity.Result;
import com.ruilonglai.texas_scan.entity.UserName;
import com.ruilonglai.texas_scan.newprocess.MainProcessUtil;
import com.ruilonglai.texas_scan.newprocess.MainServer;
import com.ruilonglai.texas_scan.newprocess.Package;
import com.zhy.autolayout.utils.AutoUtils;

import org.byteam.superadapter.SuperAdapter;
import org.byteam.superadapter.SuperViewHolder;
import org.json.JSONException;
import org.json.JSONObject;
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
    private List<View> seatViews;
    private SparseArray<String> names;
    private SparseArray<String> seatContents;
    private List<Integer> percents;
    private String userId;
    private int appCount;
    private int widthIdx;//默认是720,当widthIdx=1时,适配1080*1920
    List<Double> vpips; //当前牌桌各玩家得入池率
    List<Double> flops; //当前牌桌各玩家得翻牌率
    private int levelIdx = -1;
    private volatile static WindowTool instance = null;
    private int[] cardArr = new int[]
            {R.drawable.paibg,R.drawable.paibg,R.drawable.paibg,R.drawable.paibg,
            R.drawable.paibg,R.drawable.paibg,R.drawable.paibg,R.drawable.paibg,
            R.drawable.paibg,R.drawable.paibg,R.drawable.paibg,R.drawable.paibg,
            R.drawable.paibg,R.drawable.paibg,R.drawable.paibg,R.drawable.paibg};

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
                        handCount = 0;
                    } catch (Exception e) {
                        MyLog.e("error", "异常退出");
                        MainProcessUtil.getInstance().exit(context);
                    }
                }
                windowManager.updateViewLayout(openCloseView, params);
            } else if (msg.arg1 == 2) {
                int seatIdx = msg.arg2;
                seatContents.put(seatIdx, getPlayerMessage(names.get(seatIdx)));
                updateNineWindow();
            }else if(msg.arg1 == 3) {//设置图片
                if (!haveSettingView) {
                    haveSettingView = createSettingView(msg.arg2, (int)msg.obj);
                }
            }else if (msg.arg1 == 8) {//错误显示
                String json = (String) msg.obj;
                Toast.makeText(context, json, Toast.LENGTH_SHORT).show();
            }
        }
    };

    private WindowTool() {
        seatViews = new ArrayList<>();
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
        MyLog.e("isInit", isInit + "");
        this.winIndex = winIndex;
        if (vpips == null)
            vpips = new ArrayList<>();
        if (flops == null)
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
            nameParams.width = 320;
            nameParams.x = 200;
            if (winIndex == 2) {
                nameParams.y = 750;
            } else {
                nameParams.y = 680;
            }
            nameParams.format = PixelFormat.RGBA_8888;
            nameParams.gravity = Gravity.BOTTOM | Gravity.LEFT;
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

    public static int handCount = 0;

    /*更新平均入池率*/
    public void updateOpenViewVPIP() {
        if (openCloseView != null && ovh != null) {
            params.width = 100;
            params.height = WindowManager.LayoutParams.WRAP_CONTENT;
            params.x = 20;
            params.y = 250;
            ovh.avgVpip.setText(getAvgVpip());
            String openstr = ovh.open.getText().toString();
            if (openstr.contains("(")) {
                ovh.open.setText(openstr.substring(0, openstr.indexOf("(")) + "(" + handCount + ")");
            } else {
                ovh.open.setText(openstr + "(" + handCount + ")");
            }
            windowManager.updateViewLayout(openCloseView, params);
        }
    }

    public void setPokers(){

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
        } else if (widthIdx == 1) {
            params.width = winPos_1080[winIndex][0][0][2];
            params.height = winPos_1080[winIndex][0][0][3];
            params.x = winPos_1080[winIndex][0][0][0];
            params.y = winPos_1080[winIndex][0][0][1];
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
    /*创建修改笔记备注窗口*/
    public boolean createSettingView(final int seatIdx, int changeType) {
        levelIdx = -1;
        if (haveSettingView) {
            return false;
        }
        if (settingView == null) {
            settingView = LayoutInflater.from(context).inflate(R.layout.window_setting, null, false);
        }
        final SettingViewHolder vh = new SettingViewHolder(settingView);
        vh.name.setText("");
        vh.remark.setText("");
        vh.remarkContent.setText("");
        vh.turnCards.setVisibility(View.VISIBLE);
        vh.card1.setBackground(context.getResources().getDrawable(cardArr[0]));
        vh.card2.setBackground(context.getResources().getDrawable(cardArr[1]));
        vh.card3.setBackground(context.getResources().getDrawable(cardArr[2]));
        vh.card4.setBackground(context.getResources().getDrawable(cardArr[3]));
        vh.card5.setBackground(context.getResources().getDrawable(cardArr[4]));
        vh.card6.setBackground(context.getResources().getDrawable(cardArr[5]));
        vh.card7.setBackground(context.getResources().getDrawable(cardArr[6]));
        vh.card8.setBackground(context.getResources().getDrawable(cardArr[7]));
        vh.card9.setBackground(context.getResources().getDrawable(cardArr[8]));
        vh.card10.setBackground(context.getResources().getDrawable(cardArr[9]));
        vh.card11.setBackground(context.getResources().getDrawable(cardArr[10]));
        vh.card12.setBackground(context.getResources().getDrawable(cardArr[11]));
        vh.card13.setBackground(context.getResources().getDrawable(cardArr[12]));
        vh.card14.setBackground(context.getResources().getDrawable(cardArr[13]));
        vh.card15.setBackground(context.getResources().getDrawable(cardArr[14]));
        vh.card16.setBackground(context.getResources().getDrawable(cardArr[15]));
        switch (changeType) {
            case Constant.FLAG_REMARK:
                vh.seatIdx.setText(seatIdx + "号位(备注)");
                vh.remark_layout.setVisibility(View.GONE);
                vh.search_name_layout.setVisibility(View.VISIBLE);
                vh.selectColors.setVisibility(View.GONE);
                vh.changeEntry.setText("备注");
                break;
            case Constant.FLAG_NOTE:
                vh.seatIdx.setText(seatIdx + "号位(笔记)");
                vh.remark_layout.setVisibility(View.VISIBLE);
                vh.search_name_layout.setVisibility(View.GONE);
                vh.selectColors.setVisibility(View.GONE);
                vh.changeEntry.setText("笔记");
                break;
            case Constant.FLAG_CHANGE_COLOR:
                vh.seatIdx.setText(seatIdx + "号位(标记)");
                vh.remark_layout.setVisibility(View.GONE);
                vh.selectColors.setVisibility(View.VISIBLE);
                vh.search_name_layout.setVisibility(View.GONE);
                vh.nameList.setVisibility(View.VISIBLE);
                vh.turnCards.setVisibility(View.GONE);
                break;
        }
        vh.level.setBackgroundColor(context.getResources().getColor(R.color.numbers_text_color));
        if (names.get(seatIdx) != null) {
            List<PlayerData> playerDatas = getPlayerDatas(names.get(seatIdx));
            if (playerDatas.size() > 0) {
                PlayerData playerData = playerDatas.get(0);
                int level = playerData.getLevel();
                vh.level.setBackgroundColor(context.getResources().getColor(Constant.colors[level == -1 ? 0 : level]));
                if (!TextUtils.isEmpty(playerData.getRemark())) {
                    vh.remark.setText(playerData.getRemark());
                    vh.remarkContent.setText(playerData.getRemark());
                }
            }
        }
        vh.close.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                windowManager.removeViewImmediate(settingView);
                haveSettingView = false;
            }
        });
        //保存监听
        vh.sure.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String name = vh.name.getText().toString();
                String remark = vh.remark.getText().toString();
                if (TextUtils.isEmpty(name)) {
                    name = "";
                }
                if (TextUtils.isEmpty(remark)) {
                    remark = "";
                }
                Package pkg = new Package();
                pkg.setType(Constant.SOCKET_UPDATE_NAME);
                pkg.setContent("{\"seatIdx\":" + seatIdx + ",\"name\":\"" + name + "\",\"remark\":\"" + "" + "\",\"level\":" + -1 + "}");
                if (!TextUtils.isEmpty(name))
                    MainServer.newInstance().send(pkg, context);
                windowManager.removeViewImmediate(settingView);
                haveSettingView = false;
                updatePlayerData("{\"seatIdx\":" + seatIdx + ",\"remark\":\"" + remark + "\",\"level\":" + levelIdx + "}");
            }
        });
        vh.nameList.setAdapter(null);
        //输入框监听
        vh.name.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
               vh.turnCards.setVisibility(View.GONE);
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                MyLog.e("WindowTool", s.toString());
                String content = s.toString();
                List<String> list = new ArrayList<String>();
                if (!TextUtils.isEmpty(content)) {
                    List<UserName> userNames = DataSupport.where("name like ?", "%" + content + "%").find(UserName.class);
                    for (int i = 0; i < userNames.size(); i++) {
                        UserName userName = userNames.get(i);
                        if (userName != null) {
                            String name = userName.name;
                            if (!TextUtils.isEmpty(name) && !name.contains("self"))
                                list.add(name);
                        }
                    }
                }
                vh.nameList.setAdapter(new SuperAdapter<String>(context, list, R.layout.item_log) {
                    @Override
                    public void onBind(SuperViewHolder holder, int viewType, int layoutPosition, String item) {
                        holder.setText(R.id.log_item, item);
                        holder.setBackgroundColor(R.id.log_item, context.getResources().getColor(R.color.hui));
                        holder.setBackgroundColor(R.id.item_layout, context.getResources().getColor(R.color.hui));
                    }
                });
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
        vh.remarkContent.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                vh.turnCards.setVisibility(View.GONE);
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                vh.remark.setText(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
        vh.changeEntry.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String str = vh.changeEntry.getText().toString();
                vh.selectColors.setVisibility(View.GONE);
                if (str.equals("备注")) {
                    vh.search_name_layout.setVisibility(View.GONE);
                    vh.remark_layout.setVisibility(View.VISIBLE);
                    vh.changeEntry.setText("笔记");
                    vh.seatIdx.setText(seatIdx + "号位(笔记)");
                } else {
                    vh.search_name_layout.setVisibility(View.VISIBLE);
                    vh.remark_layout.setVisibility(View.GONE);
                    vh.changeEntry.setText("备注");
                    vh.seatIdx.setText(seatIdx + "号位(备注)");
                }
            }
        });
        /*查询名字选取*/
        vh.nameList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String name = vh.nameList.getItemAtPosition(position).toString();
                vh.name.setText(name);
            }
        });
        /**/
        vh.level.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (vh.selectColors.getVisibility() == View.VISIBLE) {
                    vh.selectColors.setVisibility(View.GONE);
                    if (vh.changeEntry.getText().toString().equals("备注")) {
                        vh.seatIdx.setText(seatIdx + "号位(备注)");
                        vh.search_name_layout.setVisibility(View.VISIBLE);
                        vh.remark_layout.setVisibility(View.GONE);
                    } else {
                        vh.seatIdx.setText(seatIdx + "号位(笔记)");
                        vh.search_name_layout.setVisibility(View.GONE);
                        vh.remark_layout.setVisibility(View.VISIBLE);
                    }
                    return;
                }
                vh.selectColors.setVisibility(View.VISIBLE);
                vh.search_name_layout.setVisibility(View.GONE);
                vh.remark_layout.setVisibility(View.GONE);
                vh.seatIdx.setText(seatIdx + "号位(标记)");
            }
        });
        vh.color0.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                vh.level.setBackgroundColor(context.getResources().getColor(R.color.numbers_text_color));
                levelIdx = 0;
                vh.selectColors.setVisibility(View.INVISIBLE);
            }
        });
        vh.color1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                vh.level.setBackgroundColor(context.getResources().getColor(R.color.color1));
                levelIdx = 1;
                vh.selectColors.setVisibility(View.INVISIBLE);
            }
        });
        vh.color2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                vh.level.setBackgroundColor(context.getResources().getColor(R.color.color2));
                levelIdx = 2;
                vh.selectColors.setVisibility(View.INVISIBLE);
            }
        });
        vh.color3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                vh.level.setBackgroundColor(context.getResources().getColor(R.color.color3));
                levelIdx = 3;
                vh.selectColors.setVisibility(View.INVISIBLE);
            }
        });
        vh.color4.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                vh.level.setBackgroundColor(context.getResources().getColor(R.color.color4));
                levelIdx = 4;
                vh.selectColors.setVisibility(View.INVISIBLE);
            }
        });
        vh.color5.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                vh.level.setBackgroundColor(context.getResources().getColor(R.color.color5));
                levelIdx = 5;
                vh.selectColors.setVisibility(View.INVISIBLE);
            }
        });
        vh.color6.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                vh.level.setBackgroundColor(context.getResources().getColor(R.color.color6));
                levelIdx = 6;
                vh.selectColors.setVisibility(View.INVISIBLE);
            }
        });
        vh.color7.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                vh.level.setBackgroundColor(context.getResources().getColor(R.color.color7));
                levelIdx = 7;
                vh.selectColors.setVisibility(View.INVISIBLE);
            }
        });
        vh.color8.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                vh.level.setBackgroundColor(context.getResources().getColor(R.color.color8));
                levelIdx = 8;
                vh.selectColors.setVisibility(View.INVISIBLE);
            }
        });
        windowManager.addView(settingView, nameParams);
        return true;
    }

    public void getCardsList(final int seatIdx, final int changeType){
        Gson gson = new Gson();
        String name = names.get(seatIdx);
        QueryPlayerPoker qpp = new QueryPlayerPoker();
        if(!TextUtils.isEmpty(name)){
            qpp.setPlattype(appCount);
            qpp.setUsername(name);
            if(name.equals("self")){
                qpp.setUsername(context.getSharedPreferences(LoginActivity.PREF_FILE, Context.MODE_PRIVATE).getString("name", ""));
            }
        }
        ReqData req = new ReqData();
        req.setReqid(context.getSharedPreferences(LoginActivity.PREF_FILE, Context.MODE_PRIVATE).getString("name", ""));
        req.setReqno(TimeUtil.getCurrentDateToMinutes(new Date()) + ActionsTool.disposeNumber());
        req.setParam(gson.toJson(qpp));
        req.setReqfunc("queryplayerpoker");
        HttpUtil.sendPostRequestData("queryplayerpoker", gson.toJson(req), new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Message msg = new Message();
                msg.arg1 = 8;
                msg.obj = "查询玩家历史手牌失败";
                handler.sendMessage(msg);
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String json = response.body().string();
                MyLog.e("WindowTool", "response(queryplayerpoker):" + json);
                Result result = GsonUtil.parseJsonWithGson(json, Result.class);
                Map<String, String> map = result.getRets();
                String players = map.get("listpoker");
                for (int i = 0; i < cardArr.length; i++) {
                    cardArr[i] = R.drawable.paibg;
                }
                if(players!=null && !players.equals("null")){
                    List<PlayerPoker> pokers = new ArrayList<PlayerPoker>();
                    Type listType = new TypeToken<List<PlayerPoker>>() {
                    }.getType();
                    pokers = new Gson().fromJson(players, listType);
                    if(pokers!=null && pokers.size()!=0) {
                        for (int i = 0; i < pokers.size(); i++) {
                            PlayerPoker poker = pokers.get(i);
                            int card1 = poker.getCard1();
                            int card2 = poker.getCard2();
                            if(card1>=0 && card2>=0 && i<8) {
                                cardArr[2*i] = CardUtil.imgs[card1/100][card1%100];
                                cardArr[2*i+1] = CardUtil.imgs[card2/100][card2%100];
                            }
                        }
                    }
                }
                Message msg = new Message();
                msg.arg1 = 3;
                msg.arg2 = seatIdx;
                msg.obj = changeType;
                handler.sendMessage(msg);

            }
        });
    }
    /*修改备注和标记颜色*/
    public void updatePlayerData(String json) {
        PlayerData playerData = null;
        final int seatIdx;
        boolean sendReq = false;
        try {
            JSONObject object = new JSONObject(json);
            seatIdx = object.getInt("seatIdx");
            String remark = object.getString("remark");
            int level = object.getInt("level");
            String name = names.get(seatIdx);
            if (!TextUtils.isEmpty(name)) {
                if (appCount == 0) {
                    List<PlayerData> datas = DataSupport.where("name=?", name).find(PlayerData.class);
                    if (datas.size() != 0) {
                        PlayerData playerData0 = datas.get(0);
                        if (playerData0 != null) {
                            if (!remark.equals(playerData0.getRemark())) {
                                sendReq = true;
                                playerData0.setRemark(remark);
                            }
                            if (level != playerData0.getLevel() && level != -1) {
                                sendReq = true;
                                playerData0.setLevel(level);
                            }
                            if (sendReq) {
                                playerData0.updateAll("name=?", name);
                            }
                        }
                        playerData = playerData0;
                    }
                } else if (appCount == 1) {
                    List<PlayerData1> datas = DataSupport.where("name=?", name).find(PlayerData1.class);
                    if (datas.size() != 0) {
                        PlayerData1 playerData1 = datas.get(0);
                        if (playerData1 != null) {
                            if (!remark.equals(playerData1.getRemark())) {
                                sendReq = true;
                                playerData1.setRemark(remark);
                            }
                            if (level != playerData1.getLevel() && level != -1) {
                                sendReq = true;
                                playerData1.setLevel(level);
                            }
                            if (sendReq) {
                                playerData1.updateAll("name=?", name);
                            }
                        }
                        playerData = playerData1;
                    }
                } else if (appCount == 2) {
                    List<PlayerData2> datas = DataSupport.where("name=?", name).find(PlayerData2.class);
                    if (datas.size() != 0) {
                        PlayerData2 playerData2 = datas.get(0);
                        if (playerData2 != null) {
                            if (!remark.equals(playerData2.getRemark())) {
                                sendReq = true;
                                playerData2.setRemark(remark);
                            }
                            if (level != playerData2.getLevel() && level != -1) {
                                sendReq = true;
                                playerData2.setLevel(level);
                            }
                            if (sendReq) {
                                playerData2.updateAll("name=?", name);
                                playerData2.getFoldRiverCount();
                            }
                        }
                        playerData = playerData2;
                    }
                } else if (appCount == 3) {
                    List<PlayerData3> datas = DataSupport.where("name=?", name).find(PlayerData3.class);
                    if (datas.size() != 0) {
                        PlayerData3 playerData3 = datas.get(0);
                        if (playerData3 != null) {
                            if (!remark.equals(playerData3.getRemark())) {
                                sendReq = true;
                                playerData3.setRemark(remark);
                            }
                            if (level != playerData3.getLevel() && level != -1) {
                                sendReq = true;
                                playerData3.setLevel(level);
                            }
                            if (sendReq) {
                                playerData3.updateAll("name=?", name);
                            }
                        }
                        playerData = playerData3;
                    }
                }
            }

        } catch (JSONException e) {
            Message msg = new Message();
            msg.arg1 = 8;
            msg.obj = "修改失败";
            handler.sendMessage(msg);
            return;
        }
        if (sendReq) {
            ReqData data = new ReqData();
            ReqUpdateUser req = new ReqUpdateUser();
            req.setPlattype(appCount);
            req.setUserdata(playerData);
            data.setReqno(TimeUtil.getCurrentDateToMinutes(new Date()) + ActionsTool.disposeNumber());
            data.setReqid(context.getSharedPreferences(LoginActivity.PREF_FILE, Context.MODE_PRIVATE).getString("name", ""));
            data.setParam(new Gson().toJson(req));
            data.setReqfunc("requpdateuser");
            String reqData = new Gson().toJson(data);
            HttpUtil.sendPostRequestData("requpdateuser", reqData, new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    Message msg = new Message();
                    msg.arg1 = 8;
                    msg.obj = "服务器未响应";
                    handler.sendMessage(msg);
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    MyLog.e(getClass().getName() + "response(requpdateuser)", response.body().string());
                    Message msg = new Message();
                    msg.arg1 = 2;
                    msg.arg2 = seatIdx;
                    handler.sendMessage(msg);
                }
            });
        }

    }

    public boolean createNinePointWindow(int appCount, int playCount, SparseArray<String> seatNames, int isWatch) {

        this.isWatch = isWatch;
        this.appCount = appCount;
        this.winIndex = appCount;
        if (this.winIndex == 2) {
            nameParams.y = 750;
        } else {
            nameParams.y = 680;
        }
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
            for (int j = 0; j < seatViews.size(); j++) {
                View text = seatViews.get(j);
                windowManager.removeViewImmediate(text);
            }
            haveSeatsWindow = false;
        }
        seatViews.clear();
        for (int j = 2 + isWatch; j < playCount + 2 + isWatch; j++) {
            View view = LayoutInflater.from(context).inflate(R.layout.window_data_item, null, false);
            NoteItemViewHolder nvh = new NoteItemViewHolder(view);
            nvh.content.setTag(j - 2);
            nvh.content.setTextColor(Color.WHITE);
            nvh.noteColor.setVisibility(View.GONE);
            nvh.note_color_right.setVisibility(View.GONE);
            nvh.name_left.setVisibility(View.GONE);
            nvh.name_right.setVisibility(View.GONE);
            nvh.noteColor.setTag(j - 2);
            nvh.note_color_right.setTag(j - 2);
            nvh.name_left.setTag(j - 2);
            nvh.name_right.setTag(j - 2);
            String name = names.get(j - 2);
            if (!TextUtils.isEmpty(name)) {
                if (name.length() == 4) {
                    name = name.substring(0, 2) + "\n" + name.substring(2, 4);
                } else if (name.length() > 4) {
                    name = name.substring(0, 3) + "\n" + ((name.length() <= 6) ? name.substring(3, name.length()) : name.substring(3, 6));
                }
                nvh.name_left.setText(name);
                nvh.name_right.setText(name);
            }
            nvh.noteColor.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int seatIdx = (int) v.getTag();
                    getCardsList(seatIdx, Constant.FLAG_CHANGE_COLOR);
                }
            });
            nvh.note_color_right.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int seatIdx = (int) v.getTag();
                    getCardsList(seatIdx, Constant.FLAG_CHANGE_COLOR);
                }
            });
            nvh.name_left.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int seatIdx = (int) v.getTag();
                    getCardsList(seatIdx, Constant.FLAG_REMARK);
                }
            });
            nvh.name_right.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int seatIdx = (int) v.getTag();
                    getCardsList(seatIdx, Constant.FLAG_REMARK);
                }
            });
            if (names != null && names.get(j - 2) != null) {
                String text = seatContents.get(j - 2);
                if (text.contains("#")) {
                    String[] strs = text.split("#");
                    nvh.content.setText(strs[0]);
                    int colorIdx = Integer.valueOf(strs[1]).intValue();
                    if (colorIdx > 0) {
                        nvh.noteColor.getDrawable().setLevel(colorIdx * 12 + 1);
                        nvh.note_color_right.getDrawable().setLevel(colorIdx * 12 + 1);
                        if (j > Math.round(playCount / 2) + 2) {
                            nvh.note_color_right.setVisibility(View.VISIBLE);
                            nvh.name_right.setVisibility(View.VISIBLE);
                        } else {
                            if (!name.equals("self") && !name.equals("se\nlf")) {
                                nvh.noteColor.setVisibility(View.VISIBLE);
                                nvh.name_left.setVisibility(View.VISIBLE);
                            }
                        }
                    } else {
                        nvh.noteColor.getDrawable().setLevel(1);
                        nvh.note_color_right.getDrawable().setLevel(1);
                        if (j > Math.round(playCount / 2) + 2) {
                            nvh.note_color_right.setVisibility(View.VISIBLE);
                            nvh.name_right.setVisibility(View.VISIBLE);
                        } else {
                            if (!name.equals("self") && !name.equals("se\nlf")) {
                                nvh.noteColor.setVisibility(View.VISIBLE);
                                nvh.name_left.setVisibility(View.VISIBLE);
                            }
                        }
                    }
                } else {
                    nvh.content.setText(text);
                }
                setParamGravity(j);
                if (widthIdx == 0) {
                    params.width = WindowManager.LayoutParams.WRAP_CONTENT;
                    params.height = winPos[appCount][arr3Idx][j][3];
                    params.x = winPos[appCount][arr3Idx][j][0];
                    params.y = winPos[appCount][arr3Idx][j][1];
                } else if (widthIdx == 1) {
                    params.width = WindowManager.LayoutParams.WRAP_CONTENT;
                    params.height = winPos_1080[appCount][arr3Idx][j][3];
                    params.x = winPos_1080[appCount][arr3Idx][j][0];
                    params.y = winPos_1080[appCount][arr3Idx][j][1];
                }
            } else {
//                nvh.content.setText("－|－|－\n－|－|－");
//                if (widthIdx == 0) {
//                    params.width = WindowManager.LayoutParams.WRAP_CONTENT;
//                    params.height = winPos[appCount][arr3Idx][j][3];
//                    params.x = winPos[appCount][arr3Idx][j][0];
//                    params.y = winPos[appCount][arr3Idx][j][1];
//                } else if (widthIdx == 1) {
//                    params.width = WindowManager.LayoutParams.WRAP_CONTENT;
//                    params.height = winPos_1080[appCount][arr3Idx][j][3];
//                    params.x = winPos_1080[appCount][arr3Idx][j][0];
//                    params.y = winPos_1080[appCount][arr3Idx][j][1];
//                }
            }
            nvh.content.setGravity(Gravity.CENTER);
            nvh.content.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int seatIdx = (int) v.getTag();
                    String name = names.get(seatIdx);
                    if (!TextUtils.isEmpty(name)) {
                        createPlayerMessage(seatIdx);
                    } else {
                        getCardsList(seatIdx, Constant.FLAG_NOTE);
                    }
                }
            });
            seatViews.add(view);
            windowManager.addView(view, params);
        }
        haveSeatsWindow = true;
        params.gravity = Gravity.BOTTOM | Gravity.LEFT;
    }

    public void setParamGravity(int j) {
        if (j - 2 > 4 && (playCount == 9 || playCount == 8)) {
            params.gravity = Gravity.BOTTOM | Gravity.RIGHT;
        } else if (j - 2 > 3 && (playCount == 7)) {
            params.gravity = Gravity.BOTTOM | Gravity.RIGHT;
        } else if (j - 2 > 3 && playCount == 6) {
            params.gravity = Gravity.BOTTOM | Gravity.RIGHT;
        }
    }

    class NoteItemViewHolder {
        @BindView(R.id.note_color)
        ImageView noteColor;
        @BindView(R.id.content)
        TextView content;
        @BindView(R.id.note_color_right)
        ImageView note_color_right;
        @BindView(R.id.name_left)
        TextView name_left;
        @BindView(R.id.name_right)
        TextView name_right;

        NoteItemViewHolder(View view) {
            ButterKnife.bind(this, view);
            AutoUtils.autoSize(view);
        }
    }

    public String getAvgVpip() {//获取平均入池率
        double sum = 0.0;
        double sumFlop = 0.0;
        String str = "";
        if (vpips.size() == 0) {
            str += "0.0";
        } else {
            for (int i = 0; i < vpips.size(); i++) {
                Double aDouble = vpips.get(i);
                sum += aDouble;
            }
            String avgVpip = String.format("%.1f", sum / vpips.size());
            if (Double.valueOf(avgVpip) > 100)
                avgVpip = "100.0";
            str += avgVpip;
        }

        if (flops.size() == 0) {
            str += "|0.0";
        } else {
            for (int i = 0; i < flops.size(); i++) {
                Double aDouble = flops.get(i);
                sumFlop += aDouble;
            }
            String avgFlop = String.format("%.1f", sumFlop / flops.size());
            if (Double.valueOf(avgFlop) > 100)
                avgFlop = "100.0";
            str += "|" + avgFlop;
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
            if (name != null && !TextUtils.isEmpty(name)) {
                if (name.contains("self")) {
                    QuerySelf queryself = new QuerySelf();
                    queryself.setUserid(userId);
                    queryself.setPlatType(appCount);
                    String param = gson.toJson(queryself);
                    data.setParam(param);
                    data.setReqno(TimeUtil.getCurrentDateToMinutes(new Date()) + ActionsTool.disposeNumber());
                    data.setReqid(context.getSharedPreferences(LoginActivity.PREF_FILE, Context.MODE_PRIVATE).getString("name", ""));
                    HttpUtil.sendPostRequestData("queryself", gson.toJson(data), new Callback() {
                        @Override
                        public void onFailure(Call call, IOException e) {
                            MyLog.e("WindowTool", "response:(error)" + e.toString());
                        }

                        @Override
                        public void onResponse(Call call, Response response) throws IOException {
                            String json = response.body().string();
                            MyLog.e("WindowTool", "response(queryself):" + json);
                            Result result = GsonUtil.parseJsonWithGson(json, Result.class);
                            Map<String, String> map = result.getRets();
                            String players = map.get("listself");
                            saveSelfData(players);
                        }
                    });
                } else {
                    usernames.add(name);
                }
            }
        }
        QueryUser user = new QueryUser();
        user.setUsernames(usernames);
        user.setPlatType(appCount);
        data.setParam(gson.toJson(user));
        data.setReqno(TimeUtil.getCurrentDateToMinutes(new Date()) + ActionsTool.disposeNumber());
        data.setReqid(context.getSharedPreferences(LoginActivity.PREF_FILE, Context.MODE_PRIVATE).getString("name", ""));
        HttpUtil.sendPostRequestData("queryuser", gson.toJson(data), new Callback() {
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
                msg.arg1 = 0;
                handler.sendMessage(msg);
            }
        });
    }

    public void saveUserData(String json) {
        Gson gson = new Gson();
        if (appCount == 0) {
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
                        playerData.save();
                        List<UserName> userNames1 = DataSupport.where("name=? and plattype=?", playerData.getName(), appCount + "").find(UserName.class);
                        if (userNames1.size() == 0) {
                            DataSupport.deleteAll(UserName.class, "name=? and plattype=?", playerData.getName(), appCount + "");
                            UserName un = new UserName();
                            un.name = playerData.getName();
                            un.plattype = appCount;
                            un.save();
                        }
                    }
                }
            }
        } else if (appCount == 1) {
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
                        List<UserName> userNames1 = DataSupport.where("name=?", playerData.getName()).find(UserName.class);
                        if (userNames1.size() == 0) {
                            UserName un = new UserName();
                            un.name = playerData.getName();
                            un.save();
                        }
                    }
                }
            }
        } else if (appCount == 2) {
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
                        List<UserName> userNames1 = DataSupport.where("name=?", playerData.getName()).find(UserName.class);
                        if (userNames1.size() == 0) {
                            UserName un = new UserName();
                            un.name = playerData.getName();
                            un.save();
                        }
                    }
                }
            }
        } else if (appCount == 3) {
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

    public void saveSelfData(String json) {
        if (appCount == 0) {//德扑圈
            List<PlayerData> playerDatas = new ArrayList<PlayerData>();
            Type listType = new TypeToken<List<PlayerData>>() {
            }.getType();
            playerDatas = new Gson().fromJson(json, listType);
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
        } else if (appCount == 1) {//德友圈
            List<PlayerData1> playerDatas = new ArrayList<PlayerData1>();
            Type listType = new TypeToken<List<PlayerData1>>() {
            }.getType();
            playerDatas = new Gson().fromJson(json, listType);
            if (playerDatas != null) {
                for (int i = 0; i < playerDatas.size(); i++) {
                    PlayerData1 playerData = playerDatas.get(i);
                    if (playerData != null) {
                        List<PlayerData1> datas = DataSupport.where("name=? and seatFlag=?", playerData.getName(), playerData.getSeatFlag()).find(PlayerData1.class);
                        if (datas.size() > 0) {
                            DataSupport.deleteAll(PlayerData1.class, "name=? and seatFlag=?", playerData.getName(), playerData.getSeatFlag());
                        }
                        playerData.save();
                    }
                }
            }
        } else if (appCount == 2) {//扑克部落MTT
            List<PlayerData2> playerDatas = new ArrayList<PlayerData2>();
            Type listType = new TypeToken<List<PlayerData2>>() {
            }.getType();
            playerDatas = new Gson().fromJson(json, listType);
            if (playerDatas != null) {
                for (int i = 0; i < playerDatas.size(); i++) {
                    PlayerData2 playerData = playerDatas.get(i);
                    if (playerData != null) {
                        List<PlayerData2> datas = DataSupport.where("name=? and seatFlag=?", playerData.getName(), playerData.getSeatFlag()).find(PlayerData2.class);
                        if (datas.size() > 0) {
                            DataSupport.deleteAll(PlayerData2.class, "name=? and seatFlag=?", playerData.getName(), playerData.getSeatFlag());
                        }
                        playerData.save();
                    }
                }
            }
        } else if (appCount == 3) {//扑克部落SNG
            List<PlayerData3> playerDatas = new ArrayList<PlayerData3>();
            Type listType = new TypeToken<List<PlayerData3>>() {
            }.getType();
            playerDatas = new Gson().fromJson(json, listType);
            if (playerDatas != null) {
                for (int i = 0; i < playerDatas.size(); i++) {
                    PlayerData3 playerData = playerDatas.get(i);
                    if (playerData != null) {
                        List<PlayerData3> datas = DataSupport.where("name=? and seatFlag=?", playerData.getName(), playerData.getSeatFlag()).find(PlayerData3.class);
                        if (datas.size() > 0) {
                            DataSupport.deleteAll(PlayerData3.class, "name=? and seatFlag=?", playerData.getName(), playerData.getSeatFlag());
                        }
                        playerData.save();
                    }
                }
            }
        }
    }

    public void getUpdateSeatContents(SparseArray<String> names) {
        if (names == null || names.size() == 0)
            return;
        vpips.clear();
        flops.clear();
        for (int i = isWatch; i < playCount + isWatch; i++) {
            String name = names.get(i);
            if (name != null) {
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
            return "－|－|－\n－|－|－#-1";
        StringBuilder sb = new StringBuilder();
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
        List<PlayerData> playerDatas = getPlayerDatas(name);
        if (playerDatas.size() == 0 || "E".equals(name) || "玲".equals(name) || "玟".equals(name) || "C".equals(name) || "c".equals(name)
                || "5".equals(name) || "2".equals(name) || "河".equals(name) || "招".equals(name) || "[".equals(name)) {
            sb.append("－|－|－\n－|－|－#-1");
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
                flops.add(Double.valueOf(playerData.getFlopCount() * 100 / playCount));
                sb.append("#" + playerData.getLevel());
            }
        }
        return sb.toString();
    }

    public List<PlayerData> getPlayerDatas(String name) {
        List<PlayerData> playerDatas = new ArrayList<>();
        if (appCount == 0) {
            playerDatas = DataSupport.where("name=?", name).find(PlayerData.class);
        } else if (appCount == 1) {
            List<PlayerData1> playerData1s = DataSupport.where("name=?", name).find(PlayerData1.class);
            for (int i = 0; i < playerData1s.size(); i++) {
                playerDatas.add(playerData1s.get(i));
            }
        } else if (appCount == 2) {
            List<PlayerData2> playerData1s = DataSupport.where("name=?", name).find(PlayerData2.class);
            for (int i = 0; i < playerData1s.size(); i++) {
                playerDatas.add(playerData1s.get(i));
            }
        } else if (appCount == 3) {
            List<PlayerData3> playerData1s = DataSupport.where("name=?", name).find(PlayerData3.class);
            for (int i = 0; i < playerData1s.size(); i++) {
                playerDatas.add(playerData1s.get(i));
            }
        }
        return playerDatas;
    }

    //个人的总类
    public PlayerData getSelfPlayerData() {
        List<PlayerData> dataList = new ArrayList<>();
        if (appCount == 0) {
            dataList = where("name=?", "self").find(PlayerData.class);
        } else if (appCount == 1) {
            List<PlayerData1> list = where("name=?", "self").find(PlayerData1.class);
            for (PlayerData1 playerData : list) {
                dataList.add(playerData);
            }
        } else if (appCount == 2) {
            List<PlayerData2> list = where("name=?", "self").find(PlayerData2.class);
            for (PlayerData2 playerData : list) {
                dataList.add(playerData);
            }
        } else if (appCount == 3) {
            List<PlayerData3> list = where("name=?", "self").find(PlayerData3.class);
            for (PlayerData3 playerData : list) {
                dataList.add(playerData);
            }
        }
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
                player = getSelfPlayerData();
            } else {
                if ("E".equals(playerName) || "玲".equals(playerName) || "玟".equals(playerName) || "C".equals(playerName) || "c".equals(playerName)
                        || "5".equals(playerName) || "2".equals(playerName) || "河".equals(playerName) || "招".equals(playerName) || "[".equals(playerName)) {
                    player = new PlayerData();
                } else {
                    List<PlayerData> playerDatas = getPlayerDatas(playerName);
                    if (playerDatas.size() > 0) {
                        player = playerDatas.get(0);
                    }
                }
            }
        }
        if (player != null) {
            if ("self".equals(playerName)) {
                vh.pos1.setText("Hero" + Constant.getPercent(player, Constant.TYPE_HAND));
            } else {
                vh.pos1.setText(playerName + Constant.getPercent(player, Constant.TYPE_HAND));
            }
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
            vh.pos14.setText(Constant.percentTypes[13] + "(" + Constant.getPercent(player, Constant.TYPE_WTSD) + "%)");
            vh.pos15.setText(Constant.percentTypes[14] + "(" + Constant.getPercent(player, Constant.TYPE_WWSD) + "%)");
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
            vh.pos14.setText(Constant.percentTypes[13] + "(-%)");
            vh.pos15.setText(Constant.percentTypes[14] + "(-%)");
        }
        vh.item.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getCardsList(seatIdx, Constant.FLAG_REMARK);
            }
        });
        vh.note.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getCardsList(seatIdx, Constant.FLAG_NOTE);
            }
        });
        vh.note.setText("笔记:\n无");
        if (names.get(seatIdx) != null) {
            List<PlayerData> playerDatas = getPlayerDatas(names.get(seatIdx));
            if (playerDatas.size() > 0) {
                PlayerData playerData = playerDatas.get(0);
                if (!TextUtils.isEmpty(playerData.getRemark())) {
                    vh.note.setText("笔记:\n" + playerData.getRemark());
                }
            }
        }
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
            for (int j = 0; j < seatViews.size(); j++) {
                View text = seatViews.get(j);
                windowManager.removeViewImmediate(text);
            }
            seatViews.clear();
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
        @BindView(R.id.note)
        TextView note;

        ViewHolder(View view) {
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
            AutoUtils.autoSize(view);
        }
    }


    class SettingViewHolder {
        @BindView(R.id.sure)
        Button sure;
        @BindView(R.id.close)
        Button close;
        @BindView(R.id.seatIdx)
        TextView seatIdx;
        @BindView(R.id.level)
        TextView level;
        @BindView(R.id.line)
        TextView line;
        @BindView(R.id.color0)
        TextView color0;
        @BindView(R.id.color1)
        TextView color1;
        @BindView(R.id.color2)
        TextView color2;
        @BindView(R.id.color3)
        TextView color3;
        @BindView(R.id.color4)
        TextView color4;
        @BindView(R.id.color5)
        TextView color5;
        @BindView(R.id.color6)
        TextView color6;
        @BindView(R.id.color7)
        TextView color7;
        @BindView(R.id.color8)
        TextView color8;
        @BindView(R.id.select_colors)
        LinearLayout selectColors;
        @BindView(R.id.entry)
        LinearLayout entry;
        @BindView(R.id.name)
        EditText name;
        @BindView(R.id.nameList)
        ListView nameList;
        @BindView(R.id.search_name_layout)
        LinearLayout search_name_layout;
        @BindView(R.id.remarkContent)
        EditText remarkContent;
        @BindView(R.id.remark)
        TextView remark;
        @BindView(R.id.remark_layout)
        LinearLayout remark_layout;
        @BindView(R.id.card1)
        ImageView card1;
        @BindView(R.id.card2)
        ImageView card2;
        @BindView(R.id.card3)
        ImageView card3;
        @BindView(R.id.card4)
        ImageView card4;
        @BindView(R.id.card5)
        ImageView card5;
        @BindView(R.id.card6)
        ImageView card6;
        @BindView(R.id.card7)
        ImageView card7;
        @BindView(R.id.card8)
        ImageView card8;
        @BindView(R.id.card9)
        ImageView card9;
        @BindView(R.id.card10)
        ImageView card10;
        @BindView(R.id.card11)
        ImageView card11;
        @BindView(R.id.card12)
        ImageView card12;
        @BindView(R.id.card13)
        ImageView card13;
        @BindView(R.id.card14)
        ImageView card14;
        @BindView(R.id.card15)
        ImageView card15;
        @BindView(R.id.card16)
        ImageView card16;
        @BindView(R.id.turn_cards)
        LinearLayout turnCards;
        @BindView(R.id.changeEntry)
        TextView changeEntry;

        SettingViewHolder(View view) {
            ButterKnife.bind(this, view);
        }
    }
}
