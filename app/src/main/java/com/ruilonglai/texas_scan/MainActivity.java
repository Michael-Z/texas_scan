package com.ruilonglai.texas_scan;


import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.util.SparseArray;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.ruilonglai.texas_scan.activity.LoginActivity;
import com.ruilonglai.texas_scan.activity.SerialActivity;
import com.ruilonglai.texas_scan.activity.SettingActivity;
import com.ruilonglai.texas_scan.adapter.TabFragmentAdapter;
import com.ruilonglai.texas_scan.entity.JsonBean;
import com.ruilonglai.texas_scan.entity.MyData;
import com.ruilonglai.texas_scan.entity.OneHand;
import com.ruilonglai.texas_scan.entity.OneHandLog;
import com.ruilonglai.texas_scan.entity.PlayerData;
import com.ruilonglai.texas_scan.entity.PokerUser;
import com.ruilonglai.texas_scan.entity.QuerySerial;
import com.ruilonglai.texas_scan.entity.SerialInfo;
import com.ruilonglai.texas_scan.fragment.MineFragment;
import com.ruilonglai.texas_scan.fragment.PlayerFragment;
import com.ruilonglai.texas_scan.fragment.TargetFragment;
import com.ruilonglai.texas_scan.newprocess.Main;
import com.ruilonglai.texas_scan.util.ActionsTool;
import com.ruilonglai.texas_scan.newprocess.JsonTool;
import com.ruilonglai.texas_scan.newprocess.MainProcessUtil;
import com.ruilonglai.texas_scan.newprocess.MainServer;
import com.ruilonglai.texas_scan.newprocess.Package;
import com.ruilonglai.texas_scan.service.ScreenCapService;
import com.ruilonglai.texas_scan.util.AssetsCopyUtil;
import com.ruilonglai.texas_scan.util.Constant;
import com.ruilonglai.texas_scan.util.GsonUtil;
import com.ruilonglai.texas_scan.util.HttpUtil;
import com.ruilonglai.texas_scan.util.TimeUtil;
import com.ruilonglai.texas_scan.util.WindowTool;
import com.ruilonglai.texas_scan.view.TabContainerView;
import com.tencent.mm.opensdk.openapi.IWXAPI;

import org.litepal.crud.DataSupport;
import org.litepal.tablemanager.Connector;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import butterknife.ButterKnife;
import okhttp3.Call;
import okhttp3.Response;

import static com.ruilonglai.texas_scan.R.drawable.play;
import static org.litepal.crud.DataSupport.where;

public class MainActivity extends AppCompatActivity implements View.OnClickListener, ViewPager.OnPageChangeListener,AdapterView.OnItemClickListener{

   /* @BindView(R.id.test)
    TextView test;*/
    private static final String TAG = "MainActivity";

    private boolean isOpen = false;

    private boolean ninePointWindow;

    private ImageButton open_server;

    private List<String> percentList;

    private ScreenCapService.ChangeMsgBinder myBinder;

    private MineFragment mineFragment = new MineFragment();

    private PlayerFragment messageFragment = new PlayerFragment();

    private TargetFragment targetFragment = new TargetFragment();

    private MainServer mainServer = null;

    private DrawerLayout drawerLayout;

    private ListView listView;

    private FragmentManager fragmentManager;

    private String[] str;

    private WindowTool wt;

    private int fragIdx = 2;

    private int winIndex = 1;

    private int playCount;

    private int isWatch;

    private String percent = "";

    private SparseArray<String> names;
    /*tab图标集合*/
    private final int ICONS_RES[][] = {
            {R.mipmap.ic_message_normal,R.mipmap.ic_message_focus},
            {R.mipmap.ic_home_normal,R.mipmap.ic_home_focus},
            {R.mipmap.ic_mine_normal,R.mipmap.ic_mine_focus}
    };

    /*tab 颜色值 */
    private final int[] TAB_COLORS = new int[]{
            R.color.main_bottom_tab_textcolor_normal,
            R.color.main_bottom_tab_textcolor_selected};

    private Fragment[] fragments = {
            messageFragment,
            targetFragment,
            mineFragment
    };

    private String[] seatFlags = {"BTN", "SB", "BB", "UTG", "UTG+1", "MP", "MP+1", "HJ", "CO"};

    private boolean haveCreateWindow;

    private String phone = "";

