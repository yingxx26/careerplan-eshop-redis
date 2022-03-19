package com.ruyuan.careerplan.social.domain.dto;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

@Data
public class SocialMasterDetailResultDTO implements Serializable {

    /**
     * 菜谱ID
     */
    private String cookbookId;

    /**
     * 助力状态（0-活动中，1-活动完成，2-活动过期，3-活动未开始）
     */
    private Integer helpStatus;

    /**
     * 活动剩余时间（时间戳）
     */
    private Long remainTime;

    /**
     * 团长昵称
     */
    private String masterNickname;

    /**
     * 团长头像
     */
    private String masterAvatar;

    /**
     * 是否进入自己的页面
     */
    private Boolean oneself;

    /**
     * 总金额
     */
    private Integer totalAmount;

    /**
     * 待到账金额 单位分
     */
    private Integer waitAmount;

    /**
     * 已提的金额 单位分
     */
    private Integer readyAmount;

    /**
     * 待提现金额 单位分
     */
    private Integer remainAmount;
    /**
     * 已到账金额
     */
    private Integer receiveAmount;

    /**
     * 第二次分享奖励金额
     */
    private Integer shareAmount;

    /**
     * 活动规则
     */
    private String ruleDocuments;

    /**
     * 是否新人
     */
    private Boolean newUserFlag;

    /**
     * 是否展示额外奖励金额
     */
    private Boolean showPremiums;

    /**
     * 额外奖励金额
     */
    private Integer premiumsAmount;

    /**
     * 再分享N个群或再邀请X个好友
     */
    private Integer needInviteOrShareCount;

    /**
     * 总共所需分享群
     */
    private Integer totalNeedShareCount;

    /**
     * 所处阶段（1-群分享，2-任务中，3-弹红包，4-无任务，5-最后一个阶段）
     */
    private Integer currentStage;

    /**
     * 本次活动共邀请了好友数
     */
    private Integer totalInviteeCount;

    /**
     * 本次活动额外奖励总金额
     */
    private Integer totalPremiumsAmount;

    /**
     * 已经分享次数
     */
    private Long alreadyShareCount;

    /**
     * 微信小程序分享参数
     */
    private WeChatShareDataDTO weChatShareData;

    /**
     * 助力者列表
     */
    private List<SocialInviteeCollectDTO> inviteeList;

    /**
     * 助力结果
     */
    private SocialActivityHelpResultDTO helpActivityResult;

}
