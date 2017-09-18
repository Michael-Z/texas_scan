package com.ruilonglai.texas_scan.entity;

import java.util.List;

/**
 * Created by Administrator on 2017/5/27.
 */

public class PokerRecord {
    int[] poker = {-1,-1};       //两张自己的手牌    整除0-黑桃 1-红心 2-梅花 3-方块    取余0~13 –>2~A
    String  pokerno;	  //手牌流水号,tableno+seqno；全局唯一，pokerno为0001计数器
    List<GameUser> users;	  //牌手，当前玩家存放在0
    int  button=-1;	 	  //按钮位位置,0~8
    int[] flop = {-1,-1,-1};        //翻牌
    int	turn=-1;           //转牌
    int river=-1;          //河牌
    int totalPool;	    //总底池
    String  winner;		//获胜玩家
    List<GameAction> actions;	//操作历史

    public int[] getPoker() {
        return poker;
    }

    public void setPoker(int[] poker) {
        this.poker = poker;
    }

    public String getPokerno() {
        return pokerno;
    }

    public void setPokerno(String pokerno) {
        this.pokerno = pokerno;
    }

    public List<GameUser> getUsers() {
        return users;
    }

    public void setUsers(List<GameUser> users) {
        this.users = users;
    }

    public int getButton() {
        return button;
    }

    public void setButton(int button) {
        this.button = button;
    }

    public int[] getFlop() {
        return flop;
    }

    public void setFlop(int[] flop) {
        this.flop = flop;
    }

    public int getTurn() {
        return turn;
    }

    public void setTurn(int turn) {
        this.turn = turn;
    }

    public int getRiver() {
        return river;
    }

    public void setRiver(int river) {
        this.river = river;
    }

    public int getTotalPool() {
        return totalPool;
    }

    public void setTotalPool(int totalPool) {
        this.totalPool = totalPool;
    }

    public String getWinner() {
        return winner;
    }

    public void setWinner(String winner) {
        this.winner = winner;
    }

    public List<GameAction> getActions() {
        return actions;
    }

    public void setActions(List<GameAction> actions) {
        this.actions = actions;
    }
}
