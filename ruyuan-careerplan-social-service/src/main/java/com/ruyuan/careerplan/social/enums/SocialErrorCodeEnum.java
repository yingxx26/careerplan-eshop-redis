package com.ruyuan.careerplan.social.enums;


import com.ruyuan.careerplan.common.exception.BaseErrorCodeEnum;

/**
 * @author zhonghuashishan
 */
public enum SocialErrorCodeEnum implements BaseErrorCodeEnum {

    /**
     * 参数错误
     */
    PARAM_ERROR("100001", "参数错误"),

    /**
     * 参数校验失败
     */
    PARAM_CHECK_ERROR("100002", "参数校验失败:{0}"),

    ;

    private String errorCode;

    private String errorMsg;

    SocialErrorCodeEnum(String errorCode, String errorMsg) {
        this.errorCode = errorCode;
        this.errorMsg = errorMsg;
    }

    @Override
    public String getErrorCode() {
        return errorCode;
    }

    public void setErrorCode(String errorCode) {
        this.errorCode = errorCode;
    }

    @Override
    public String getErrorMsg() {
        return errorMsg;
    }

    public void setErrorMsg(String errorMsg) {
        this.errorMsg = errorMsg;
    }

}