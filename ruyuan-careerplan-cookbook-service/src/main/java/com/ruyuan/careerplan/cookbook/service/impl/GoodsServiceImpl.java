package com.ruyuan.careerplan.cookbook.service.impl;

import com.alibaba.fastjson.JSON;
import com.google.common.collect.Lists;
import com.ruyuan.careerplan.common.cache.CacheSupport;
import com.ruyuan.careerplan.common.core.JsonResult;
import com.ruyuan.careerplan.common.exception.BaseBizException;
import com.ruyuan.careerplan.common.redis.RedisCache;
import com.ruyuan.careerplan.common.redis.RedisLock;
import com.ruyuan.careerplan.common.utils.JsonUtil;
import com.ruyuan.careerplan.common.utils.RandomUtil;
import com.ruyuan.careerplan.cookbook.constants.RedisKeyConstants;
import com.ruyuan.careerplan.cookbook.converter.SkuInfoConverter;
import com.ruyuan.careerplan.cookbook.dao.SkuInfoDAO;
import com.ruyuan.careerplan.cookbook.domain.dto.SaveOrUpdateSkuDTO;
import com.ruyuan.careerplan.cookbook.domain.dto.SkuInfoDTO;
import com.ruyuan.careerplan.cookbook.domain.entity.SkuInfoDO;
import com.ruyuan.careerplan.cookbook.domain.request.SaveOrUpdateSkuRequest;
import com.ruyuan.careerplan.cookbook.domain.request.SkuInfoQueryRequest;
import com.ruyuan.careerplan.cookbook.domain.request.SkuSaleableRequest;
import com.ruyuan.careerplan.cookbook.enums.SkuStatusEnum;
import com.ruyuan.careerplan.cookbook.exception.CookbookErrorCodeEnum;
import com.ruyuan.careerplan.cookbook.service.GoodsService;
import com.ruyuan.careerplan.inventory.api.InventoryApi;
import com.ruyuan.careerplan.inventory.domain.request.InventoryRequest;
import lombok.extern.slf4j.Slf4j;
import org.apache.dubbo.config.annotation.DubboReference;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

/**
 * 商品服务
 *
 * @author zhonghuashishan
 */
@Slf4j
@Service
public class GoodsServiceImpl implements GoodsService {

    @Autowired
    private SkuInfoDAO skuInfoDAO;

    @Autowired
    private SkuInfoConverter skuInfoConverter;

    @Autowired
    private RedisCache redisCache;

    @Autowired
    private RedisLock redisLock;

    @DubboReference(version = "1.0.0")
    private InventoryApi inventoryApi;

    @Override
    public SaveOrUpdateSkuDTO saveOrUpdateSku(SaveOrUpdateSkuRequest request) {

        String goodsUpdateLock = RedisKeyConstants.GOODS_UPDATE_LOCK_PREFIX + request.getOperator();
        boolean lock = redisLock.lock(goodsUpdateLock);

        if (!lock) {
            log.info("商品修改获取锁失败，operator:{}", request.getOperator());
            throw new BaseBizException("新增/修改失败");
        }
        try {
            // 保存商品信息
            Long skuId = saveSku(request);

            // 保存商品库存
            Boolean saveFlag = saveInventory(skuId, request);

            return SaveOrUpdateSkuDTO.builder().success(saveFlag).skuId(skuId).build();
        } finally {
            redisLock.unlock(goodsUpdateLock);
        }
    }

    private Boolean saveInventory(Long skuId, SaveOrUpdateSkuRequest request) {
        List<InventoryRequest> inventoryRequests = buildInventoryRequest(skuId, request);
        for (InventoryRequest inventoryRequest : inventoryRequests) {
            inventoryApi.putStorage(inventoryRequest);
        }
        return true;
    }

    private List<InventoryRequest> buildInventoryRequest(Long skuId, SaveOrUpdateSkuRequest request) {
        String warehouseCode = generateNo(1);
        List<SaveOrUpdateSkuRequest.Inventory> inventories = request.getInventories();
        List<InventoryRequest> inventoryRequests = new ArrayList<>();
        for (SaveOrUpdateSkuRequest.Inventory inventory : inventories) {
            InventoryRequest inventoryRequest = new InventoryRequest();
            inventoryRequest.setInventoryNum(inventory.getInventoryNum());
            inventoryRequest.setWarehouse(inventory.getWarehouse());
            inventoryRequest.setWarehouseCode(warehouseCode);
            inventoryRequest.setSkuId(skuId);
            inventoryRequest.setOperator(request.getOperator());
            inventoryRequests.add(inventoryRequest);
        }
        return inventoryRequests;
    }

