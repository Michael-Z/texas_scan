package com.ruilonglai.texas_scan.entity;

/**
 * Created by Administrator on 2017/11/21.
 */

public class PlayerPoker {

    String name;
    String date;//例如：20171121
    int card1;
    int card2;
    int[] boards = new int[]{-1,-1,-1,-1,-1};//5张公共牌
    boolean isWin;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
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

    public int[] getBoards() {
        return boards;
    }

    public void setBoards(int[] boards) {
        this.boards = boards;
    }

    public boolean isWin() {
        return isWin;
    }

    public void setWin(boolean win) {
        isWin = win;
    }
}
