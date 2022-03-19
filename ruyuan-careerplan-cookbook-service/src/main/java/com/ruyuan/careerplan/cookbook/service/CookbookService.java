package com.ruyuan.careerplan.cookbook.service;

import com.ruyuan.careerplan.common.page.PagingInfo;
import com.ruyuan.careerplan.cookbook.domain.dto.CookbookDTO;
import com.ruyuan.careerplan.cookbook.domain.dto.SaveOrUpdateCookbookDTO;
import com.ruyuan.careerplan.cookbook.domain.request.CookbookQueryRequest;
import com.ruyuan.careerplan.cookbook.domain.request.SaveOrUpdateCookbookRequest;

/**
 * 菜谱服务
 *
 * @author zhonghuashishan
 */
public interface CookbookService {

    /**
     * 新增/修改菜谱
     *
     * @param request
     * @return
     */
    SaveOrUpdateCookbookDTO saveOrUpdateCookbook(SaveOrUpdateCookbookRequest request);

    /**
     * 获取菜谱信息
     *
     * @param request
     * @return
     */
    CookbookDTO getCookbookInfo(CookbookQueryRequest request);

    /**
     * 查询菜谱信息列表
     *
     * @param request
     * @return
     */
    PagingInfo<CookbookDTO> listCookbookInfo(CookbookQueryRequest request);
}
