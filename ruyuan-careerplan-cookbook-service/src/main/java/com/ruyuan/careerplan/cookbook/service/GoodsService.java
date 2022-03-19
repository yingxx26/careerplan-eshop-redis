package com.ruyuan.careerplan.cookbook.service;

import com.ruyuan.careerplan.cookbook.domain.dto.SaveOrUpdateSkuDTO;
import com.ruyuan.careerplan.cookbook.domain.dto.SkuInfoDTO;
import com.ruyuan.careerplan.cookbook.domain.request.SaveOrUpdateSkuRequest;
import com.ruyuan.careerplan.cookbook.domain.request.SkuInfoQueryRequest;
import com.ruyuan.careerplan.cookbook.domain.request.SkuSaleableRequest;

import java.util.List;

/**
 * 商品服务
 *
 * @author zhonghuashishan
 */
public interface GoodsService {

    /**
     * 新增/修改商品
     *
     * @param request
     * @return
     */
    SaveOrUpdateSkuDTO saveOrUpdateSku(SaveOrUpdateSkuRequest request);

    /**
     * 根据商品编码获取商品详情
     *
     * @param request
     * @return
     */
    SkuInfoDTO getSkuInfoBySkuId(SkuInfoQueryRequest request);

    /**
     * 获取商品列表
     *
     * @param request
     * @return
     */
    List<SkuInfoDTO> listSkuInfo(SkuInfoQueryRequest request);

    /**
     * 获取标签对应的商品信息
     * 模拟商品中心，标签对应的商品
     *
     * @param tags
     * @return
     */
    List<Long> getSkuIdsByTags(List<String> tags);

    /**
     * 校验商品是否可售：库存、上下架
     * @param request
     * @return
     */
    Boolean skuIsSaleable(SkuSaleableRequest request);

    /**
     * 商品进行上下架操作
     * @param request
     * @return
     */
    void skuUpOrDown(SkuSaleableRequest request);

}
