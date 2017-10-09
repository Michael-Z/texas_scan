package com.ruilonglai.texas_scan.util;

import android.text.TextUtils;
import android.util.Log;
import android.util.SparseArray;
import android.util.SparseIntArray;

import com.google.gson.Gson;
import com.ruilonglai.texas_scan.entity.GameAction;
import com.ruilonglai.texas_scan.entity.GameUser;
import com.ruilonglai.texas_scan.entity.PokerRecord;
import com.ruilonglai.texas_scan.entity.ReqData;
import com.ruilonglai.texas_scan.entity.SaveRecordParam;
import com.ruilonglai.texas_scan.entity.TableRecord;
import com.ruilonglai.texas_scan.util.Constant;
import com.ruilonglai.texas_scan.util.FileIOUtil;
import com.ruilonglai.texas_scan.util.GsonUtil;
import com.ruilonglai.texas_scan.util.HttpUtil;
import com.ruilonglai.texas_scan.util.SaveDataUtil;
import com.ruilonglai.texas_scan.util.TimeUtil;

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
    public static void disposeAction(String json,SparseArray<String> names,String userId){
        if(TextUtils.isEmpty(json)){
            return;
        }
        SaveRecordParam param = null;
        try {
            param = GsonUtil.parseJsonWithGson(json, SaveRecordParam.class);
        } catch (Exception e) {
            Log.e(TAG,"解析动作错误");
            return;
        }
        PokerRecord pokerRecord = param.getPokerRecord();
        TableRecord tableRecord = param.getTableRecord();
        int blinds = tableRecord.getBlindType();
        int straddle = tableRecord.getStraddle();
        SparseArray<GameUser> users = new SparseArray<>();
        List<GameUser> gameUsers = pokerRecord.getUsers();
        for (int i = 0; i < gameUsers.size(); i++) {
            GameUser user = gameUsers.get(i);
            users.put(user.getSeatIdx(),user);
            if(names!=null)
            names.put(user.getSeatIdx(),user.getUserName());
        }
        int button = pokerRecord.getButton();
        List<GameAction> actions = pokerRecord.getActions();
        if(button == -1){
          return;
        }
        SparseIntArray lastActionMoney = new SparseIntArray();
        if(users.size()==2){
            for (int i = 0; i < users.size(); i++) {
                int seatIdx = users.keyAt(i);
                GameUser user = users.valueAt(i);
                if(button==seatIdx){
                    lastActionMoney.put(seatIdx,blinds/2);
                }else{
                    lastActionMoney.put(seatIdx,blinds);
                }
            }
        }else if(users.size()==3 || (users.size()==4 && straddle<=0) || (users.size()>4 && straddle <=0)){
            for (int i = 0; i < users.size(); i++) {
                int seatIdx = users.keyAt(i);
                GameUser user = users.valueAt(i);
                if("SB".equals(user.getSeatFlag())){
                    lastActionMoney.put(seatIdx,blinds/2);
                }else if("BB".equals(user.getSeatFlag())){
                    lastActionMoney.put(seatIdx,blinds);
                }else{
                    lastActionMoney.put(seatIdx,0);
                }
            }
        }else if(users.size()==4 && straddle>0){
            for (int i = 0; i < users.size(); i++) {
                int seatIdx = users.keyAt(i);
                GameUser user = users.valueAt(i);
                if("SB".equals(user.getSeatFlag())){
                    lastActionMoney.put(seatIdx,blinds/2);
                }else if("BB".equals(user.getSeatFlag())){
                    lastActionMoney.put(seatIdx,blinds);
                }else if("CO".equals(user.getSeatFlag())){
                    lastActionMoney.put(seatIdx,blinds*2);
                }else{
                    lastActionMoney.put(seatIdx,0);
                }
            }
        }else if(users.size()>4 && straddle>0){
            for (int i = 0; i < users.size(); i++) {
                int seatIdx = users.keyAt(i);
                GameUser user = users.valueAt(i);
                if("SB".equals(user.getSeatFlag())){
                    lastActionMoney.put(seatIdx,blinds/2);
                }else if("BB".equals(user.getSeatFlag())){
                    lastActionMoney.put(seatIdx,blinds);
                }else if("UTG".equals(user.getSeatFlag())){
                    lastActionMoney.put(seatIdx,blinds*2);
                }else{
                    lastActionMoney.put(seatIdx,0);
                }
            }
        }
        int changeMoney = blinds;
        if(straddle>0){
            changeMoney = blinds*2;
        }
        int pfrRaiseCount = 0;
        int lastRaiseSeatIdx = -1;
        boolean haveKnowTheLastRaiseSeatIdx = false;
        boolean haveCB = false;
        int lastRoundIdx = 0;
        for (int i = 0; i < actions.size(); i++) {
            GameAction action = actions.get(i);
            int seatIdx = action.getSeatIdx();
            GameUser user = users.get(seatIdx);
            if(user==null)
                continue;
            if(action.getRound()<lastRoundIdx){//当前都做所在街数小于上一个动作的街数，则后续动作都不记录
                break;
            }
            int addMoney = action.getAddMoney();
            int lastMoney = lastActionMoney.get(seatIdx);
//            if(action.getAction()== -1){//action只有两种，一种弃牌==3，另外全部都是-1
                curPlayerIfFaceFold(users,user,actions,i,button);
                if(lastRoundIdx != action.getRound()){//换圈初始化每个位置的钱
                    for (int j = 0; j < lastActionMoney.size(); j++) {
                        int key = lastActionMoney.keyAt(j);
                        lastActionMoney.put(key,0);
                    }
                    lastMoney = 0;
                    changeMoney = 0;
                    lastRoundIdx = action.getRound();
                    if(!haveKnowTheLastRaiseSeatIdx && lastRaiseSeatIdx!=-1){//是否是翻牌前最后一个加注
                        users.get(lastRaiseSeatIdx).setPreFlopLastRaise(true);
                        haveKnowTheLastRaiseSeatIdx = true;
                    }
                }
                user.setLastActionRound(action.getRound());
                switch (action.getRound()){
                    case 0:
                        if((addMoney+lastMoney)==changeMoney){
                            user.setJoin(true);
                            if(pfrRaiseCount>=1){
                                user.setFaceOpen(true);
                            }
                            if(pfrRaiseCount>=2){
                                user.setFace3Bet(true);
                            }
                            action.setAction(Constant.ACTION_CALL);
                            lastActionMoney.put(seatIdx,addMoney+lastMoney);
                        }else if(addMoney+lastMoney>changeMoney){
                            user.setJoin(true);
                            user.setPFR(true);//翻牌前加注
                            if(pfrRaiseCount>=1){
                                user.setFaceOpen(true);
                                user.setIs3Bet(true);
                            }
                            if(pfrRaiseCount>=2){
                                user.setFace3Bet(true);
                            }
                            if(user.isStlPosition() && haveStealBlinds(users)==null){
                                 user.setSTL(true);
                            }
                            action.setAction(Constant.ACTION_RAISE);
                            lastRaiseSeatIdx = seatIdx;
                            changeMoney = addMoney+lastMoney;
                            pfrRaiseCount++;
                            lastActionMoney.put(seatIdx,addMoney+lastMoney);

                        }else{
                            if(action.getAction() == Constant.ACTION_FOLD){
                                user.setFoldRound(0);
                                if(pfrRaiseCount>=1){
                                    user.setFaceOpen(true);
                                }
                                if(pfrRaiseCount>=2){
                                    user.setFace3Bet(true);
                                }
                                if(user.isFaceSTL()){//fold偷盲
                                    user.setFoldSTL(true);
                                }
                            }
                        }
                        break;
                    case 3 :
                        if(haveCB){
                            user.setFaceCB(true);
                        }
                        if((addMoney+lastMoney)==changeMoney){
                            user.setCallCount(user.getCallCount()+1);
                            action.setAction(Constant.ACTION_CALL);
                            lastActionMoney.put(seatIdx,addMoney+lastMoney);
                            lastMoney = addMoney +lastMoney;
                        }else if(addMoney+lastMoney>changeMoney){
                            user.setRaiseCount(user.getRaiseCount()+1);
                            action.setAction(Constant.ACTION_RAISE);
                            if(user.isPreFlopLastRaise()){//翻拍后再加注
                                user.setCB(true);
                                haveCB = true;
                            }
                            changeMoney = addMoney + lastMoney;
                            lastActionMoney.put(seatIdx,addMoney+lastMoney);
                        }else{
                            if(action.getAction() == Constant.ACTION_FOLD){
                                user.setFoldRound(action.getRound());
                                if(haveCB){
                                    user.setFoldCB(true);
                                }
                            }
                        }
                        break;
                    case 4: case 5:
                        if((addMoney+lastMoney)==changeMoney){
                            user.setCallCount(user.getCallCount()+1);
                            action.setAction(Constant.ACTION_CALL);
                            lastActionMoney.put(seatIdx,addMoney+lastMoney);
                            lastMoney = addMoney + changeMoney;
                        }else if(addMoney+lastMoney>changeMoney){
                            user.setRaiseCount(user.getRaiseCount()+1);
                            action.setAction(Constant.ACTION_RAISE);
                            changeMoney = addMoney + lastMoney;
                            lastActionMoney.put(seatIdx,addMoney+lastMoney);
                        }else{
                            if(action.getAction() == Constant.ACTION_FOLD){
                                user.setFoldRound(action.getRound());
                            }
                        }
                    break;
                }
//            }
        }
        gameUsers.clear();
        for (int i = 0; i < users.size(); i++) {
            gameUsers.add(users.valueAt(i));
        }
        Gson gson = new Gson();
        ReqData reqData = new ReqData();
        String saveJson = gson.toJson(param, SaveRecordParam.class);
        reqData.setParam(saveJson);
        reqData.setReqfunc("reqSaveRecord");
        reqData.setReqid(userId);
        reqData.setReqno(TimeUtil.getCurrentDateToMinutes(new Date())+disposeNumber());
        String data = gson.toJson(reqData, ReqData.class);
        FileIOUtil.saveToFile(data);
        new SaveDataUtil().disposeHandLog(data);
        //传输数据到服务器
        HttpUtil.sendPostRequestData("savehand", data, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.e(TAG,"发送失败"+e.toString());
            }
            @Override
            public void onResponse(Call call, Response response) throws IOException {
                Log.e(TAG,"response:"+response.toString());
                Log.e(TAG,"response:"+response.body().string());
            }
        });
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
    public static boolean isSTLSeat(GameUser gameUser) {
        if (gameUser == null) {
            return false;
        }
        //在三个位置，并且前面所有动作都是fold
        if ("CO".equals(gameUser.getSeatFlag()) || "BTN".equals(gameUser.getSeatFlag()) || "SB".equals(gameUser.getSeatFlag())) {
            return true;
        }
        return false;
    }

    /*前面动作的人都是fold牌*/
    public static boolean lastActionsAllFold(List<GameAction> actions,int index){
        for (int i = 0; i < index; i++) {
            GameAction action = actions.get(i);
             if(action.getAction()!=Constant.ACTION_FOLD){
                 return false;
             }
        }
        return true;
    }
    /*
    * index：当前动作的角标
    * */
    public static void curPlayerIfFaceFold(SparseArray<GameUser> users,GameUser gameUser,List<GameAction> actions,int index,int btnIdx){
        if(users.size()>3){
            if(isSTLSeat(gameUser)){//判定偷盲机会
                if(lastActionsAllFold(actions,index)){
                    gameUser.setStlPosition(true);
                }
            }
            //判断是否有被偷盲机会
            GameUser stlUser = haveStealBlinds(users);
            if(stlUser !=null){//有人偷盲
                if ("SB".equals(gameUser.getSeatFlag())){
                    if("CO".equals(stlUser.getSeatFlag())){
                        if(users.get(btnIdx).getFoldRound()==0){//btn位fold
                            gameUser.setFaceSTL(true);
                        }
                    }else if("BTN".equals(stlUser.getSeatFlag())){
                        gameUser.setFaceSTL(true);
                    }
                }else if("BB".equals(gameUser.getSeatFlag())){
                    if("CO".equals(stlUser.getSeatFlag())){
                        boolean sbFold = false;
                        boolean btnFold = false;
                        for (int j = 0; j < users.size(); j++) {
                            GameUser user = users.valueAt(j);
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
                        for (int j = 0; j < users.size(); j++) {
                            GameUser user = users.valueAt(j);
                            if("SB".equals(user.getSeatFlag()) && user.getFoldRound()==0){
                                gameUser.setFaceSTL(true);
                            }
                        }
                    }else if("SB".equals(stlUser.getSeatFlag())){
                        gameUser.setFaceSTL(true);
                    }
                }
            }
        }else if(users.size()==3){
            if(isSTLSeat(gameUser)){
                if("BTN".equals(gameUser.getSeatFlag())){
                    gameUser.setStlPosition(true);
                }else if("SB".equals(gameUser.getSeatFlag())){
                    GameUser btnUser = users.get(btnIdx);
                    if(btnUser != null && btnUser.getFoldRound()==0){
                        gameUser.setStlPosition(true);
                    }
                }
            }
            GameUser user = haveStealBlinds(users);
            if(user!=null){
                if("SB".equals(gameUser.getSeatFlag())){
                    if("BTN".equals(user.getSeatFlag())){ //偷盲位是btn
                        gameUser.setFaceSTL(true);
                    }
                }else if("BB".equals(gameUser.getSeatFlag())){
                    if("BTN".equals(user.getSeatFlag())){ //偷盲位是btn
                        for (int j = 0; j < users.size(); j++) {
                            GameUser user1 = users.valueAt(j);
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
    public static GameUser haveStealBlinds(SparseArray<GameUser> users){//获取偷盲玩家
        for (int i = 0; i < users.size(); i++) {
            GameUser user = users.valueAt(i);
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

}
