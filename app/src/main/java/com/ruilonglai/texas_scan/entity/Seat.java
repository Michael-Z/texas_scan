package com.ruilonglai.texas_scan.entity;

/**
 * Created by Administrator on 2017/8/10.
 */

public class Seat {
    int empty;
    int money;
    int bet;
    int hidecard;
    int card1;
    int card2;
    int action;

    public int getEmpty() {
        return empty;
    }

    public void setEmpty(int empty) {
        this.empty = empty;
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

    public int getHidecard() {
        return hidecard;
    }

    public void setHidecard(int hidecard) {
        this.hidecard = hidecard;
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

    public int getAction() {
        return action;
    }

    public void setAction(int action) {
        this.action = action;
    }

    @Override
    public String toString() {
        return "Seat{" +
                "empty=" + empty +
                ", money=" + money +
                ", bet=" + bet +
                ", hidecard=" + hidecard +
                ", card1=" + card1 +
                ", card2=" + card2 +
                ", action=" + action +
                '}';
    }
}
