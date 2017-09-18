package com.ruilonglai.texas_scan.entity;

/**
 * Created by Administrator on 2017/5/27.
 */

public class GameAction {
    String userName;	//用户id
    int seatIdx;
    String  seatFlag;
    int  round;		//当前轮次 0-preflop 3-flop 4-turn 5-river
    int	 bet;	//下注大小
    int addMoney = -1;
    int  action = -1;     //动作类型,0-check,1-call,2-bet,3-fold,4-allin

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public int getSeatIdx() {
        return seatIdx;
    }

    public void setSeatIdx(int seatIdx) {
        this.seatIdx = seatIdx;
    }

    public String getSeatFlag() {
        return seatFlag;
    }

    public void setSeatFlag(String seatFlag) {
        this.seatFlag = seatFlag;
    }

    public int getRound() {
        return round;
    }

    public void setRound(int round) {
        this.round = round;
    }

    public int getBet() {
        return bet;
    }

    public void setBet(int bet) {
        this.bet = bet;
    }

    public int getAction() {
        return action;
    }

    public void setAction(int action) {
        this.action = action;
    }

    public int getAddMoney() {
        return addMoney;
    }

    public void setAddMoney(int addMoney) {
        this.addMoney = addMoney;
    }

    @Override
    public String toString() {
        return "GameAction{" +
                "userName='" + userName + '\'' +
                ", seatIdx=" + seatIdx +
                ", seatFlag='" + seatFlag + '\'' +
                ", round=" + round +
                ", bet=" + bet +
                ", addMoney=" + addMoney +
                ", action=" + action +
                '}';
    }
}
