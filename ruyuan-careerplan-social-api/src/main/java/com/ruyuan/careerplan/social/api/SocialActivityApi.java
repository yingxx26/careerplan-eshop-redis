package com.ruyuan.careerplan.social.api;

import com.ruyuan.careerplan.common.core.JsonResult;
import com.ruyuan.careerplan.social.domain.dto.SocialMasterDetailResultDTO;

import java.util.Map;

/**
 * 社交服务接口
 *
 * @author zhonghuashishan
 */
public interface SocialActivityApi {

    /**
     * 菜谱分享活动
     *
     * @param userId     用户ID
     * @param cookbookId 菜谱ID
     * @return com.ruyuan.careerplan.common.core.JsonResult<java.util.Map < java.lang.String, java.lang.Object>>
     * @author zhonghuashishan
     */
    JsonResult<Map<String, Object>> cookbookShare(Long userId, String cookbookId);

    /**
     * 进入社交活动
     *
     * @param userId     用户ID
     * @param cookbookId 菜谱ID
     * @param ip         IP地址
     * @return com.ruyuan.careerplan.common.core.JsonResult<com.ruyuan.careerplan.social.domain.dto.SocialMasterDetailResultDTO>
     * @author zhonghuashishan
     */
    JsonResult<SocialMasterDetailResultDTO> enterSocialActivity(Long userId, String cookbookId, String ip);

}
