package com.ruilonglai.texas_scan.util;

import android.graphics.Bitmap;
import android.text.TextUtils;
import android.util.SparseArray;

import com.google.gson.Gson;
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
import com.ruilonglai.texas_scan.newprocess.Package;

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
    private int firstSeatCount = -1;
    private int dichi = -1;
    private int curDichi = -1;
    private int playerCount = -1;
    private boolean isBegin = false;
    private boolean canAddAction = false;
    private boolean noNeedPic = false;
    private SparseArray<String> seatNames;//每个位置解析的名字
    private ExecutorService service;
    private List<Integer> boards;
    private int curRoundIdx = -1;
    private int changeMoney = -1;
    private int lastRoundChangeMoney = -1;
    private boolean newBtnIdx;
    private boolean needSendBoards;
    private boolean sendCloseWindow;
    private boolean nextHandScanName = true;
    private int isWatch;
    private int flag;
    private boolean allOnline;
    private boolean havePoker;
    private boolean noHideCard;
    boolean haveTurnRiver = false;
    private SparseArray<Seat> seats;//每个位置的信息
    private SparseArray<Seat> lastSeats;//上一条记录各位置的钱
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
        lastRoundChangeMoney = -1;
        straddleIdx = 0;
        noHideCard = false;
        haveTurnRiver = false;
    }

    public void analysisBitmap(Bitmap bitmap,int flag)
    {
        this.flag = flag;
        if(bitmap==null){
            return;
        }
        int isImgOK = ScanTool.IsImgOK(bitmap);
        MyLog.e(TAG,isImgOK+"");
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
                printAction();
                initView();
            }
            getBaseMessage(bitmap);
            if(noNeedPic)
                return;
            MyLog.e(TAG,"canAddAction "+canAddAction);
            if(!canAddAction)
            {
//                canAddAction = haveHalfBlindsAndBlinds();
                if(!canAddAction){
                    if(straddle>0)
                    {
                        changeMoney = straddle;
                    }
                    else
                    {
                        changeMoney = blinds;
                    }
                    canAddAction = true;
                }
            }
            disposeMoney();//记录钱的变化
            disposeBitmap(bitmap);
        }
        else
        {
            //不是模板图
            if(!sendCloseWindow)
            {
                Package pkg = new Package();
                pkg.setType(Constant.SOCKET_CLOSE_WINDOW);
                Connect.send(pkg);
                sendCloseWindow = true;
            }
        }
    }
    /*处理bitmap*/
    public void disposeBitmap(Bitmap bitmap){
        if(newBtnIdx)
        {//解析名字
            if(nextHandScanName){
                try {
                    bitmaps.add(bitmap);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
        else
        {
            bitmap.recycle();
            bitmap = null;
            System.gc();
        }
    }
    /*打印动作*/
    private void printAction(){
        MyLog.e("GameAction","--------------------一手记录的动作begin-------------------");
        for (int i = 0; i < actions.size(); i++)
        {
            GameAction gameAction = actions.get(i);
            MyLog.e("GameAction",gameAction.toString());
        }
        MyLog.e("GameAction","--------------------一手记录的动作end---------------------");
    }
    /*保存玩家show的牌*/
    private void saveOtherPlayerCards(){
        for (int j = 0; j < playSeats.size(); j++) {
            int seatIdx = playSeats.keyAt(j);
            Seat seat = seats.get(seatIdx);
            int card1 = seat.getCard1();
            int card2 = seat.getCard2();
            if(card1!=-1 && card2!=-1){
                GameUser gameUser = gamers.get(seatIdx);
                MyLog.e(TAG,"haveTurnRiver:"+haveTurnRiver+" card1:"+card1+" card2:"+card2+" "+gameUser);
                if(gameUser!=null){
                    gameUser.setCard1(card1);
                    gameUser.setCard2(card2);
                    if(seatIdx!=0){
                        gameUser.setTurn(true);
                        haveTurnRiver = true;
                    }
                }
            }
        }
        GameUser user = gamers.get(0);
        if(user !=null && haveTurnRiver && !user.isTurn()){
            user.setTurn(true);
        }
    }
    //接收外部接口解析名字
    public void updatename(){
        nextHandScanName = true;
    }
    //接收外部接口修改名字
    public void changeName(int seatIdx,String name,String remark,int level){
        synchronized(seatNames){
            if(seatIdx==-1){
                initView();
                instance = new PokerAnalysisTool();
            }else{
                if(!TextUtils.isEmpty(name))
                seatNames.put(seatIdx,name);
            }
        }
        for (int i = 0; i < seatNames.size(); i++) {
            String seatname = seatNames.valueAt(i);
            if(seatname!=null)
            MyLog.e(TAG, seatname);
        }
        String json = new Gson().toJson(seatNames);
        MyLog.e(TAG, "发送的名字1"+json);
        Package pkg = new Package();
        pkg.setType(Constant.SOCKET_KNOW_NAME);
        pkg.setContent(json);
        Connect.send(pkg);
    }
    /*从图片获取位置名字*/
    private void getSeatNames(Bitmap bitmap)
    {
        if(bitmap==null)
            return;
        if(seatCount<=0)
            return;
        for (int i = isWatch; i < seatCount+isWatch; i++)
        {
            int seatIdx = i;
            if(playSeats.get(seatIdx)!=null){
                String oldName = seatNames.get(seatIdx);
                if("".equals(oldName)){
                    String name = getJsonName(ScanTool.ScanSeatName(bitmap, seatIdx, 0), "name");
                    MyLog.e("name",seatIdx+"  "+name);
                    String userName = FileIOUtil.replaceBlank(name);
                    userName.replace("\\n","");
                    MyLog.e(TAG,"重新识别"+seatIdx+"号位名字:"+userName);
                    if(!TextUtils.isEmpty(userName)){
                        seatNames.put(seatIdx,userName);
                    }
                    if(seatIdx==0)
                    {
                        seatNames.put(seatIdx,"self");
                    }
                }
            }
        }
        for (int i = 0; i < seatNames.size(); i++) {
            String seatname = seatNames.valueAt(i);
            if(seatname!=null)
                MyLog.e(TAG, seatname);
        }
        String json = new Gson().toJson(seatNames);
        MyLog.e(TAG, "发送的名字2"+json);
        Package pkg = new Package();
        pkg.setType(Constant.SOCKET_KNOW_NAME);
        pkg.setContent(json);
        Connect.send(pkg);
    }
    /*删除数字*/
    public static String changeName(String name){
        if(name.contains("\n") && name.indexOf("\n")!=0)
            name = name.substring(0,name.indexOf("\n"));
        return name;
    }
    /*替换数字*/
    public static String removeDigital(String value){
        Pattern p = Pattern.compile("[\\d]");
        Matcher matcher = p.matcher(value);
        String result = matcher.replaceAll("");
       return result;
    }
    /*一手只需识别一次的数据*/
    private void getBaseMessage(Bitmap bitmap)
    {
//        try {
            MyLog.e("seatCount","识别座位数");
            String json = ScanTool.ScanSeatCount(bitmap);
            int count = getJsonMes(json,"seatcount");
            if(seatCount==-1){
                if(count!=-1){
                  seatCount = count;
                    if(firstSeatCount==-1)
                  firstSeatCount  = seatCount;
                  if(firstSeatCount==seatCount){
                      isWatch = getJsonMes(json,"iswatch");
                      Package pkg = new Package();
                      pkg.setType(Constant.SOCKET_SEATCOUNT);
                      pkg.setContent(json);
                      Connect.send(pkg);
                  }
                  if(firstSeatCount!=-1 && firstSeatCount!=seatCount){
                    seatCount = firstSeatCount;
                      MyLog.e(TAG,"几人桌识别错误");
                  }
                }
            }
//            json = ScanTool.ScanCurDichi(bitmap);
//            curDichi = getJsonMes(json,"curdichi ");
//            json = ScanTool.ScanAnte(bitmap);
//            MyLog.e(TAG,json);
//            ante = getJsonMes(json,"ante");
            if(blinds < 0)
            {
                json = ScanTool.ScanBlinds(bitmap);
                MyLog.e("blindsjson",json);
                straddle = getJsonMes(json,"straddle");
                if(flag!=1){
                    blinds =  getJsonMes(json,"half-blinds")*2;
                    if(straddle==0 && flag==0){
                        blinds = getJsonMes(json,"blinds");
                    }
                }else{
                    blinds =  getJsonMes(json,"blinds");
                }

                if(straddle>0){
                    straddle = blinds*2;
                }
            }
            boards = JsonTool.getBoards(ScanTool.ScanBoardCards(bitmap)).getBoards();
            if(boards.size()>=3 && pokerRecord.getFlop()[2]==-1)
            {
                pokerRecord.setFlop(new int[]{boards.get(0),boards.get(1),boards.get(2)});
            }
            if(boards.size()>=4 && pokerRecord.getTurn()==-1)
            {
                pokerRecord.setTurn(boards.get(3));
            }
            if(boards.size()==5 && pokerRecord.getRiver()==-1)
            {
                pokerRecord.setRiver(boards.get(4));
            }
            int size = boards.size();
            if(size>0 && size<3)
                size = 3;
            if(size >curRoundIdx && (size ==0 || size==3 || size==4 || size==5 ))
            {
                sendWinPercent();
                needSendBoards = true;
                curRoundIdx = size;
                lastRoundChangeMoney = changeMoney;
                changeMoney = -1;
            }else{
                needSendBoards = false;
            }
            if(firstSeatCount==seatCount)
            getSeats(bitmap);
//        } catch (Exception e) {
//            MyLog.e("error","getBaseMessage  "+e.toString());
//        }
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
                MyLog.e(TAG,i+"号位"+seat.toString());
                if((seat.getEmpty()==0 && ((seat.getMoney()!=0 ||seat.getBet()>0)))
                        || i==btnIdx || ((seat.getEmpty()==1 &&seat.getMoney()!=0)))
                {
                    playSeats.put(i,i);
                }
                seats.put(i,seat);
            }
            playerCount = playSeats.size();
        }else{
            boolean haveEmpty = false;
            for (int i = isWatch; i < seatCount+isWatch; i++)
            {
                if(playSeats.get(i) != null)
                {
                    String json = ScanTool.ScanSeat(bitmap, i);
                    Seat seat = gson.fromJson(json, Seat.class);
                    MyLog.e(TAG,i+"号位"+seat.toString());
                    if(i==0 && !havePoker)
                    {
                        if(seat.getCard1()!=-1 && seat.getCard2()!=-1)
                        {
                            havePoker = true;
                            card1 = seat.getCard1();
                            card2 = seat.getCard2();
                            MyLog.e(TAG,"解析出手牌");
                            sendWinPercent();
                        }else{
                            havePoker = false;
                        }
                    }
                    String name = seatNames.get(i);
                    if(seat.getEmpty()==1 && (seat.getMoney()==0 || seat.getMoney()>10000000)){
                        if(!TextUtils.isEmpty(name)){
                            haveEmpty = true;
                            MyLog.e(TAG,"清除"+i+"位置的名字");
                            seatNames.delete(i);
                        }
                    }else{
                        if(name==null && seat.getEmpty()==0 && seat.getMoney()>0){
                            haveEmpty = true;
                            String newName = getJsonName(ScanTool.ScanSeatName(bitmap, i, 0), "name");
                            newName = FileIOUtil.replaceBlank(newName);
                            newName.replace("\\n","");
                            if(flag>=2){
                                //扑克部落名字要去掉回车换行后的数字
                                newName = changeName(newName);
                            }
                            seatNames.put(i,newName);
                            MyLog.e(TAG,"重新识别名字seatNames-->"+seatNames.toString());
                        }
                    }
                    if(seat.getHidecard()==0){
                        if(i==seatCount+isWatch-1){
                            noHideCard = true;//最后结账状态
                        }
                    }else{
                        noHideCard = false;
                    }
                    seats.put(i,seat);
                }
            }
            if(haveEmpty){
                for (int i = 0; i < seatNames.size(); i++) {
                    String seatname = seatNames.valueAt(i);
                    if(seatname!=null)
                        MyLog.e(TAG,seatNames.keyAt(i)+"--"+seatname);
                }
                String json = new Gson().toJson(seatNames);
                MyLog.e(TAG, "发送的名字3"+json);
                Package pkg = new Package();
                pkg.setType(Constant.SOCKET_KNOW_NAME);
                pkg.setContent(json);
                Connect.send(pkg);
                haveEmpty = false;
            }
        }
        saveOtherPlayerCards();//保存玩家show牌的牌型
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
         if(curBtnIdx==-1){
             noNeedPic = true;
             return false;
         }else{
             noNeedPic = false;
         }
         if(btnIdx==-1)
         {
             btnIdx = curBtnIdx;
         }else{
             if(curBtnIdx != btnIdx)
             {
                 int seatcount = getJsonMes(ScanTool.ScanSeatCount(bitmap), "seatcount");
                 if(seatcount==firstSeatCount){
                     isBegin = true;
                     pokerRecord.setButton(btnIdx);
                     btnIdx = curBtnIdx;
                     return true;
                 }
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
             MyLog.e(TAG,"保存图片失败！");
         }
     }
     private void saveLastEndMoney(Bitmap bitmap)
     {
         Gson gson = new Gson();
         for (int i = isWatch; i < seatCount+isWatch; i++)
         {
             String json = ScanTool.ScanSeat(bitmap, i);
             Seat seat = gson.fromJson(json, Seat.class);
             MyLog.e(TAG,i+"号位endMoney-->"+seat.getMoney() + " bet-->"+seat.getBet());
             GameUser user = gamers.get(i);

             if(user!=null){
                 if(seat.getMoney()>0 && seat.getMoney()<1000000){
                     user.setEndMoney(seat.getMoney()+seat.getBet());
                 }else if(seat.getMoney()==0){
                     user.setEndMoney(lastSeats.get(i).getMoney());
                 }else if(seat.getMoney()<0 || seat.getMoney()>1000000){
                     user.setBeginMoney(0);
                     user.setEndMoney(0);
                 }
             }
         }
     }
    /*保存上一手数据*/
    private void saveLastHandData()
    {
        List<GameUser> users = new ArrayList<>();
        for (int i = 0; i < gamers.size(); i++)
        {
            GameUser user = gamers.valueAt(i);
            String name = seatNames.get(user.getSeatIdx());
            if(!TextUtils.isEmpty(name)){
                user.setUserName(name);
            }
            users.add(user);
        }
        tableRecord.setPlatformType(flag);
        tableRecord.setAnte(ante);
        tableRecord.setStraddle(straddle);
        tableRecord.setBlindType(blinds);
        tableRecord.setMaxPlayCount(seatCount);
        MyLog.e("button",btnIdx+"");
        pokerRecord.setPoker(new int[]{card1,card2});
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
        MyLog.e(TAG,"allOnLine-->"+allOnline);
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
                GameUser user = gamers.get(seatIdx);
                if(user != null && (seat.getMoney()<1000000 && seat.getMoney()>0) && (seat.getBet()>0 && seat.getBet()<1000000))
                {
                    user.setBeginMoney(seat.getMoney()+seat.getBet());
                    MyLog.e(TAG,i+"修改beginMoney"+user.getBeginMoney());
                }
                if(i==playSeats.size()-1)
                {
                    allOnline = true;
                }
            }
        }
        if(newBtnIdx){
            MyLog.e(TAG,"money初始化");
            gamers.clear();
            for (int i = isWatch; i < seatCount+isWatch; i++)
            {
                Seat seat = seats.get(i);
                if(seat!=null && seat.getMoney()>=0)
                {
                    /*上一手的最后一条记录*/
                    if((seat.getEmpty()==0 && (seat.getBet()!=0 || seat.getMoney()!=0)) || i==btnIdx
                            || ((seat.getEmpty()==1 &&(seat.getMoney()>0 && seat.getMoney()<1000000)))){
                        GameUser gamer = new GameUser();
                        gamer.setBeginMoney(seat.getMoney()+seat.getBet());
                        MyLog.e(TAG,"初始化beginMoney"+gamer.getBeginMoney());
                        gamer.setSeatIdx(i);
                        gamers.put(i,gamer);
                        if(seatNames.get(i)==null){
                            seatNames.put(i,"");
                        }
                    }else{
                        if(seat.getEmpty()==1 && seat.getMoney()==0 && seatNames.get(i)!=null){
                            seatNames.delete(i);
                        }
                    }
                    lastSeats.put(i,seat);
                }
            }
            int btnKeyIdx = gamers.indexOfKey(btnIdx);
            if(gamers.size()>0){
                for (int i = btnKeyIdx+1; i < gamers.size()+btnKeyIdx+1; i++) {
                    int idx = 0;
                    GameUser gameUser = null;
                    if(i<gamers.size()){
                        gameUser = gamers.valueAt(i);
                    }else{
                        gameUser = gamers.valueAt(i%gamers.size());
                    }
                    if(gameUser!=null){
                        gameUser.setSeatFlag(CardUtil.getSeatFlag(gamers.size(),i-btnKeyIdx-1));
                        MyLog.e(TAG,gameUser.toString());
                    }
                }

            }
        }else{
            if(btnIdx!=-1)
            {
                if(!canAddAction) {
                    return;
                }
                MyLog.e(TAG,"playseats-->"+playSeats.toString());
                int beginIdx = playSeats.indexOfKey(btnIdx);
                int size = boards.size();
                if(size>0 && size<3)
                    size = 3;
                for (int i = beginIdx+1; i < playSeats.size()+beginIdx+1; i++)
                {
                    int seatIdx = -1;
                    if(i<playSeats.size()){
                        seatIdx = playSeats.valueAt(i);
                    }else{
                        seatIdx = playSeats.valueAt(i%playSeats.size());
                    }
                    if(seatIdx==-1)
                        continue;
                    GameUser user = gamers.get(seatIdx);
                    if(user==null)
                        continue;
                    int lastMoney = lastSeats.get(seatIdx).getMoney();
                    Seat seat = seats.get(seatIdx);
                    int money = seat.getMoney();
                    MyLog.e(TAG,"seatIdx--"+seatIdx+"  money-->"+money+"  lastMoney-->"+lastMoney);
                    if(money>1000000){//钱识别错误
                        break;
                    }
                    saveAction(seat,user,seatIdx,money,lastMoney);
                    lastSeats.put(seatIdx,seat);
                }
            }
        }
    }
    int straddleIdx = 0;
    /*保存一个动作*/
    public void saveAction(Seat seat,GameUser user,int seatIdx,int money,int lastMoney){
        GameAction action = new  GameAction();
        action.setSeatIdx(seatIdx);
        int size = boards.size();
        if(size>0 && size<3)
            size = 3;
        action.setRound(size);
            action.setBet(seat.getBet());
            if(flag==1){
                switch (seat.getAction()){
                    case Constant.ACTION_DYQ_STRADDLE:
                        if(!haveAddStraddleAction(seatIdx)){
                            action.setAction(Constant.ACTION_STRADDLE);
                            MyLog.e(TAG,seatIdx+"号位记录动作dyq(straddle) "+action.toString());
                            actions.add(action);
                            straddleIdx++;
                            changeMoney = blinds*straddleIdx*2;
                        }
                        break;
                    case Constant.ACTION_DYQ_CHECK://看牌
                        if(!haveAddCheckAction(seatIdx)){
                            action.setAction(Constant.ACTION_CHECK);
                            MyLog.e(TAG,seatIdx+"号位记录动作dyq(看牌) "+action.toString());
                            if(size == 0|| size==3||size==4||size==5)
                            actions.add(action);
                        }
                        break;
                    case Constant.ACTION_DYQ_CALL://跟注
                        if(money<lastMoney){
                            action.setAction(Constant.ACTION_CALL);
                            action.setAddMoney(lastMoney-money);
                            MyLog.e(TAG,seatIdx+"号位记录动作dyq(跟注) "+action.toString());
                            actions.add(action);
                        }
                        break;
                    case Constant.ACTION_OPEN:
                        if(money!=lastMoney){
                            action.setAction(Constant.ACTION_OPEN);
                            action.setAddMoney(lastMoney-money);
                            MyLog.e(TAG,seatIdx+"号位记录动作dyq(Open) "+action.toString());
                            if(seat.getBet()<= changeMoney){
                                changeMoney = seat.getBet()+lastMoney-money;
                            }else {
                                changeMoney = seat.getBet();
                            }
                            action.setBet(changeMoney);
                            actions.add(action);
                        }
                        break;
                    case Constant.ACTION_3Bet:
                        if(money!=lastMoney){
                            action.setAction(Constant.ACTION_3Bet);
                            action.setAddMoney(lastMoney-money);
                            MyLog.e(TAG,seatIdx+"号位记录动作dyq(3Bet) "+action.toString());
                            if(seat.getBet()<= changeMoney){
                                changeMoney = seat.getBet()+lastMoney-money;
                            }else {
                                changeMoney = seat.getBet();
                            }
                            action.setBet(changeMoney);
                            actions.add(action);
                        }
                        break;
                    case Constant.ACTION_DYQ_RAISE://加注
                         if(money!=lastMoney){
                             action.setAction(Constant.ACTION_RAISE);
                             action.setAddMoney(lastMoney-money);
                             MyLog.e(TAG,seatIdx+"号位记录动作dyq(加注) "+action.toString());
                             if(seat.getBet()<= changeMoney){
                                 changeMoney = seat.getBet()+lastMoney-money;
                             }else {
                                 changeMoney = seat.getBet();
                             }
                             action.setBet(changeMoney);
                             actions.add(action);
                         }
                        break;
                    case Constant.ACTION_DYQ_BET:
                        if(money!=lastMoney){
                            action.setAction(Constant.ACTION_BET);
                            action.setAddMoney(lastMoney-money);
                            MyLog.e(TAG,seatIdx+"号位记录动作dyq(下注) "+action.toString());
                            if(seat.getBet()<= changeMoney){
                                changeMoney = seat.getBet()+lastMoney-money;
                            }else {
                                changeMoney = seat.getBet();
                            }
                            action.setBet(changeMoney);
                            actions.add(action);
                        }
                        break;
                    case Constant.ACTION_DYQ_ALLIN://全压
                        if(lastMoney>money){
                            action.setAction(Constant.ACTION_ALLIN);
                            action.setAddMoney(lastMoney);
                            MyLog.e(TAG,seatIdx+"号位记录动作dyq(allin) "+action.toString());
                            actions.add(action);
                        }
                        break;
                    case Constant.ACTION_DYQ_FOLD://弃牌
                        if(user.getFoldRound()==-1){
                            action.setAction(Constant.ACTION_FOLD);
                            MyLog.e(TAG,seatIdx+"号位记录动作dyq(弃牌) "+action.toString());
                            actions.add(action);
                            user.setFoldRound(boards.size());
                        }
                        break;
                    case -1:
                        if(seat.getHidecard()==1 && changeMoney>0){
                            if(money<lastMoney){
                                if(seat.getBet()==changeMoney || (seat.getBet()==0 && lastMoney-money==changeMoney)
                                        || (seat.getBet()>0 && seat.getBet()<1000000 && (seat.getBet()+lastMoney-money==changeMoney))){
                                    action.setAction(Constant.ACTION_CALL);
                                    action.setAddMoney(lastMoney-money);
                                    MyLog.e(TAG,seatIdx+"号位记录动作dyq[action=-1 (跟注) ] "+action.toString());
                                    actions.add(action);
                                }else if(seat.getBet()>changeMoney || (seat.getBet()==0 && lastMoney-money>changeMoney)
                                        || (seat.getBet()>0 && seat.getBet()<1000000 && (seat.getBet()+lastMoney-money>changeMoney))){
                                    action.setAction(Constant.ACTION_RAISE);
                                    action.setAddMoney(lastMoney-money);
                                    MyLog.e(TAG,seatIdx+"号位记录动作dyq[action=-1 (加注|Open|下注) ] "+action.toString());
                                    changeMoney = lastMoney-money+seat.getBet();
                                    if(seat.getBet()==0)
                                        changeMoney = lastMoney-money;
                                    action.setBet(changeMoney);
                                    actions.add(action);
                                }
                            }
                        }
                        break;
                }
            }else if(flag==0){//德扑圈
                switch (seat.getAction()){
                    case Constant.ACTION_DPQ_STRADDLE:
                        if(!haveAddStraddleAction(seatIdx)){
                            action.setAction(Constant.ACTION_STRADDLE);
                            MyLog.e(TAG,seatIdx+"号位记录动作dpq(straddle) "+action.toString());
                            actions.add(action);
                            changeMoney = seat.getBet();
                        }
                        break;
                    case Constant.ACTION_DPQ_CHECK://看牌
                        if(seat.getBet()==0 && money==lastMoney){
                            if(!haveAddCheckAction(seatIdx)){
                                action.setAction(Constant.ACTION_CHECK);
                                MyLog.e(TAG,seatIdx+"号位记录动作dpq(看牌) "+action.toString());
                                if(size == 0|| size==3||size==4||size==5)
                                    actions.add(action);
                            }
                        }else{
                            if(money<lastMoney){
                                action.setAction(Constant.ACTION_CALL);
                                action.setAddMoney(lastMoney-money);
                                MyLog.e(TAG,seatIdx+"号位记录动作dpq(看牌-->跟注) "+action.toString());
                                actions.add(action);
                            }
                        }
                        break;
                    case Constant.ACTION_DPQ_CALL://跟注
                        if(money!=lastMoney){
                            action.setAction(Constant.ACTION_CALL);
                            action.setAddMoney(lastMoney-money);
                            MyLog.e(TAG,seatIdx+"号位记录动作dpq(跟注) "+action.toString());
                            actions.add(action);
                        }
                        break;
                    case Constant.ACTION_DPQ_RAISE://加注
                        if(money!=lastMoney) {
                            action.setAction(Constant.ACTION_RAISE);
                            action.setAddMoney(lastMoney - money);
                            MyLog.e(TAG, seatIdx + "号位记录动作dpq(加注) " + action.toString());
                            actions.add(action);
                            changeMoney = seat.getBet();
                        }
                        break;
                    case Constant.ACTION_DPQ_ALLIN://全压
                        if(money<lastMoney){
                            action.setAction(Constant.ACTION_ALLIN);
                            action.setAddMoney(lastMoney);
                            MyLog.e(TAG,seatIdx+"号位记录动作dpq(allin) "+action.toString());
                            actions.add(action);
                            if(seat.getBet()>changeMoney){
                                changeMoney = seat.getBet();
                                action.setAction(Constant.ACTION_RAISE);
                            }else if(seat.getBet()<=changeMoney){
                                action.setAction(Constant.ACTION_CALL);
                            }
                        }
                        break;
                    case Constant.ACTION_DPQ_FOLD://弃牌
                        if(user.getFoldRound()==-1){
                            action.setAction(Constant.ACTION_FOLD);
                            MyLog.e(TAG,seatIdx+"号位记录动作dpq(弃牌) "+action.toString());
                            actions.add(action);
                            user.setFoldRound(size);
                        }
                        break;
                    case -1:
                        if(money<lastMoney && (money>0 && money<1000000) && (lastMoney>0 && lastMoney<1000000)){
                           if(lastMoney-money<changeMoney){
                               if(actions.size()!=0){
                                   action.setAction(Constant.ACTION_CALL);
                                   action.setAddMoney(lastMoney-money);
                                   MyLog.e(TAG,seatIdx+"号位记录动作dpq(action=-1跟注) "+action.toString());
                                   actions.add(action);
                               }
                           }else if(seat.getBet()>changeMoney){
                               action.setAction(Constant.ACTION_RAISE);
                               action.setAddMoney(lastMoney - money);
                               MyLog.e(TAG, seatIdx + "号位记录动作dpq(action=-1加注) " + action.toString());
                               actions.add(action);
                               changeMoney = seat.getBet();
                               if(seat.getBet()==0)
                                   changeMoney = lastMoney-money;
                           }
                        }
                        if(!noHideCard){
                            if(user.getFoldRound()==-1 && seat.getHidecard()==0 && seat.getMoney()>0){
                                action.setAction(Constant.ACTION_FOLD);
                                MyLog.e(TAG,seatIdx+"号位记录动作dpq(action=-1弃牌) "+action.toString());
                                actions.add(action);
                                user.setFoldRound(size);
                            }
                        }
                        break;
                }
            }else if(flag>1){
                switch (seat.getAction()){
                    case Constant.ACTION_PKBL_CHECK://看牌
                        if(!haveAddCheckAction(seatIdx)){
                            action.setAction(Constant.ACTION_CHECK);
                            MyLog.e(TAG,seatIdx+"号位记录动作pkbl(看牌) "+action.toString());
                            if(size == 0|| size==3||size==4||size==5)
                            actions.add(action);
                        }
                        break;
                    case Constant.ACTION_PKBL_CALL://跟注
                        if(money<lastMoney){
                            if(seat.getBet()<=changeMoney){
                                action.setAction(Constant.ACTION_CALL);
                                action.setAddMoney(lastMoney-money);
                                MyLog.e(TAG,seatIdx+"号位记录动作pkbl(跟注) "+action.toString());
                                actions.add(action);
                            }else if(seat.getBet()>changeMoney){
                                action.setAction(Constant.ACTION_RAISE);
                                action.setAddMoney(lastMoney - money);
                                MyLog.e(TAG, seatIdx + "号位记录动作pkbl(跟注-->加注) " + action.toString());
                                actions.add(action);
                                changeMoney = seat.getBet();
                                if(seat.getBet()==0)
                                    changeMoney = lastMoney-money;
                            }

                        }
                        break;
                    case Constant.ACTION_PKBL_RAISE://加注
                        if(money!=lastMoney) {
                            action.setAction(Constant.ACTION_RAISE);
                            action.setAddMoney(lastMoney - money);
                            MyLog.e(TAG, seatIdx + "号位记录动作pkbl(加注) " + action.toString());
                            actions.add(action);
                            changeMoney = seat.getBet();
                            if(seat.getBet()<changeMoney)
                                changeMoney = lastMoney-money+seat.getBet();
                        }
                        break;
                    case Constant.ACTION_PKBL_ALLIN://全压
                        if(money<lastMoney){
                            action.setAction(Constant.ACTION_ALLIN);
                            action.setAddMoney(lastMoney);
                            MyLog.e(TAG,seatIdx+"号位记录动作pkbl(allin) "+action.toString());
                            actions.add(action);
                            if(seat.getBet()>changeMoney)
                                changeMoney = seat.getBet();
                        }
                        break;
                    case Constant.ACTION_PKBL_FOLD://弃牌
                        if(user.getFoldRound()==-1){
                            action.setAction(Constant.ACTION_FOLD);
                            MyLog.e(TAG,seatIdx+"号位记录动作pkbl(弃牌) "+action.toString());
                            actions.add(action);
                            user.setFoldRound(boards.size());
                        }
                        break;
                    case -1:
                        if(!noHideCard){
                            if(user.getFoldRound()==-1 && seat.getHidecard()==0 && seat.getMoney()>0){
                                action.setAction(Constant.ACTION_FOLD);
                                MyLog.e(TAG,seatIdx+"号位记录动作pkbl(action=-1弃牌) "+action.toString());
                                actions.add(action);
                                user.setFoldRound(boards.size());
                            }
                            if(money<lastMoney && (money>0 && money<1000000) && (lastMoney>0 && lastMoney<1000000) && seat.getHidecard()==1) {
                                if (lastMoney - money < changeMoney) {
                                    action.setAction(Constant.ACTION_CALL);
                                    action.setAddMoney(lastMoney - money);
                                    MyLog.e(TAG, seatIdx + "号位记录动作pkbl(action=-1跟注) " + action.toString());
                                    actions.add(action);
                                }
                            }
                        }
                        break;
                }
            }
    }
    /*判断在一个位置是否有straddle*/
    public boolean haveAddStraddleAction(int seatIdx){
        for (int i = 0; i < actions.size(); i++) {
            GameAction action = actions.get(i);
            if(action.getSeatIdx()==seatIdx && action.getAction()==Constant.ACTION_STRADDLE){
                return true;
            }
        }
        return false;
    }
    /*判断在一个位置是否有check*/
    public boolean haveAddCheckAction(int seatIdx){
        for (int i = 0; i < actions.size(); i++) {
            GameAction action = actions.get(i);
            int size = boards.size();
            if(size==1||size==2)
                size = 3;
            if(action.getSeatIdx()==seatIdx && (action.getAction()==Constant.ACTION_CHECK || action.getAction()==Constant.ACTION_CALL)
                    && action.getRound()== size){
                //如果在一圈中已经有看牌或者跟注，就不可能再添加看牌动作
                return true;
            }

        }
        return false;
    }
    /*判断是否有大小盲*/
    public boolean haveHalfBlindsAndBlinds(){
        if(blinds != -1)
        {//有blinds存在的时候
            boolean haveHalfBlinds = false;
            boolean haveBlinds = false;
            int blindIdx = -1;
            int halfBlindIdx = -1;
            for (int i = 0; i < seats.size(); i++)
            {
                Seat seat = seats.valueAt(i);
                int seatIdx = seats.keyAt(i);
                if(seat.getBet()==blinds)
                {
                    haveBlinds = true;
                    blindIdx = seatIdx;
                }
                if(seat.getBet() == blinds%2)
                {
                    haveHalfBlinds = true;
                    halfBlindIdx = seatIdx;
                }
            }
            if(haveBlinds && haveHalfBlinds)
            {
                //重新定义大小盲的筹码
                lastSeats.put(blindIdx,seats.get(blindIdx));
                lastSeats.put(halfBlindIdx,seats.get(halfBlindIdx));
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
        lastSeats = new SparseArray();
        tableRecord = new TableRecord();
        pokerRecord = new PokerRecord();
        actions = new ArrayList<>();
        playSeats = new SparseArray();
        gamers = new SparseArray<GameUser>();
        service = Executors.newCachedThreadPool();
        service.submit(new ScanNameThread());
        firstSeatCount = -1;
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
                        MyLog.e(TAG,"线程获取任务失败");
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