    private Long saveSku(SaveOrUpdateSkuRequest request) {
        SkuInfoDO skuInfoDO = skuInfoConverter.convertSkuInfoDO(request);
        skuInfoDO.setId(request.getSkuId());
        skuInfoDO.setSkuImage(JsonUtil.object2Json(request.getSkuImage()));
        skuInfoDO.setDetailImage(JsonUtil.object2Json(request.getDetailImage()));
        if (Objects.isNull(skuInfoDO.getId())) {
            skuInfoDO.setCreateUser(request.getOperator());
        }
        skuInfoDO.setUpdateUser(request.getOperator());
        skuInfoDAO.saveOrUpdate(skuInfoDO);
        return skuInfoDO.getId();
    }

    /**
     * 模拟唯一id的生成，生成商品编码或者入库单号的接口
     *
     * @return
     */
    private String generateNo(Integer prefix) {
        return prefix + RandomUtil.genRandomNumber(9);
    }

    @Override
    public SkuInfoDTO getSkuInfoBySkuId(SkuInfoQueryRequest request) {
        return getSkuInfoBySkuId(request.getSkuId());
    }

    private SkuInfoDTO getSkuInfoBySkuId(Long skuId) {
        String goodsInfoKey = RedisKeyConstants.GOODS_INFO_PREFIX + skuId;
        // 从内存或者缓存中获取数据
        Object goodsInfoValue = redisCache.getCache(goodsInfoKey);

        if (Objects.equals(CacheSupport.EMPTY_CACHE, goodsInfoValue)) {
            // 如果是空缓存，则是防止缓存穿透的，直接返回null
            return null;
        } else if (goodsInfoValue instanceof SkuInfoDTO) {
            // 如果是对象，则是从内存中获取到的数据，直接返回
            return (SkuInfoDTO) goodsInfoValue;
        } else if (goodsInfoValue instanceof String) {
            // 如果是字符串，则是从缓存中获取到的数据，转换成对象之后返回
            Long expire = redisCache.getExpire(goodsInfoKey, TimeUnit.SECONDS);
            /*
             * 对于临期缓存两种做法：
             * 1、临期再续期
             * 2、不续期，随机过期时间，过期了直接加锁查库，然后放入缓存
             * 这里采用第一种做法，如果缓存过期时间小于一小时，则重新设置过期时间
             */
            if (expire < CacheSupport.ONE_HOURS_SECONDS) {
                redisCache.expire(goodsInfoKey, CacheSupport.generateCacheExpireSecond());
            }
            return JsonUtil.json2Object((String) goodsInfoValue, SkuInfoDTO.class);
        }

        // 未在内存和缓存中获取到值，从数据库中获取
        return getSkuInfoBySkuIdFromDB(skuId);
    }

    private SkuInfoDTO getSkuInfoBySkuIdFromDB(Long skuId) {
        String skuInfoLock = RedisKeyConstants.GOODS_LOCK_PREFIX + skuId;
        boolean lock = redisLock.lock(skuInfoLock);

        if (!lock) {
            log.info("缓存数据为空，从数据库查询商品信息时获取锁失败，skuId:{}", skuId);
            throw new BaseBizException("查询失败");
        }
        try {
            log.info("缓存数据为空，从数据库中获取数据，skuId:{}", skuId);
            SkuInfoDO skuInfoDO = skuInfoDAO.getById(skuId);
            String goodsInfoKey = RedisKeyConstants.GOODS_INFO_PREFIX + skuId;
            if (Objects.isNull(skuInfoDO)) {
                /*
                 * 如果商品编码对应的商品一开始不存在，设置空缓存，防止缓存穿透，
                 * 后来增加了对应的商品编码的商品，需要覆盖当前缓存值
                 * 这里没有做商品的维护，所以不涉及。
                 * 在菜谱服务中有体现。
                 */
                redisCache.setCache(goodsInfoKey, CacheSupport.EMPTY_CACHE,
                        CacheSupport.generateCachePenetrationExpireSecond());
                return null;
            }

            SkuInfoDTO dto = skuInfoConverter.convertSkuInfoDTO(skuInfoDO);
            dto.setSkuId(skuInfoDO.getId());
            dto.setSkuImage(JSON.parseArray(skuInfoDO.getSkuImage(), SkuInfoDTO.ImageInfo.class));
            dto.setDetailImage(JSON.parseArray(skuInfoDO.getDetailImage(), SkuInfoDTO.ImageInfo.class));

            // 设置缓存过期时间，2天加上随机几小时
            redisCache.setCache(goodsInfoKey, dto, CacheSupport.generateCacheExpireSecond());

            return dto;
        } finally {
            redisLock.unlock(skuInfoLock);
        }
    }

