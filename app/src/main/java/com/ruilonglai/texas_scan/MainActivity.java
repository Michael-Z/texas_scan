package com.ruilonglai.texas_scan;


import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.util.SparseArray;
import android.view.KeyEvent;
import android.widget.ListView;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;
import com.ruilonglai.texas_scan.activity.LoginActivity;
import com.ruilonglai.texas_scan.adapter.TabFragmentAdapter;
import com.ruilonglai.texas_scan.entity.PlayerData;
import com.ruilonglai.texas_scan.entity.QueryUserName;
import com.ruilonglai.texas_scan.entity.ReqData;
import com.ruilonglai.texas_scan.entity.Result;
import com.ruilonglai.texas_scan.entity.UserName;
import com.ruilonglai.texas_scan.fragment.HomeFragment;
import com.ruilonglai.texas_scan.fragment.MineFragment;
import com.ruilonglai.texas_scan.fragment.PlayerFragment;
import com.ruilonglai.texas_scan.fragment.PokerDetialsFragment;
import com.ruilonglai.texas_scan.fragment.SettingFragment;
import com.ruilonglai.texas_scan.newprocess.JsonTool;
import com.ruilonglai.texas_scan.newprocess.MainServer;
import com.ruilonglai.texas_scan.newprocess.Package;
import com.ruilonglai.texas_scan.util.ActionsTool;
import com.ruilonglai.texas_scan.util.AssetsCopyUtil;
import com.ruilonglai.texas_scan.util.Constant;
import com.ruilonglai.texas_scan.util.GsonUtil;
import com.ruilonglai.texas_scan.util.HttpUtil;
import com.ruilonglai.texas_scan.util.TimeUtil;
import com.ruilonglai.texas_scan.util.WindowTool;
import com.ruilonglai.texas_scan.view.TabContainerView;

import org.litepal.crud.DataSupport;
import org.litepal.tablemanager.Connector;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import butterknife.ButterKnife;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

import static org.litepal.crud.DataSupport.where;

public class MainActivity extends AppCompatActivity implements ViewPager.OnPageChangeListener{

    private static final String TAG = "MainActivity";

    private boolean isOpen = false;

    private boolean hideWP;

    private List<String> percentList;


    private MineFragment mineFragment = new MineFragment();

    private PlayerFragment messageFragment = new PlayerFragment();

    private SettingFragment settingFragment = new SettingFragment();

    private HomeFragment homeFragment = new HomeFragment();

    private PokerDetialsFragment pokerDetialsFragment = new PokerDetialsFragment();

    private MainServer mainServer = null;

    private DrawerLayout drawerLayout;

    private ListView listView;

    private FragmentManager fragmentManager;

    public WindowTool wt;

    private int fragIdx = 2;

    public int winIndex = 0;

    private int seatCount;

    private int playCount;

    private int isWatch;


    private String percent = "";

    private SparseArray<String> names;
    /*tab图标集合*/
    private final int ICONS_RES[][] = {
            {R.mipmap.ic_message_normal,R.mipmap.ic_message_focus},
            {R.mipmap.ic_mine_normal,R.mipmap.ic_mine_focus},
            {R.mipmap.bottom_home,R.mipmap.bottom_home2},
            {R.mipmap.bottom_poker,R.mipmap.bottom_poker2},
            {R.mipmap.bottom_setting,R.mipmap.bottom_setting2}
    };

    /*tab 颜色值 */
    private final int[] TAB_COLORS = new int[]{
            R.color.main_bottom_tab_textcolor_normal,
            R.color.main_bottom_tab_textcolor_selected};

    private Fragment[] fragments = {
            messageFragment,
            mineFragment,
            homeFragment,
            pokerDetialsFragment,
            settingFragment
    };

    private boolean haveCreateWindow;

    public String phone = "";

    public String password = "";

