package com.ruilonglai.texas_scan.util;

import android.text.TextUtils;
import android.util.SparseArray;

import com.google.gson.Gson;
import com.ruilonglai.texas_scan.entity.BackData;
import com.ruilonglai.texas_scan.entity.GameAction;
import com.ruilonglai.texas_scan.entity.GameUser;
import com.ruilonglai.texas_scan.entity.OneHandLog;
import com.ruilonglai.texas_scan.entity.PokerRecord;
import com.ruilonglai.texas_scan.entity.ReqData;
import com.ruilonglai.texas_scan.entity.SaveRecordParam;
import com.ruilonglai.texas_scan.entity.TableRecord;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by wgl on 2017/4/27.
 */

public class AnalysisImgUtil {
    private static int seq = 0;
    private int lastChangeMoney;//一圈中上一个改变玩家得钱
    private boolean firstRaise= true;//一圈中第一个raise
    private int curBtnIdx=-1;//当前的
    private int btnIdx = -1;//当前btn位是第几个玩家
    public int roundIdx = -1;
    private boolean isPreFlopFistCall = true;
    private int preRaiseTotalCount;
    public int curTotalPlayCount=0;
    public int curTotalSeatCount=0;
    private SaveRecordParam param;
    public TableRecord tableRecord;
    private PokerRecord pokerRecord;
    public boolean isNewHand = false;
    private boolean isBegin = false;//完整的一手开始
    private boolean isHideCard = false;
    private boolean haveBet = false;
    private SparseArray<GameUser> curSeats;//当前牌桌玩家信息
    private SparseArray<String> lastSeatsNames;
    private SparseArray<Integer> lastSeatsMoney;//上一条记录中玩家得钱
    private SparseArray<Integer> lastRoundBeginMoney;//上一圈开始每个玩家开始的钱
    private SparseArray<Integer> liveSeatIdxs;//该圈还活着的玩家
    private SparseArray<String> seatNames;//每个位置对应的名字
    private List<GameAction> actions;//每个玩家得动作
    private static AnalysisImgUtil instance;
    private int curAppType;
    private AnalysisImgUtil() {
        initView();
        seatNames = new SparseArray<>();
        lastSeatsNames = new SparseArray<>();
    }
    public static AnalysisImgUtil getInstance() {
        if (instance == null) {
            instance = new AnalysisImgUtil();
        }
        return instance;
    }
    public void initView(){
        preRaiseTotalCount = 0;
        lastChangeMoney = 0;
        roundIdx = -1;
        haveBet = false;
        firstRaise = true;
        isPreFlopFistCall = true;
        tableRecord = new TableRecord();
        pokerRecord = new PokerRecord();
        param = new SaveRecordParam();
        param.setTableRecord(tableRecord);
        param.setPokerRecord(pokerRecord);
        actions = new ArrayList<GameAction>();
        pokerRecord.setActions(actions);
        curSeats = new SparseArray<GameUser>();
        lastSeatsMoney = new SparseArray<Integer>();
        lastRoundBeginMoney = new SparseArray<>();
        liveSeatIdxs = new SparseArray<>();
    }
    public void analysisPlayerAction(String json,int appType){
        curAppType = appType;
        if (!TextUtils.isEmpty(json) || (!"{}".equals(json))) {
            BackData data = null;
            try {
               data = GsonUtil.parseJsonWithGson(json, BackData.class);//解析json数据
           }catch (Exception e){
                MyLog.e("texas_scan","gson解析异常"+e.toString());
                return;
            }
            List<Integer> boards = data.getBoards();
            List<BackData.Player> seats = data.getSeats();
            if(curBtnIdx==-1){
                curBtnIdx = data.getBtnIdx();
            }else{
               if(curBtnIdx!=data.getBtnIdx()){
                   curTotalSeatCount = data.getSeats().size();
                   if(isBegin){
                       saveEndMoney(seats);
                       long beginTime = System.currentTimeMillis();
                       sendReqData();//保存数据到服务器
                       saveLastName();//保存上一手的名字
                   }
                   //FileIOUtil.saveToFile("BTN变化 新的一手");
                   initView();//重新初始化
                   isBegin = true;
                   curBtnIdx = data.getBtnIdx();
                   isNewHand = true;
               }else{
                   isNewHand = false;
                   //手牌改变也表示是新的一手
                   BackData.Player self = seats.get(0);
                   int card0 = self.getCard0();
                   int card1 = self.getCard1();
                   int[] poker = pokerRecord.getPoker();
                   if(card0 !=-1 && card1 != -1 && poker[0]!= -1 && poker[1]!= -1 && card0!=poker[0] && card1 != poker[1]){
                      // FileIOUtil.saveToFile("手牌改变 新的一手");
                       initView();//重新初始化
                       isBegin = true;
                       curBtnIdx = data.getBtnIdx();
                       isNewHand = true;
                   }
               }
            }

            isHideCard = haveHideCard(seats);
            curTotalPlayCount = getCurTotalPlayCount(seats);
            if(tableRecord.getTimestamp()==0){
                tableRecord.setTimestamp(System.currentTimeMillis());
            }
            if(pokerRecord.getButton()<data.getBtnIdx()){
                pokerRecord.setButton(data.getBtnIdx());
            }
            if(data.getM_ante()>tableRecord.getAnte()){//记录ante
                tableRecord.setAnte(data.getM_ante());
            }
            if(data.getBlinds()>tableRecord.getBlindType()){//记录盲注
                tableRecord.setBlindType(data.getBlinds());
            }
            if(tableRecord.getStraddle()==0 && data.getIsStraddle()==1){//记录straddle
                tableRecord.setStraddle(1);
            }
            if(tableRecord.getMaxPlayCount()==0){//几人桌
                tableRecord.setMaxPlayCount(seats.size());
            }
            if(pokerRecord.getTotalPool()<data.getDichi()){//记录底池
                pokerRecord.setTotalPool(data.getDichi());
            }
            if(boards.size()==3 && pokerRecord.getFlop()[2]<boards.get(2)){
                pokerRecord.setFlop(new int[]{boards.get(0),boards.get(1),boards.get(2)});
            }
            if(boards.size()==4 && pokerRecord.getTurn()<boards.get(3)){
                pokerRecord.setTurn(boards.get(3));
            }
            if(boards.size()==5 && pokerRecord.getRiver()<boards.get(4)){
                pokerRecord.setRiver(boards.get(4));
            }
            /**记录两张手牌*/
            BackData.Player self = seats.get(0);
            int card0 = self.getCard0();
            int card1 = self.getCard1();
            if(card0 !=-1 && card1 !=-1){
                if(pokerRecord.getPoker()[0]==-1){
                    int[] poker = {-1,-1};
                    poker[0] = card0;
                    poker[1] = card1;
                    pokerRecord.setPoker(poker);
                }
            }
            if(curTotalSeatCount!=0 && curTotalSeatCount!=seats.size()){
                FileIOUtil.saveToFile("几人桌识别错误");
                return;
            }
            switch (appType){
                case 2://德友圈
                    texasPoker(data);
                    break;
                case 1://扑克部落
                    nutsPoker(data);
                    break;
                case 0://德扑圈
                    pokerFish(data);
                    break;
            }
        }
    }
    //德扑圈动作解析
    public void pokerFish(BackData data){
        List<BackData.Player> seats = data.getSeats();
        if(isBegin){
            if(curSeats.size()==0){ /*初始化玩家*/
                    /*初始化curSeats，lastSeatMoney*/
                btnIdx = -1;
                haveName(seats);
                for (int i = 0; i < seats.size(); i++) {
                    BackData.Player player = seats.get(i);
                    if(player.getMoney()>0){
                        GameUser user = new GameUser();
                        user.setBeginMoney(player.getMoney()+player.getBet());
                        if(!TextUtils.isEmpty(player.getName())){
                            seatNames.put(i,player.getName());
                            user.setUserName(player.getName());
                        }else{
                            if(i==0){
                                user.setUserName("self");
                            }
                        }
                        curSeats.put(i,user);
                        lastSeatsMoney.put(i,player.getMoney());
                        lastRoundBeginMoney.put(i,player.getMoney());
                    }else{
                        if(i==curBtnIdx){
                            GameUser user = new GameUser();
                            user.setBeginMoney(player.getMoney()+player.getBet());
                            user.setBeginMoney(0);
                            curSeats.put(i,user);
                            lastSeatsMoney.put(i,0);
                            lastRoundBeginMoney.put(i,0);
                        }
                    }
                    if(i==curBtnIdx){//庄位排第几
                        btnIdx = curSeats.size()-1;
                    }
                }
                    /*初始化seatFlag:BTN,SB等*/
                int size = curSeats.size();
                if(btnIdx!=-1){
                    for (int i = 0; i < size; i++) {
                        int seatIdx = curSeats.keyAt(i);
                        GameUser gameUser = curSeats.get(seatIdx);
                        String seatFlag = "";
                        if(i<btnIdx){
                            seatFlag = CardUtil.getSeatFlag(size,size+i-btnIdx);
                        }else{
                            seatFlag = CardUtil.getSeatFlag(size,i-btnIdx);
                        }
                        if("BB".equals(seatFlag)){
                            if(tableRecord.getBlindType()==0)
                                tableRecord.setBlindType(seats.get(seatIdx).getBet());
                        }
                        gameUser.setSeatFlag(seatFlag);
                    }
                    for (int i = 0; i <seats.size(); i++) {
                        BackData.Player player = seats.get(i);
                        if(player.getBet()>lastChangeMoney){
                            lastChangeMoney = player.getBet();
                        }
                    }
                }else{
                    FileIOUtil.saveToFile("位置初始化错误");
                    curSeats.clear();
                    lastSeatsMoney.clear();
                }
            }else{//记录玩家动作
                if(!haveCurSeatsName()){//还没记录到名字
                    if(haveName(seats)){//该条记录出现名字
                        for (int i = 0; i < curSeats.size(); i++) {
                            int seatIdx = curSeats.keyAt(i);
                            BackData.Player player = seats.get(seatIdx);
                            GameUser user = curSeats.valueAt(i);
                            if(seatIdx==0){
                                user.setUserName("self");
                            }else{
                                user.setUserName(player.getName());
                            }
                        }
                    }
                }
                int size = data.getBoards().size();
                if(roundIdx != size){
                    BackData.Player selfPlayer = seats.get(0);
                    searchPreFlopLastRaisePlayer(size);//找出翻牌前最后一个加注的玩家
                    if(size==0){
                        roundIdx = size;
                    }else if(size == 3 && roundIdx<size){
                        for (int i = 0; i < curSeats.size(); i++) {//判断还活着的人
                            int seatIdx = curSeats.keyAt(i);
                            GameUser user = curSeats.get(seatIdx);
                            BackData.Player player = seats.get(seatIdx);
                            if((user.getBeginMoney()-player.getMoney()-player.getBet())>= lastChangeMoney){
                                liveSeatIdxs.put(seatIdx,lastChangeMoney);
                                lastRoundBeginMoney.put(seatIdx,user.getBeginMoney()-lastChangeMoney);
                            }else{
                                lastRoundBeginMoney.put(seatIdx,player.getMoney()+player.getBet());
                            }
                        }
                        roundIdx = size;
                        lastChangeMoney = 0;
                        haveBet = false;
                        firstRaise = true;
                    }else if(size == 4 && roundIdx<size){
                        liveSeatIdxs.clear();
                        for (int i = 0; i < curSeats.size(); i++) {//判断还活着的人
                            int seatIdx = curSeats.keyAt(i);
                            Integer lastMoney = lastRoundBeginMoney.get(seatIdx);
                            BackData.Player player = seats.get(seatIdx);
                            if((lastMoney.intValue()-player.getMoney()-player.getBet()) >= lastChangeMoney){
                                liveSeatIdxs.put(seatIdx,lastChangeMoney);
                                lastRoundBeginMoney.put(seatIdx,lastMoney-lastChangeMoney);
                            }else{
                                lastRoundBeginMoney.put(seatIdx,player.getMoney()+player.getBet());
                            }
                        }
                        roundIdx = size;
                        lastChangeMoney = 0;
                        haveBet = false;
                        firstRaise = true;
                    }else if(size == 5 && roundIdx<size){
                        liveSeatIdxs.clear();
                        for (int i = 0; i < curSeats.size(); i++) {//判断还活着的人
                            int seatIdx = curSeats.keyAt(i);
                            Integer lastMoney = lastRoundBeginMoney.get(seatIdx);
                            BackData.Player player = seats.get(seatIdx);
                            if((lastMoney.intValue()-player.getMoney()-player.getBet()) >= lastChangeMoney){
                                liveSeatIdxs.put(seatIdx,lastChangeMoney);
                                lastRoundBeginMoney.put(seatIdx,lastMoney-lastChangeMoney);
                            }else{
                                lastRoundBeginMoney.put(seatIdx,player.getMoney()+player.getBet());
                            }
                        }
                        roundIdx = size;
                        lastChangeMoney = 0;
                        haveBet = false;
                        firstRaise = true;
                    }
                }
                if(isHideCard){
                        /*初始化最大下注量和上一手改变的位置*/
                    if(lastChangeMoney==0 && roundIdx==0){
                        for (int i = 0; i < curSeats.size(); i++) {
                            int seatIdx = curSeats.keyAt(i);
                            GameUser user = curSeats.get(seatIdx);
                            BackData.Player player = seats.get(seatIdx);
                            user.setBeginMoney(player.getMoney()+player.getBet());
                            lastSeatsMoney.put(seatIdx,player.getMoney());
                        }
                        lastChangeMoney = tableRecord.getBlindType();
                        if(data.getIsStraddle()>0){
                            lastChangeMoney = tableRecord.getBlindType()*2;
                        }
                        return;
                    }
                    int seatIdx = -1;
                    //FileIOUtil.saveToFile("记录动作开始");
                    for (int i = btnIdx+1; i < (curSeats.size()+btnIdx+1); i++) {
                        if(i<curSeats.size()){
                            seatIdx = curSeats.keyAt(i);
                        }else{
                            seatIdx = curSeats.keyAt(i-curSeats.size());
                        }
                        BackData.Player player = seats.get(seatIdx);//新的数据
                        GameUser gameUser = curSeats.get(seatIdx);
                        int curBeginMoney = -1;
                        if(roundIdx>=3){
                            Integer integer = lastRoundBeginMoney.get(seatIdx);
                            if(integer!=null){
                                curBeginMoney = integer.intValue();
                            }
                        }else{
                            curBeginMoney = gameUser.getBeginMoney();
                        }
                        int newMoney = player.getMoney();
                        int bet = player.getBet();
                        Integer oldMoney = lastSeatsMoney.get(seatIdx);
                        int addMoney = oldMoney-newMoney;
                        int totalChangeMoney = curBeginMoney-newMoney;
                        GameAction action = new GameAction();
                        action.setUserName(gameUser.getUserName());
                        action.setSeatFlag(gameUser.getSeatFlag());
                        action.setRound(roundIdx);
                        if(newMoney==0){
                            if(player.getHideCard()==1){
                                action.setAction(Constant.ACTION_ALLIN);//Allin
                                gameUser.setJoin(true);
                                action.setBet(addMoney);
                                actions.add(action);
                            }else {
                                if(seatIdx==0){//判断自己allin
                                    if(seats.get(0).getCard0()!=-1 && seats.get(0).getCard1() != -1){
                                        action.setAction(Constant.ACTION_ALLIN);//Allin
                                        gameUser.setJoin(true);
                                        action.setBet(addMoney);
                                        actions.add(action);
                                    }
                                }
                            }
                        }else{
                            if(newMoney<oldMoney){//钱改变了
                                gameUser.setJoin(true);
                                if(roundIdx==0){
                                    if(preRaiseTotalCount>=1){//是否面临open
                                        gameUser.setFaceOpen(true);
                                    }
                                    if(preRaiseTotalCount>=2){//面临3bet
                                        gameUser.setFace3Bet(true);
                                    }
                                    //判定stl，foldStl
                                    curPlayerIfFaceFold(gameUser);
                                    //判定动作类型
                                    if (totalChangeMoney <= lastChangeMoney) {
                                        if(bet!=0){
                                            if(lastChangeMoney==tableRecord.getBlindType()){
                                                if(bet*2 == lastChangeMoney && "SB".equals(gameUser.getSeatFlag())){
                                                    continue;
                                                }
                                                if(bet*2 == lastChangeMoney && "BTN".equals(gameUser.getSeatFlag())){
                                                    continue;
                                                }
                                                if(bet == lastChangeMoney && "BB".equals(gameUser.getSeatFlag())){
                                                    continue;
                                                }
                                            }
                                        }
                                        action.setAction(Constant.ACTION_CALL);//跟注
                                        action.setBet(addMoney);
                                        actions.add(action);
                                    } else if(totalChangeMoney > lastChangeMoney){
                                        preRaiseTotalCount++;
                                        gameUser.setPFR(true);
                                        if(haveBet){
                                            action.setAction(Constant.ACTION_RAISE);//加注
                                        }else{
                                            action.setAction(Constant.ACTION_BET);
                                            haveBet = true;
                                        }
                                        if(gameUser.isStlPosition()){//有偷盲机会
                                            gameUser.setSTL(true);
                                        }
                                        if(preRaiseTotalCount>=2){
                                            gameUser.setIs3Bet(true);
                                        }
                                        action.setBet(addMoney);
                                        lastChangeMoney = totalChangeMoney;
                                        actions.add(action);
                                    }
                                }else{
                                    if (totalChangeMoney == lastChangeMoney && lastChangeMoney!=0) {
                                        action.setAction(Constant.ACTION_CALL);//跟注
                                        gameUser.setCallCount(gameUser.getCallCount()+1);
                                        action.setBet(addMoney);
                                        actions.add(action);
                                    } else{
                                        Integer lastRoundChangeMoney = liveSeatIdxs.get(seatIdx);
                                        if(lastRoundChangeMoney!=null && lastRoundBeginMoney.get(seatIdx)==player.getMoney()){
                                            action.setAction(Constant.ACTION_CALL);//跟注
                                            if(roundIdx>3){
                                                gameUser.setCallCount(gameUser.getCallCount()+1);
                                            }else{
                                                if(preRaiseTotalCount>=1){//是否面临open
                                                    gameUser.setFaceOpen(true);
                                                }
                                                if(preRaiseTotalCount>=2){//面临3bet
                                                    gameUser.setFace3Bet(true);
                                                }
                                            }
                                            action.setBet(addMoney);
                                            action.setRound(roundIdx==3?1:roundIdx==4?3:4);
                                            actions.add(action);
                                        }else{
                                            if(totalChangeMoney>lastChangeMoney){
                                                if(roundIdx==3 && gameUser.isPreFlopLastRaise()){//是否翻牌圈再加注
                                                    gameUser.setCB(true);
                                                }
                                                gameUser.setRaiseCount(gameUser.getRaiseCount()+1);
                                                if(haveBet){
                                                    action.setAction(Constant.ACTION_RAISE);//加注
                                                }else{
                                                    action.setAction(Constant.ACTION_BET);
                                                    haveBet = true;
                                                }
                                                action.setBet(addMoney);
                                                lastChangeMoney = totalChangeMoney;
                                                if(ifAllLiveInRiver(seats)){
                                                    actions.add(action);
                                                }
                                            }
                                        }
                                    }
                                }
                            }else if(newMoney==oldMoney){//钱没改变
                               /*判断弃牌*/
                                if(seatIdx!=0){//专属德扑圈的弃牌识别
                                    if((player.getHideCard()==1) && gameUser.getFoldRound()==-1){
                                        curPlayerIfFaceFold(gameUser);//先判断该玩家是否具备资格
                                        action.setAction(Constant.ACTION_FOLD);
                                        Integer lastRoundChangeMoney = liveSeatIdxs.get(seatIdx);
                                        if(roundIdx ==0 || (lastRoundChangeMoney!=null && lastRoundBeginMoney.get(seatIdx)== player.getMoney()) ){
                                            if(preRaiseTotalCount>=1){//是否面临open
                                                gameUser.setFaceOpen(true);
                                            }
                                            if(preRaiseTotalCount>=2){//面临3bet
                                                gameUser.setFace3Bet(true);
                                            }
                                            if(isSTLSeat(gameUser) && curTotalPlayCount>=3){//是偷盲位置
                                                if(allFoldBefore()){
                                                    gameUser.setStlPosition(true);
                                                }
                                            }
                                            if(gameUser.isFaceSTL()){//有被偷盲机会
                                                gameUser.setFoldSTL(true);
                                            }
                                        }
                                        gameUser.setFoldRound(roundIdx);
                                        actions.add(action);
                                    }
                                }else{
                                    if((player.getHideCard()==0) && gameUser.getFoldRound()==-1){
                                        curPlayerIfFaceFold(gameUser);//先判断该玩家是否具备资格
                                        action.setAction(Constant.ACTION_FOLD);
                                        Integer lastRoundChangeMoney = liveSeatIdxs.get(seatIdx);
                                        if(roundIdx ==0 || (lastRoundChangeMoney!=null && lastRoundBeginMoney.get(seatIdx)== player.getMoney()) ){
                                            if(preRaiseTotalCount>=1){//是否面临open
                                                gameUser.setFaceOpen(true);
                                            }
                                            if(preRaiseTotalCount>=2){//面临3bet
                                                gameUser.setFace3Bet(true);
                                            }
                                            if(isSTLSeat(gameUser) && curTotalPlayCount>=3){//是偷盲位置
                                                if(allFoldBefore()){
                                                    gameUser.setStlPosition(true);
                                                }
                                            }
                                            if(gameUser.isFaceSTL()){//有被偷盲机会
                                                gameUser.setFoldSTL(true);
                                            }
                                        }
                                        gameUser.setFoldRound(roundIdx);
                                        actions.add(action);
                                    }
                                }
                            }else{//钱变多(加筹码)

                            }
                        }
                    }
                    //FileIOUtil.saveToFile("记录动作结束");
                }
                //更新money
                for (int i = 0; i < lastSeatsMoney.size(); i++) {
                    int seatIdx = lastSeatsMoney.keyAt(i);
                    int money = seats.get(seatIdx).getMoney();
                    if(money >0){
                        lastSeatsMoney.put(seatIdx,money);
                    }
                }
            }
        }
    }
    //扑克部落动作解析
    public void nutsPoker(BackData data){
        List<BackData.Player> seats = data.getSeats();
        if(isBegin){
            if(curSeats.size()==0){ /*初始化玩家*/
                    /*初始化curSeats，lastSeatMoney*/
                btnIdx = -1;
                haveName(seats);
                for (int i = 0; i < seats.size(); i++) {
                    BackData.Player player = seats.get(i);
                    if(player.getMoney()>0){
                        GameUser user = new GameUser();
                        user.setBeginMoney(player.getMoney()+player.getBet());
                        if(!TextUtils.isEmpty(player.getName())){
                            seatNames.put(i,player.getName());
                            user.setUserName(player.getName());
                        }else{
                            if(i==0){
                                user.setUserName("self");
                            }
                        }
                        curSeats.put(i,user);
                        lastSeatsMoney.put(i,player.getMoney());
                        lastRoundBeginMoney.put(i,player.getMoney());
                    }else{
                        if(i==curBtnIdx){
                            GameUser user = new GameUser();
                            user.setBeginMoney(player.getMoney()+player.getBet());
                            user.setBeginMoney(0);
                            curSeats.put(i,user);
                            lastSeatsMoney.put(i,0);
                            lastRoundBeginMoney.put(i,0);
                            btnIdx = curSeats.size()-1;
                        }
                    }
                    if(i==curBtnIdx){//庄位排第几
                        btnIdx = curSeats.size()-1;
                    }
                }
                    /*初始化seatFlag:BTN,SB等*/
                int size = curSeats.size();
                if(btnIdx!=-1){
                    for (int i = 0; i < size; i++) {
                        int seatIdx = curSeats.keyAt(i);
                        GameUser gameUser = curSeats.get(seatIdx);
                        String seatFlag = "";
                        if(i<btnIdx){
                            seatFlag = CardUtil.getSeatFlag(size,size+i-btnIdx);
                        }else{
                            seatFlag = CardUtil.getSeatFlag(size,i-btnIdx);
                        }
                        if("BB".equals(seatFlag)){
                            if(tableRecord.getBlindType()==0)
                                tableRecord.setBlindType(seats.get(seatIdx).getBet());
                        }
                        gameUser.setSeatFlag(seatFlag);
                    }
                    for (int i = 0; i <seats.size(); i++) {
                        BackData.Player player = seats.get(i);
                        if(player.getBet()>lastChangeMoney){
                            lastChangeMoney = player.getBet();
                        }
                    }
                }else{
                    FileIOUtil.saveToFile("位置初始化错误");
                    curSeats.clear();
                    lastSeatsMoney.clear();
                }
            }else{//记录玩家动作
                if(!haveCurSeatsName()){//还没记录到名字
                    if(haveName(seats)){
                        for (int i = 0; i < curSeats.size(); i++) {
                            int seatIdx = curSeats.keyAt(i);
                            BackData.Player player = seats.get(seatIdx);
                            GameUser user = curSeats.valueAt(i);
                            if(seatIdx==0){
                                user.setUserName("self");
                            }else{
                                user.setUserName(player.getName());
                            }
                        }
                    }
                }
                int size = data.getBoards().size();
                if(roundIdx != size){
                    BackData.Player selfPlayer = seats.get(0);
                    int money = selfPlayer.getMoney()+selfPlayer.getBet();
                    if(size == 3 && roundIdx == 0){//查找翻牌前最后一个加注
                        label:   for (int i = actions.size()-1; i >=0 ; i--) {
                            GameAction action = actions.get(i);
                            if(action.getAction()== Constant.ACTION_BET || action.getAction()==Constant.ACTION_RAISE){
                                for (int j = 0; j < curSeats.size(); j++) {
                                    GameUser user = curSeats.valueAt(j);
                                    if(user.getSeatFlag().equals(action.getSeatFlag())){
                                        user.setPreFlopLastRaise(true);
                                        break label;
                                    }
                                }
                            }
                        }
                    }
                    if(size==0){
                        roundIdx = size;
                    }else if(size == 3 && roundIdx<size){
                        for (int i = 0; i < curSeats.size(); i++) {//判断还活着的人
                            int seatIdx = curSeats.keyAt(i);
                            GameUser user = curSeats.get(seatIdx);
                            BackData.Player player = seats.get(seatIdx);
                            if((user.getBeginMoney()-player.getMoney()-player.getBet())== lastChangeMoney){
                                liveSeatIdxs.put(seatIdx,lastChangeMoney);
                            }
                            lastRoundBeginMoney.put(seatIdx,player.getMoney()+player.getBet());
                        }
                        roundIdx = size;
                        lastChangeMoney = 0;
                        haveBet = false;
                        firstRaise = true;
                    }else if(size == 4 && roundIdx<size){
                        liveSeatIdxs.clear();
                        for (int i = 0; i < curSeats.size(); i++) {//判断还活着的人
                            int seatIdx = curSeats.keyAt(i);
                            Integer lastMoney = lastRoundBeginMoney.get(seatIdx);
                            BackData.Player player = seats.get(seatIdx);
                            if((lastMoney.intValue()-player.getMoney()-player.getBet()) == lastChangeMoney){
                                liveSeatIdxs.put(seatIdx,lastChangeMoney);
                            }
                            lastRoundBeginMoney.put(seatIdx,player.getMoney()+player.getBet());
                        }
                        roundIdx = size;
                        lastChangeMoney = 0;
                        haveBet = false;
                        firstRaise = true;
                    }else if(size == 5 && roundIdx<size){
                        liveSeatIdxs.clear();
                        for (int i = 0; i < curSeats.size(); i++) {//判断还活着的人
                            int seatIdx = curSeats.keyAt(i);
                            Integer lastMoney = lastRoundBeginMoney.get(seatIdx);
                            BackData.Player player = seats.get(seatIdx);
                            if((lastMoney.intValue()-player.getMoney()-player.getBet()) == lastChangeMoney){
                                liveSeatIdxs.put(seatIdx,lastChangeMoney);
                            }
                            lastRoundBeginMoney.put(seatIdx,player.getMoney()+player.getBet());
                        }
                        roundIdx = size;
                        lastChangeMoney = 0;
                        haveBet = false;
                        firstRaise = true;
                    }
                }
                if(isHideCard){
                        /*初始化最大下注量和上一手改变的位置*/
                    if(lastChangeMoney==0 && roundIdx==0){
                        for (int i = 0; i < curSeats.size(); i++) {
                            int seatIdx = curSeats.keyAt(i);
                            GameUser user = curSeats.get(seatIdx);
                            BackData.Player player = seats.get(seatIdx);
                            user.setBeginMoney(player.getMoney()+player.getBet());
                            lastSeatsMoney.put(seatIdx,player.getMoney());
                        }
                        lastChangeMoney = tableRecord.getBlindType();
                        if(data.getIsStraddle()>0){
                            lastChangeMoney = tableRecord.getBlindType()*2;
                        }
                        return;
                    }
                    int seatIdx = -1;
                    //FileIOUtil.saveToFile("记录动作开始");
                    for (int i = btnIdx+1; i < (curSeats.size()+btnIdx+1); i++) {
                        if(i<curSeats.size()){
                            seatIdx = curSeats.keyAt(i);
                        }else{
                            seatIdx = curSeats.keyAt(i-curSeats.size());
                        }
                        BackData.Player player = seats.get(seatIdx);//新的数据
                        GameUser gameUser = curSeats.get(seatIdx);
                        int newMoney = player.getMoney();
                        int bet = player.getBet();
                        Integer oldMoney = lastSeatsMoney.get(seatIdx);
                        int addMoney = oldMoney-newMoney;
                        GameAction action = new GameAction();
                        action.setUserName(gameUser.getUserName());
                        action.setSeatFlag(gameUser.getSeatFlag());
                        action.setRound(roundIdx);
                        if(newMoney==0){
                            if(player.getHideCard()==1){
                                action.setAction(Constant.ACTION_ALLIN);//Allin
                                gameUser.setJoin(true);
                                action.setBet(addMoney);
                                actions.add(action);
                            }else {
                                if(seatIdx==0){//判断自己allin
                                    /*if(seats.get(0).getCard0()!=-1 && seats.get(0).getCard1() != -1 && lastRoundSelfMoney==bet){
                                        action.setAction(Constant.ACTION_ALLIN);//Allin
                                        gameUser.setJoin(true);
                                        action.setBet(addMoney);
                                        actions.add(action);
                                    }*/
                                }
                            }
                        }else{
                            if(newMoney<oldMoney){//钱改变了
                                if(bet==0 && lastChangeMoney!=0){
                                    continue;
                                }
                                gameUser.setJoin(true);
                                if(roundIdx==0){
                                    if(preRaiseTotalCount>=1){//是否面临open
                                        gameUser.setFaceOpen(true);
                                    }
                                    if(preRaiseTotalCount>=2){//面临3bet
                                        gameUser.setFace3Bet(true);
                                    }
                                    //判定stl，foldStl
                                    curPlayerIfFaceFold(gameUser);
                                    //判定动作类型
                                    if (bet <= lastChangeMoney) {
                                        if(bet!=0){
                                            if(lastChangeMoney==tableRecord.getBlindType()){
                                                if(bet*2 == lastChangeMoney && "SB".equals(gameUser.getSeatFlag())){
                                                    continue;
                                                }
                                                if(bet*2 == lastChangeMoney && "BTN".equals(gameUser.getSeatFlag())){
                                                    continue;
                                                }
                                                if(bet == lastChangeMoney && "BB".equals(gameUser.getSeatFlag())){
                                                    continue;
                                                }
                                            }
                                            action.setAction(Constant.ACTION_CALL);//跟注
                                            action.setBet(addMoney);
                                            actions.add(action);
                                        }
                                    } else if(bet > lastChangeMoney){
                                        preRaiseTotalCount++;
                                        gameUser.setPFR(true);
                                        if(haveBet){
                                            action.setAction(Constant.ACTION_RAISE);//加注
                                        }else{
                                            action.setAction(Constant.ACTION_BET);
                                            haveBet = true;
                                        }
                                        if(gameUser.isStlPosition()){//有偷盲机会
                                            gameUser.setSTL(true);
                                        }
                                        if(preRaiseTotalCount>=2){
                                            gameUser.setIs3Bet(true);
                                        }
                                        action.setBet(addMoney);
                                        lastChangeMoney = bet;
                                        if(bet==0){
                                            lastChangeMoney = addMoney;
                                        }
                                        actions.add(action);
                                    }
                                }else{
                                    if (bet <= lastChangeMoney && lastChangeMoney!=0) {
                                        action.setAction(Constant.ACTION_CALL);//跟注
                                        gameUser.setCallCount(gameUser.getCallCount()+1);
                                        action.setBet(addMoney);
                                        actions.add(action);
                                    } else {
                                        Integer lastRoundChangeMoney = liveSeatIdxs.get(seatIdx);
                                        if(lastRoundChangeMoney!=null && lastRoundBeginMoney.get(seatIdx)== player.getMoney()){
                                            action.setAction(Constant.ACTION_CALL);//跟注
                                            if(roundIdx>3){
                                                gameUser.setCallCount(gameUser.getCallCount()+1);
                                            }else{
                                                if(preRaiseTotalCount>=1){//是否面临open
                                                    gameUser.setFaceOpen(true);
                                                }
                                                if(preRaiseTotalCount>=2){//面临3bet
                                                    gameUser.setFace3Bet(true);
                                                }
                                            }
                                            action.setBet(addMoney);
                                            action.setRound(roundIdx==3?1:roundIdx==4?3:4);
                                            actions.add(action);
                                            lastSeatsMoney.put(seatIdx,player.getMoney());
                                        }else{
                                            if(roundIdx==3 && gameUser.isPreFlopLastRaise()){//是否翻牌圈再加注
                                                gameUser.setCB(true);
                                            }
                                            gameUser.setRaiseCount(gameUser.getRaiseCount()+1);
                                            if(haveBet){
                                                action.setAction(Constant.ACTION_RAISE);//加注
                                            }else{
                                                action.setAction(Constant.ACTION_BET);
                                                haveBet = true;
                                            }
                                            action.setBet(addMoney);
                                            lastChangeMoney = bet;
                                            if(bet==0){
                                                lastChangeMoney = addMoney;
                                            }
                                            if(ifAllLiveInRiver(seats)){
                                                actions.add(action);
                                            }
                                        }
                                    }
                                }
                            }else if(newMoney==oldMoney){//钱没改变
                               /*判断弃牌*/
                                if(curAppType==0 && seatIdx!=0){//专属德扑圈的弃牌识别
                                    if((player.getHideCard()==1) && gameUser.getFoldRound()==-1){
                                        curPlayerIfFaceFold(gameUser);//先判断该玩家是否具备资格
                                        action.setAction(Constant.ACTION_FOLD);
                                        Integer lastRoundChangeMoney = liveSeatIdxs.get(seatIdx);
                                        if(roundIdx ==0 || (lastRoundChangeMoney!=null && lastRoundBeginMoney.get(seatIdx)== player.getMoney()) ){
                                            if(preRaiseTotalCount>=1){//是否面临open
                                                gameUser.setFaceOpen(true);
                                            }
                                            if(preRaiseTotalCount>=2){//面临3bet
                                                gameUser.setFace3Bet(true);
                                            }
                                            if(isSTLSeat(gameUser) && curTotalPlayCount>=3){//是偷盲位置
                                                if(allFoldBefore()){
                                                    gameUser.setStlPosition(true);
                                                }
                                            }
                                            if(gameUser.isFaceSTL()){//有被偷盲机会
                                                gameUser.setFoldSTL(true);
                                            }
                                        }
                                        gameUser.setFoldRound(roundIdx);
                                        actions.add(action);
                                    }
                                }else{
                                    if((player.getHideCard()==0) && gameUser.getFoldRound()==-1){
                                        curPlayerIfFaceFold(gameUser);//先判断该玩家是否具备资格
                                        action.setAction(Constant.ACTION_FOLD);
                                        Integer lastRoundChangeMoney = liveSeatIdxs.get(seatIdx);
                                        if(roundIdx ==0 || (lastRoundChangeMoney!=null && lastRoundBeginMoney.get(seatIdx)== player.getMoney()) ){
                                            if(preRaiseTotalCount>=1){//是否面临open
                                                gameUser.setFaceOpen(true);
                                            }
                                            if(preRaiseTotalCount>=2){//面临3bet
                                                gameUser.setFace3Bet(true);
                                            }
                                            if(isSTLSeat(gameUser) && curTotalPlayCount>=3){//是偷盲位置
                                                if(allFoldBefore()){
                                                    gameUser.setStlPosition(true);
                                                }
                                            }
                                            if(gameUser.isFaceSTL()){//有被偷盲机会
                                                gameUser.setFoldSTL(true);
                                            }
                                        }
                                        gameUser.setFoldRound(roundIdx);
                                        actions.add(action);
                                    }
                                }
                            }else{//钱变多(加筹码)

                            }
                        }
                        //更新这一条记录的钱
                        if(newMoney==0){
                            if(player.getHideCard()==1 && seatIdx!=0){
                                lastSeatsMoney.put(seatIdx,0);
                            }else{
                                /*if(seatIdx==0){
                                    if(seats.get(0).getCard0()!=-1 && seats.get(0).getCard1() != -1 && lastRoundSelfMoney==bet){
                                        lastSeatsMoney.put(0,0);
                                    }
                                }*/
                            }
                        }else{
                            if(bet!=0){
                                lastSeatsMoney.put(seatIdx,player.getMoney());
                            }
                        }
                    }
                    //FileIOUtil.saveToFile("记录动作结束");
                }else{//还没出现hideCard时候更新money
                    for (int i = 0; i < lastSeatsMoney.size(); i++) {
                        int seatIdx = lastSeatsMoney.keyAt(i);
                        int money = seats.get(seatIdx).getMoney();
                        if(money >0){
                            lastSeatsMoney.put(seatIdx,money);
                        }
                    }
                }
            }
        }
    }
    //德友圈动作解析
    public void texasPoker(BackData data){
        List<BackData.Player> seats = data.getSeats();
        if(isBegin){
            if(curSeats.size()==0){ /*初始化玩家*/
                    /*初始化curSeats，lastSeatMoney*/
                btnIdx = -1;
                haveName(seats);
                for (int i = 0; i < seats.size(); i++) {
                    BackData.Player player = seats.get(i);
                    if(player.getMoney()>0){
                        GameUser user = new GameUser();
                        user.setBeginMoney(player.getMoney()+player.getBet());
                        if(!TextUtils.isEmpty(player.getName())){
                            seatNames.put(i,player.getName());
                            user.setUserName(player.getName());
                        }else{
                            if(i==0){
                                user.setUserName("self");
                            }
                        }
                        curSeats.put(i,user);
                        lastSeatsMoney.put(i,player.getMoney());
                        lastRoundBeginMoney.put(i,player.getMoney());
                    }else{
                        if(i==curBtnIdx){
                            GameUser user = new GameUser();
                            user.setBeginMoney(player.getMoney()+player.getBet());
                            user.setBeginMoney(0);
                            curSeats.put(i,user);
                            lastSeatsMoney.put(i,0);
                            lastRoundBeginMoney.put(i,0);
                        }
                    }
                    if(i==curBtnIdx){//庄位排第几
                        btnIdx = curSeats.size()-1;
                    }
                }
                    /*初始化seatFlag:BTN,SB等*/
                int size = curSeats.size();
                if(btnIdx!=-1){
                    for (int i = 0; i < size; i++) {
                        int seatIdx = curSeats.keyAt(i);
                        GameUser gameUser = curSeats.get(seatIdx);
                        String seatFlag = "";
                        if(i<btnIdx){
                            seatFlag = CardUtil.getSeatFlag(size,size+i-btnIdx);
                        }else{
                            seatFlag = CardUtil.getSeatFlag(size,i-btnIdx);
                        }
                        if("BB".equals(seatFlag)){
                            if(tableRecord.getBlindType()==0)
                                tableRecord.setBlindType(seats.get(seatIdx).getBet());
                        }
                        gameUser.setSeatFlag(seatFlag);
                    }
                    for (int i = 0; i <seats.size(); i++) {
                        BackData.Player player = seats.get(i);
                        if(player.getBet()>lastChangeMoney){
                            lastChangeMoney = player.getBet();
                        }
                    }
                }else{
                    FileIOUtil.saveToFile("位置初始化错误");
                    curSeats.clear();
                    lastSeatsMoney.clear();
                }
            }else{//记录玩家动作
                if(!haveCurSeatsName()){//还没记录到名字
                    if(haveName(seats)){
                        for (int i = 0; i < curSeats.size(); i++) {
                            int seatIdx = curSeats.keyAt(i);
                            BackData.Player player = seats.get(seatIdx);
                            GameUser user = curSeats.valueAt(i);
                            if(seatIdx==0){
                                user.setUserName("self");
                            }else{
                                user.setUserName(player.getName());
                            }
                        }
                    }
                }
                int size = data.getBoards().size();
                if(roundIdx != size){
                    BackData.Player selfPlayer = seats.get(0);
                    int money = selfPlayer.getMoney()+selfPlayer.getBet();
                    if(size == 3 && roundIdx == 0){//查找翻牌前最后一个加注
                        label:   for (int i = actions.size()-1; i >=0 ; i--) {
                            GameAction action = actions.get(i);
                            if(action.getAction()== Constant.ACTION_BET || action.getAction()==Constant.ACTION_RAISE){
                                for (int j = 0; j < curSeats.size(); j++) {
                                    GameUser user = curSeats.valueAt(j);
                                    if(user.getSeatFlag().equals(action.getSeatFlag())){
                                        user.setPreFlopLastRaise(true);
                                        break label;
                                    }
                                }
                            }
                        }
                    }
                    if(size==0){
                        roundIdx = size;
                    }else if(size == 3 && roundIdx<size){
                        for (int i = 0; i < curSeats.size(); i++) {//判断还活着的人
                            int seatIdx = curSeats.keyAt(i);
                            GameUser user = curSeats.get(seatIdx);
                            BackData.Player player = seats.get(seatIdx);
                            if((user.getBeginMoney()-player.getMoney()-player.getBet())== lastChangeMoney){
                                liveSeatIdxs.put(seatIdx,lastChangeMoney);
                            }
                            lastRoundBeginMoney.put(seatIdx,player.getMoney()+player.getBet());
                        }
                        roundIdx = size;
                        lastChangeMoney = 0;
                        haveBet = false;
                        firstRaise = true;
                    }else if(size == 4 && roundIdx<size){
                        liveSeatIdxs.clear();
                        for (int i = 0; i < curSeats.size(); i++) {//判断还活着的人
                            int seatIdx = curSeats.keyAt(i);
                            Integer lastMoney = lastRoundBeginMoney.get(seatIdx);
                            BackData.Player player = seats.get(seatIdx);
                            if((lastMoney.intValue()-player.getMoney()-player.getBet()) == lastChangeMoney){
                                liveSeatIdxs.put(seatIdx,lastChangeMoney);
                            }
                            lastRoundBeginMoney.put(seatIdx,player.getMoney()+player.getBet());
                        }
                        roundIdx = size;
                        lastChangeMoney = 0;
                        haveBet = false;
                        firstRaise = true;
                    }else if(size == 5 && roundIdx<size){
                        liveSeatIdxs.clear();
                        for (int i = 0; i < curSeats.size(); i++) {//判断还活着的人
                            int seatIdx = curSeats.keyAt(i);
                            Integer lastMoney = lastRoundBeginMoney.get(seatIdx);
                            BackData.Player player = seats.get(seatIdx);
                            if((lastMoney.intValue()-player.getMoney()-player.getBet()) == lastChangeMoney){
                                liveSeatIdxs.put(seatIdx,lastChangeMoney);
                            }
                            lastRoundBeginMoney.put(seatIdx,player.getMoney()+player.getBet());
                        }
                        roundIdx = size;
                        lastChangeMoney = 0;
                        haveBet = false;
                        firstRaise = true;
                    }
                }
                if(isHideCard){
                        /*初始化最大下注量和上一手改变的位置*/
                    if(lastChangeMoney==0 && roundIdx==0){
                        for (int i = 0; i < curSeats.size(); i++) {
                            int seatIdx = curSeats.keyAt(i);
                            GameUser user = curSeats.get(seatIdx);
                            BackData.Player player = seats.get(seatIdx);
                            user.setBeginMoney(player.getMoney()+player.getBet());
                            lastSeatsMoney.put(seatIdx,player.getMoney());
                        }
                        lastChangeMoney = tableRecord.getBlindType();
                        if(data.getIsStraddle()>0){
                            lastChangeMoney = tableRecord.getBlindType()*2;
                        }
                        return;
                    }
                    int seatIdx = -1;
                    //FileIOUtil.saveToFile("记录动作开始");
                    for (int i = btnIdx+1; i < (curSeats.size()+btnIdx+1); i++) {
                        if(i<curSeats.size()){
                            seatIdx = curSeats.keyAt(i);
                        }else{
                            seatIdx = curSeats.keyAt(i-curSeats.size());
                        }
                        BackData.Player player = seats.get(seatIdx);//新的数据
                        GameUser gameUser = curSeats.get(seatIdx);
                        int newMoney = player.getMoney();
                        int bet = player.getBet();
                        Integer oldMoney = lastSeatsMoney.get(seatIdx);
                        int addMoney = oldMoney-newMoney;
                        GameAction action = new GameAction();
                        action.setUserName(gameUser.getUserName());
                        action.setSeatFlag(gameUser.getSeatFlag());
                        action.setRound(roundIdx);
                        if(newMoney==0){
                            if(player.getHideCard()==1){
                                action.setAction(Constant.ACTION_ALLIN);//Allin
                                gameUser.setJoin(true);
                                action.setBet(addMoney);
                                actions.add(action);
                            }else {
                                /*if(seatIdx==0){//判断自己allin
                                    if(seats.get(0).getCard0()!=-1 && seats.get(0).getCard1() != -1 && lastRoundSelfMoney==bet){
                                        action.setAction(Constant.ACTION_ALLIN);//Allin
                                        gameUser.setJoin(true);
                                        action.setBet(addMoney);
                                        actions.add(action);
                                    }
                                }*/
                            }
                        }else{
                            if(newMoney<oldMoney){//钱改变了
                                if(bet==0 && lastChangeMoney!=0){
                                    continue;
                                }
                                gameUser.setJoin(true);
                                if(roundIdx==0){
                                    if(preRaiseTotalCount>=1){//是否面临open
                                        gameUser.setFaceOpen(true);
                                    }
                                    if(preRaiseTotalCount>=2){//面临3bet
                                        gameUser.setFace3Bet(true);
                                    }
                                    //判定stl，foldStl
                                    curPlayerIfFaceFold(gameUser);
                                    //判定动作类型
                                    if (bet <= lastChangeMoney) {
                                        if(bet!=0){
                                            if(lastChangeMoney==tableRecord.getBlindType()){
                                                if(bet*2 == lastChangeMoney && "SB".equals(gameUser.getSeatFlag())){
                                                    continue;
                                                }
                                                if(bet*2 == lastChangeMoney && "BTN".equals(gameUser.getSeatFlag())){
                                                    continue;
                                                }
                                                if(bet == lastChangeMoney && "BB".equals(gameUser.getSeatFlag())){
                                                    continue;
                                                }
                                            }
                                            action.setAction(Constant.ACTION_CALL);//跟注
                                            action.setBet(addMoney);
                                            actions.add(action);
                                        }
                                    } else if(bet > lastChangeMoney){
                                        preRaiseTotalCount++;
                                        gameUser.setPFR(true);
                                        if(haveBet){
                                            action.setAction(Constant.ACTION_RAISE);//加注
                                        }else{
                                            action.setAction(Constant.ACTION_BET);
                                            haveBet = true;
                                        }
                                        if(gameUser.isStlPosition()){//有偷盲机会
                                            gameUser.setSTL(true);
                                        }
                                        if(preRaiseTotalCount>=2){
                                            gameUser.setIs3Bet(true);
                                        }
                                        action.setBet(addMoney);
                                        lastChangeMoney = bet;
                                        if(bet==0){
                                            lastChangeMoney = addMoney;
                                        }
                                        actions.add(action);
                                    }
                                }else{
                                    if (bet <= lastChangeMoney && lastChangeMoney!=0) {
                                        action.setAction(Constant.ACTION_CALL);//跟注
                                        gameUser.setCallCount(gameUser.getCallCount()+1);
                                        action.setBet(addMoney);
                                        actions.add(action);
                                    } else {
                                        Integer lastRoundChangeMoney = liveSeatIdxs.get(seatIdx);
                                        if(lastRoundChangeMoney!=null && lastRoundBeginMoney.get(seatIdx)== player.getMoney()){
                                            action.setAction(Constant.ACTION_CALL);//跟注
                                            if(roundIdx>3){
                                                gameUser.setCallCount(gameUser.getCallCount()+1);
                                            }else{
                                                if(preRaiseTotalCount>=1){//是否面临open
                                                    gameUser.setFaceOpen(true);
                                                }
                                                if(preRaiseTotalCount>=2){//面临3bet
                                                    gameUser.setFace3Bet(true);
                                                }
                                            }
                                            action.setBet(addMoney);
                                            action.setRound(roundIdx==3?1:roundIdx==4?3:4);
                                            actions.add(action);
                                            lastSeatsMoney.put(seatIdx,player.getMoney());
                                        }else{
                                            if(roundIdx==3 && gameUser.isPreFlopLastRaise()){//是否翻牌圈再加注
                                                gameUser.setCB(true);
                                            }
                                            gameUser.setRaiseCount(gameUser.getRaiseCount()+1);
                                            if(haveBet){
                                                action.setAction(Constant.ACTION_RAISE);//加注
                                            }else{
                                                action.setAction(Constant.ACTION_BET);
                                                haveBet = true;
                                            }
                                            action.setBet(addMoney);
                                            lastChangeMoney = bet;
                                            if(bet==0){
                                                lastChangeMoney = addMoney;
                                            }
                                            if(ifAllLiveInRiver(seats)){
                                                actions.add(action);
                                            }
                                        }
                                    }
                                }
                            }else if(newMoney==oldMoney){//钱没改变
                               /*判断弃牌*/
                                if(curAppType==0 && seatIdx!=0){//专属德扑圈的弃牌识别
                                    if((player.getHideCard()==1) && gameUser.getFoldRound()==-1){
                                        curPlayerIfFaceFold(gameUser);//先判断该玩家是否具备资格
                                        action.setAction(Constant.ACTION_FOLD);
                                        Integer lastRoundChangeMoney = liveSeatIdxs.get(seatIdx);
                                        if(roundIdx ==0 || (lastRoundChangeMoney!=null && lastRoundBeginMoney.get(seatIdx)== player.getMoney()) ){
                                            if(preRaiseTotalCount>=1){//是否面临open
                                                gameUser.setFaceOpen(true);
                                            }
                                            if(preRaiseTotalCount>=2){//面临3bet
                                                gameUser.setFace3Bet(true);
                                            }
                                            if(isSTLSeat(gameUser) && curTotalPlayCount>=3){//是偷盲位置
                                                if(allFoldBefore()){
                                                    gameUser.setStlPosition(true);
                                                }
                                            }
                                            if(gameUser.isFaceSTL()){//有被偷盲机会
                                                gameUser.setFoldSTL(true);
                                            }
                                        }
                                        gameUser.setFoldRound(roundIdx);
                                        actions.add(action);
                                    }
                                }else{
                                    if((player.getHideCard()==0) && gameUser.getFoldRound()==-1){
                                        curPlayerIfFaceFold(gameUser);//先判断该玩家是否具备资格
                                        action.setAction(Constant.ACTION_FOLD);
                                        Integer lastRoundChangeMoney = liveSeatIdxs.get(seatIdx);
                                        if(roundIdx ==0 || (lastRoundChangeMoney!=null && lastRoundBeginMoney.get(seatIdx)== player.getMoney()) ){
                                            if(preRaiseTotalCount>=1){//是否面临open
                                                gameUser.setFaceOpen(true);
                                            }
                                            if(preRaiseTotalCount>=2){//面临3bet
                                                gameUser.setFace3Bet(true);
                                            }
                                            if(isSTLSeat(gameUser) && curTotalPlayCount>=3){//是偷盲位置
                                                if(allFoldBefore()){
                                                    gameUser.setStlPosition(true);
                                                }
                                            }
                                            if(gameUser.isFaceSTL()){//有被偷盲机会
                                                gameUser.setFoldSTL(true);
                                            }
                                        }
                                        gameUser.setFoldRound(roundIdx);
                                        actions.add(action);
                                    }
                                }
                            }else{//钱变多(加筹码)

                            }
                        }
                        //更新这一条记录的钱
                        if(newMoney==0){
                            if(player.getHideCard()==1 && seatIdx!=0){
                                lastSeatsMoney.put(seatIdx,0);
                            }else{
                               /* if(seatIdx==0){
                                    if(seats.get(0).getCard0()!=-1 && seats.get(0).getCard1() != -1 && lastRoundSelfMoney==bet){
                                        lastSeatsMoney.put(0,0);
                                    }
                                }*/
                            }
                        }else{
                            if(bet!=0){
                                lastSeatsMoney.put(seatIdx,player.getMoney());
                            }
                        }
                    }
                    //FileIOUtil.saveToFile("记录动作结束");
                }else{//还没出现hideCard时候更新money
                    for (int i = 0; i < lastSeatsMoney.size(); i++) {
                        int seatIdx = lastSeatsMoney.keyAt(i);
                        int money = seats.get(seatIdx).getMoney();
                        if(money >0){
                            lastSeatsMoney.put(seatIdx,money);
                        }
                    }
                }
            }
        }
    }
    /*在河牌圈是否全部都没弃牌*/
    public boolean ifAllLiveInRiver(List<BackData.Player> seats){
        if(roundIdx==5 && curAppType==0){
            for (int i = 0; i < curSeats.size(); i++) {
                int seatIdx = curSeats.keyAt(i);
                GameUser user = curSeats.get(seatIdx);
                BackData.Player player = seats.get(seatIdx);
                if(i==0 && player.getHideCard()==0){
                    return false;
                }
                if(i>0 && player.getHideCard()==1){
                    return false;
                }
            }
        }
        return true;
    }
    /*是否有偷盲机会和被偷盲机会*/
    public void curPlayerIfFaceFold(GameUser gameUser){
        if(curTotalPlayCount>3){
            if(isSTLSeat(gameUser)){//判定偷盲机会
                if(allFoldBefore()){
                    gameUser.setStlPosition(true);
                }
            }
            //判断是否有被偷盲机会
            GameUser stlUser = haveStealBlinds();
            if(stlUser !=null){//有人偷盲
                if ("SB".equals(gameUser.getSeatFlag())){
                    if("CO".equals(stlUser.getSeatFlag())){
                        if(curSeats.get(curBtnIdx).getFoldRound()==0){//btn位fold
                            gameUser.setFaceSTL(true);
                        }
                    }else if("BTN".equals(stlUser.getSeatFlag())){
                        gameUser.setFaceSTL(true);
                    }
                }else if("BB".equals(gameUser.getSeatFlag())){
                    if("CO".equals(stlUser.getSeatFlag())){
                        boolean sbFold = false;
                        boolean btnFold = false;
                        for (int j = 0; j < curSeats.size(); j++) {
                            GameUser user = curSeats.valueAt(j);
                            if("BTN".equals(user.getSeatFlag()) && user.getFoldRound()==0){
                                btnFold = true;
                            }else if("SB".equals(user.getSeatFlag()) && user.getFoldRound()==0){
                                sbFold = true;
                            }
                        }
                        if(sbFold && btnFold){
                            gameUser.setFaceSTL(true);
                        }
                    }else if("BTN".equals(stlUser.getSeatFlag())){
                        for (int j = 0; j < curSeats.size(); j++) {
                            GameUser user = curSeats.valueAt(j);
                            if("SB".equals(user.getSeatFlag()) && user.getFoldRound()==0){
                                gameUser.setFaceSTL(true);
                            }
                        }
                    }else if("SB".equals(stlUser.getSeatFlag())){
                        gameUser.setFaceSTL(true);
                    }
                }
            }
        }else if(curTotalPlayCount==3){
            if(isSTLSeat(gameUser)){
                if("BTN".equals(gameUser.getSeatFlag())){
                    gameUser.setStlPosition(true);
                }else if("SB".equals(gameUser.getSeatFlag())){
                    if(curSeats.get(curBtnIdx).getFoldRound()==0){
                        gameUser.setStlPosition(true);
                    }
                }
            }
            GameUser user = haveStealBlinds();
            if(user!=null){
                if("SB".equals(gameUser.getSeatFlag())){
                    if("BTN".equals(user.getSeatFlag())){ //偷盲位是btn
                        gameUser.setFaceSTL(true);
                    }
                }else if("BB".equals(gameUser.getSeatFlag())){
                    if("BTN".equals(user.getSeatFlag())){ //偷盲位是btn
                        for (int j = 0; j < curSeats.size(); j++) {
                            GameUser user1 = curSeats.valueAt(j);
                            if("SB".equals(user1.getSeatFlag()) && user1.getFoldRound()==0){
                                gameUser.setFaceSTL(true);
                            }
                        }
                    }else if("SB".equals(user.getSeatFlag())){
                        gameUser.setFaceSTL(true);
                    }
                }
            }

        }
    }
    /*是否有大小盲*/
    public boolean haveHalfBlindsAndBlinds(BackData data){
        if(tableRecord.getBlindType()==0 || curSeats.size()==0){
            return false;
        }
        boolean haveHalfBlinds = false;
        boolean haveBlinds = false;
        List<BackData.Player> seats = data.getSeats();
        for (int i = 0; i < curSeats.size(); i++) {
            BackData.Player player = seats.get(curSeats.keyAt(i));
            GameUser user = curSeats.valueAt(i);
            if(player.getBet()*2==tableRecord.getBlindType() && ("BTN".equals(user.getSeatFlag()) || "SB".equals(user.getSeatFlag()))){
                haveHalfBlinds = true;
            }else if(player.getBet()==tableRecord.getBlindType() && "BB".equals(user.getSeatFlag())){
                haveBlinds = true;
            }
        }
        if(haveHalfBlinds && haveBlinds){
            return true;
        }
        return false;
    }
    /** 判断一手数据是否出现有左下角小牌 */
    public boolean haveHideCard(List<BackData.Player> seats){
        if(curAppType==0){
            for (int i = 1; i < seats.size(); i++) {
                if(seats.get(i).getHideCard()==0){
                    return true;
                }
            }
        }else{
            for (int i = 1; i < seats.size(); i++) {
                if(seats.get(i).getHideCard()==1){
                    return true;
                }
            }
        }
        return false;
    }

