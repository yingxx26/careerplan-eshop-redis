package com.ruyuan.careerplan.social.domain.dto;


import lombok.Data;

import java.io.Serializable;


/**
 * 全局配置
 *
 * @author zhonghuashishan
 */
@Data
public class SocialActivityConfigDTO implements Serializable {

    /**
     * 提现活动倒计时（单位：小时）
     */
    private Integer countdownTime;

    /**
     * 每人每天参团上限
     */
    private Integer joinUpperLimit;

    /**
     * 新用户参团上限
     */
    private Integer newUserJoinUpperLimit;

    /**
     * 助力间隔天数，默认2天，防止薅羊毛
     */
    private Integer joinIntervalDayLimit;

    /**
     * 分享文案
     */
    private String shareMsg;

    /**
     * 分享配图
     */
    private String shareImg;

    /**
     * 新用户优惠券码
     */
    private String newUserCouponCode;

    /**
     * 新用户优惠券码跳转链接
     */
    private String newUserCouponUrl;

    /**
     * 老用户优惠券码
     */
    private String oldUserCouponCode;

    /**
     * 老用户优惠券码跳转链接
     */
    private String oldUserCouponUrl;

    /**
     * 第2次分享到账
     */
    private Integer shareTwoAmount;

    /**
     * 新用户助力金额上限
     */
    private Integer newUserHelpUpperLimit;

    /**
     * 新用户助力金额下限
     */
    private Integer newUserHelpLowerLimit;

    /**
     * 老用户助力金额上限
     */
    private Integer oldUserHelpUpperLimit;

    /**
     * 老用户助力金额下限
     */
    private Integer oldUserHelpLowerLimit;

    /**
     * 分享额外金额上限
     */
    private Integer shareExtraUpperLimit;

    /**
     * 分享额外金额下限
     */
    private Integer shareExtraLowerLimit;

    /**
     * 阶段一需要助力人数
     */
    private Integer stageOneMember;

    /**
     * 阶段二需要助力人数
     */
    private Integer stageTwoMember;

    /**
     * 阶段三需要助力人数
     */
    private Integer stageThreeMember;

    /**
     * 阶段四需要助力人数
     */
    private Integer stageFourMember;

    /**
     * 活动规则配置
     */
    private String ruleDocuments;

}
