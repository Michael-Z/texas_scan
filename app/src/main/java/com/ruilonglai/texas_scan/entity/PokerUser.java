package com.ruilonglai.texas_scan.entity;


import java.util.ArrayList;
import java.util.List;

public class PokerUser {

	public String id;
	public String nick;
	public String passwd;
	public int license;
	public String serialno;
	public int logined;
	public String curmachine;
	public String sendtime;
	public List<String> machines=new ArrayList<String>();
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	public String getNick() {
		return nick;
	}
	public void setNick(String nick) {
		this.nick = nick;
	}
	public String getPasswd() {
		return passwd;
	}
	public void setPasswd(String passwd) {
		this.passwd = passwd;
	}
	public int getLicense() {
		return license;
	}
	public void setLicense(int license) {
		this.license = license;
	}
	public List<String> getMachines() {
		return machines;
	}
	public void setMachines(List<String> machines) {
		this.machines = machines;
	}
	public int getLogined() {
		return logined;
	}
	public void setLogined(int logined) {
		this.logined = logined;
	}
	public String getCurmachine() {
		return curmachine;
	}
	public void setCurmachine(String curmachine) {
		this.curmachine = curmachine;
	}
	public String getSerialno() {
		return serialno;
	}
	public void setSerialno(String serialno) {
		this.serialno = serialno;
	}

	public String getSendtime() {
		return sendtime;
	}

	public void setSendtime(String sendtime) {
		this.sendtime = sendtime;
	}
}
