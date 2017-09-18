package com.ruilonglai.texas_scan.entity;

import org.litepal.crud.DataSupport;

/**
 * Created by WangShuai on 2017/5/3.
 */

public class OneHand extends DataSupport{
    private String name;
    private double money;
    private int isWin;
    private int poker1;
    private int poker2;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public double getMoney() {
        return money;
    }

    public void setMoney(double money) {
        this.money = money;
    }

    public int getIsWin() {
        return isWin;
    }

    public void setIsWin(int isWin) {
        this.isWin = isWin;
    }

    public void setPoker2(int poker2) {
        this.poker2 = poker2;
    }

    public int getPoker1() {
        return poker1;
    }

    public void setPoker1(int poker1) {
        this.poker1 = poker1;
    }

    public int getPoker2() {
        return poker2;
    }

}
