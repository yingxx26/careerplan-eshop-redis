package com.ruyuan.careerplan.goodscart.service;

import com.ruyuan.careerplan.goodscart.domain.dto.SelectedOptimalCouponDTO;

/**
 * @author zhonghuashishan
 */
public interface CookbookCouponService {
    /**
     * 匹配到最优的优惠券
     * @param userId
     * @return
     */
    SelectedOptimalCouponDTO selectedOptimalCoupon(Long userId, Integer totalPrice);
}
