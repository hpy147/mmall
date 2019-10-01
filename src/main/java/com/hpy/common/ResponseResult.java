package com.hpy.common;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.Getter;

import java.io.Serializable;

/**
 * Author: hpy
 * Date: 2019-09-01 19:06
 * Description: 通用 响应类
 */
@Getter
@JsonSerialize(include = JsonSerialize.Inclusion.NON_NULL)
//保证序列化json的时候,如果是null的对象,key也会消失
public class ResponseResult<T> implements Serializable {

    private Integer status;
    private String msg;
    private T data;

    private ResponseResult(Integer status) {
        this.status = status;
    }

    private ResponseResult(Integer status, String msg) {
        this.status = status;
        this.msg = msg;
    }

    private ResponseResult(Integer status, T data) {
        this.status = status;
        this.data = data;
    }

    private ResponseResult(Integer status, String msg, T data) {
        this.status = status;
        this.msg = msg;
        this.data = data;
    }

    @JsonIgnore
    //使之不在json序列化结果当中
    public Boolean isSuccess() {
        return this.status == ResponseCode.SUCCESS.getCode();
    }

    /**
     * 成功的响应
     */
    public static ResponseResult createBySuccess() {
        return new ResponseResult(ResponseCode.SUCCESS.getCode());
    }

    public static ResponseResult createBySuccess(String msg) {
        return new ResponseResult(ResponseCode.SUCCESS.getCode(), msg);
    }

    public static <T> ResponseResult<T> createBySuccess(T data) {
        return new ResponseResult<>(ResponseCode.SUCCESS.getCode(), data);
    }

    public static <T> ResponseResult<T> createBySuccess(String msg, T data) {
        return new ResponseResult<>(ResponseCode.SUCCESS.getCode(), msg, data);
    }

    /**
     * 错误的响应
     */
    public static ResponseResult createByError() {
        return new ResponseResult(ResponseCode.ERROR.getCode(), ResponseCode.ERROR.getDesc());
    }

    public static ResponseResult createByError(String msg) {
        return new ResponseResult(ResponseCode.ERROR.getCode(), msg);
    }

    public static <T> ResponseResult<T> createByError(T data) {
        return new ResponseResult<>(ResponseCode.ERROR.getCode(), data);
    }

    public static ResponseResult createByError(Integer status, String msg) {
        return new ResponseResult(status, msg);
    }
}
