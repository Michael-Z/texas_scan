package com.ruilonglai.texas_scan.entity;

import org.litepal.crud.DataSupport;

/**
 * Created by Administrator on 2017/10/28.
 */

public class UserName extends DataSupport{

    public int plattype;
    public String name;

    public int getPlattype() {
        return plattype;
    }

    public void setPlattype(int plattype) {
        this.plattype = plattype;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