    private boolean haveOpenWindow;

    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.arg1){
                case Constant.SOCKET_BOARDS_AND_POKERS:
                    percent = (String) msg.obj;
                    wt.init(MainActivity.this,winIndex,phone);
                    if(/*!hideWP*/false){
                        if(!haveCreateWindow){
                            if(isWatch==0)
                                haveCreateWindow = wt.createWindow(percent);
                        }else{
                            if(isWatch==0)
                                wt.updateWindow(percent);
                        }
                    }
                    break;
                case  Constant.SOCKET_ONE_HAND_LOG:

                    break;
                case Constant.SOCKET_CLOSE_WINDOW:
                    WindowTool.getInstance().deleteWindow(false);
                    haveCreateWindow = false;
                    haveOpenWindow = false;
                    break;
                case Constant.SOCKET_OPEN_WINDOW:
//                    if(!haveCreateWindow && !hideWP){
//                        if(!TextUtils.isEmpty(percent) && !"0.0%".equals(percent)){
//                            wt.init(MainActivity.this,winIndex,phone);
//                            if(isWatch==0)
//                            haveCreateWindow = wt.createWindow(percent);
//                        }
//                    }
                    wt.createOpenView(MainActivity.this);
                    wt.createNinePointWindow(winIndex,seatCount,names,isWatch);
                    break;
                case Constant.SOCKET_GET_TEMPLATE:
                    setTemplate(winIndex+8);
                    break;
                case Constant.SOCKET_KNOW_NAME:
                    String json = (String) msg.obj;
                    SparseArray array = GsonUtil.parseJsonWithGson(json, SparseArray.class);
                    for (int i = 0; i < array.size(); i++) {
                        int seatIdx = array.keyAt(i);
                        names.put(seatIdx,(String)array.get(seatIdx));
                    }
                    wt.createNinePointWindow(winIndex, seatCount, names,isWatch);
                    break;
                case Constant.SOCKET_SEATCOUNT_CHANGE:

                    break;
                case Constant.SOCKET_SEATCOUNT:
                    if(!haveOpenWindow){
                        haveOpenWindow =  wt.createOpenView(MainActivity.this);
                    }
                    break;
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Intent intent = getIntent();
        Bundle bundle = intent.getBundleExtra("user");
        String[] msgs = bundle.getStringArray("user");
        phone = msgs[0];
        password = msgs[1];
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
        Connector.getDatabase();//创建数据库
        initViews();
        SharedPreferences share = getSharedPreferences("data", MODE_PRIVATE);
        boolean isUpdateDB = share.getBoolean("isUpdateDB", false);
        if(!isUpdateDB){
            boolean isUpdate = AssetsCopyUtil.copySDCardToDataBase(MainActivity.this);
            SharedPreferences.Editor editor = getSharedPreferences("data",MODE_PRIVATE).edit();
            editor.putBoolean("isUpdateDB",isUpdate);
            editor.apply();
        }
        hideWP = getSharedPreferences(LoginActivity.PREF_FILE, MODE_PRIVATE).getBoolean("hidewinpercent", false);
        mainServer = MainServer.newInstance();
        mainServer.setCallBack(new MainServer.CallBack() {
            @Override
            public void recMsg(String json) {
                Log.e("response",""+json);
                Gson gson = new Gson();
                Package pkg = gson.fromJson(json, Package.class);
                Message msg  = new Message();
                switch (pkg.getType()){
                    case Constant.SOCKET_RESTART_MAIN_PROCESS:
//                        MainProcessUtil.getInstance().exit(MainActivity.this);
//                        MainProcessUtil.getInstance().createMainProcess(AssetsCopyUtil.getPackageName(MainActivity.this));
                        break;
                    case Constant.SOCKET_BOARDS_AND_POKERS://手牌和公共牌
                        msg.arg1 = Constant.SOCKET_BOARDS_AND_POKERS;
                        msg.obj = pkg.getContent();
                        handler.sendMessage(msg);
                        break;
                    case Constant.SOCKET_ONE_HAND_LOG://一手详情，异步处理
                        new ActionsTool().disposeAction(pkg.getContent(),null,phone);
                        msg.arg1 = Constant.SOCKET_ONE_HAND_LOG;
                        handler.sendMessage(msg);
                        break;
                    case Constant.SOCKET_SEATCOUNT://几人桌
                        msg.arg1 = Constant.SOCKET_SEATCOUNT;
                        seatCount = JsonTool.getJsonMes(pkg.getContent(),"seatcount");
                        isWatch = JsonTool.getJsonMes(pkg.getContent(),"iswatch");
                        msg.arg1 = Constant.SOCKET_SEATCOUNT;
                        handler.sendMessage(msg);
                        break;
                    case Constant.SOCKET_SEATCOUNT_CHANGE://几人桌
                        msg.arg1 = Constant.SOCKET_SEATCOUNT_CHANGE;
                        seatCount = JsonTool.getJsonMes(pkg.getContent(),"seatcount");
                        isWatch = JsonTool.getJsonMes(pkg.getContent(),"iswatch");
                        break;
                    case Constant.SOCKET_CLOSE_WINDOW://关闭悬浮窗
                        msg.arg1 = Constant.SOCKET_CLOSE_WINDOW;
                        handler.sendMessage(msg);
                        break;
                    case Constant.SOCKET_OPEN_WINDOW://重启悬浮窗
                        msg.arg1 = Constant.SOCKET_OPEN_WINDOW;
                        handler.sendMessage(msg);
                        break;
                    case Constant.SOCKET_GET_TEMPLATE:
                        msg.arg1 = Constant.SOCKET_GET_TEMPLATE;
                        handler.sendMessage(msg);
                        break;
                    case Constant.SOCKET_KNOW_NAME:
                        msg.arg1 = Constant.SOCKET_KNOW_NAME;
                        msg.obj = pkg.getContent();
                        handler.sendMessage(msg);
                        break;
                }
            }
        });
        for (int i = 0; i < Constant.APPNAMES.length; i++) {
            ReqData data = new ReqData();
            QueryUserName queryUserName = new QueryUserName();
            queryUserName.setPlattype(i);
            String param = new Gson().toJson(queryUserName);
            data.setParam(param);
            data.setReqno(TimeUtil.getCurrentDateToMinutes(new Date()) + ActionsTool.disposeNumber());
            data.setReqid(getSharedPreferences(LoginActivity.PREF_FILE, Context.MODE_PRIVATE).getString("name", ""));
            HttpUtil.sendPostRequestData("queryusernames", new Gson().toJson(data), new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    Log.e("WindowTool", "response:(error)" + e.toString());
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    String json = response.body().string();
                    Log.e("MineFragment", "response:" + json);
                    try {
                        Result result = GsonUtil.parseJsonWithGson(json, Result.class);
                        Map<String, String> map = result.getRets();
                        String players = map.get("listnames");
                        List<String> names = new ArrayList<String>();
                        Type listType = new TypeToken<List<String>>() {
                        }.getType();
                        names = new Gson().fromJson(players, listType);
                        if (names != null) {
                            for (int j = 0; j < names.size(); j++) {
                                UserName userName = new UserName();
                                List<UserName> userNames = DataSupport.where("name=?", names.get(j)).find(UserName.class);
                                if(userNames.size()==0){
                                    userName.name = names.get(j);
                                    userName.save();
                                }
                            }
                        }
                    } catch (JsonSyntaxException e) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
//                                Toast.makeText(MainActivity.this,"更新数据异常",Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                }
            });
        }
