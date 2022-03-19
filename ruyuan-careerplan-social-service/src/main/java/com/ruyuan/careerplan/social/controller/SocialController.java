package com.ruyuan.careerplan.social.controller;

import com.alibaba.fastjson.JSON;
import com.ruyuan.careerplan.common.core.JsonResult;
import com.ruyuan.careerplan.social.api.SocialActivityApi;
import com.ruyuan.careerplan.social.domain.dto.SocialMasterDetailResultDTO;
import com.ruyuan.careerplan.social.domain.request.SocialMasterRequest;
import com.ruyuan.careerplan.social.service.SocialCommonService;
import com.ruyuan.careerplan.social.utils.BaseServiceUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.net.URLDecoder;
import java.util.Map;

/**
 * 社交活动控制类
 *
 * @author zhonghuashishan
 */
@Slf4j
@RestController
@RequestMapping("/api/social")
public class SocialController extends BaseServiceUtil {

    @Resource
    private SocialActivityApi socialActivityApi;

    @Resource
    private SocialCommonService socialCommonService;

    /**
     * 菜谱活动分享
     *
     * @param socialMasterRequest
     * @param request
     * @return com.ruyuan.careerplan.common.core.JsonResult<java.util.Map<java.lang.String,java.lang.Object>>
     * @author zhonghuashishan
     */
    @PostMapping("/cookbookShare")
    public JsonResult<Map<String,Object>> cookbookShare(@RequestBody SocialMasterRequest socialMasterRequest, HttpServletRequest request) {
        try {
            Long userId = socialCommonService.getUserIdByToken(URLDecoder.decode(socialMasterRequest.getAccessToken(), "UTF-8"));
            return socialActivityApi.cookbookShare(userId, socialMasterRequest.getCookbookId());
        } catch (Exception e) {
            String ipAddress = getIpAddress(request);
            log.error("菜谱分享活动异常 入参为socialMasterRequest={},ipAddress={},error=", JSON.toJSONString(socialMasterRequest), ipAddress, e);
            return JsonResult.buildError("菜谱分享活动异常");
        }
    }

    /**
     * 进入社交活动
     *
     * @param socialMasterRequest
     * @param request
     * @return com.ruyuan.careerplan.common.core.JsonResult<com.ruyuan.careerplan.social.domain.dto.SocialMasterDetailResultDTO>
     * @author zhonghuashishan
     */
    @PostMapping("/enterSocialActivity")
    public JsonResult<SocialMasterDetailResultDTO> enterSocialActivity(@RequestBody SocialMasterRequest socialMasterRequest, HttpServletRequest request) {
        try {
            Long userId = socialCommonService.getUserIdByToken(URLDecoder.decode(socialMasterRequest.getAccessToken(), "UTF-8"));
            return socialActivityApi.enterSocialActivity(userId, socialMasterRequest.getCookbookId(), getIpAddress(request));
        } catch (Exception e) {
            log.error("进入社交活动异常 入参为socialMasterRequest={},ipAddress={},error=", JSON.toJSONString(socialMasterRequest), getIpAddress(request), e);
            return JsonResult.buildError("进入社交活动异常");
        }
    }

}
