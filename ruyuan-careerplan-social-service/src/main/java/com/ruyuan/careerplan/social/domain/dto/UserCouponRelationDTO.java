package com.ruyuan.careerplan.social.domain.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 用户的优惠券信息
 *
 * @author zhonghuashishan
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserCouponRelationDTO {

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 用户领取的优惠券
     */
    List<CouponInfoDTO> couponInfoDTOList;

}
