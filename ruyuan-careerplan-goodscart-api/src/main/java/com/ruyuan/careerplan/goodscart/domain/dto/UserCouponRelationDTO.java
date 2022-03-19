package com.ruyuan.careerplan.goodscart.domain.dto;

import com.ruyuan.careerplan.goodscart.domain.entity.CookBookCouponReceiveDO;
import com.sun.tracing.dtrace.ArgsAttributes;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;

/**
 * @author zhonghuashishan
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserCouponRelationDTO implements Serializable {
    private Long id;
    private List<CouponInfoDTO> couponInfoDTOList;
}
