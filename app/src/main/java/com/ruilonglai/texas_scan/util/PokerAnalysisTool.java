package com.ruilonglai.texas_scan.util;

import android.graphics.Bitmap;
import android.text.TextUtils;
import android.util.Log;
import android.util.SparseArray;
import android.util.SparseIntArray;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.ruilonglai.texas_scan.ScanTool;
import com.ruilonglai.texas_scan.entity.Boards;
import com.ruilonglai.texas_scan.entity.GameAction;
import com.ruilonglai.texas_scan.entity.GameUser;
import com.ruilonglai.texas_scan.entity.PokerRecord;
import com.ruilonglai.texas_scan.entity.SaveRecordParam;
import com.ruilonglai.texas_scan.entity.Seat;
import com.ruilonglai.texas_scan.entity.TableRecord;
import com.ruilonglai.texas_scan.newprocess.Connect;
import com.ruilonglai.texas_scan.newprocess.JsonTool;
import com.ruilonglai.texas_scan.newprocess.Main;
import com.ruilonglai.texas_scan.newprocess.Package;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.BlockingDeque;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.ruilonglai.texas_scan.newprocess.JsonTool.getJsonMes;
import static com.ruilonglai.texas_scan.newprocess.JsonTool.getJsonName;

/**
 * Created by Administrator on 2017/8/10.
 */

public class PokerAnalysisTool {

    public final static String TAG = "PokerAnalysisTool";
    private int btnIdx = -1;//庄位
    private int card1 = -1;//自己的手牌1
    private int card2 = -1;//自己的手牌2
    private int straddle = -1;
    private int ante = -1;
    private int blinds = -1;
    private int seatCount = -1;
    private int dichi = -1;
    private int curDichi = -1;
    private int playerCount = -1;
    private boolean isBegin = false;
    private boolean canAddAction = false;
    private SparseArray<String> seatNames;//每个位置解析的名字
    private ExecutorService service;
    private List<Integer> boards;
    private int curRoundIdx = -1;
    private int changeMoney = -1;
    private boolean newBtnIdx;
    private boolean needSendBoards;
    private boolean sendCloseWindow;
    private int isWatch;
    private int flag;
    private boolean allOnline;
    private boolean havePoker;
    private SparseArray<Seat> seats;//每个位置的信息
    private SparseIntArray lastSeats;//上一条记录各位置的钱
    public TableRecord tableRecord;
    private PokerRecord pokerRecord;
    private List<GameAction> actions;//每个玩家得动作
    private SparseArray<Integer> playSeats;
    private SparseArray<GameUser> gamers;//当前牌桌玩家信息
    /*还原一些数据*/
    private void initView()
    {
        straddle = -1;
        ante = -1;
        blinds = -1;
        seatCount = -1;
        card1 = -1;
        card2 = -1;
        isWatch = 0;
        curRoundIdx = -1;
        needSendBoards = false;
        havePoker = false;
        allOnline = false;
        canAddAction = false;
        boards.clear();
        tableRecord = new TableRecord();
        pokerRecord = new PokerRecord();
        actions.clear();
        gamers.clear();
        seats.clear();
        playSeats.clear();
        changeMoney = -1;
    }

