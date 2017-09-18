package com.ruilonglai.texas_scan.entity;

/**
 * Created by wgl on 2017/4/13.
 */

public class Player{
    private int winCount = 0;
    private int loseCount = 0;
    private int playCount = 0;
    private double bbCount = 0;
    private int seatIdx = -1;
    private int money = 0;
    private int bet = 0;
    public boolean isLive = true;
    private String seatFlag = "";
    private String curAction = "";
    private String name = "";

    public int getWinCount() {
        return winCount;
    }

    public void setWinCount(int winCount) {
        this.winCount = winCount;
    }

    public int getLoseCount() {
        return loseCount;
    }

    public void setLoseCount(int loseCount) {
        this.loseCount = loseCount;
    }

    public int getPlayCount() {
        return playCount;
    }

    public void setPlayCount(int playCount) {
        this.playCount = playCount;
    }

    public int getSeatIdx() {
        return seatIdx;
    }

    public void setSeatIdx(int seatIdx) {
        this.seatIdx = seatIdx;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getMoney() {
        return money;
    }

    public void setMoney(int money) {
        this.money = money;
    }

    public int getBet() {
        return bet;
    }

    public void setBet(int bet) {
        this.bet = bet;
    }

    public String getCurAction() {
        return curAction;
    }

    public void setCurAction(String curAction) {
        this.curAction = curAction;
    }

    public String getSeatFlag() {
        return seatFlag;
    }

    public void setSeatFlag(String seatFlag) {
        this.seatFlag = seatFlag;
    }

    public double getBbCount() {
        return bbCount;
    }

    public void setBbCount(double bbCount) {
        this.bbCount = bbCount;
    }
}