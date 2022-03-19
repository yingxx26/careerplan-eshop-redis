package com.ruyuan.careerplan.home.enums;

public enum ErrorMsgEnum {

    PARAM_ERROR("1", "参数错误"),
    SERVICE_ERROR("2", "服务异常"),
    ;

    private String code;

    private String msg;

    ErrorMsgEnum(String code, String msg) {
        this.code = code;
        this.msg = msg;
    }

    public String getCode() {
        return code;
    }

    public String getMsg() {
        return msg;
    }

}
