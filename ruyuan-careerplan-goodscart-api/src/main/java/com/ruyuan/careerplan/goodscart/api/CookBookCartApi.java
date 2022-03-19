package com.ruyuan.careerplan.goodscart.api;

import com.ruyuan.careerplan.common.core.JsonResult;

/**
 * 购物车服务接口
 *
 * @author zhonghuashishan
 */
public interface CookBookCartApi {

    /**
     * 购物车的商品降价消息推送给MQ
     *
     * @param skuId
     * @return
     */
    JsonResult<String> pushSaleMessage(Long skuId);

    /**
     * 计算购物车选择商品的价格，不减免优惠券
     * @param userId
     * @return
     */
    JsonResult<Integer> calculateCartPrice(Long userId);

}
