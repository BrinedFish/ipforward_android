package com.mak.myapplication;

import org.json.JSONObject;

public class ApiResult{
    public boolean ok = false;
    public String msg = "";
    public JSONObject data = null;

    public ApiResult(){}

    public ApiResult(boolean ok, String msg, JSONObject data){
        this.ok = ok;
        this.msg = msg;
        this.data = data;
    }
}