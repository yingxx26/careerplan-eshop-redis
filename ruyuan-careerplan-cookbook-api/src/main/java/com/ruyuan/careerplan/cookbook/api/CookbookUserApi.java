package com.ruyuan.careerplan.cookbook.api;

import com.ruyuan.careerplan.common.core.JsonResult;
import com.ruyuan.careerplan.cookbook.domain.dto.CookbookUserDTO;
import com.ruyuan.careerplan.cookbook.domain.dto.SaveOrUpdateUserDTO;
import com.ruyuan.careerplan.cookbook.domain.request.CookbookUserQueryRequest;
import com.ruyuan.careerplan.cookbook.domain.request.SaveOrUpdateUserRequest;

/**
 * 菜谱作者服务接口
 *
 * @author zhonghuashishan
 */
public interface CookbookUserApi {

    /**
     * 新增/修改作者
     *
     * @param request
     * @return
     */
    JsonResult<SaveOrUpdateUserDTO> saveOrUpdateUser(SaveOrUpdateUserRequest request);


    /**
     * 获取作者信息
     *
     * @param request
     * @return
     */
    JsonResult<CookbookUserDTO> getUserInfo(CookbookUserQueryRequest request);

}
