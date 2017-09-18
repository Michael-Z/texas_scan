package com.ruilonglai.texas_scan.entity;

/**
 * Created by Administrator on 2017/5/27.
 */

public class TableRecord {
    String tableno;		//牌桌流水号,id+date+seqno  ;全局唯一，tableno为0001计数器
    long timestamp;	//时间戳
    int  pokerType;	//牌局类型
    int  blindType = 0;	//盲注类型：0：1/2，1:2/4,2:5/10,3:10/20,4:20/50,5:50/100…
    int  straddle;		//强抖设置大小：
    int  ante;			//anti设置大小：
    int  maxPlayCount;		//几人桌
    public String getTableno() {
        return tableno;
    }

    public void setTableno(String tableno) {
        this.tableno = tableno;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public int getPokerType() {
        return pokerType;
    }

    public void setPokerType(int pokerType) {
        this.pokerType = pokerType;
    }

    public int getBlindType() {
        return blindType;
    }

    public void setBlindType(int blindType) {
        this.blindType = blindType;
    }

    public int getStraddle() {
        return straddle;
    }

    public void setStraddle(int straddle) {
        this.straddle = straddle;
    }

    public int getAnte() {
        return ante;
    }

    public void setAnte(int ante) {
        this.ante = ante;
    }

    public int getMaxPlayCount() {
        return maxPlayCount;
    }

    public void setMaxPlayCount(int maxPlayCount) {
        this.maxPlayCount = maxPlayCount;
    }
}
