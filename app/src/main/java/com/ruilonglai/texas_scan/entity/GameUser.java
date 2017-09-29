package com.ruilonglai.texas_scan.entity;

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
    private boolean isFaceSTL; //是否面临偷盲
    private boolean isStlPosition; //是否是偷盲位
    private int callCount;   //跟注次数
    private int raiseCount;  //加注次数
    private boolean isPreFlopLastRaise;  //翻牌前最后一个加注
    private boolean isCB;  //是否是翻牌圈再加注

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
}
