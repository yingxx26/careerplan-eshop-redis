package com.ruyuan.careerplan.cookbook.api.impl;

import com.alibaba.fastjson.JSON;
import com.ruyuan.careerplan.common.core.JsonResult;
import com.ruyuan.careerplan.cookbook.api.CookbookUserApi;
import com.ruyuan.careerplan.cookbook.domain.dto.CookbookUserDTO;
import com.ruyuan.careerplan.cookbook.domain.dto.SaveOrUpdateUserDTO;
import com.ruyuan.careerplan.cookbook.domain.request.CookbookUserQueryRequest;
import com.ruyuan.careerplan.cookbook.domain.request.SaveOrUpdateUserRequest;
import com.ruyuan.careerplan.cookbook.exception.CookbookBizException;
import com.ruyuan.careerplan.cookbook.service.CookbookUserService;
import lombok.extern.slf4j.Slf4j;
import org.apache.dubbo.config.annotation.DubboService;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 菜谱作者服务
 *
 * @author zhonghuashishan
 */
@Slf4j
@DubboService(version = "1.0.0", interfaceClass = CookbookUserApi.class, retries = 0)
public class CookbookUserApiImpl implements CookbookUserApi {

    @Autowired
    private CookbookUserService cookbookUserService;


    @Override
    public JsonResult<SaveOrUpdateUserDTO> saveOrUpdateUser(SaveOrUpdateUserRequest request) {
        try {
            SaveOrUpdateUserDTO dto = cookbookUserService.saveOrUpdateUser(request);
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
    public JsonResult<CookbookUserDTO> getUserInfo(CookbookUserQueryRequest request) {
        try {
            CookbookUserDTO dto = cookbookUserService.getUserInfo(request);
            return JsonResult.buildSuccess(dto);
        } catch (CookbookBizException e) {
            log.error("biz error: request={}", JSON.toJSONString(request), e);
            return JsonResult.buildError(e.getErrorCode(), e.getErrorMsg());
        } catch (Exception e) {
            log.error("system error: request={}", JSON.toJSONString(request), e);
            return JsonResult.buildError(e.getMessage());
        }
    }
}
