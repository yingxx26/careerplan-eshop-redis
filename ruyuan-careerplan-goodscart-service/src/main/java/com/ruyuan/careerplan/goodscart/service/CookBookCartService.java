package com.ruyuan.careerplan.goodscart.service;

import com.ruyuan.careerplan.goodscart.domain.dto.CookBookCartInfoDTO;
import com.ruyuan.careerplan.goodscart.domain.request.AddCookBookCartRequest;
import com.ruyuan.careerplan.goodscart.domain.request.CheckedCartRequest;
import com.ruyuan.careerplan.goodscart.domain.request.UpdateCookBookCartRequest;

/**
 * @author zhonghuashishan
 */
public interface CookBookCartService {

    /**
     * 购物车加购
     *
     * @param request
     */
    void addCartGoods(AddCookBookCartRequest request);

    /**
     * 购物车商品更新
     *
     * @param request
     * @return
     */
    CookBookCartInfoDTO updateCartGoods(UpdateCookBookCartRequest request);

    /**
     * 查看购物车
     *
     * @param userId
     * @return
     */
    CookBookCartInfoDTO queryCart(Long userId);

    /**
     * 选中购物车商品
     *
     * @param request
     * @return
     */
    CookBookCartInfoDTO checkedCartGoods(CheckedCartRequest request);

    /**
     * 计算购物车选择商品的价格，不减免优惠券
     * @param userId
     * @return
     */
    Integer calculateCartPrice(Long userId);
}
