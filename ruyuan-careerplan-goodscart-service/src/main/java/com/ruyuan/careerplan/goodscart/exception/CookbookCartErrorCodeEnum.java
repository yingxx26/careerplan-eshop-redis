package com.ruyuan.careerplan.goodscart.exception;


import com.ruyuan.careerplan.common.exception.BaseErrorCodeEnum;

/**
 * @author zhonghuashishan
 */
public enum CookbookCartErrorCodeEnum implements BaseErrorCodeEnum {

    /**
     * 参数错误
     */
    PARAM_ERROR("100001", "参数错误"),

    /**
     * 参数校验失败
     */
    PARAM_CHECK_ERROR("100002", "参数校验失败:{0}"),

    /**
     * 购物车sku数量达到阈值
     */
    CART_SKU_COUNT_THRESHOLD_ERROR("100003", "购物车商品数量达到上限"),

    /**
     * 商品目前暂未销售
     */
    SKU_SELL_STATUS_ERROR("100004", "商品目前未开放销售"),

    /**
     * 购物车中没有该商品
     */
    SKU_NOT_EXIST_CART_ERROR("100005", "购物车中没有该商品"),

    /**
     * 购物车商品持久化失败
     */
    CART_PERSISTENCE_ERROR("100006", "购物车商品持久化失败"),

    ;

    private String errorCode;

    private String errorMsg;

    CookbookCartErrorCodeEnum(String errorCode, String errorMsg) {
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