package com.ruilonglai.texas_scan.entity;

import java.util.List;

/**
 * Created by Administrator on 2017/9/16.
 */

public class QueryUser {

    private List<String> usernames;

    private int plattype;

    public List<String> getUsernames() {
        return usernames;
    }

    public void setUsernames(List<String> usernames) {
        this.usernames = usernames;
    }

    public int getPlatType() {
        return plattype;
    }

    public void setPlatType(int platType) {
        this.plattype = platType;
    }
}
