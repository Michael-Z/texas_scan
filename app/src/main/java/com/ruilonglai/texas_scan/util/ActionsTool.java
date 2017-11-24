package com.ruilonglai.texas_scan.util;

import android.text.TextUtils;
import android.util.SparseArray;
import android.util.SparseIntArray;

import com.google.gson.Gson;
import com.ruilonglai.texas_scan.entity.GameAction;
import com.ruilonglai.texas_scan.entity.GameUser;
import com.ruilonglai.texas_scan.entity.OneHandLog;
import com.ruilonglai.texas_scan.entity.PokerRecord;
import com.ruilonglai.texas_scan.entity.ReqData;
import com.ruilonglai.texas_scan.entity.SaveRecordParam;
import com.ruilonglai.texas_scan.entity.TableRecord;

import java.io.IOException;
import java.util.Date;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

/**
 * Created by Administrator on 2017/8/14.
 */

public class ActionsTool {

    private final static String TAG = "ActionTool";
    private static int seq = 0;
    private int straddle;
    private int lastChangeMoney = 0;//每一圈上一条改变的money，主要用于allin一些概率的记录
    private int pfrRaiseCount = 0;//翻牌前加注次数
    private int lastRaiseSeatIdx = -1;//翻牌前上一条加注动作的seatIdx
    private boolean haveCB = false;//在翻牌圈有人cb
    private int lastRoundIdx = 0;//上一条动作的阶段数
    private int blinds;

