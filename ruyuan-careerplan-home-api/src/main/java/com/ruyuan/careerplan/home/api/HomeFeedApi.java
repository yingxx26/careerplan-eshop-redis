package com.ruyuan.careerplan.home.api;

import com.ruyuan.careerplan.common.core.JsonResult;
import com.ruyuan.careerplan.home.domain.dto.CookbookViewResultDTO;
import com.ruyuan.careerplan.home.domain.request.CookbookViewRequestDTO;

/**
 * 首页feed流服务
 */
public interface HomeFeedApi {

    /**
     * 首页feed流
     *
     * @param cookbookViewRequestDTO
     * @return com.ruyuan.careerplan.common.core.JsonResult<com.ruyuan.careerplan.home.domain.dto.CookbookViewResultDTO>
     */
    JsonResult<CookbookViewResultDTO> getCookbookViewList(CookbookViewRequestDTO cookbookViewRequestDTO);

}
