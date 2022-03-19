package com.ruyuan.careerplan.social.service;

import com.ruyuan.careerplan.social.domain.dto.SocialActivityHelpResultDTO;

/**
 * 社交服务助力接口
 *
 * @author zhonghuashishan
 */
public interface SocialActivityService {

    /**
     * 进入助力活动
     *
     * @param cookbookId
     * @param userId
     * @param ip
     * @return com.ruyuan.careerplan.common.core.JsonResult<com.ruyuan.careerplan.social.domain.dto.SocialInviteeHelpResultDTO>
     * @author zhonghuashishan
     */
    SocialActivityHelpResultDTO enterSocialInviteeActivity(String cookbookId, Long userId, String ip);

    /**
     * 初始化社交活动
     *
     * @param userId
     * @param cookbookId
     * @return java.lang.Boolean
     * @author zhonghuashishan
     */
    Boolean initSocialActivity(Long userId, String cookbookId);

}