//        StrictMode.setThreadPolicy(new StrictMode.ThreadPolicy.Builder()
//                .detectDiskReads().detectDiskWrites().detectNetwork()
//                .penaltyLog().build());
//        StrictMode.setVmPolicy(new StrictMode.VmPolicy.Builder()
//                .detectLeakedSqlLiteObjects().detectLeakedClosableObjects()
//                .penaltyLog().penaltyDeath().build());
    }
    private void initViews() {
        percentList = new ArrayList<>();
        names = new SparseArray<>();
        TabFragmentAdapter mAdapter = new TabFragmentAdapter(getSupportFragmentManager(), fragments);
        ViewPager mPager = (ViewPager) findViewById(R.id.tab_pager);
        //设置当前可见Item左右可见page数，次范围内不会被销毁
        mPager.setOffscreenPageLimit(1);
        mPager.setAdapter(mAdapter);
        TabContainerView mTabLayout = (TabContainerView) findViewById(R.id.ll_tab_container);
        mTabLayout.setOnPageChangeListener(this);
        mTabLayout.initContainer(getResources().getStringArray(R.array.tab_main_title), ICONS_RES, TAB_COLORS, true);

        int width = getResources().getDimensionPixelSize(R.dimen.tab_icon_width);
        int height = getResources().getDimensionPixelSize(R.dimen.tab_icon_height);
        mTabLayout.setContainerLayout(R.layout.tab_container_view, R.id.iv_tab_icon, R.id.tv_tab_text, width, height);
        mTabLayout.setViewPager(mPager);
        //设置第一个显示的界面(碎片)
        mPager.setCurrentItem(getIntent().getIntExtra("tab",2));
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

        wt = WindowTool.getInstance();
        wt.init(MainActivity.this,winIndex,phone);
        fragmentManager = getSupportFragmentManager();
    }
    /*设置解析模板*/
    public void setTemplate(int type){
        winIndex = type-8;
        switch (type){
            case Constant.SOCKET_PLATFORM_POKERFISHS:
            case Constant.SOCKET_PLATFORM_TEXASPOKER:
            case Constant.SOCKET_PLATFORM_NUTSPOKER:
            case Constant.SOCKET_PLATFORM_NUTSPOKER_SNG:
                Package pkg = new Package();
                pkg.setType(type);
                if(mainServer!=null)
                mainServer.send(pkg,this);
                break;
            default:
                break;
        }
    }
    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {}

    @Override
    public void onPageSelected(int position) {

        for (int index = 0, len = fragments.length; index < len; index++) {
            fragments[index].onHiddenChanged(index != position);
        }
        if(position==0){
            messageFragment.notifyDataSetChaged();
        }
        fragIdx = position;
    }

    @Override
    public void onPageScrollStateChanged(int state) {}

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == 0 && resultCode == 1){
            String result = data.getStringExtra("result");
            Log.e(TAG,"设置页面response:"+result);
            if(wt!=null)
            wt.clearPercents();
        }
        hideWP = getSharedPreferences(LoginActivity.PREF_FILE, MODE_PRIVATE).getBoolean("hidewinpercent", false);
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        WindowTool.getInstance().deleteWindow(false);
        haveOpenWindow = false;
        if(fragIdx==0){
            /*刷新玩家列表*/
            messageFragment.notifyDataSetChaged();
        }else if(fragIdx == 1){
            /*刷新个人列表*/
            mineFragment.getSelfSeatsData();
        }
    }
    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        if (event.getKeyCode() == KeyEvent.KEYCODE_BACK && event.getRepeatCount() == 0 && event.getAction() == KeyEvent.ACTION_DOWN) {
            System.exit(0);
        }
        return super.dispatchKeyEvent(event);
    }
}
