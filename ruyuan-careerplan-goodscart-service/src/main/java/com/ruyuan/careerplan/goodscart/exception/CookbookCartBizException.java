package com.ruyuan.careerplan.goodscart.exception;

import com.ruyuan.careerplan.common.exception.BaseBizException;
import com.ruyuan.careerplan.common.exception.BaseErrorCodeEnum;

/**
 * 菜谱自定义业务异常类
 *
 * @author zhonghuashishan
 */
public class CookbookCartBizException extends BaseBizException {

    public CookbookCartBizException(String errorMsg) {
        super(errorMsg);
    }

    public CookbookCartBizException(String errorCode, String errorMsg) {
        super(errorCode, errorMsg);
    }

    public CookbookCartBizException(BaseErrorCodeEnum baseErrorCodeEnum) {
        super(baseErrorCodeEnum);
    }

    public CookbookCartBizException(String errorCode, String errorMsg, Object... arguments) {
        super(errorCode, errorMsg, arguments);
    }

    public CookbookCartBizException(BaseErrorCodeEnum baseErrorCodeEnum, Object... arguments) {
        super(baseErrorCodeEnum, arguments);
    }
}