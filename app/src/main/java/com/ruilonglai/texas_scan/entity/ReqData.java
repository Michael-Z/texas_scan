package com.ruilonglai.texas_scan.entity;

/**
 * Created by wangshuai on 2017/5/27.
 */

public class ReqData{
    String reqid;		//请求发起端id,全局唯一
    String reqno;	    //请求流水号
    String reqfunc;		//请求方法名
    String param;	    //请求参数对象

    public String getReqid() {
        return reqid;
    }

    public void setReqid(String reqid) {
        this.reqid = reqid;
    }

    public String getReqno() {
        return reqno;
    }

    public void setReqno(String reqno) {
        this.reqno = reqno;
    }

    public String getReqfunc() {
        return reqfunc;
    }

    public void setReqfunc(String reqfunc) {
        this.reqfunc = reqfunc;
    }

    public String getParam() {
        return param;
    }

    public void setParam(String param) {
        this.param = param;
    }
}
