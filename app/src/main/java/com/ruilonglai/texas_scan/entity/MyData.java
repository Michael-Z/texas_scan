package com.ruilonglai.texas_scan.entity;

import org.litepal.crud.DataSupport;

/**
 * Created by wangshuai on 2017/5/4.
 */

public class MyData extends DataSupport {
    private String date;
    private int money;
    private double bbCount;
    private int card0;
    private int card1;
    private String pokerName;
    private int playCount;
    private double minBbCount;
    private double maxBbCount;

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public int getMoney() {
        return money;
    }

    public void setMoney(int money) {
        this.money = money;
    }

    public int getCard0() {
        return card0;
    }

    public void setCard0(int card0) {
        this.card0 = card0;
    }

    public int getCard1() {
        return card1;
    }

    public void setCard1(int card1) {
        this.card1 = card1;
    }

    public String getPokerName() {
        return pokerName;
    }

    public void setPokerName(String pokerName) {
        this.pokerName = pokerName;
    }

    public int getPlayCount() {
        return playCount;
    }

    public void setPlayCount(int playCount) {
        this.playCount = playCount;
    }

    public double getBbCount() {
        return bbCount;
    }

    public void setBbCount(double bbCount) {
        this.bbCount = bbCount;
    }

    public double getMinBbCount() {
        return minBbCount;
    }

    public void setMinBbCount(double minBbCount) {
        this.minBbCount = minBbCount;
    }

    public double getMaxBbCount() {
        return maxBbCount;
    }

    public void setMaxBbCount(double maxBbCount) {
        this.maxBbCount = maxBbCount;
    }
}
