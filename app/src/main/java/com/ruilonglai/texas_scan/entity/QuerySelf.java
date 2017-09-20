package com.ruilonglai.texas_scan.entity;

import java.util.List;

public class QuerySelf {

	final String[] allseat={"BTN","SB","BB","UTG","UTG+1","MP","MP+1","HJ","CO"};
	String userid;
	String seat;

	public String getUserid() {
		return userid;
	}
	public void setUserid(String userid) {
		this.userid = userid;
	}
	public String getSeat() {
		return seat;
	}
	public void setSeat(String seat) {
		this.seat = seat;
	}
}
