package com.ruilonglai.texas_scan.entity;

import com.ruilonglai.texas_scan.util.Constant;

/**
 * Created by Administrator on 2017/5/27.
 */

public class GameUser {
    private int seatIdx;
    private int card1 = -1;
    private int card2 = -1;
    private String seatFlag;    //玩家位置  如BTN、BB，CO等
    private String userName;      //玩家名字
    private int beginMoney;    //起始筹码  盲注为单位
    private int endMoney;     //结束筹码
    private boolean is3Bet;   //再加注
    private boolean isJoin;    //是否入局
    private int foldRound=-1;   //弃牌圈
    private boolean isSTL;   //是否偷盲
    private boolean isFoldSTL;  //被偷盲
    private boolean isPFR;   //翻牌前加注
    private boolean isFaceOpen; //是否面临一次加注
    private boolean isFace3Bet; //是否面临3bet
    private boolean isFold3Bet; //fold3bet
    private boolean isFaceSTL; //是否面临偷盲
    private boolean isStlPosition; //是否是偷盲位
    private int callCount;   //跟注次数
    private int raiseCount;  //加注次数
    private boolean isPreFlopLastRaise;  //翻牌前最后一个加注
    private boolean isCB;  //是否是翻牌圈再加注
    private boolean isFoldCB; //面对翻牌再加注弃牌
    private boolean isFaceCB; //是否面对翻牌再加注
    private int lastActionRound;//最后一个动作的圈数
    private boolean isTurn; //是否摊牌
    private boolean isWinTurn; //摊牌获胜


    public int getSeatIdx() {
        return seatIdx;
    }

    public void setSeatIdx(int seatIdx) {
        this.seatIdx = seatIdx;
    }

    public int getCard1() {
        return card1;
    }

    public void setCard1(int card1) {
        this.card1 = card1;
    }

    public int getCard2() {
        return card2;
    }

    public void setCard2(int card2) {
        this.card2 = card2;
    }

    public boolean is3Bet() {
        return is3Bet;
    }

    public void setIs3Bet(boolean is3Bet) {
        this.is3Bet = is3Bet;
    }

    public boolean isJoin() {
        return isJoin;
    }

    public void setJoin(boolean join) {
        isJoin = join;
    }

    public int getFoldRound() {
        return foldRound;
    }

    public void setFoldRound(int foldRound) {
        this.foldRound = foldRound;
    }

    public boolean isSTL() {
        return isSTL;
    }

    public void setSTL(boolean STL) {
        isSTL = STL;
    }

    public boolean isFoldSTL() {
        return isFoldSTL;
    }

    public void setFoldSTL(boolean foldSTL) {
        isFoldSTL = foldSTL;
    }

    public String getSeatFlag() {
        return seatFlag;
    }

