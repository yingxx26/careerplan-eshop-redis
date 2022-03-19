package com.ruyuan.careerplan.home.controller;

import com.alibaba.fastjson.JSON;
import com.ruyuan.careerplan.common.core.JsonResult;
import com.ruyuan.careerplan.home.api.HomeFeedApi;
import com.ruyuan.careerplan.home.constants.HomeConstant;
import com.ruyuan.careerplan.home.domain.dto.CookbookViewResultDTO;
import com.ruyuan.careerplan.home.domain.request.CookbookViewRequestDTO;
import com.ruyuan.careerplan.home.domain.request.HomeFeedRequest;
import com.ruyuan.careerplan.home.enums.ErrorMsgEnum;
import com.ruyuan.careerplan.home.service.HomeCommonService;
import com.ruyuan.careerplan.home.utils.BaseServiceUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.Objects;

/**
 * @author zhonghuashishan
 */
@Slf4j
@RestController
@RequestMapping("/api/cookbook/home")
public class CookbookHomeController extends BaseServiceUtil {

    @Resource
    private HomeCommonService homeCommonService;

    @Resource
    private HomeFeedApi homeFeedApi;

    /**
     * 查询首页feed流
     *
     * @param request
     * @param feedRequest
     * @return com.ruyuan.careerplan.common.core.JsonResult<com.ruyuan.careerplan.home.domain.dto.CookbookViewResultDTO>
     */
    @PostMapping(value = "/feed")
    public JsonResult<CookbookViewResultDTO> feed(HttpServletRequest request, @RequestBody HomeFeedRequest feedRequest) {
        if (Objects.isNull(feedRequest) || Objects.isNull(feedRequest.getPageNo())) {
            return JsonResult.buildError(ErrorMsgEnum.PARAM_ERROR.getCode(), ErrorMsgEnum.PARAM_ERROR.getMsg());
        }

        Long userId = homeCommonService.getUserIdByToken(feedRequest.getAccessToken());
        if(Objects.isNull(userId)) {
            return JsonResult.buildError(ErrorMsgEnum.PARAM_ERROR.getCode(), ErrorMsgEnum.PARAM_ERROR.getMsg());
        }

        // 封装请求参数
        CookbookViewRequestDTO cookbookViewRequestDTO = new CookbookViewRequestDTO();
        cookbookViewRequestDTO.setFeedVersion(feedRequest.getFeedVersion());
        cookbookViewRequestDTO.setPageNo(feedRequest.getPageNo());
        cookbookViewRequestDTO.setPageSize(HomeConstant.DEFAULT_PAGE_SIZE);
        cookbookViewRequestDTO.setPullToRefresh(feedRequest.getPullToRefresh());
        cookbookViewRequestDTO.setUserId(userId);

        try {
            JsonResult<CookbookViewResultDTO> result = homeFeedApi.getCookbookViewList(cookbookViewRequestDTO);
            if (result.getSuccess() && result.getData() != null) {
                return result;
            }
        } catch (Exception e) {
            String ipAddress = getIpAddress(request);
            log.error("查询首页feed流异常，feedRequest={},ipAddress={},error=" + JSON.toJSONString(feedRequest), ipAddress, e);
        }
        return JsonResult.buildError(ErrorMsgEnum.SERVICE_ERROR.getCode(), ErrorMsgEnum.SERVICE_ERROR.getMsg());
    }

}
