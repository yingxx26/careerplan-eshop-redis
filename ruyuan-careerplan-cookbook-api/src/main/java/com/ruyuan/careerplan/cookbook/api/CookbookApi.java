package com.ruyuan.careerplan.cookbook.api;

import com.ruyuan.careerplan.common.core.JsonResult;
import com.ruyuan.careerplan.common.page.PagingInfo;
import com.ruyuan.careerplan.cookbook.domain.dto.CookbookDTO;
import com.ruyuan.careerplan.cookbook.domain.dto.SaveOrUpdateCookbookDTO;
import com.ruyuan.careerplan.cookbook.domain.request.CookbookQueryRequest;
import com.ruyuan.careerplan.cookbook.domain.request.SaveOrUpdateCookbookRequest;

/**
 * 菜谱服务接口
 *
 * @author zhonghuashishan
 */
public interface CookbookApi {

    /**
     * 新增/修改菜谱
     *
     * @param request
     * @return
     */
    JsonResult<SaveOrUpdateCookbookDTO> saveOrUpdateCookbook(SaveOrUpdateCookbookRequest request);


    /**
     * 获取菜谱信息
     *
     * @param request
     * @return
     */
    JsonResult<CookbookDTO> getCookbookInfo(CookbookQueryRequest request);

    /**
     * 查询菜谱信息列表
     *
     * @param request
     * @return
     */
    JsonResult<PagingInfo<CookbookDTO>> listCookbookInfo(CookbookQueryRequest request);
}