    private String password = "";

    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.arg1){
                case Constant.SOCKET_BOARDS_AND_POKERS:
                    percent = (String) msg.obj;
                    wt.init(MainActivity.this,winIndex,phone);
                    if(!haveCreateWindow){
                        if(isWatch==0)
                        haveCreateWindow = wt.createWindow(percent);
                    }else{
                        if(isWatch==0)
                        wt.updateWindow(percent);
                    }
                    break;
                case Constant.SOCKET_CLOSE_WINDOW:
                    WindowTool.getInstance().deleteWindow();
                    haveCreateWindow = false;
                    break;
                case Constant.SOCKET_OPEN_WINDOW:
                    if(!haveCreateWindow){
                        if(!TextUtils.isEmpty(percent) && !"0.0%".equals(percent)){
                            wt.init(MainActivity.this,winIndex,phone);
                            if(isWatch==0)
                            haveCreateWindow = wt.createWindow(percent);
                        }
                    }
                    wt.createNinePointWindow(winIndex,playCount,names,isWatch);
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
                    wt.createNinePointWindow(winIndex, playCount, names,isWatch);
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
        open_server = (ImageButton) findViewById(R.id.open_server);
        open_server.setOnClickListener(this);
        initViews();
        Connector.getDatabase();//创建数据库
        SharedPreferences share = getSharedPreferences("data", MODE_PRIVATE);
        boolean isUpdateDB = share.getBoolean("isUpdateDB", false);
        if(!isUpdateDB){
            boolean isUpdate = AssetsCopyUtil.copySDCardToDataBase(MainActivity.this);
            SharedPreferences.Editor editor = getSharedPreferences("data",MODE_PRIVATE).edit();
            editor.putBoolean("isUpdateDB",isUpdate);
            editor.apply();
        }
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
                        MainProcessUtil.getInstance().exit(MainActivity.this);
                        MainProcessUtil.getInstance().createMainProcess(AssetsCopyUtil.getPackageName(MainActivity.this));
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
                        playCount = JsonTool.getJsonMes(pkg.getContent(),"seatcount");
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
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.open_server) {
            if(!isOpen){
                open_server.setImageDrawable(MainActivity.this.getResources().getDrawable(R.drawable.stop));
                MainProcessUtil.getInstance().exit(MainActivity.this);
                MainProcessUtil.getInstance().createMainProcess(AssetsCopyUtil.getPackageName(MainActivity.this));
            }else{
                open_server.setImageDrawable(this.getResources().getDrawable(play));
                try {
                    Package pkg = new Package();
                    pkg.setType( Constant.SOCKET_EXIT);
                    pkg.setContent("exit");
                    if(mainServer!=null)
                    mainServer.send(pkg);
                } catch (Exception e) {
                    Log.e("error","异常退出");
                    MainProcessUtil.getInstance().exit(MainActivity.this);
                }
            }
            isOpen = !isOpen;
        }
        if (v.getId() == R.id.showLog) {
           showDrawerLayout();
//            for (int i = 0; i < 6; i++) {
//                names.put(i,"name"+i);
//            }
//            wt.init(this,3,"18850547689");
//            wt.createNinePointWindow(3,6,names,0);
        }
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
            player.setSeatFlag(seatFlags[i]);
            player.setName("_self");
            List<PlayerData> self = where("name=? and seatFlag=?", "_self", seatFlags[i]).find(PlayerData.class);
            if (self.size() == 0) {//不存在则创建一个
                player.setDate(TimeUtil.getCurrentDateToDay(new Date()));
                player.save();
            }
        }

        drawerLayout=(DrawerLayout) findViewById(R.id.id_drawerlayout);
        drawerLayout.setDrawerListener(new DrawerLayout.SimpleDrawerListener() {
            @Override
            public void onDrawerClosed(View drawerView) {
                super.onDrawerClosed(drawerView);
            }
            @Override
            public void onDrawerOpened(View drawerView) {
                super.onDrawerOpened(drawerView);
            }
        });
        listView=(ListView) findViewById(R.id.listview);
        str = new String[] { "清除玩家信息", "清除个人数据", "全部清除","卸载前保存本地数据","数据显示设置","序列号信息","退出登录"};
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,
                android.R.layout.simple_list_item_1, str);
        listView.setAdapter(adapter);
        listView.setOnItemClickListener(this);
        TextView phon = (TextView) findViewById(R.id.phone);
        phon.setText(getSharedPreferences(LoginActivity.PREF_FILE, MODE_PRIVATE).getString("name", ""));
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
                mainServer.send(pkg);
                break;
            default:
                break;
        }
    }

    private void showDrawerLayout() {
        if (!drawerLayout.isDrawerOpen(Gravity.LEFT)) {
            drawerLayout.openDrawer(Gravity.LEFT);
        } else {
            drawerLayout.closeDrawer(Gravity.LEFT);
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

    /*抽屉选项*/
    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        switch (position){
            case 0:
                DataSupport.deleteAll(PlayerData.class, "not name=?", "_self");
                if(fragIdx==0){
                    messageFragment.notifyDataSetChaged();
                }
                break;
            case 1:
                DataSupport.deleteAll(PlayerData.class, "name=?","_self");
                for (int i = 0; i < 9; i++) {
                    PlayerData player = new PlayerData();
                    player.setSeatFlag(seatFlags[i]);
                    player.setName("_self");
                    List<PlayerData> self = DataSupport.where("name=? and seatFlag=?", "_self", seatFlags[i]).find(PlayerData.class);
                    if (self.size() == 0) {//不存在则创建一个
                        player.setDate(TimeUtil.getCurrentDateToDay(new Date()));
                        player.save();
                    }
                }
                mineFragment.getSelfSeatsData();
                break;
            case 2:
                DataSupport.deleteAll(PlayerData.class);
                DataSupport.deleteAll(MyData.class);
                DataSupport.deleteAll(OneHand.class);
                DataSupport.deleteAll(OneHandLog.class);
                mineFragment.getSelfSeatsData();
                break;
            case 3:
                AssetsCopyUtil.copyDataBaseToSD(MainActivity.this);
                break;
            case 4:
                Intent intent = new Intent(MainActivity.this, SettingActivity.class);
                startActivityForResult(intent,0);
                break;
            case 5:
                QuerySerial serial = new QuerySerial();
                serial.setUserid(phone);
                String json = new Gson().toJson(serial);
                HttpUtil.sendPostRequestData("queryserial",json,new okhttp3.Callback(){
                    @Override
                    public void onFailure(Call call, IOException e) {
                        Toast.makeText(MainActivity.this,"获取序列号信息失败",Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onResponse(Call call, Response response) throws IOException {
                        String string = response.body().string();
//                        JsonBean jsonBean = GsonUtil.parseJsonWithGson(string, JsonBean.class);
//                        Log.e(TAG,string);
//                        if(jsonBean.result=="true"){
//                            string = "已登录终端数:"+jsonBean.logined+"#"+
//                                    "剩余可登陆终端数:"+jsonBean.remained+"#";
//                            if(jsonBean.serialInfos!=null){
//                                int size = jsonBean.serialInfos.size();
//                                for (int i = 0; i < size; i++) {
//                                    SerialInfo serialInfo = jsonBean.serialInfos.get(i);
//                                    string+= "serialno:"+serialInfo.getSerialno()+"    "+"remaindays:"+serialInfo.getRemaindays()+"#";
//                                }
//                            }
//                            Intent intent = new Intent(MainActivity.this, SerialActivity.class);
//                            intent.putExtra("log",string);
//                            startActivity(intent);
//                        }else{
//                            Toast.makeText(MainActivity.this,"获取序列号信息失败",Toast.LENGTH_SHORT).show();
//                        }
                    }
                });
                break;
            case 6:
                PokerUser pu = new PokerUser();
                pu.id = phone;
                pu.nick = "";
                pu.passwd = password;
                pu.license = 10;
                Gson gson = new Gson();
                String jsonstr = gson.toJson(pu);
                HttpUtil.sendPostRequestData("logout",jsonstr,new okhttp3.Callback(){

                    @Override
                    public void onFailure(Call call, IOException e) {
                       Log.e(TAG,e.toString());
                    }

                    @Override
                    public void onResponse(Call call, Response response) throws IOException {
                         Log.e(TAG,response.toString());
                        Intent in = new Intent(MainActivity.this,LoginActivity.class);
                        startActivity(in);
                        finish();
                    }
                });
                break;
        }
        showDrawerLayout();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == 0 && resultCode == 1){
            String result = data.getStringExtra("result");
            Log.e(TAG,"设置页面response:"+result);
            if(wt!=null)
            wt.clearPercents();
        }
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        WindowTool.getInstance().deleteWindow();
        if(fragIdx==0){
            /*刷新玩家列表*/
            messageFragment.notifyDataSetChaged();
        }else if(fragIdx == 2){
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
