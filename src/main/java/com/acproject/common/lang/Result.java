package com.acproject.common.lang;

import lombok.Data;

import java.io.Serializable;

@Data
public class Result implements Serializable {
    /**
     * 状态码
     * 200 正常
     * 非200 异常
     */
    private int code;
    private String msg;
    private Object data;

    public static Result success(Object data){
        return success(200,"操作成功",data);
    }

    public static Result success(int code,String msg,Object data){
        Result res = new Result();
        res.setCode(code);
        res.setMsg(msg);
        res.setData(data);
        return res;
    }

    public static Result fail(String msg){
        return fail(400,msg,null);
    }

    public static Result fail(String msg,Object data){
        return fail(400,msg,data);
    }

    public static Result fail(int code,String msg,Object data){
        Result res = new Result();
        res.setCode(code);
        res.setMsg(msg);
        res.setData(data);
        return res;
    }






}
