package com.ruilonglai.texas_scan.entity;

import java.util.List;

public class ReqDelUser {
	int plattype;
	List<String> usernames;
	public int getPlattype() {
		return plattype;
	}
	public void setPlattype(int plattype) {
		this.plattype = plattype;
	}
	public List<String> getUsernames() {
		return usernames;
	}
	public void setUsernames(List<String> usernames) {
		this.usernames = usernames;
	}
	
}
