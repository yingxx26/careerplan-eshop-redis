package com.ruyuan.careerplan.goodscart.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.google.common.collect.Lists;
import com.ruyuan.careerplan.goodscart.constants.CookbookCartConstants;
import com.ruyuan.careerplan.goodscart.dao.CookbookCouponDAO;
import com.ruyuan.careerplan.goodscart.dao.CookbookCouponReceiveDAO;
import com.ruyuan.careerplan.goodscart.domain.dto.CouponInfoDTO;
import com.ruyuan.careerplan.goodscart.domain.dto.SelectedOptimalCouponDTO;
import com.ruyuan.careerplan.goodscart.domain.dto.UserCouponRelationDTO;
import com.ruyuan.careerplan.goodscart.domain.entity.CookBookCouponDO;
import com.ruyuan.careerplan.goodscart.domain.entity.CookBookCouponReceiveDO;
import com.ruyuan.careerplan.goodscart.service.CookbookCouponService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * @author zhonghuashishan
 */
@Service
public class CookbookCouponServiceImpl implements CookbookCouponService {

    @Autowired
    private CookbookCouponDAO couponDAO;

    @Autowired
    private CookbookCouponReceiveDAO couponReceiveDAO;

    /**
     * 营销中台：用户领取的优惠券信息
     */
    private static final Map<Long, UserCouponRelationDTO> userCouponMap = new HashMap<Long, UserCouponRelationDTO>() {{
        put(1001L, new UserCouponRelationDTO(1001L, Lists.newArrayList(new CouponInfoDTO(10000001L, "新用户优惠券", 50, 0, 0, "首次注册用户优惠券"))));
        put(1002L, new UserCouponRelationDTO(1002L, Lists.newArrayList(new CouponInfoDTO(10000002L, "活跃用户优惠券", 30, 150, 0, "活跃用户优惠券"))));
        put(1003L, new UserCouponRelationDTO(1003L, Lists.newArrayList(new CouponInfoDTO(10000003L, "vip用户优惠券", 100, 0, 0, "vip用户优惠券"))));
    }};


    /**
     * 匹配到最优的优惠券
     *
     * @param userId
     * @return
     */
    @Override
    public SelectedOptimalCouponDTO selectedOptimalCoupon(Long userId, Integer totalPrice) {
        UserCouponRelationDTO userCouponRelationDTO = userCouponMap.get(userId);
        if (Objects.isNull(userCouponRelationDTO) || CollectionUtils.isEmpty(userCouponRelationDTO.getCouponInfoDTOList())) {
            return null;
        }

        List<CouponInfoDTO> couponInfoDTOList = userCouponRelationDTO.getCouponInfoDTOList();

        // 遍历用户的所有优惠券
        return couponInfoDTOList.stream()
                // 过滤null
                .filter(Objects::nonNull)
                // 过滤出状态为生效状态的优惠券
                .filter(couponInfoDTO -> couponInfoDTO.getStatus().equals(CookbookCartConstants.AVAILABLE_COUPON_STATUS))
                // 过滤出达到使用门槛的优惠券
                .filter(couponInfoDTO -> couponInfoDTO.getThreshold() < totalPrice)
                // 获取金额最大的优惠券
                .max(Comparator.comparing(CouponInfoDTO::getCouponAmount))
                // DO转换为DTO
                .map(this::buildSelectedOptimalCouponDTO)
                .orElse(null);
    }

    /**
     * 构造优惠券返回数据
     * @param couponInfoDTO
     * @return
     */
    private SelectedOptimalCouponDTO buildSelectedOptimalCouponDTO(CouponInfoDTO couponInfoDTO) {
        return SelectedOptimalCouponDTO.builder()
                .couponId(couponInfoDTO.getId())
                .couponAmount(couponInfoDTO.getCouponAmount())
                .build();
    }

    /**
     * 获取用户所有未使用的优惠券领取记录
     *
     * @param userId
     * @return
     */
    private List<CookBookCouponReceiveDO> getCookbookCouponReceive(Long userId) {
        // 构造查询条件
        LambdaQueryWrapper<CookBookCouponReceiveDO> queryWrapper = Wrappers.lambdaQuery();
        queryWrapper.eq(CookBookCouponReceiveDO::getUserId, userId);
        queryWrapper.eq(CookBookCouponReceiveDO::getStatus, CookbookCartConstants.UNUSED_COUPON_STATUS);

        // 用户优惠券领取记录
        return couponReceiveDAO.list(queryWrapper);
    }
}
