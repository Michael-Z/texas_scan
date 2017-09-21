package com.ruilonglai.texas_scan.entity;

public class SerialInfo {

	String serialno;	//序列号
	int validdays;		//有效期
	String regday;		//注册日
	int remaindays;		//剩余有效期
	
	public String getSerialno() {
		return serialno;
	}
	public void setSerialno(String serialno) {
		this.serialno = serialno;
	}
	public int getValiddays() {
		return validdays;
	}
	public void setValiddays(int validdays) {
		this.validdays = validdays;
	}
	public int getRemaindays() {
		return remaindays;
	}
	public void setRemaindays(int remaindays) {
		this.remaindays = remaindays;
	}
	public String getRegday() {
		return regday;
	}
	public void setRegday(String regday) {
		this.regday = regday;
	}

	
}
