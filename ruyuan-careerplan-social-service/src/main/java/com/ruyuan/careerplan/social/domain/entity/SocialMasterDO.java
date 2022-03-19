package com.ruyuan.careerplan.social.domain.entity;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.annotation.TableName;
import com.ruyuan.careerplan.common.domain.BaseEntity;
import com.ruyuan.careerplan.social.domain.dto.SocialMasterExtendDTO;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.io.Serializable;
import java.util.Date;
import java.util.Objects;


/**
 * 团长信息
 *
 * @author zhonghuashishan
 */
@Slf4j
@Data
@TableName("social_master")
public class SocialMasterDO extends BaseEntity implements Serializable {

    /**
     * 主键ID
     */
    private Long id;

    /**
     * 菜谱ID
     */
    private String cookbookId;

    /**
     * 用户ID
     */
    private Long creatorId;

    /**
     * 头像
     */
    private String masterAvatar;

    /**
     * 发起人昵称
     */
    private String masterNickname;

    /**
     * 助力红包开始时间
     */
    private Date startTime;

    /**
     * 助力过期时间
     */
    private Date endTime;

    /**
     * 总金额 单位分
     */
    private Integer totalAmount;

    /**
     * 总助力金额 单位分
     */
    private Integer helpAmount;

    /**
     * 助力状态（0-活动中，1-活动完成，2-活动过期，3-活动未开始）
     */
    private Integer helpStatus;

    /**
     * 扩展Json
     */
    private String masterConfig;

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
     * 团长信息
     */
    private SocialMasterExtendDTO socialMasterExtendDTO;

    public SocialMasterExtendDTO getSocialMasterExtendDTO() {
        try {
            if(Objects.isNull(socialMasterExtendDTO) && StringUtils.isNotEmpty(masterConfig)) {
                socialMasterExtendDTO = JSON.parseObject(masterConfig, SocialMasterExtendDTO.class);
            }
        } catch (Exception e) {
            log.error("getSocialInviteeExtendDTO error", e);
        }
        return socialMasterExtendDTO;
    }

    public void setSocialMasterExtendDTO(SocialMasterExtendDTO socialMasterExtendDTO) {
        try {
            if(StringUtils.isEmpty(this.getMasterConfig())) {
                this.setMasterConfig(JSON.toJSONString(socialMasterExtendDTO));
            }
        } catch (Exception e) {
            log.error("setSocialMasterExtendDTO error", e);
        }
        this.socialMasterExtendDTO = socialMasterExtendDTO;
    }

}
