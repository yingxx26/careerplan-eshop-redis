package com.ruyuan.careerplan.cookbook.api.impl;

import com.alibaba.fastjson.JSON;
import com.ruyuan.careerplan.common.core.JsonResult;
import com.ruyuan.careerplan.common.page.PagingInfo;
import com.ruyuan.careerplan.cookbook.api.CookbookApi;
import com.ruyuan.careerplan.cookbook.domain.dto.CookbookDTO;
import com.ruyuan.careerplan.cookbook.domain.dto.SaveOrUpdateCookbookDTO;
import com.ruyuan.careerplan.cookbook.domain.request.CookbookQueryRequest;
import com.ruyuan.careerplan.cookbook.domain.request.SaveOrUpdateCookbookRequest;
import com.ruyuan.careerplan.cookbook.exception.CookbookBizException;
import com.ruyuan.careerplan.cookbook.service.CookbookService;
import lombok.extern.slf4j.Slf4j;
import org.apache.dubbo.config.annotation.DubboService;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 菜谱服务
 *
 * @author zhonghuashishan
 */
@Slf4j
@DubboService(version = "1.0.0", interfaceClass = CookbookApi.class, retries = 0)
public class CookbookApiImpl implements CookbookApi {

    @Autowired
    private CookbookService cookbookService;

    @Override
    public JsonResult<SaveOrUpdateCookbookDTO> saveOrUpdateCookbook(SaveOrUpdateCookbookRequest request) {
        try {
            SaveOrUpdateCookbookDTO dto = cookbookService.saveOrUpdateCookbook(request);
            return JsonResult.buildSuccess(dto);
        } catch (CookbookBizException e) {
            log.error("biz error: request={}", JSON.toJSONString(request), e);
            return JsonResult.buildError(e.getErrorCode(), e.getErrorMsg());
        } catch (Exception e) {
            log.error("system error: request={}", JSON.toJSONString(request), e);
            return JsonResult.buildError(e.getMessage());
        }
    }

    @Override
    public JsonResult<CookbookDTO> getCookbookInfo(CookbookQueryRequest request) {
        try {
            CookbookDTO dto = cookbookService.getCookbookInfo(request);
            return JsonResult.buildSuccess(dto);
        } catch (CookbookBizException e) {
            log.error("biz error: request={}", JSON.toJSONString(request), e);
            return JsonResult.buildError(e.getErrorCode(), e.getErrorMsg());
        } catch (Exception e) {
            log.error("system error: request={}", JSON.toJSONString(request), e);
            return JsonResult.buildError(e.getMessage());
        }
    }

    @Override
    public JsonResult<PagingInfo<CookbookDTO>> listCookbookInfo(CookbookQueryRequest request) {
        try {
            PagingInfo<CookbookDTO> pagingInfo = cookbookService.listCookbookInfo(request);
            return JsonResult.buildSuccess(pagingInfo);
        } catch (CookbookBizException e) {
            log.error("biz error: request={}", JSON.toJSONString(request), e);
            return JsonResult.buildError(e.getErrorCode(), e.getErrorMsg());
        } catch (Exception e) {
            log.error("system error: request={}", JSON.toJSONString(request), e);
            return JsonResult.buildError(e.getMessage());
        }
    }
}
