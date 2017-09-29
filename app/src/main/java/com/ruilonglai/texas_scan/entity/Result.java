package com.ruilonglai.texas_scan.entity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class Result {
	public boolean suc;
	public String code;
	public String msg;
	public HashMap<String,String> rets=new HashMap<String, String>();
	public List<String> listuser;

	public boolean isSuc() {
		return suc;
	}
	public void setSuc(boolean suc) {
		this.suc = suc;
	}
	public String getMsg() {
		return msg;
	}
	public void setMsg(String msg) {
		this.msg = msg;
	}
	public String getCode() {
		return code;
	}
	public void setCode(String code) {
		this.code = code;
	}

	public HashMap<String, String> getRets() {
		return rets;
	}
	public void setRets(HashMap<String, String> rets) {
		this.rets = rets;
	}



}
