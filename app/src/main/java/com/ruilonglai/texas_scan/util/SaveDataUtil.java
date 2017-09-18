package com.ruilonglai.texas_scan.util;

import android.text.TextUtils;

import com.google.gson.Gson;
import com.ruilonglai.texas_scan.entity.GameUser;
import com.ruilonglai.texas_scan.entity.MyData;
import com.ruilonglai.texas_scan.entity.PlayerData;
import com.ruilonglai.texas_scan.entity.PokerRecord;
import com.ruilonglai.texas_scan.entity.ReqData;
import com.ruilonglai.texas_scan.entity.SaveRecordParam;
import com.ruilonglai.texas_scan.entity.TableRecord;

import org.litepal.crud.DataSupport;

import java.util.Date;
import java.util.List;

/**
 * Created by wgl on 2017/4/22.
 */

public class SaveDataUtil {
    private PokerRecord pokerRecord;
    private List<GameUser> users;
    private TableRecord tableRecord;

    public void disposeHandLog(String json){
        if(TextUtils.isEmpty(json)){
            return;
        }
        ReqData reqData = GsonUtil.parseJsonWithGson(json, ReqData.class);
        String saveJson = reqData.getParam();
        Gson gson = new Gson();
        SaveRecordParam param = gson.fromJson(saveJson, SaveRecordParam.class);
        pokerRecord = param.getPokerRecord();
        tableRecord = param.getTableRecord();
        users = pokerRecord.getUsers();
        int[] poker = pokerRecord.getPoker();
        //保存个人手牌记录
        if(tableRecord.getBlindType()==0 || users.size()==0){
            return;
        }
        GameUser user = getSelfGameUser(users);
        double bbCount = 0;
        if(user != null){
            if(tableRecord.getBlindType()!=0){
                bbCount = (user.getEndMoney() - user.getBeginMoney()) / Double.valueOf(tableRecord.getBlindType());
            }
            if(user.isJoin()){//记录入局的手牌
                if(user.getUserName().contains("self") && poker[0] != -1){
                    String pokerName = CardUtil.getPokerName(poker[0], poker[1]);
                    List<MyData> datas = DataSupport.where("pokername=? and date=?", pokerName,TimeUtil.getCurrentDateToDay(new Date())).find(MyData.class);
                    if(datas.size()>0){
                        MyData dbData = datas.get(0);
                        dbData.setPlayCount(dbData.getPlayCount()+1);
                        dbData.setBbCount(bbCount +dbData.getBbCount());
                        if(bbCount>dbData.getMaxBbCount()){
                            dbData.setMaxBbCount(bbCount);
                        }else if(bbCount<dbData.getMinBbCount()){
                            dbData.setMinBbCount(bbCount);
                        }
                        dbData.updateAll("pokerName=? and date=?",pokerName,TimeUtil.getCurrentDateToDay(new Date()));
                    }else{
                        MyData myData = new MyData();
                        myData.setCard0(poker[0]);
                        myData.setCard1(poker[1]);
                        myData.setPokerName(pokerName);
                        myData.setBbCount(bbCount);
                        myData.setPlayCount(1);
                        if(bbCount<0){
                            myData.setMinBbCount(bbCount);
                            myData.setMaxBbCount(0);
                        }else {
                            myData.setMinBbCount(0);
                            myData.setMaxBbCount(bbCount);
                        }
                        myData.setMoney(user.getEndMoney()-user.getBeginMoney());
                        myData.setDate(TimeUtil.getCurrentDateToDay(new Date()));
                        myData.save();
                    }
                }
            }
            //保存个人位置盈利
            String seatFlag = user.getSeatFlag();
            if(!TextUtils.isEmpty(seatFlag)){
                List<PlayerData> playerDatas = DataSupport.where("seatflag=? and name=?", seatFlag, "_self").find(PlayerData.class);
                PlayerData playerData = null;
                if(playerDatas.size()>0){
                    playerData = playerDatas.get(0);
                }else{
                    playerData = new PlayerData();
                }
                playerData.setName("_self");
                playerData.setSeatFlag(seatFlag);
                playerData.setBbCount(playerData.getBbCount()+bbCount);
                playerData.setPlayCount(playerData.getPlayCount()+1);
                playerData.setDate(TimeUtil.getCurrentDateToDay(new Date()));
                if(user.getBeginMoney()<user.getEndMoney()){
                    playerData.setWinCount(playerData.getWinCount()+1);
                }else if(user.getBeginMoney()>user.getEndMoney()){
                    playerData.setLoseCount(playerData.getLoseCount()+1);
                }
                if(user!=null){
                    if(user.getFoldRound()>-1){
                        playerData.setFoldCount(playerData.getFoldCount()+1);
                    }
                    if(user.isJoin()){
                        playerData.setJoinCount(playerData.getJoinCount()+1);
                    }
                    if(user.is3Bet()){
                        playerData.setBet3Count(playerData.getBet3Count()+1);
                    }
                    if(user.isPFR()){
                        playerData.setPfrCount(playerData.getPfrCount()+1);
                    }
                    if(user.isSTL()){
                        playerData.setStlCount(playerData.getStlCount()+1);
                    }
                    if(user.isFoldSTL()){
                        playerData.setFoldStlCount(playerData.getFoldStlCount()+1);
                    }
                    if(user.isFaceOpen()){
                        playerData.setFaceOpenCount(playerData.getFaceOpenCount()+1);
                    }
                    if(user.getCallCount()>0){
                        playerData.setCallCount(playerData.getCallCount()+user.getCallCount());
                    }
                    if(user.getRaiseCount()>0){
                        playerData.setRaiseCount(playerData.getRaiseCount()+user.getRaiseCount());
                    }
                    if(user.isFace3Bet()){
                        playerData.setFace3BetCount(playerData.getFace3BetCount()+1);
                        if(user.getFoldRound()==0){
                            playerData.setFold3BetCount(playerData.getFold3BetCount()+1);
                        }
                    }
                    if(user.isPreFlopLastRaise()){
                        playerData.setLastRaiseCount(playerData.getLastRaiseCount()+1);
                    }
                    if(user.isCB()){
                        playerData.setCbCount(playerData.getCbCount()+1);
                    }
                    if(user.isFaceSTL()){
                        playerData.setFaceStlCount(playerData.getFaceStlCount()+1);
                    }
                    if(user.isStlPosition()){
                        playerData.setStlPosCount(playerData.getStlPosCount()+1);
                    }
                    if(playerDatas.size()>0){
                        playerData.updateAll("seatflag=? and name=?", seatFlag, "_self");
                    }else{
                        playerData.save();
                    }
                }
            }
        }
        /*保存玩家数据*/
        for (int i = 0; i < users.size(); i++) {
            GameUser gamer = users.get(i);
            if("self".equals(gamer.getUserName())){
                continue;
            }
            int gamerBeginMoney = gamer.getBeginMoney();
            int gamerEndMoney = gamer.getEndMoney();
            double otherBBCount = (gamerEndMoney-gamerBeginMoney)/Double.valueOf(tableRecord.getBlindType());
            String name = gamer.getUserName();
            if(!TextUtils.isEmpty(name)){
                List<PlayerData> datas = DataSupport.where("name=?", name).find(PlayerData.class);
                PlayerData playerData = null;
                if(datas.size()>0){
                    playerData = datas.get(0);
                }else{
                    playerData = new PlayerData();
                }
                playerData.setName(name);
                playerData.setSeatFlag(gamer.getSeatFlag());
                playerData.setDate(TimeUtil.getCurrentDateToDay(new Date()));
                playerData.setPlayCount(playerData.getPlayCount()+1);
                playerData.setBbCount(playerData.getBbCount()+otherBBCount);
                if(gamerBeginMoney>gamerEndMoney){
                    playerData.setLoseCount(playerData.getLoseCount()+1);
                }
                if(gamerBeginMoney<gamerEndMoney){
                    playerData.setWinCount(playerData.getWinCount()+1);
                }
                if(gamer.getFoldRound()>-1){
                    playerData.setFoldCount(playerData.getFoldCount()+1);
                }
                if(gamer.isJoin()){
                    playerData.setJoinCount(playerData.getJoinCount()+1);
                }
                if(gamer.is3Bet()){
                    playerData.setBet3Count(playerData.getBet3Count()+1);
                }
                if(gamer.isPFR()){
                    playerData.setPfrCount(playerData.getPfrCount()+1);
                }
                if(gamer.isSTL()){
                    playerData.setStlCount(playerData.getStlCount()+1);
                }
                if(gamer.isFoldSTL()){
                    playerData.setFoldStlCount(playerData.getFoldStlCount()+1);
                }
                if(gamer.isFaceOpen()){
                    playerData.setFaceOpenCount(playerData.getFaceOpenCount()+1);
                }
                if(gamer.getCallCount()>0){
                    playerData.setCallCount(playerData.getCallCount()+gamer.getCallCount());
                }
                if(gamer.getRaiseCount()>0){
                    playerData.setRaiseCount(playerData.getRaiseCount()+gamer.getRaiseCount());
                }
                if(gamer.isFace3Bet()){
                    playerData.setFace3BetCount(playerData.getFace3BetCount()+1);
                    if(gamer.getFoldRound()==0){
                        playerData.setFold3BetCount(playerData.getFold3BetCount()+1);
                    }
                }
                if(gamer.isPreFlopLastRaise()){
                    playerData.setLastRaiseCount(playerData.getLastRaiseCount()+1);
                }
                if(gamer.isCB()){
                    playerData.setCbCount(playerData.getCbCount()+1);
                }
                if(gamer.isFaceSTL()){
                    playerData.setFaceStlCount(playerData.getFaceStlCount()+1);
                }
                if(gamer.isStlPosition()){
                    playerData.setStlPosCount(playerData.getStlPosCount()+1);
                }
                if(datas.size()>0){
                    playerData.updateAll("name=?",name);
                }else{
                    playerData.save();
                }
            }
        }
        FileIOUtil.saveToFile("保存数据成功!");
    }
    public GameUser getSelfGameUser(List<GameUser> users){
        if(users==null){
            return null;
        }
        for (int i = 0; i < users.size(); i++) {
            GameUser user = users.get(i);
            if(user==null)
                return null;
            String userName = user.getUserName();
            if(userName!= null && userName.contains("self")){
                return user;
            }
        }
        return null;
    }
}
