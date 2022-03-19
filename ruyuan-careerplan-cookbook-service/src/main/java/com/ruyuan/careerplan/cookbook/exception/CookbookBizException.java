package com.ruyuan.careerplan.cookbook.exception;

import com.ruyuan.careerplan.common.exception.BaseBizException;
import com.ruyuan.careerplan.common.exception.BaseErrorCodeEnum;

/**
 * 菜谱自定义业务异常类
 *
 * @author zhonghuashishan
 */
public class CookbookBizException extends BaseBizException {

    public CookbookBizException(String errorMsg) {
        super(errorMsg);
    }

    public CookbookBizException(String errorCode, String errorMsg) {
        super(errorCode, errorMsg);
    }

    public CookbookBizException(BaseErrorCodeEnum baseErrorCodeEnum) {
        super(baseErrorCodeEnum);
    }

    public CookbookBizException(String errorCode, String errorMsg, Object... arguments) {
        super(errorCode, errorMsg, arguments);
    }

    public CookbookBizException(BaseErrorCodeEnum baseErrorCodeEnum, Object... arguments) {
        super(baseErrorCodeEnum, arguments);
    }
}