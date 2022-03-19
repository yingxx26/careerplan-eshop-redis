package com.ruyuan.careerplan.inventory.exception;

import com.ruyuan.careerplan.common.exception.BaseBizException;
import com.ruyuan.careerplan.common.exception.BaseErrorCodeEnum;

/**
 * 自定义业务异常类
 *
 * @author zhonghuashishan
 */
public class InventoryBizException extends BaseBizException {

    public InventoryBizException(String errorMsg) {
        super(errorMsg);
    }

    public InventoryBizException(String errorCode, String errorMsg) {
        super(errorCode, errorMsg);
    }

    public InventoryBizException(BaseErrorCodeEnum baseErrorCodeEnum) {
        super(baseErrorCodeEnum);
    }

    public InventoryBizException(String errorCode, String errorMsg, Object... arguments) {
        super(errorCode, errorMsg, arguments);
    }

    public InventoryBizException(BaseErrorCodeEnum baseErrorCodeEnum, Object... arguments) {
        super(baseErrorCodeEnum, arguments);
    }
}