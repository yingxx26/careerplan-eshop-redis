package com.ruyuan.careerplan.social.domain.entity;

import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.annotation.TableName;
import com.ruyuan.careerplan.common.domain.BaseEntity;
import com.ruyuan.careerplan.social.domain.dto.SocialInviteeExtendDTO;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.io.Serializable;
import java.util.Date;
import java.util.Objects;


/**
 * 助力者信息
 *
 * @author zhonghuashishan
 */
@Slf4j
@Data
@TableName("social_invitee")
public class SocialInviteeDO extends BaseEntity implements Serializable {

    /**
     * 主键id
     */
    private Long id;

    /**
     * 团ID
     */
    private String cookbookId;

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
     * 助力金额 单位分
     */
    private Integer helpAmount;

    /**
     * 扩展Json
     */
    private String helpConfig;

    /**
     * 删除标记（1-有效，0-删除）
     */
    private Integer delFlag;

    /**
     * 创建用户
     */
    private String createUser;

    /**
     * 更新用户
     */
    private String updateUser;

    /**
     * 创建时间
     */
    private Date createTime;

    /**
     * 修改时间
     */
    private Date updateTime;

    /**
     * 参团信息
     */
    private SocialInviteeExtendDTO socialInviteeExtendDTO;

    public SocialInviteeExtendDTO getSocialInviteeExtendDTO() {
        try {
            if(Objects.isNull(socialInviteeExtendDTO) && StringUtils.isNotEmpty(helpConfig)) {
                socialInviteeExtendDTO = JSON.parseObject(helpConfig, SocialInviteeExtendDTO.class);
            }
        } catch (Exception e) {
            log.error("getSocialInviteeExtendDTO error", e);
        }
        return socialInviteeExtendDTO;
    }

    public void setSocialInviteeExtendDTO(SocialInviteeExtendDTO socialInviteeExtendDTO) {
        try {
            if(StringUtils.isEmpty(this.getHelpConfig())) {
                this.setHelpConfig(JSON.toJSONString(socialInviteeExtendDTO));
            }
        } catch (Exception e) {
            log.error("setSocialInviteeExtendDTO error", e);
        }
        this.socialInviteeExtendDTO = socialInviteeExtendDTO;
    }

}
