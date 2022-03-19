package com.ruyuan.careerplan.social.exception;

import com.ruyuan.careerplan.common.exception.BaseBizException;
import com.ruyuan.careerplan.common.exception.BaseErrorCodeEnum;

/**
 * 自定义业务异常类
 *
 * @author zhonghuashishan
 */
public class SocialBizException extends BaseBizException {

    public SocialBizException(String errorMsg) {
        super(errorMsg);
    }

    public SocialBizException(String errorCode, String errorMsg) {
        super(errorCode, errorMsg);
    }

    public SocialBizException(BaseErrorCodeEnum baseErrorCodeEnum) {
        super(baseErrorCodeEnum);
    }

    public SocialBizException(String errorCode, String errorMsg, Object... arguments) {
        super(errorCode, errorMsg, arguments);
    }

    public SocialBizException(BaseErrorCodeEnum baseErrorCodeEnum, Object... arguments) {
        super(baseErrorCodeEnum, arguments);
    }
}