    public void analysisBitmap(Bitmap bitmap,int flag)
    {
        this.flag = flag;
        if(bitmap==null){
            return;
        }
        int isImgOK = ScanTool.IsImgOK(bitmap);
        Log.e(TAG,isImgOK+"");
        if(isImgOK==1)
        {
            if(sendCloseWindow){
                Package pkg = new Package();
                pkg.setType(Constant.SOCKET_OPEN_WINDOW);
                Connect.send(pkg);
                sendCloseWindow = false;
            }
            newBtnIdx = isNewBtnIdx(bitmap);
            if(newBtnIdx)
            {
                saveLastEndMoney(bitmap);
                saveLastHandData();
                Log.e("GameAction","--------------------一手记录的动作begin-------------------");
                for (int i = 0; i < actions.size(); i++)
                {
                    GameAction gameAction = actions.get(i);
                    Log.e("GameAction",gameAction.toString());
                }
                Log.e("GameAction","--------------------一手记录的动作end---------------------");
                initView();
            }
            getBaseMessage(bitmap);
            Log.e(TAG,"canAddAction "+canAddAction);
            if(!canAddAction)
            {
                canAddAction = haveHalfBlindsAndBlinds();
                if(canAddAction){
                    for (int i = 0; i < seats.size(); i++)
                    {
                        int seatIdx = seats.keyAt(i);
                        Seat seat = seats.get(seatIdx);
                        lastSeats.put(seatIdx,seat.getMoney());
                        GameUser user = gamers.get(seatIdx);
                        if(user != null)
                        {
                            user.setBeginMoney(seat.getMoney()+seat.getBet());
                        }
                    }
                    if(straddle>0)
                    {
                        changeMoney = straddle;
                    }
                    else
                    {
                        changeMoney = blinds;
                    }
                }
            }
            disposeMoney();//记录钱的变化
            if(newBtnIdx)
            {//解析名字
                try {
                    bitmaps.add(bitmap);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            else
            {
                bitmap.recycle();
                bitmap = null;
                System.gc();
            }
        }
        else
        {
            if(!sendCloseWindow)
            {
                Package pkg = new Package();
                pkg.setType(Constant.SOCKET_CLOSE_WINDOW);
                Connect.send(pkg);
                sendCloseWindow = true;
            }
        }
    }
    private void saveOtherPlayerCards(){
        for (int j = 0; j < playSeats.size(); j++) {
            int seatIdx = playSeats.keyAt(j);
            Seat seat = seats.get(seatIdx);
            int card1 = seat.getCard1();
            int card2 = seat.getCard2();
            if(card1!=-1 && card2!=-1){
                GameUser gameUser = gamers.get(seatIdx);
                if(gameUser!=null){
                    gameUser.setCard1(card1);
                    gameUser.setCard2(card2);
                }
            }
        }
    }
    /*从图片获取位置名字*/
    private void getSeatNames(Bitmap bitmap)
    {
        if(bitmap==null)
            return;
        if(seatCount<=0)
            return;
        synchronized (gamers)
        {
            for (int i = 0; i < gamers.size(); i++)
            {
                int seatIdx = gamers.keyAt(i);
                String name = getJsonName(ScanTool.ScanSeatName(bitmap, seatIdx, 0), "name");

//                String name = "位置"+seatIdx;
                Log.e("name",seatIdx+"  "+name);
                String userName = FileIOUtil.replaceBlank(name);
                userName.replace("\\n","");
//                if(flag==2 || flag == 3)
//                userName = removeDigital(userName);
//                Log.e("name","flag:"+flag+ " " +seatIdx+" 去除数字 "+userName);
                if(!TextUtils.isEmpty(userName))
                seatNames.put(seatIdx,userName);
                GameUser user = gamers.get(seatIdx);
                if(user!=null && (!TextUtils.isEmpty(userName)) && (!userName.contains("+")) && (!"null".equals(userName)))
                {
                    user.setUserName(userName);
                }else{
                    if(seatNames.get(seatIdx)!=null)
                    user.setUserName(seatNames.get(seatIdx));
                }
                if(seatIdx==0)
                {
                    user.setUserName("self");
                    seatNames.put(seatIdx,"self");
                }
            }
            String json = new Gson().toJson(seatNames);
            Package pkg = new Package();
            pkg.setType(Constant.SOCKET_KNOW_NAME);
            pkg.setContent(json);
            Connect.send(pkg);
        }
    }
    public static String removeDigital(String value){
        Pattern p = Pattern.compile("[\\d]");
        Matcher matcher = p.matcher(value);
        String result = matcher.replaceAll("");
       return result;
    }
    /*一手只需识别一次的数据*/
    private void getBaseMessage(Bitmap bitmap)
    {
        try {
            Log.e("seatCount","识别座位数");
            String json = ScanTool.ScanSeatCount(bitmap);
            int count = getJsonMes(json,"seatcount");
            if(seatCount==-1){
                if(count!=-1){
                  seatCount = count;
                  isWatch = getJsonMes(json,"iswatch");
                  Package pkg = new Package();
                  pkg.setType(Constant.SOCKET_SEATCOUNT);
                  pkg.setContent(json);
                  Connect.send(pkg);
                }
            }else{
                if(seatCount != count)
                {
                    btnIdx = -1;
                    seatNames.clear();
                    Package pkg = new Package();
                    pkg.setType(Constant.SOCKET_SEATCOUNT_CHANGE);
                    pkg.setContent(json);
                    Connect.send(pkg);
                    initView();
                    return;
                }
            }
            json = ScanTool.ScanCurDichi(bitmap);
            curDichi = getJsonMes(json,"curdichi ");
            json = ScanTool.ScanDichi(bitmap);
            Log.e(TAG,json);
            dichi = getJsonMes(json,"dichi");
            json = ScanTool.ScanAnte(bitmap);
            Log.e(TAG,json);
            ante = getJsonMes(json,"ante");
            if(blinds == -1 || blinds == 0)
            {
                json = ScanTool.ScanBlinds(bitmap);
                Log.e("blindsjson",json);
                blinds =  getJsonMes(json,"blinds");
                straddle = getJsonMes(json,"straddle");
                if(straddle>0){
                    straddle = blinds*2;
                }
            }
            boards = JsonTool.getBoards(ScanTool.ScanBoardCards(bitmap)).getBoards();
            if(boards.size()>curRoundIdx && (boards.size()==0 || boards.size()==3 || boards.size()==4 || boards.size()==5 ))
            {
                sendWinPercent();
                needSendBoards = true;
                curRoundIdx = boards.size();
                changeMoney = -1;
            }else{
                needSendBoards = false;
            }
            getSeats(bitmap);
        } catch (Exception e) {
            Log.e("error","getBaseMessage  "+e.toString());
        }
    }

    public void getSeats(Bitmap bitmap){
        if(seatCount<2)
            return;
        Gson gson = new Gson();
        if(newBtnIdx){
            for (int i = isWatch; i < seatCount+isWatch; i++)
            {
                String json = ScanTool.ScanSeat(bitmap, i);
                Seat seat = gson.fromJson(json, Seat.class);
                Log.e(TAG,i+"号位"+seat.toString());
                if(seat.getMoney() > 0 || i == btnIdx || seat.getHidecard()==1)
                {
                    playSeats.put(i,i);
                }
                seats.put(i,seat);
            }
            playerCount = playSeats.size();
        }else{
            for (int i = isWatch; i < seatCount+isWatch; i++)
            {
                if(playSeats.get(i) != null)
                {
                    String json = ScanTool.ScanSeat(bitmap, i);
                    Seat seat = gson.fromJson(json, Seat.class);
                    if(i==0 && !havePoker)
                    {
                        if(seat.getCard1()!=-1 && seat.getCard2()!=-1)
                        {
                            havePoker = true;
                            card1 = seat.getCard1();
                            card2 = seat.getCard2();
                            Log.e(TAG,"解析出手牌");
                            sendWinPercent();
                        }else{
                            havePoker = false;
                        }
                    }
                    Log.e(TAG,i+"号位"+seat.toString());
                    seats.put(i,seat);
                }
            }
            /*删除旁观*/
            if(havePoker && playSeats.get(seatCount)!=null)
            {
                playSeats.delete(seatCount);
                List<GameAction> newActions = new ArrayList<>();
                for (int i = 0; i < actions.size(); i++)
                {
                    GameAction action = actions.get(i);
                    int seatIdx = action.getSeatIdx();
                    if(seatIdx!=seatCount)
                    {
                       newActions.add(action);
                    }
                }
                actions.clear();
                actions.addAll(newActions);
            }
        }
    }

    private void sendWinPercent(){
        Gson gson1 = new Gson();
        Boards bod = new Boards();
        bod.setBoards(boards);
        bod.setSeat(seats.get(0));
        Package pkg = new Package();
        pkg.setType(Constant.SOCKET_BOARDS_AND_POKERS);
        int[] cards = new int[]{-1,-1,-1,-1,-1};
        if(boards.size()==3 || boards.size()==4 || boards.size()==5)
        {
            for (int i = 0; i < boards.size(); i++)
            {
                cards[i] = boards.get(i).intValue();
            }
        }
        if(isWatch==0){
            pkg.setContent(CardUtil.getWinPercent(cards,card1,card2,playerCount)+"%");
            Connect.send(pkg);
        }
    }
    /*旁观者模式 是否庄位改变 用于判断新的一手*/
     private boolean isNewBtnIdx(Bitmap bitmap)
     {
         int curBtnIdx = getJsonMes(ScanTool.ScanBtn(bitmap),"btnIdx");
         if(curBtnIdx==-1)
             return false;
         if(btnIdx==-1)
         {
             btnIdx = curBtnIdx;
         }else{
             if(curBtnIdx != btnIdx)
             {
                 isBegin = true;
                 pokerRecord.setButton(btnIdx);
                 btnIdx = curBtnIdx;
                 return true;
             }
         }
         return false;
     }
     /*保存图片到SD卡中*/
     public void saveImgToSDCard(Bitmap bitmap,int testType,int i)
     {
         String path = "";
         switch (testType)
         {
             case Constant.TEST_SEATCOUNT:
                 path = "/sdcard/desk_scan/" + "test_seatcount"+TimeUtil.getCurrentDateToSecond(new Date()) + ".png";
                 break;
             case Constant.TEST_NAME:
                 path = "/sdcard/desk_scan/" + i + ".png";
                 break;
         }
         i++;
         try {
             FileOutputStream os = new FileOutputStream(new File(path));
             bitmap.compress(Bitmap.CompressFormat.PNG,100,os);
             os.flush();
             os.close();
         } catch (IOException e) {
             Log.e(TAG,"保存图片失败！");
         }
     }
     private void saveLastEndMoney(Bitmap bitmap)
     {
         Gson gson = new Gson();
         for (int i = isWatch; i < seatCount+isWatch; i++)
         {
             String json = ScanTool.ScanSeat(bitmap, i);
             Seat seat = gson.fromJson(json, Seat.class);
             Log.e(TAG,i+"号位endMoney-->"+seat.getMoney() + " bet-->"+seat.getBet());
             GameUser user = gamers.get(i);
             if(user!=null){
                 user.setEndMoney(seat.getMoney()+seat.getBet());
             }
         }
     }
    /*保存上一手数据*/
    private void saveLastHandData()
    {
        List<GameUser> users = new ArrayList<>();
        for (int i = 0; i < gamers.size(); i++)
        {
            users.add(gamers.valueAt(i));
        }
        tableRecord.setAnte(ante);
        tableRecord.setStraddle(straddle);
        tableRecord.setBlindType(blinds);
        tableRecord.setMaxPlayCount(seatCount);
        Log.e("button",btnIdx+"");
        pokerRecord.setPoker(new int[]{card1,card2});
        if(boards.size()>=3)
        {
            pokerRecord.setFlop(new int[]{boards.get(0),boards.get(1),boards.get(2)});
        }
        if(boards.size()>=4)
        {
            pokerRecord.setTurn(boards.get(3));
        }
        if(boards.size()==5)
        {
            pokerRecord.setRiver(boards.get(4));
        }
        pokerRecord.setUsers(users);
        pokerRecord.setActions(actions);
        SaveRecordParam param = new SaveRecordParam();
        param.setTableRecord(tableRecord);
        Gson gson = new Gson();
        param.setPokerRecord(pokerRecord);
        Package pkg = new Package();
        pkg.setType(Constant.SOCKET_ONE_HAND_LOG);
        if((card1 != -1 && card2 != -1 && isWatch==0) || (card1==-1 && card2==-1 && isWatch==1)){
            pkg.setContent(gson.toJson(param, SaveRecordParam.class));
            Connect.send(pkg);
        }
    }
    public void disposeMoney()
    {
        Log.e(TAG,"allOnLine-->"+allOnline);
        if(!allOnline)
        {
            for (int i = 0; i < playSeats.size(); i++)
            {
                int seatIdx = playSeats.keyAt(i);
                Seat seat = seats.get(seatIdx);
                if (seat.getHidecard()==0 && seatIdx!=0 )
                {
                    break;
                }
                if(i==playSeats.size()-1)
                {
                    allOnline = true;
                }
            }
        }
        if(newBtnIdx){
            Log.e(TAG,"money初始化");
            for (int i = isWatch; i < seatCount+isWatch; i++)
            {
                Seat seat = seats.get(i);
                if(seat!=null && seat.getMoney()>=0)
                {
                    /*上一手的最后一条记录*/
                    if(seat.getMoney()>0 || seat.getHidecard()==1){
                        GameUser gamer = new GameUser();
                        gamer.setBeginMoney(lastSeats.get(i));
                        gamer.setSeatIdx(i);
                        gamers.put(i,gamer);
                    }
                    lastSeats.put(i,seat.getMoney());
                }
            }
        }else{
            if(btnIdx!=-1)
            {
                if(!canAddAction) {
                    return;
                }
                int indexOfValue = playSeats.indexOfValue(btnIdx);
                for (int i = indexOfValue; i < playSeats.size()+indexOfValue; i++)
                {
                    int seatIdx = -1;
                    if(i<playSeats.size())
                    {
                        seatIdx = playSeats.valueAt(i);
                    }else{
                        seatIdx = playSeats.valueAt(i%playSeats.size());
                    }
                    if(seatIdx==-1)
                        continue;
                    GameUser user = gamers.get(seatIdx);
                    if(user!=null && TextUtils.isEmpty(user.getSeatFlag()))
                    {
                        user.setSeatFlag(CardUtil.getSeatFlag(playSeats.size(),i-indexOfValue));
                    }
                    int lastMoney = lastSeats.get(seatIdx);
                    Seat seat = seats.get(seatIdx);
                    int money = seat.getMoney();
                    Log.e(TAG,"seatIdx--"+seatIdx+"  money-->"+money+"  lastMoney-->"+lastMoney);
                    if(seat.getBet()>lastMoney)
                    {
                        break;
                    }
                    if(money<lastMoney)
                    {
                        int addMoney = lastMoney - money;
                        if(money>0)
                        {
                            GameAction action = new  GameAction();
                            action.setSeatIdx(seatIdx);
                            action.setRound(boards.size());
                            action.setBet(seat.getBet());
                            action.setAddMoney(addMoney);
                            Log.e(TAG,"roundIdx--"+boards.size()+" seatIdx--"+seatIdx + "  blinds-->"+blinds+"  lastMoney--"+lastMoney +"  money--"+money+"  bet--"
                                    +seat.getBet()+"  addMoney-->"+addMoney);
                            actions.add(action);
                            lastSeats.put(seatIdx,money);
                        }else if(money==0)
                        {//判断ALLIN 还有手牌，而且下的bet是上一条记录的钱
                            if(seat.getHidecard()==1 && seat.getBet()==lastMoney)
                            {
                                Log.e(TAG,"roundIdx--"+boards.size()+" seatIdx--"+seatIdx+"  lastMoney--"+lastMoney +"  money--"+money+"---changeMoney-->"+(lastMoney-money));
                                GameAction action = new  GameAction();
                                action.setSeatIdx(seatIdx);
                                action.setRound(boards.size());
                                action.setBet(seat.getBet());
                                action.setAddMoney(addMoney);
                                action.setAction(Constant.ACTION_ALLIN);
                                actions.add(action);
                                lastSeats.put(seatIdx,money);
                            }
                        }
                    }else
                        {
                        if(allOnline)
                        {
                            if((seat.getHidecard()==0 && seatIdx!=0) || (seat.getHidecard()== 0 && seatIdx ==0))
                            {
                                if(user.getFoldRound()==-1)
                                {
                                    if(playerCount>1)
                                    {
                                        playerCount--;
                                        Log.e(TAG,"seatIdx（changeMoney）-->"+seatIdx + "号位  弃牌");
                                        GameAction action = new  GameAction();
                                        action.setSeatIdx(seatIdx);
                                        action.setRound(boards.size());
                                        action.setAction(Constant.ACTION_FOLD);
                                        actions.add(action);
                                        user.setFoldRound(boards.size());
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
    /*判断是否有大小盲*/
    public boolean haveHalfBlindsAndBlinds(){
        if(blinds != -1)
        {//有blinds存在的时候
            boolean haveHalfBlinds = false;
            boolean haveBlinds = false;
            for (int i = 0; i < seats.size(); i++)
            {
                Seat seat = seats.valueAt(i);
                if(seat.getBet()==blinds)
                {
                    haveBlinds = true;
                }
                if(seat.getBet() == blinds%2)
                {
                    haveHalfBlinds = true;
                }
            }
            if(haveBlinds && haveHalfBlinds)
            {
                return true;
            }
        }
        return false;
    }


/********************************************************************************
 *                                      分割线                                   *
 ********************************************************************************/
    private volatile static PokerAnalysisTool instance;
    private PokerAnalysisTool()
    {
        seatNames = new SparseArray();
        boards = new ArrayList<Integer>();
        seats = new SparseArray<Seat>();
        lastSeats = new SparseIntArray();
        tableRecord = new TableRecord();
        pokerRecord = new PokerRecord();
        actions = new ArrayList<>();
        playSeats = new SparseArray();
        gamers = new SparseArray<GameUser>();
        service = Executors.newCachedThreadPool();
        service.submit(new ScanNameThread());
    }
    public static PokerAnalysisTool getInstance()
    {
        if(instance==null)
        {
            synchronized (PokerAnalysisTool.class)
            {
                if(instance == null)
                    instance = new PokerAnalysisTool();
            }
        }
        return instance;
    }
    BlockingDeque<Bitmap> bitmaps = new LinkedBlockingDeque<>(3);

    boolean circleScreenShot = true;
    class ScanNameThread implements Runnable
    {
        @Override
        public void run() {
            Bitmap bitmap = null;
            while (circleScreenShot){
                if(!bitmaps.isEmpty())
                {
                    try {
                        bitmap = bitmaps.take();
                        getSeatNames(bitmap);
//                        saveImgToSDCard(bitmap,Constant.TEST_NAME);
                    } catch (InterruptedException e) {
                        Log.e(TAG,"线程获取任务失败");
                    }
                }else{
                    try {
                        Thread.sleep(2000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }
}
