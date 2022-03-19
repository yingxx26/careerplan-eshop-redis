package com.ruyuan.careerplan.cookbook.api;

import com.ruyuan.careerplan.common.core.JsonResult;
import com.ruyuan.careerplan.common.page.PagingInfo;
import com.ruyuan.careerplan.cookbook.domain.dto.SaveOrUpdateCookbookDTO;
import com.ruyuan.careerplan.cookbook.domain.dto.SaveOrUpdateSkuDTO;
import com.ruyuan.careerplan.cookbook.domain.dto.SkuInfoDTO;
import com.ruyuan.careerplan.cookbook.domain.request.SaveOrUpdateCookbookRequest;
import com.ruyuan.careerplan.cookbook.domain.request.SaveOrUpdateSkuRequest;
import com.ruyuan.careerplan.cookbook.domain.request.SkuInfoQueryRequest;
import com.ruyuan.careerplan.cookbook.domain.request.SkuSaleableRequest;

import java.util.List;

/**
 * 商品服务接口
 *
 * @author zhonghuashishan
 */
public interface GoodsApi {


    /**
     * 新增/修改商品
     *
     * @param request
     * @return
     */
    JsonResult<SaveOrUpdateSkuDTO> saveOrUpdateSku(SaveOrUpdateSkuRequest request);

    /**
     * 根据商品编码获取商品详情
     *
     * @param request
     * @return
     */
    JsonResult<SkuInfoDTO> getSkuInfoBySkuId(SkuInfoQueryRequest request);

    /**
     * 获取商品列表
     *
     * @param request
     * @return
     */
    JsonResult<List<SkuInfoDTO>> listSkuInfo(SkuInfoQueryRequest request);

    /**
     * 校验商品是否可售：库存、上下架
     *
     * @return
     */
    JsonResult<Boolean> skuIsSaleable(SkuSaleableRequest request);

    /**
     *  对商品进行上下架
     * @param request
     * @return
     */
    JsonResult<Boolean>  skuUpOrDown(SkuSaleableRequest request);
}
