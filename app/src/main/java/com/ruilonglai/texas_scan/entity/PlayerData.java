package com.ruilonglai.texas_scan.entity;

import org.litepal.crud.DataSupport;

/**
 * Created by wangshuai on 2017/4/22.
 */

public class PlayerData extends DataSupport {

    private String name="";
    private int loseCount = 0;
    private int playCount = 0;
    private int winCount = 0;
    private int money = 0;
    private double bbCount = 0;
    private String date;
    private String seatFlag="";
    private int joinCount;
    private int bet3Count;
    private int foldCount;
    private int callCount;
    private int raiseCount;
    private int stlCount;
    private int foldStlCount;
    private int pfrCount;
    private  int faceOpenCount;
    private int face3BetCount;
    private int fold3BetCount;
    private int lastRaiseCount;
    private int cbCount;
    private int stlPosCount;
    private int faceStlCount;

    public String getSeatFlag() {
        return seatFlag;
    }

    public void setSeatFlag(String seatFlag) {
        this.seatFlag = seatFlag;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
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

    public int getWinCount() {
        return winCount;
    }

    public void setWinCount(int winCount) {
        this.winCount = winCount;
    }

    public int getMoney() {
        return money;
    }

    public void setMoney(int money) {
        this.money = money;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public double getBbCount() {
        return bbCount;
    }

    public void setBbCount(double bbCount) {
        this.bbCount = bbCount;
    }

    public int getBet3Count() {
        return bet3Count;
    }

    public void setBet3Count(int bet3Count) {
        this.bet3Count = bet3Count;
    }

    public int getJoinCount() {
        return joinCount;
    }

    public void setJoinCount(int joinCount) {
        this.joinCount = joinCount;
    }

    public int getFoldCount() {
        return foldCount;
    }

    public void setFoldCount(int foldCount) {
        this.foldCount = foldCount;
    }

    public int getCallCount() {
        return callCount;
    }

    public void setCallCount(int callCount) {
        this.callCount = callCount;
    }

    public int getStlCount() {
        return stlCount;
    }

    public void setStlCount(int stlCount) {
        this.stlCount = stlCount;
    }

    public int getFoldStlCount() {
        return foldStlCount;
    }

    public void setFoldStlCount(int foldStlCount) {
        this.foldStlCount = foldStlCount;
    }

    public int getPfrCount() {
        return pfrCount;
    }

    public void setPfrCount(int pfrCount) {
        this.pfrCount = pfrCount;
    }

    public int getRaiseCount() {
        return raiseCount;
    }

    public void setRaiseCount(int raiseCount) {
        this.raiseCount = raiseCount;
    }

    public int getFace3BetCount() {
        return face3BetCount;
    }

    public void setFace3BetCount(int face3BetCount) {
        this.face3BetCount = face3BetCount;
    }

    public int getFold3BetCount() {
        return fold3BetCount;
    }

    public void setFold3BetCount(int fold3BetCount) {
        this.fold3BetCount = fold3BetCount;
    }

    public int getLastRaiseCount() {
        return lastRaiseCount;
    }

    public void setLastRaiseCount(int lastRaiseCount) {
        this.lastRaiseCount = lastRaiseCount;
    }

    public int getCbCount() {
        return cbCount;
    }

    public void setCbCount(int cbCount) {
        this.cbCount = cbCount;
    }

    public int getFaceOpenCount() {
        return faceOpenCount;
    }

    public void setFaceOpenCount(int faceOpenCount) {
        this.faceOpenCount = faceOpenCount;
    }

    public int getStlPosCount() {
        return stlPosCount;
    }

    public void setStlPosCount(int stlPosCount) {
        this.stlPosCount = stlPosCount;
    }

    public int getFaceStlCount() {
        return faceStlCount;
    }

    public void setFaceStlCount(int faceStlCount) {
        this.faceStlCount = faceStlCount;
    }
}