    /** 判断一条记录是否出现名字 */
    public boolean haveName(List<BackData.Player> seats){
        boolean haveName = false;
        for (int i = 0; i < seats.size(); i++) {
            if(!TextUtils.isEmpty(seats.get(i).getName())){
                seatNames.put(i,seats.get(i).getName());
                if(!haveName){
                    //FileIOUtil.saveToFile("这一手有识别到名字");
                    haveName = true;
                }
            }
        }
        return haveName;
    }
    public boolean haveCurSeatsName(){//是否已经记录数据
        for (int i = 0; i < curSeats.size(); i++) {
            GameUser gameUser = curSeats.valueAt(i);
            if(!TextUtils.isEmpty(gameUser.getUserName()) && (!"self".equals(gameUser.getUserName()))){
                return true;
            }
        }
        return false;
    }
    public GameUser haveStealBlinds(){//是否有人偷盲
        if(roundIdx!=0){
            return null;
        }
        for (int i = 0; i < curSeats.size(); i++) {
            GameUser user = curSeats.valueAt(i);
            if("CO".equals(user.getSeatFlag()) || "BTN".equals(user.getSeatFlag()) || "SB".equals(user.getSeatFlag())){
                if(user.isSTL()){
                    return user;
                }
            }else{
                continue;
            }
        }
        return null;
    }
    public void saveEndMoney(List<BackData.Player> seats){
        for (int i = 0; i < curSeats.size(); i++) {
            GameUser user = curSeats.get(curSeats.keyAt(i));
            if(curSeats.keyAt(i)>=seats.size()){
                FileIOUtil.saveToFile("座位号异常");
                continue;
            }
            BackData.Player player = seats.get(curSeats.keyAt(i));
            if(player!=null){
                if(player.getMoney()==0){
                    user.setEndMoney(lastSeatsMoney.get(curSeats.keyAt(i)).intValue());
                }else{
                    user.setEndMoney(player.getMoney()+player.getBet());
                }
            }
        }
    }
    public void saveLastName(){//保存上一手的各位置名字
        if(haveCurSeatsName()){
            for (int i = 0; i < curSeats.size(); i++) {
                int seatIdx = curSeats.keyAt(i);
                GameUser user = curSeats.get(seatIdx);
                if(!TextUtils.isEmpty(user.getUserName())){
                    lastSeatsNames.put(seatIdx,user.getUserName());
                }else{
                    if(seatIdx==0){
                       lastSeatsNames.put(0,"self");
                    }
                }
            }
        }
    }
    public boolean allFoldBefore(){//前面的人全弃牌
        for (int i = actions.size()-1; i >=0; i--) {
            if(actions.get(i).getAction()!=Constant.ACTION_FOLD){
                return false;
            }
        }
        return true;
    }
    public boolean isSTLSeat(GameUser gameUser) {
        if (gameUser == null) {
            return false;
        }
        //在三个位置，并且前面所有动作都是fold
        if ("CO".equals(gameUser.getSeatFlag()) || "BTN".equals(gameUser.getSeatFlag()) || "SB".equals(gameUser.getSeatFlag())) {
            return true;
        }
        return false;
    }
    public void setWinner(){
        StringBuffer winner = new StringBuffer();
        for (int i = 0; i < curSeats.size(); i++) {
            GameUser gamer = curSeats.valueAt(i);
            int endMoney = gamer.getEndMoney();
            int beginMoney = gamer.getBeginMoney();
            if (endMoney>beginMoney){
                if(tableRecord.getBlindType()>0){
                    if(TextUtils.isEmpty(winner.toString())){
                        winner.append(gamer.getUserName()).append(" 获得 ").append((endMoney-beginMoney)/Double.valueOf(tableRecord.getBlindType())+" BB");
                    }else{
                        winner.append("&").append(gamer.getUserName()).append(" 获得 ").append((endMoney-beginMoney)/Double.valueOf(tableRecord.getBlindType())+" BB");
                    }
                }
            }
        }
        pokerRecord.setWinner(winner.toString());
    }
    public void sendReqData(){
        List<GameUser> users = new ArrayList<GameUser>();
        for (int i = 0; i < curSeats.size(); i++) {
            int seatIdx = curSeats.keyAt(i);
            GameUser user = curSeats.get(seatIdx);
            if(TextUtils.isEmpty(user.getUserName())){//没有名字，则用上一手的名字，底层识别名字不出错可避免chucuo
                String lastName = lastSeatsNames.get(seatIdx);
                if(!TextUtils.isEmpty(lastName)){
                    user.setUserName(lastName);
                }
            }
            users.add(user);
        }
        setWinner();//获取赢家
        if(curSeats.size()>0){
            pokerRecord.setUsers(users);
        }
        Gson gson = new Gson();
        ReqData reqData = new ReqData();
        String saveJson = gson.toJson(param, SaveRecordParam.class);
        reqData.setParam(saveJson);
        reqData.setReqfunc("reqSaveRecord");
        reqData.setReqid("18800344554");
        reqData.setReqno(TimeUtil.getCurrentDateToMinutes(new Date())+disposeNumber());
        final String json = gson.toJson(reqData, ReqData.class);
        OneHandLog log = new OneHandLog();
        log.setDate(System.currentTimeMillis()/1000);
        log.setLog(json);
        log.save();
        new Thread(new Runnable() {
            @Override
            public void run() {//保存数据
                FileIOUtil.writeLine();
                FileIOUtil.saveToFile(json);
                FileIOUtil.writeLine();
            }
        }).start();
//        new SaveDataUtil().disposeHandLog(json);
    }
    public String disposeNumber(){//11---->000011
        String s = new Integer(seq).toString();
        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < 6 - s.length(); i++) {
            sb.append("0");
        }
        sb.append(s);
        seq++;
        return sb.toString();
    }
    public void searchPreFlopLastRaisePlayer(int size){
        if(size == 3 && roundIdx == 0){//查找翻牌前最后一个加注
            label:   for (int i = actions.size()-1; i >=0 ; i--) {
                GameAction action = actions.get(i);
                if(action.getAction()== Constant.ACTION_BET || action.getAction()==Constant.ACTION_RAISE){
                    for (int j = 0; j < curSeats.size(); j++) {
                        GameUser user = curSeats.valueAt(j);
                        if(user.getSeatFlag().equals(action.getSeatFlag())){
                            user.setPreFlopLastRaise(true);
                            break label;
                        }
                    }
                }
            }
        }
    }
    public int getCurTotalPlayCount(List<BackData.Player> seats){
        int count = 0;
        if(isHideCard){
            if(curAppType==0){
                for (int i = 0; i < seats.size(); i++) {
                    if(seats.get(i).getMoney()>0){
                        count++;
                    }
                }
            }else{
                count = 1;
                for (int i = 1; i < seats.size(); i++) {
                    if(seats.get(i).getHideCard()==1){
                        count++;
                    }
                }
            }
        }else{
            for (int i = 0; i < seats.size(); i++) {
                if(seats.get(i).getMoney()>0){
                    count++;
                }
            }
        }

        return count;
    }
    public String getSeatName(int seatIdx){
        String name = seatNames.get(seatIdx);
        if(TextUtils.isEmpty(name)){
            return "";
        }
        return name;
    }
    public boolean haveSeatsName(){
        if(seatNames.size()>0){
            return true;
        }
        return false;
    }
    public PokerRecord getPokerRecord(){
        return pokerRecord;
    }
}
