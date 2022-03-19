package com.ruyuan.careerplan.social.domain.request;

import lombok.Data;

import java.io.Serializable;

@Data
public class SocialMasterRequest implements Serializable {

    /**
     * 用户平台授权token
     */
    private String accessToken;

    /**
     * 菜谱分享活动ID
     */
    private String cookbookId;

}