package com.ruyuan.careerplan.cookbook.service;

import com.ruyuan.careerplan.cookbook.domain.dto.CookbookUserDTO;
import com.ruyuan.careerplan.cookbook.domain.dto.SaveOrUpdateUserDTO;
import com.ruyuan.careerplan.cookbook.domain.request.CookbookUserQueryRequest;
import com.ruyuan.careerplan.cookbook.domain.request.SaveOrUpdateUserRequest;

/**
 * 菜谱作者服务
 *
 * @author zhonghuashishan
 */
public interface CookbookUserService {

    /**
     * 新增/修改作者
     *
     * @param request
     * @return
     */
    SaveOrUpdateUserDTO saveOrUpdateUser(SaveOrUpdateUserRequest request);

    /**
     * 获取作者信息
     *
     * @param request
     * @return
     */
    CookbookUserDTO getUserInfo(CookbookUserQueryRequest request);
}
