package com.ruyuan.careerplan.goodscart.domain.request;

import lombok.Builder;
import lombok.Data;

/**
 * @author zhonghuashishan
 */
@Data
@Builder
public class SelectedOptimalCouponRequest {
    /**
     * 用户ID
     */
    private Long userId;
}