    public void disposeAction(String json, SparseArray<String> names, String userId){
        if(TextUtils.isEmpty(json)){
            return;
        }
        SaveRecordParam param = null;
        try {
            param = GsonUtil.parseJsonWithGson(json, SaveRecordParam.class);
        } catch (Exception e) {
            MyLog.e(TAG,"解析动作错误");
            return;
        }
        PokerRecord pokerRecord = param.getPokerRecord();
        TableRecord tableRecord = param.getTableRecord();
        blinds = tableRecord.getBlindType();
        lastChangeMoney = blinds;
        straddle = tableRecord.getStraddle();
        SparseArray<GameUser> users = new SparseArray<>();
        SparseIntArray foldSeats = new SparseIntArray();
        List<GameUser> gameUsers = pokerRecord.getUsers();
        for (int i = 0; i < gameUsers.size(); i++) {
            GameUser user = gameUsers.get(i);
            MyLog.e(TAG,"seatIdx-->"+user.getSeatIdx()+" beginMoney-->"+user.getBeginMoney()+" endMoney-->"+user.getEndMoney());
            if(Math.abs((user.getEndMoney()-user.getBeginMoney())/blinds)>10000 || user.getBeginMoney()>1000000
                    || user.getEndMoney()>1000000 || user.getBeginMoney()<0 || user.getEndMoney()<0){
                user.setBeginMoney(0);
                user.setEndMoney(0);
            }
            foldSeats.put(user.getSeatIdx(),0);
            users.put(user.getSeatIdx(),user);
            if(names!=null)
            names.put(user.getSeatIdx(),user.getUserName());
        }
        int button = pokerRecord.getButton();
        List<GameAction> actions = pokerRecord.getActions();
        if(button == -1){
          return;
        }
        int changeMoney = blinds;
        if(straddle>0){
            changeMoney = blinds*2;
        }
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < actions.size(); i++) {
            GameAction action = actions.get(i);
            int seatIdx = action.getSeatIdx();
            GameUser user = users.get(seatIdx);
            if(user==null)
                continue;
            action.setUserName(user.getUserName());
            action.setSeatFlag(user.getSeatFlag());
            sb.append(action.getSeatIdx()+"号位").append("["+Constant.ACTIONS[action.getAction()]+"]\n").append(action.toString(1)).append("#");
            if(action.getRound()<lastRoundIdx){//当前都做所在街数小于上一个动作的街数，则后续动作都不记录
                break;
            }
            if(action.getRound()==0){//判断是否是偷盲位置，是否面对偷盲
                curPlayerIfFaceFold(users,user,actions,i,button,straddle);
            }
            disposeOneAction(action,user,i,actions,users);
            if(action.getAction()==Constant.ACTION_FOLD){
                foldSeats.put(seatIdx,1);
            }
        }
        if(tableRecord.getPlatformType()==1){
            for (int i = 0; i < foldSeats.size(); i++) {
                int seatIdx = foldSeats.keyAt(i);
                if(foldSeats.get(seatIdx)==0 && pokerRecord.getRiver()!=-1){
                    GameUser user = users.get(seatIdx);
                    if(user!=null){
                        user.setTurn(true);
                        if(user.getCard1()==-1 && user.getCard2()==-1){
                            user.setWinTurn(false);
                        }
                    }
                }
            }
        }
        getTurnRiverData(users);
        gameUsers.clear();
        sb.append("#").append("各玩家记录的数据").append("#");
        for (int i = 0; i < users.size(); i++) {
            gameUsers.add(users.valueAt(i));
            sb.append(users.valueAt(i).toString(true)).append("#");
        }
        if(sb.length()>0)
        sb.deleteCharAt(sb.length()-1);
        /*保存每一手的动作日志*/
        OneHandLog oneHandLog = new OneHandLog();
        oneHandLog.setPlattype(tableRecord.getPlatformType());
        oneHandLog.setDate(System.currentTimeMillis());
        oneHandLog.setLog(sb.toString());
        oneHandLog.save();
        Gson gson = new Gson();
        ReqData reqData = new ReqData();
        String saveJson = gson.toJson(param, SaveRecordParam.class);
        reqData.setParam(saveJson);
        reqData.setReqfunc("reqSaveRecord");
        reqData.setReqid(userId);
        reqData.setReqno(TimeUtil.getCurrentDateToMinutes(new Date())+disposeNumber());
        String data = gson.toJson(reqData, ReqData.class);
        FileIOUtil.saveToFile(data);
//        new SaveDataUtil().disposeHandLog(data);
        //传输数据到服务器
        HttpUtil.sendPostRequestData("savehand", data, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                MyLog.e(TAG,"发送失败"+e.toString());
            }
            @Override
            public void onResponse(Call call, Response response) throws IOException {
                MyLog.e(TAG,"response:"+response.toString());
                MyLog.e(TAG,"response:"+response.body().string());
            }
        });
    }
    /*处理动作记录概率*/
    public void disposeOneAction(GameAction action,GameUser user,int i,List<GameAction> actions,SparseArray<GameUser> users){
        int round = action.getRound();
        if(lastRoundIdx<round){
            lastChangeMoney = 0;
            lastRoundIdx = round;
        }
        if(round==3){//判断最后一个加注者
            if(lastRaiseSeatIdx!=-1){
                users.get(lastRaiseSeatIdx).setPreFlopLastRaise(true);
                lastRaiseSeatIdx = -1;
            }
            if(haveCB){
                user.setFaceCB(true);
            }
        }
        user.setLastActionRound(round);
        if(round==0){//翻牌前
            if(pfrRaiseCount>0){
                user.setFaceOpen(true);
                if(pfrRaiseCount>1){
                    user.setFace3Bet(true);
                }
            }
        }
        switch (action.getAction()){
            case Constant.ACTION_CHECK://看牌
                break;
            case Constant.ACTION_CALL://跟注
                if(round==0){//翻牌前
                    user.setJoin(true);
                }else{
                    user.setCallCount(user.getCallCount()+1);
                }
                break;
            case Constant.ACTION_OPEN://open
                if(round==0){
                    user.setJoin(true);
                    user.setPFR(true);
                    lastRaiseSeatIdx = action.getSeatIdx();
                    pfrRaiseCount++;
                    if(user.isStlPosition()){
                        user.setSTL(true);
                    }
                }else if(round==3){
                    user.setRaiseCount(user.getRaiseCount()+1);
                    if(user.isPreFlopLastRaise()){
                        user.setCB(true);
                        haveCB = true;
                    }
                }else if(round==4){
                    user.setRaiseCount(user.getRaiseCount()+1);
                }else if(round==5){
                    user.setRaiseCount(user.getRaiseCount()+1);
                }
                lastChangeMoney = action.getBet();
                break;
            case Constant.ACTION_BET://下注
                if(round==0){
                    user.setJoin(true);
                    user.setPFR(true);
                    lastRaiseSeatIdx = action.getSeatIdx();
                    pfrRaiseCount++;
                    if(user.isStlPosition()){
                        user.setSTL(true);
                    }
                }else if(round==3){
                    user.setRaiseCount(user.getRaiseCount()+1);
                    if(user.isPreFlopLastRaise()){
                        user.setCB(true);
                        haveCB = true;
                    }
                }else if(round==4){
                    user.setRaiseCount(user.getRaiseCount()+1);
                }else if(round==5){
                    user.setRaiseCount(user.getRaiseCount()+1);
                }
                lastChangeMoney = action.getBet();
                break;
            case Constant.ACTION_RAISE://加注
                if(round==0){
                    user.setJoin(true);
                    user.setPFR(true);
                    lastRaiseSeatIdx = action.getSeatIdx();
                    if(pfrRaiseCount>0){
                        user.setIs3Bet(true);
                    }
                    if(user.isStlPosition()){
                        user.setSTL(true);
                    }
                    pfrRaiseCount++;
                }else if(round==3){
                    user.setRaiseCount(user.getRaiseCount()+1);
                    if(user.isPreFlopLastRaise()){
                        user.setCB(true);
                        haveCB = true;
                    }
                }else if(round==4){
                    user.setRaiseCount(user.getRaiseCount()+1);
                }else if(round==5){
                    user.setRaiseCount(user.getRaiseCount()+1);
                }
                lastChangeMoney = action.getBet();
                break;
            case Constant.ACTION_3Bet://3bet
                if(round==0){
                    user.setJoin(true);
                    user.setPFR(true);
                    lastRaiseSeatIdx = action.getSeatIdx();
                    if(pfrRaiseCount>0){
                        user.setIs3Bet(true);
                    }
                    if(user.isStlPosition()){
                        user.setSTL(true);
                    }
                    pfrRaiseCount++;
                }else if(round==3){
                    user.setRaiseCount(user.getRaiseCount()+1);
                    if(user.isPreFlopLastRaise()){
                        user.setCB(true);
                        haveCB = true;
                    }
                }else if(round==4){
                    user.setRaiseCount(user.getRaiseCount()+1);
                }else if(round==5){
                    user.setRaiseCount(user.getRaiseCount()+1);
                }
                lastChangeMoney = action.getBet();
                break;
            case Constant.ACTION_ALLIN://allin
                if(round==0){
                    user.setJoin(true);
                    if(action.getAddMoney()>lastChangeMoney){
                        user.setPFR(true);
                        if(action.getAddMoney()>(lastChangeMoney-blinds)*2){
                            if(pfrRaiseCount>0){
                                user.setIs3Bet(true);
                            }
                            pfrRaiseCount++;
                        }
                    }
                }
                break;
            case Constant.ACTION_STRADDLE://straddle
                lastChangeMoney = action.getBet();
                break;
            case Constant.ACTION_FOLD://弃牌
                user.setFoldRound(action.getRound());
                if(round==0){
                   if(user.isFaceSTL()){
                       user.setFoldSTL(true);
                   }
                }else if(round==3){
                    if(haveCB && !user.isCB()){
                        user.setFoldCB(true);
                    }
                }else if(round==4){

                }else if(round==5){

                }
                break;
        }
    }
    public static String disposeNumber(){//11---->000011
        String s = new Integer(seq).toString();
        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < 6 - s.length(); i++) {
            sb.append("0");
        }
        sb.append(s);
        seq++;
        return sb.toString();
    }
    /*是不是偷盲位置*/
    public boolean isSTLSeat(GameUser gameUser,int size) {
        if (gameUser == null) {
            return false;
        }
        //在三个位置，并且前面所有动作都是fold
        if (("CO".equals(gameUser.getSeatFlag()) && size>4) || "BTN".equals(gameUser.getSeatFlag())
                || "SB".equals(gameUser.getSeatFlag())||(straddle>0 && "BB".equals(gameUser.getSeatFlag()) && size>3) ) {
            return true;
        }
        return false;
    }

    /*前面动作的人都是fold牌*/
    public static boolean lastActionsAllFold(List<GameAction> actions,int index){
        for (int i = 0; i < index; i++) {
            GameAction action = actions.get(i);
             if(action.getAction()!=Constant.ACTION_FOLD && action.getAction()!=Constant.ACTION_STRADDLE){
                 return false;
             }
        }
        return true;
    }
    /*
    * index：当前动作的角标
    * */
    public void curPlayerIfFaceFold(SparseArray<GameUser> users,GameUser gameUser,List<GameAction> actions,int index,int btnIdx,int straddle){
        GameUser stlUser = haveStealBlinds(users);
        if(isSTLSeat(gameUser,users.size()) && stlUser==null){//判定偷盲机会
            if(lastActionsAllFold(actions,index)){
                gameUser.setStlPosition(true);
            }
        }
        if(stlUser!=null){
            switch (stlUser.getSeatFlag()){
                case "CO":
                case "BTN":
                case "SB":
                   if(straddle>0){
                       for (int i = 0; i < users.size(); i++) {
                           GameUser user = users.valueAt(i);
                           if(user.getSeatFlag().equals("UTG") && allActionsFoldToSTL(actions,stlUser.getSeatIdx(),index)){
                                   user.setFaceSTL(true);
                           }
                       }
                   }else{
                       for (int i = 0; i < users.size(); i++) {
                           GameUser user = users.valueAt(i);
                           if(user.getSeatFlag().equals("BB") && allActionsFoldToSTL(actions,stlUser.getSeatIdx(),index)){
                               user.setFaceSTL(true);
                           }
                       }
                   }
                    break;
                case "BB":
                    if(straddle>0){
                        for (int i = 0; i < users.size(); i++) {
                            GameUser user = users.valueAt(i);
                            if(user.getSeatFlag().equals("UTG") && allActionsFoldToSTL(actions,stlUser.getSeatIdx(),index)){
                                user.setFaceSTL(true);
                            }
                        }
                    }
                    break;
            }
        }
    }
    public boolean allActionsFoldToSTL(List<GameAction> actions,int stlIdx,int index){
        for (int i = 0; i < index; i++) {
            GameAction action = actions.get(i);
            if(action.getSeatIdx()==stlIdx){
                continue;
            }
            if(action.getAction()!=Constant.ACTION_FOLD && action.getAction()!=Constant.ACTION_STRADDLE){
                return false;
            }
        }
        return true;
    }
    public static GameUser haveStealBlinds(SparseArray<GameUser> users){//获取偷盲玩家
        for (int i = 0; i < users.size(); i++) {
            GameUser user = users.valueAt(i);
            if("CO".equals(user.getSeatFlag()) || "BTN".equals(user.getSeatFlag()) || "SB".equals(user.getSeatFlag())|| "BB".equals(user.getSeatFlag())){
                if(user.isSTL()){
                    return user;
                }
            }else{
                continue;
            }
        }
        return null;
    }
    /*记录翻牌胜率*/
    public void getTurnRiverData(SparseArray<GameUser> users){
        for (int i = 0; i < users.size(); i++) {
            GameUser user = users.valueAt(i);
            if(user.getEndMoney()>user.getBeginMoney() && user.isTurn()){
                user.setWinTurn(true);//翻牌胜率
            }
        }
    }
}
