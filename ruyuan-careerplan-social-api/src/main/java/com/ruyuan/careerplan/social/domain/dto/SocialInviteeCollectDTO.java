package com.ruyuan.careerplan.social.domain.dto;

import lombok.Data;

import java.io.Serializable;

@Data
public class SocialInviteeCollectDTO implements Serializable {

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 助力人ID
     */
    private Long inviteeId;

    /**
     * 助力人昵称
     */
    private String inviteeNickName;

    /**
     * 头像
     */
    private String inviteeAvatar;

    /**
     * 是否新用户
     */
    private  Boolean newUserFlag;

    /**
     * 助力金额 单位分
     */
    private Integer helpAmount;

    /**
     * 助力金额文案
     */
    private String helpAmountDoc;

    /**
     * 是否额外奖励
     */
    private Boolean premiums;

    /**
     * 是否助力成功后自己查看
     */
    private Boolean oneself;

}