    public void setSeatFlag(String seatFlag) {
        this.seatFlag = seatFlag;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public int getBeginMoney() {
        return beginMoney;
    }

    public void setBeginMoney(int beginMoney) {
        this.beginMoney = beginMoney;
    }

    public int getEndMoney() {
        return endMoney;
    }

    public void setEndMoney(int endMoney) {
        this.endMoney = endMoney;
    }

    public boolean isPFR() {
        return isPFR;
    }

    public void setPFR(boolean PFR) {
        isPFR = PFR;
    }

    public boolean isFaceOpen() {
        return isFaceOpen;
    }

    public void setFaceOpen(boolean faceOpen) {
        isFaceOpen = faceOpen;
    }

    public boolean isFace3Bet() {
        return isFace3Bet;
    }

    public void setFace3Bet(boolean face3Bet) {
        isFace3Bet = face3Bet;
    }

    public int getCallCount() {
        return callCount;
    }

    public void setCallCount(int callCount) {
        this.callCount = callCount;
    }

    public int getRaiseCount() {
        return raiseCount;
    }

    public void setRaiseCount(int raiseCount) {
        this.raiseCount = raiseCount;
    }

    public boolean isPreFlopLastRaise() {
        return isPreFlopLastRaise;
    }

    public void setPreFlopLastRaise(boolean preFlopLastRaise) {
        isPreFlopLastRaise = preFlopLastRaise;
    }

    public boolean isCB() {
        return isCB;
    }

    public void setCB(boolean CB) {
        isCB = CB;
    }

    public boolean isFaceSTL() {
        return isFaceSTL;
    }

    public void setFaceSTL(boolean faceSTL) {
        isFaceSTL = faceSTL;
    }

    public boolean isStlPosition() {
        return isStlPosition;
    }

    public void setStlPosition(boolean stlPosition) {
        isStlPosition = stlPosition;
    }

    public boolean isFoldCB() {
        return isFoldCB;
    }

    public void setFoldCB(boolean foldCB) {
        isFoldCB = foldCB;
    }

    public boolean isFaceCB() {
        return isFaceCB;
    }

    public void setFaceCB(boolean faceCB) {
        isFaceCB = faceCB;
    }

    public int getLastActionRound() {
        return lastActionRound;
    }

    public void setLastActionRound(int lastActionRound) {
        this.lastActionRound = lastActionRound;
    }

    public boolean isFold3Bet() {
        return isFold3Bet;
    }

    public void setFold3Bet(boolean fold3Bet) {
        isFold3Bet = fold3Bet;
    }

    public boolean isTurn() {
        return isTurn;
    }

    public void setTurn(boolean turn) {
        isTurn = turn;
    }

    public boolean isWinTurn() {
        return isWinTurn;
    }

    public void setWinTurn(boolean winTurn) {
        isWinTurn = winTurn;
    }

    @Override
    public String toString() {
        return "GameUser{" +
                "seatIdx=" + seatIdx +
                ", seatFlag='" + seatFlag + '\'' +
                '}';
    }
    public String toString(boolean istrue) {
        StringBuilder sb = new StringBuilder();

        sb.append("[名字=").append(userName).append("]\n");
        sb.append("[changeMoney=").append(endMoney-beginMoney).append("]\n");
        sb.append("[").append(seatFlag).append("]\n");
        if(isJoin)
            sb.append("[").append("入池").append("]\n");
        if(isPFR)
            sb.append("[").append("翻牌前加注").append("]\n");
        if(isFaceOpen)
            sb.append("[").append("3Bet机会").append("]\n");
        if(is3Bet)
            sb.append("[").append("3Bet").append("]\n");
        if(isFace3Bet)
            sb.append("[").append("面对3bet").append("]\n");
        if(isFold3Bet)
            sb.append("[").append("面对3bet弃牌").append("]\n");
        if(isStlPosition)
            sb.append("[").append("偷盲机会").append("]\n");
        if(isSTL)
            sb.append("[").append("偷盲").append("]\n");
       if(isFaceSTL)
            sb.append("[").append("面对偷盲").append("]\n");
        if(isFoldSTL)
            sb.append("[").append("fold偷盲").append("]\n");
        if(isPreFlopLastRaise)
            sb.append("[").append("cb机会").append("]\n");
        if(isCB)
            sb.append("[").append("CB").append("]\n");
        if(isFaceCB)
            sb.append("[").append("面对CB").append("]\n");
        if(isFoldCB)
            sb.append("[").append("面对CB弃牌").append("]\n");
        if(isTurn)
            sb.append("[").append("翻牌").append("]\n");
        if(isWinTurn)
            sb.append("[").append("翻牌获胜").append("]\n");
        if(raiseCount>0){
            sb.append("[").append("翻牌后加注次数:"+raiseCount).append("]\n");
        }
        if(callCount>0){
            sb.append("[").append("翻牌后跟注次数:"+callCount).append("]\n");
        }
        sb.append("[").append("记录最后动作在 "+Constant.ROUND[lastActionRound]).append("]\n");
        if(foldRound>-1)
        sb.append("[").append("弃牌在 "+Constant.ROUND[foldRound]).append("]");
        else
        sb.append("[").append("弃牌没有记录").append("]");
        return sb.toString();
    }
}