    @Override
    public List<SkuInfoDTO> listSkuInfo(SkuInfoQueryRequest request) {
        List<SkuInfoDTO> skuInfoDTOS = new ArrayList<>();
        for (Long skuId : request.getSkuIds()) {
            SkuInfoDTO skuInfoDTO = getSkuInfoBySkuId(skuId);
            if (Objects.nonNull(skuInfoDTO)){
                skuInfoDTOS.add(skuInfoDTO);
            }
        }
        return skuInfoDTOS;
    }

    /**
     * 商品中心，标签对应的商品
     * 模拟商品中心
     */
    private static Map<String, List<Long>> tagGoodsSkuIdMap = new HashMap<String, List<Long>>() {{
        put("酱油", Lists.newArrayList(10001L, 10002L));
        put("盐", Lists.newArrayList(20001L, 20002L));
        put("油", Lists.newArrayList(30001L, 30002L));
        put("菜", Lists.newArrayList(40001L));
    }};

    /**
     * 获取标签对应的商品信息
     * 模拟商品中心，标签对应的商品
     *
     * @param tags
     * @return
     */
    @Override
    public List<Long> getSkuIdsByTags(List<String> tags){
        List<Long> skuIds = new ArrayList<>();
        for (String tag : tags) {
            List<Long> tagSkuIds = tagGoodsSkuIdMap.get(tag);
            if (!CollectionUtils.isEmpty(tagSkuIds)) {
                skuIds.addAll(tagSkuIds);
            }
        }
        return skuIds;
    }


    @Override
    public Boolean skuIsSaleable(SkuSaleableRequest request) {
        Long skuId = request.getSkuId();
        // 商品状态
        SkuInfoDTO skuInfoDTO = getSkuInfoBySkuId(skuId);
        if (Objects.isNull(skuInfoDTO) || !SkuStatusEnum.UP.getCode().equals(skuInfoDTO.getSkuStatus())) {
            return false;
        }

        // 库存
        JsonResult<BigDecimal> inventory = inventoryApi.queryProductStock(skuId);
        log.info("获取商品库存, inventory: {}", JsonUtil.object2Json(inventory));

        if (!inventory.getSuccess()) {
            throw new BaseBizException(CookbookErrorCodeEnum.PARAM_ERROR, CookbookErrorCodeEnum.PARAM_ERROR.getErrorCode());
        }

        // 传入的数量不为空，库存数量要大于传入的数量
        if (Objects.nonNull(request.getCount())) {
            return new BigDecimal(request.getCount()).compareTo(inventory.getData()) <= 0;
        }else {
            // 传入的数量为空，库存数量大于0就行
            return inventory.getData().compareTo(BigDecimal.ZERO) > 0;
        }
    }

    /**
     * 对商品进行上下架操作
     * @param request
     * @return
     */
    @Override
    public void skuUpOrDown(SkuSaleableRequest request) {
        SkuInfoDO skuInfoDO = skuInfoDAO.getById(request.getSkuId());
        if (Objects.isNull(skuInfoDO)){
            throw new BaseBizException("商品不存在，无法进行上下架操作");
        }
        // 状态修改的值和当前的值一样，无需修改
        if (skuInfoDO.getSkuStatus().equals(request.getStatus())){
            String skuStatusMsg = request.getStatus().equals(1)?"上架":"下架";
            throw new BaseBizException("商品状态不能修改，已是"+skuStatusMsg+"状态");
        }
        skuInfoDO.setSkuStatus(request.getStatus());
        // 更新商品上下架状态
        boolean isUpdate = skuInfoDAO.updateById(skuInfoDO);
        if (!isUpdate){
            throw new BaseBizException("商品上下架异常");
        }
    }
}
