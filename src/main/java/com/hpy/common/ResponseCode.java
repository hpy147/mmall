package com.hpy.common;

import lombok.Getter;

/**
 * Author: hpy
 * Date: 2019-09-01 19:20
 * Description: 响应码 枚举类
 */
@Getter
public enum  ResponseCode {

    SUCCESS(0, "SUCCESS"),
    ERROR(1, "ERROR"),
    NEED_LOGIN(10, "NEED_LOGIN"),
    ILLEGAL_ARGUMENT(3, "ILLEGAL_ARGUMENT");

    private Integer code;
    private String desc;

    private ResponseCode(Integer code, String desc) {
        this.code = code;
        this.desc = desc;
    }
}
