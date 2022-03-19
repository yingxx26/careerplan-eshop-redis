package com.ruyuan.careerplan.goodscart.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.ruyuan.careerplan.common.core.JsonResult;
import com.ruyuan.careerplan.common.exception.BaseBizException;
import com.ruyuan.careerplan.common.redis.RedisCache;
import com.ruyuan.careerplan.common.redis.RedisLock;
import com.ruyuan.careerplan.common.utils.JsonUtil;
import com.ruyuan.careerplan.common.utils.RandomUtil;
import com.ruyuan.careerplan.cookbook.api.GoodsApi;
import com.ruyuan.careerplan.cookbook.domain.dto.SkuInfoDTO;
import com.ruyuan.careerplan.cookbook.domain.request.SkuInfoQueryRequest;
import com.ruyuan.careerplan.cookbook.domain.request.SkuSaleableRequest;
import com.ruyuan.careerplan.goodscart.constants.CookbookCartConstants;
import com.ruyuan.careerplan.goodscart.constants.RedisKeyConstant;
import com.ruyuan.careerplan.goodscart.constants.RedisLockKeyConstants;
import com.ruyuan.careerplan.goodscart.constants.RocketMqConstant;
import com.ruyuan.careerplan.goodscart.converter.CookbookCartConverter;
import com.ruyuan.careerplan.goodscart.dao.CookBookCartDAO;
import com.ruyuan.careerplan.goodscart.domain.dto.BillingDTO;
import com.ruyuan.careerplan.goodscart.domain.dto.CartSkuInfoDTO;
import com.ruyuan.careerplan.goodscart.domain.dto.CookBookCartInfoDTO;
import com.ruyuan.careerplan.goodscart.domain.dto.SelectedOptimalCouponDTO;
import com.ruyuan.careerplan.goodscart.domain.entity.CookBookCartDO;
import com.ruyuan.careerplan.goodscart.domain.request.AddCookBookCartRequest;
import com.ruyuan.careerplan.goodscart.domain.request.CheckedCartRequest;
import com.ruyuan.careerplan.goodscart.domain.request.UpdateCookBookCartRequest;
import com.ruyuan.careerplan.goodscart.enums.YesOrNoEnum;
import com.ruyuan.careerplan.goodscart.exception.CookbookCartBizException;
import com.ruyuan.careerplan.goodscart.exception.CookbookCartErrorCodeEnum;
import com.ruyuan.careerplan.goodscart.mq.producer.DefaultProducer;
import com.ruyuan.careerplan.goodscart.service.CookBookCartService;
import com.ruyuan.careerplan.goodscart.service.CookbookCouponService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.dubbo.config.annotation.DubboReference;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.ObjectUtils;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;


/**
 * @author zhonghuashishan
 */
@Slf4j
@Service
public class CookBookCartServiceImpl implements CookBookCartService {

    @DubboReference(version = "1.0.0")
    private GoodsApi goodsApi;
    @Resource
    private CookbookCartConverter cookbookCartConverter;
    @Autowired
    private DefaultProducer defaultProducer;
    @Autowired
    private CookBookCartDAO cookBookCartDAO;
    @Autowired
    private RedisCache redisCache;
    @Autowired
    private RedisLock redisLock;
    @Autowired
    private CookbookCouponService cookbookCouponService;

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void addCartGoods(AddCookBookCartRequest request) {
        String updateCartLockKey = RedisLockKeyConstants.UPDATE_CART_LOCK_KEY + request.getUserId() + ":" + request.getSkuId();
        boolean locked = redisLock.blockedLock(updateCartLockKey);

        if(!locked) {
            throw new BaseBizException("商品加入购物车失败");
        }

        try {
            // 构造购物车商品数据DTO
            CartSkuInfoDTO cartSkuInfoDTO = buildCartSkuInfoDTO(request);

            // 校验商品是否可售: 库存、上下架状态、购物车是否达到最大限制（购物车sku数量最多不能超过100）
            checkSellableProduct(cartSkuInfoDTO);

            // 检查商品是否已经存在，当前购物车里，这个商品如果已经存在了
            if (checkCartSkuExist(cartSkuInfoDTO)) {
                // 重新计算数量
                cartSkuInfoDTO = recalculateQuantity(cartSkuInfoDTO);
                // 更新购物车Redis缓存
                updateCartCache(cartSkuInfoDTO);
                // 发送更新消息到MQ
                sendAsyncUpdateMessage(cartSkuInfoDTO);
                return;
            }

            // 更新缓存
            // 购物车的缓存数据模型：hash:{skuId->count}, hash:{skuId->skuInfo}, zset:[skuId->timestamp]
            // 当你要查看用户的购物车的时候，zset按时间进行排序，把skuId列表都查出来，商品都显示出来，每个商品都要显示商品自己的信息，skuId->skuInfo，skuId->count
            updateCartCache(cartSkuInfoDTO);

            // 为什么购物车的主数据存储，要选用redis呢？业务上来说，购物车他其实是临时性的数据
            // 仅仅是把一些商品再购物车里进行暂存而已，迟早来说，要不然会直接去发起购买，这些数据就得从购物车里删除掉了
            // 过了很长时间都没买，你已经把他给忘了
            // 少了一些商品的话，问题不大，极端情况下，少了一些商品了以后，大不了你就是找不到商品
            // 重新把商品加入到购物车里去也就可以了

            // 如果要是我们刚刚写完缓存了之后，还没来得及发送消息到mq里去，系统崩了，导致缓存写成功了
            // 但是异步消息没过去，rocketmq出了一些故障，导致消息就是没发送成功，redis里有数据，rocketmq里没消息
            // mysql异步的数据页没有同步过去，缓存和mysql之间的数据是不一致的
            // 问题不太大，即使你出现了这样的一个问题，redis里只要他有数据就可以了，redis里后续一旦对这个商品提交了购物和订单的请求
            // 数据就会从redis里被删除掉了，就跟mysql里是同步的了
            // redis突然崩溃了，mysql里有数据，丢了一条数据，redis里那条数据页没了，用户在购物车里找不到自己刚才加入进去的商品了
            // 购物车只要没发起购物，他就是临时性的数据存储空间，商品找不到了，大不了他重新加入就可以了

            // 发送新增消息到MQ
            sendAsyncPersistenceMessage(cartSkuInfoDTO);
        } finally {
            redisLock.unlock(RedisLockKeyConstants.UPDATE_CART_LOCK_KEY);
        }
    }

    /**
     * 检查加购的商品是否在购物车中存在，如果存在，那么商品数量+1
     *
     * @param cartSkuInfoDTO
     * @return
     */
    private CartSkuInfoDTO recalculateQuantity(CartSkuInfoDTO cartSkuInfoDTO) {
        // 获取缓存中存在的商品数据
        String cartSkuInfoString = redisCache.hGet(RedisKeyConstant.SHOPPING_CART_EXTRA_HASH + cartSkuInfoDTO.getUserId(),
                String.valueOf(cartSkuInfoDTO.getSkuId())).toString();
        CartSkuInfoDTO oldCartSkuInfoDTO = JsonUtil.json2Object(cartSkuInfoString, CartSkuInfoDTO.class);
        if (Objects.isNull(oldCartSkuInfoDTO)) {
            return cartSkuInfoDTO;
        }

        // 商品数量加1
        // 多个线程过来，比如说一个用户连续点击了3次要加入同一个商品
        // 3个线程一起来进行操作，3个人会一起把一条商品数据读出来，此时他的购买数量都是1
        // 3个人再这里，都会把这个购买数量+1，3个线程，拿到的一个结果，都是1+1=2，1+1+1+1=4
        oldCartSkuInfoDTO.setCount(oldCartSkuInfoDTO.getCount() + 1);
        return oldCartSkuInfoDTO;
    }

    /**
     * 检查商品是否存在
     *
     * @param cartSkuInfoDTO
     * @return
     */
    private boolean checkCartSkuExist(CartSkuInfoDTO cartSkuInfoDTO) {
        // hash:{skuId->skuInfo, skuId->skuInfo}
        return redisCache.hFieldExists(RedisKeyConstant.SHOPPING_CART_EXTRA_HASH + cartSkuInfoDTO.getUserId(),
                String.valueOf(cartSkuInfoDTO.getSkuId()));
    }

    /**
     * 构造购物车商品数据DTO
     *
     * @param request
     * @return
     */
    private CartSkuInfoDTO buildCartSkuInfoDTO(AddCookBookCartRequest request) {
        // 获取商品数据
        SkuInfoDTO skuInfo = getSkuInfo(request.getSkuId());

        return CartSkuInfoDTO.builder()
                .skuId(request.getSkuId())
                .userId(request.getUserId())
                .warehouse(request.getWarehouse())
                .title(skuInfo.getSkuName())
                .price(skuInfo.getPrice())
                .image(skuInfo.getSkuImage())
                .updateTime(skuInfo.getUpdateTime())
                .checkStatus(YesOrNoEnum.NO.getCode())
                .count(CookbookCartConstants.ADD_CART_DEFAULT_SKU_COUNT)
                .build();
    }

    /**
     * 发送保存购物车的商品消息到MQ
     *
     * @param cartSkuInfoDTO
     */
    private void sendAsyncPersistenceMessage(CartSkuInfoDTO cartSkuInfoDTO) {
        // 需要落库的购物车实体对象
        CookBookCartDO cartDO = cookbookCartConverter.dtoToDO(cartSkuInfoDTO);

        // 发送消息到MQ
        log.info("发送加购购物车消息到MQ, topic: {}, skuInfo: {}", RocketMqConstant.COOKBOOK_ASYNC_PERSISTENCE_MESSAGE_SEND_TOPIC, JsonUtil.object2Json(cartDO));
        defaultProducer.sendMessage(RocketMqConstant.COOKBOOK_ASYNC_PERSISTENCE_MESSAGE_SEND_TOPIC,
                JsonUtil.object2Json(cartDO), "COOKBOOK购物车异步落库消息");
    }

    /**
     * 更新购物车Redis缓存
     *
     * @param cartSkuInfoDTO
     */
    private void updateCartCache(CartSkuInfoDTO cartSkuInfoDTO) {
        // 更新购物车sku数量
        updateCartNumCache(cartSkuInfoDTO);
        // 更新Redis中sku扩展信息
        updateCartExtraCache(cartSkuInfoDTO);
        // 更新Redis中购物车商品排序时间
        updateCartSortCache(cartSkuInfoDTO);
    }

    /**
     * 清除空缓存
     *
     * 判断空缓存是否，如果存在就删掉，因为这个缓存是设置了TTL的，所以如果不删掉，
     *
     * @param cartSkuInfoDTO
     */
    private void clearEmptyCache(CartSkuInfoDTO cartSkuInfoDTO) {
    }

    /**
     * 更新购物车sku数量
     *
     * @param cartSkuInfoDTO
     */
    private void updateCartNumCache(CartSkuInfoDTO cartSkuInfoDTO) {
        // 数量hash的key
        String numKey = RedisKeyConstant.SHOPPING_CART_HASH + cartSkuInfoDTO.getUserId();
        Integer count = cartSkuInfoDTO.getCount();
        String field = String.valueOf(cartSkuInfoDTO.getSkuId());
        // 更新缓存中的商品数量
        // 选用redis里面的hash数据结构
        // shopping_cart_hash:{userId} -> {
        //   {skuId}: 2,
        //   {skuId}: 3
        // }
        redisCache.hPut(numKey, field, String.valueOf(count));
        log.info("更新缓存购物车数量, key: {}, field: {}, value: {}", numKey, field, count);
    }

    /**
     * 更新购物车sku扩展信息
     *
     * @param cartSkuInfoDTO
     */
    private void updateCartExtraCache(CartSkuInfoDTO cartSkuInfoDTO) {
        // sku扩展信息hash的key
        String extraKey = RedisKeyConstant.SHOPPING_CART_EXTRA_HASH + cartSkuInfoDTO.getUserId();
        String field = String.valueOf(cartSkuInfoDTO.getSkuId());
        // 更新Redis中的商品扩展信息
        // hash数据结构
        // shopping_cart_extra_hash: {
        //  {skuId}: {skuInfo},
        //  {skuId}: {skuInfo}
        // }
        redisCache.hPut(extraKey, field, JsonUtil.object2Json(cartSkuInfoDTO));
        log.info("更新缓存购物车扩展信息, key: {}, field: {}, value: {}", extraKey, field, JsonUtil.object2Json(cartSkuInfoDTO));
    }

    /**
     * 更新购物车sku排序
     *
     * @param cartSkuInfoDTO
     */
    private void updateCartSortCache(CartSkuInfoDTO cartSkuInfoDTO) {
        // 排序缓存结构的key
        // redis五大数据结构：string、sorted list、hash、set、sorted set（命令z字打头）
        // redis里功能最强大的一个数据结构，sorted set写入的数据，都可以给一个score分数，他会默认按照score来进行排序
        // 后续基于score排序的set数据集合进行各种复杂的操作
        String sortKey = RedisKeyConstant.SHOPPING_CART_ZSET + cartSkuInfoDTO.getUserId();
        String field = String.valueOf(cartSkuInfoDTO.getSkuId());
        // 把每个skuId和他加入购物车的时间，写入到了sorted set里面去
        // sortedset: [{skuId -> score(当前时间)}, {skuId -> score(当前时间)}]
        redisCache.zadd(sortKey, field, System.currentTimeMillis());
        log.info("更新缓存购物车商品顺序, key: {}", sortKey);
    }

    /**
     * 获取skuInfo
     *
     * @param skuId
     * @return
     */
    private SkuInfoDTO getSkuInfo(Long skuId) {
        // 构造请求参数
        SkuInfoQueryRequest skuInfoQueryRequest = SkuInfoQueryRequest.builder()
                .skuId(skuId)
                .build();
        // 调用商品服务获取sku
        JsonResult<SkuInfoDTO> skuInfo = goodsApi.getSkuInfoBySkuId(skuInfoQueryRequest);
        log.info("获取商品数据, SkuInfo: {}", JsonUtil.object2Json(skuInfo));

        if (!skuInfo.getSuccess()) {
            throw new CookbookCartBizException(skuInfo.getErrorCode(), skuInfo.getErrorMessage());
        }
        return skuInfo.getData();
    }

    /**
     * 校验商品是否可售
     *
     * @return
     */
    private void checkSellableProduct(CartSkuInfoDTO cartSkuInfoDTO) {
        // 检查购物车商品数量是否达到上限
        checkCartProductThreshold(cartSkuInfoDTO.getUserId());

        // 获取商品可售状态
        Boolean saleable = getSkuSaleableStatus(cartSkuInfoDTO);
        if (!saleable) {
            throw new CookbookCartBizException(CookbookCartErrorCodeEnum.SKU_SELL_STATUS_ERROR,
                    CookbookCartErrorCodeEnum.SKU_SELL_STATUS_ERROR.getErrorCode());
        }
    }

    /**
     * 获取sku可售状态
     *
     * @param cartSkuInfoDTO
     * @return
     */
    private Boolean getSkuSaleableStatus(CartSkuInfoDTO cartSkuInfoDTO) {
        SkuSaleableRequest skuSaleableRequest = SkuSaleableRequest.builder()
                .skuId(cartSkuInfoDTO.getSkuId())
                .warehouse(cartSkuInfoDTO.getWarehouse())
                // 如果选中了，需要判断商品数量是否充足
                .count(YesOrNoEnum.YES.getCode().equals(cartSkuInfoDTO.getCheckStatus()) ? cartSkuInfoDTO.getCount(): null)
                .build();

        // 调用商品接口 获取商品可售状态
        JsonResult<Boolean> saleable = goodsApi.skuIsSaleable(skuSaleableRequest);
        log.info("调用商品接口获取商品可售状态, skuId: {}, saleable: {}", cartSkuInfoDTO.getSkuId(), JsonUtil.object2Json(saleable));
        if (!saleable.getSuccess()) {
            throw new CookbookCartBizException(saleable.getErrorCode(), saleable.getErrorMessage());
        }
        return saleable.getData();
    }

    /**
     * 校验购物车商品数量是否超过阈值
     *
     * @param userId
     */
    private void checkCartProductThreshold(Long userId) {
        // 从缓存中获取当前购物车sku数量
        // hash:{skuId->count, skuId->count}，hLen命令，就会拿到hash结构里有多少个数据条目
        // 购物车里加入了多少个商品，购物车里能加入多少商品，都是有限制的
        Long len = redisCache.hLen(RedisKeyConstant.SHOPPING_CART_HASH + userId);
        if (len >= CookbookCartConstants.CART_DEFAULT_MAX_SKU_COUNT) {
            throw new CookbookCartBizException(CookbookCartErrorCodeEnum.CART_SKU_COUNT_THRESHOLD_ERROR,
                    CookbookCartErrorCodeEnum.CART_SKU_COUNT_THRESHOLD_ERROR.getErrorCode());
        }
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public CookBookCartInfoDTO updateCartGoods(UpdateCookBookCartRequest request) {
        String updateCartLockKey = RedisLockKeyConstants.UPDATE_CART_LOCK_KEY + request.getUserId() + ":" + request.getSkuId();
        boolean locked = redisLock.blockedLock(updateCartLockKey);

        if(!locked) {
            throw new BaseBizException("商品加入购物车失败");
        }

        try {
            // 获取购物车中的商品
            CartSkuInfoDTO cartSkuInfoDTO = getCartSkuInfoDTO(request);
            // 校验商品可售状态：库存
            checkSellableProduct(cartSkuInfoDTO);
            if (request.getCount() == 0) {
                // 删除商品缓存
                clearCartCache(cartSkuInfoDTO);
                // 发MQ持久化到MySQL
                sendAsyncUpdateMessage(cartSkuInfoDTO);
                // 返回空数据
                return CookBookCartInfoDTO.builder().build();
            }
            // 更新缓存
            updateCartCache(cartSkuInfoDTO);
            // 发MQ持久化到MySQL
            sendAsyncUpdateMessage(cartSkuInfoDTO);
        } finally {
            redisLock.unlock(updateCartLockKey);
        }
        // 返回购物车数据
        return queryCart(request.getUserId());
    }

    /**
     * 更新购物车请求数量为0，删除缓存
     * @param cartSkuInfoDTO
     */
    private void clearCartCache(CartSkuInfoDTO cartSkuInfoDTO) {
        Long userId = cartSkuInfoDTO.getUserId();
        String skuId = String.valueOf(cartSkuInfoDTO.getSkuId());
        redisCache.hDel(RedisKeyConstant.SHOPPING_CART_HASH + userId, skuId);
        redisCache.hDel(RedisKeyConstant.SHOPPING_CART_EXTRA_HASH + userId, skuId);
        redisCache.zremove(RedisKeyConstant.SHOPPING_CART_ZSET + userId, skuId);
    }

    /**
     * 获取购物车中的商品
     *
     * @param request
     * @return
     */
    private CartSkuInfoDTO getCartSkuInfoDTO(UpdateCookBookCartRequest request) {
        CartSkuInfoDTO cartSkuInfoDTO = cookbookCartConverter.requestToDTO(request);
        // 从缓存中获取购物车商品
        CartSkuInfoDTO oldCartSkuInfoDTO = getCartSkuInfoDTOFromCache(cartSkuInfoDTO);
        // 查询设置价格
        oldCartSkuInfoDTO.setCount(request.getCount());
        return oldCartSkuInfoDTO;
    }

    private CartSkuInfoDTO getCartSkuInfoDTOFromCache(CartSkuInfoDTO cartSkuInfoDTO) {
        // 获取购物车中的商品
        Object extra = redisCache.hGet(RedisKeyConstant.SHOPPING_CART_EXTRA_HASH + cartSkuInfoDTO.getUserId(),
                String.valueOf(cartSkuInfoDTO.getSkuId()));
        // 购物车没有这个商品
        if (Objects.isNull(extra)) {
            throw new CookbookCartBizException(CookbookCartErrorCodeEnum.SKU_NOT_EXIST_CART_ERROR,
                    CookbookCartErrorCodeEnum.SKU_NOT_EXIST_CART_ERROR.getErrorCode());
        }

        String cartSkuInfoString = extra.toString();
        CartSkuInfoDTO oldCartSkuInfoDTO = JsonUtil.json2Object(cartSkuInfoString, CartSkuInfoDTO.class);
        // 购物车没有这个商品
        if (Objects.isNull(oldCartSkuInfoDTO)) {
            throw new CookbookCartBizException(CookbookCartErrorCodeEnum.SKU_NOT_EXIST_CART_ERROR,
                    CookbookCartErrorCodeEnum.SKU_NOT_EXIST_CART_ERROR.getErrorCode());
        }
        return oldCartSkuInfoDTO;
    }


    /**
     * 发送更新购物车的商品消息到MQ
     *
     * @param cartSkuInfoDTO 新的商品
     */
    private void sendAsyncUpdateMessage(CartSkuInfoDTO cartSkuInfoDTO) {
        // 需要落库的购物车实体对象
        CookBookCartDO cartDO = cookbookCartConverter.dtoToDO(cartSkuInfoDTO);

        // 发送消息到MQ
        log.info("发送更新购物车消息到MQ, topic: {}, cartSkuInfo: {}", RocketMqConstant.COOKBOOK_ASYNC_UPDATE_MESSAGE_SEND_TOPIC, JsonUtil.object2Json(cartDO));
        defaultProducer.sendMessage(RocketMqConstant.COOKBOOK_ASYNC_UPDATE_MESSAGE_SEND_TOPIC,
                JsonUtil.object2Json(cartDO), "COOKBOOK购物车异步更新消息");
    }

    /**
     * 查询购物车
     *
     *
     * 这里的流程是这样的：
     *
     * 1. 如果缓存中存在，那么就从缓存中查询到后返回
     *
     * 2. 如果缓存中不存在，那么就添加分布式锁，然后再查询MySQL，查询到数据后更新到缓存中，最后返回
     *
     * 3. 如果缓存、MySQL都不存在，那么就再查询MySQL后，给缓存设置一个空值，设置一个随机的过期时间，最后返回一个空数据
     * 当下次再来查询购物车时，会先判断缓存中的空值是否存在，如果存在就不查数据库了
     *
     * @param userId
     * @return
     */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public CookBookCartInfoDTO queryCart(Long userId) {
        // 检查入参
        checkParams(userId);
        // 从缓存中获取购物车数据
        CookBookCartInfoDTO cartDTO = queryCartByCache(userId);
        // 如果缓存中没有就从数据库中获取
        return Objects.nonNull(cartDTO) ?
                cartDTO :
                queryCartNoCache(userId);
    }

    /**
     * 从数据库中获取购物车数据
     *
     * @param userId
     * @param userId
     * @return
     */
    private CookBookCartInfoDTO queryCartNoCache(Long userId) {
        // 判断是否存在空缓存
        String emptyKey = RedisKeyConstant.SHOPPING_CART_EMPTY + userId;
        if (redisCache.hasKey(emptyKey)) {
            log.warn("购物车查询到空缓存，禁止查询MySQL, key: {}", emptyKey);
            return CookBookCartInfoDTO.builder().build();
        }

        List<CartSkuInfoDTO> cartInfoDTOs;
        String key = RedisLockKeyConstants.SHOPPING_CART_PERSISTENCE_KEY + userId;
        try {
            boolean lock = redisLock.lock(key);
            if (!lock) {
                throw new CookbookCartBizException(CookbookCartErrorCodeEnum.CART_PERSISTENCE_ERROR,
                        CookbookCartErrorCodeEnum.CART_PERSISTENCE_ERROR.getErrorCode());
            }
            log.warn("购物车缓存数据查询为空, 添加分布式锁查询MySQL, userId: {}", userId);
            // 从数据库中查询到购物车的商品集合
            cartInfoDTOs = getCartDTOFromPersistence(userId);
            // 更新Redis缓存中的购物车商品
            syncCacheFromPersistence(userId, cartInfoDTOs);
        } finally {
            redisLock.unlock(key);
        }
        // 构造购物车返回值
        return buildCookbookCartInfoDTO(userId, cartInfoDTOs);
    }

    /**
     * 构造购物车返回值
     *
     * @param cartInfoDTOs
     * @return
     */
    private CookBookCartInfoDTO buildCookbookCartInfoDTO(Long userId, List<CartSkuInfoDTO> cartInfoDTOs) {
        // 未失效的商品列表
        List<CartSkuInfoDTO> skuList = new ArrayList<>();
        // 失效的商品列表
        List<CartSkuInfoDTO> disabledSkuList = new ArrayList<>();
        // 拆分为未失效的和失效的
        splitCartSkuList(cartInfoDTOs, skuList, disabledSkuList);
        // 计算价格数据
        BillingDTO billingDTO = calculateCartPriceByCoupon(userId, skuList);
        // 返回购物车DTO
        return CookBookCartInfoDTO.builder()
                .skuList(skuList)
                .disabledSkuList(disabledSkuList)
                .billing(billingDTO)
                .build();
    }

    /**
     * 将购物车商品列表拆分为失效的和未失效的商品列表
     *
     * @param totalSkuList
     * @param skuList
     * @param disabledSkuList
     */
    private void splitCartSkuList(List<CartSkuInfoDTO> totalSkuList, List<CartSkuInfoDTO> skuList, List<CartSkuInfoDTO> disabledSkuList) {
        for (CartSkuInfoDTO cartSkuInfoDTO : totalSkuList) {
            // 商品校验
            Boolean saleable = getSkuSaleableStatus(cartSkuInfoDTO);
            if (saleable) {
                skuList.add(cartSkuInfoDTO);
            } else {
                disabledSkuList.add(cartSkuInfoDTO);
            }
        }
    }

    /**
     * 获取skuInfo集合
     *
     * @param cartDOs
     * @return
     */
    private List<SkuInfoDTO> getSkuInfoList(List<CookBookCartDO> cartDOs) {
        return cartDOs.stream()
                .map(cookBookCartDO -> getSkuInfo(cookBookCartDO.getSkuId()))
                .collect(Collectors.toList());
    }

    /**
     * 从数据库中查询出有序的购物车商品集合
     *
     * @param userId
     * @return
     */
    private List<CartSkuInfoDTO> getCartDTOFromPersistence(Long userId) {
        // 构造查询条件
        LambdaQueryWrapper<CookBookCartDO> queryWrapper = Wrappers.lambdaQuery();
        queryWrapper.eq(CookBookCartDO::getUserId, userId);
        queryWrapper.orderByDesc(CookBookCartDO::getUpdateTime);
        // 查询用户购物车数据
        List<CookBookCartDO> cartDOs = cookBookCartDAO.list(queryWrapper);
        log.info("从MySQL中查询购物车数据, cartDOs: {}", JsonUtil.object2Json(cartDOs));

        // 商品DO 转 商品DTO
        return cartDOs.stream().map(cookBookCartDO -> {
            SkuInfoDTO skuInfo = getSkuInfo(cookBookCartDO.getSkuId());

            return CartSkuInfoDTO.builder()
                    .skuId(cookBookCartDO.getSkuId())
                    .count(cookBookCartDO.getCount())
                    .checkStatus(cookBookCartDO.getCheckStatus())
                    .updateTime(cookBookCartDO.getUpdateTime())
                    .price(cookBookCartDO.getAddAmount())
                    .title(skuInfo.getSkuName())
                    .image(skuInfo.getSkuImage())
                    .build();
        }).collect(Collectors.toList());
    }

    /**
     * 更新Redis缓存
     * @param userId
     * @param cartSkuInfoDTOs
     */
    private void syncCacheFromPersistence(Long userId, List<CartSkuInfoDTO> cartSkuInfoDTOs) {
        // 如果数据库中也没有查到
        if (Objects.isNull(cartSkuInfoDTOs) || CollectionUtils.isEmpty(cartSkuInfoDTOs)) {
            String shoppingEmptyKey = RedisKeyConstant.SHOPPING_CART_EMPTY + userId;
            String shoppingEmptyValue = CookbookCartConstants.EMPTY_CACHE_IDENTIFY;
            int expireTime = RandomUtil.genRandomInt(30, 100);
            // 在Redis中写入空缓存
            redisCache.set(shoppingEmptyKey, shoppingEmptyValue, expireTime);
            log.warn("购物车和缓存中都没有查到请求的购物车数据, 写入空缓存, key: {}, value: {}, expire: {}秒",
                    shoppingEmptyKey, shoppingEmptyValue, expireTime);
            return;
        }

        // 数量信息hash
        Map<String, String> cartNums = new HashMap<>();
        // 扩展信息hash
        Map<String, String> cartExtras = new HashMap<>();

        // 遍历购物车商品
        for (CartSkuInfoDTO cartSkuInfoDTO : cartSkuInfoDTOs) {
            // 添加sku数量信息
            String skuId = String.valueOf(cartSkuInfoDTO.getSkuId());
            cartNums.put(skuId, String.valueOf(cartSkuInfoDTO.getCount()));
            // 添加sku扩展信息
            cartExtras.put(skuId, JsonUtil.object2Json(cartSkuInfoDTO));

            // 存储购物车sku顺序缓存
            String orderKey = RedisKeyConstant.SHOPPING_CART_ZSET + userId;
            redisCache.zadd(orderKey, skuId, cartSkuInfoDTO.getUpdateTime().getTime());
            log.info("从MySQL中查到购物车数据, 写入zset缓存, key: {}", orderKey);
        }

        // 存储购物车sku数量缓存
        String numKey = RedisKeyConstant.SHOPPING_CART_HASH + userId;
        redisCache.hPutAll(numKey, cartNums);
        log.info("从MySQL中查到购物车数据, 写入缓存, key: {}, value: {}", numKey, JsonUtil.object2Json(cartNums));

        // 存储购物车sku扩展信息缓存
        String extraKey = RedisKeyConstant.SHOPPING_CART_EXTRA_HASH + userId;
        redisCache.hPutAll(extraKey, cartExtras);
        log.info("从MySQL中查到购物车数据, 写入缓存, key: {}, value: {}", numKey, JsonUtil.object2Json(cartNums));
    }

    /**
     * 从缓存中获取购物车数据
     *
     * @param userId
     * @return
     */
    private CookBookCartInfoDTO queryCartByCache(Long userId) {
        // 从缓存中查询出有序的购物车商品集合
        List<CartSkuInfoDTO> totalSkuList = getCartInfoDTOFromCache(userId);
        // 如果缓存中没有就返回null
        if (totalSkuList.size() == 0) {
            return null;
        }
        // 未失效的商品列表
        List<CartSkuInfoDTO> skuList = new ArrayList<>();
        // 失效的商品列表
        List<CartSkuInfoDTO> disabledSkuList = new ArrayList<>();
        // 拆分购物车商品列表为：失效的商品列表、未失效的商品列表
        splitCartSkuList(totalSkuList, skuList, disabledSkuList);

        // 根据未失效的商品列表计算结算价格
        BillingDTO billingDTO = calculateCartPriceByCoupon(userId, skuList);

        // 返回购物车数据结构
        return CookBookCartInfoDTO.builder()
                .skuList(skuList)
                .disabledSkuList(disabledSkuList)
                .billing(billingDTO)
                .build();
    }

    /**
     * 从缓存中查询出有序购物车商品集合
     *
     * @param userId
     * @return
     */
    @SuppressWarnings("unchecked")
    private List<CartSkuInfoDTO> getCartInfoDTOFromCache(Long userId) {
        // 从缓存中获取有序的商品ID列表
        // 把sorted set里所有的数据都查出来，写入数据的时候默认就已经排序过了
        Set<String> orderSkuIds = redisCache.zrevrange(RedisKeyConstant.SHOPPING_CART_ZSET + userId,
                CookbookCartConstants.ZSET_ALL_RANGE_START_INDEX, CookbookCartConstants.ZSET_ALL_RANGE_END_INDEX);

        // 从缓存中获取商品扩展信息
        Map<String, String> cartInfo = redisCache.hGetAll(RedisKeyConstant.SHOPPING_CART_EXTRA_HASH + userId);

        // 遍历有序的商品ID列表，获取商品信息集合
        return orderSkuIds.stream()
                .filter(StringUtils::isNotEmpty)
                // 过滤空key标识
                .filter(skuId -> !skuId.equals(CookbookCartConstants.EMPTY_CACHE_IDENTIFY))
                .map(skuId -> JsonUtil.json2Object(cartInfo.get(skuId), CartSkuInfoDTO.class))
                .collect(Collectors.toList());
    }

    /**
     * 价格结算
     *
     * @param userId  用户ID
     * @param skuList 未失效的商品
     * @return
     */
    private BillingDTO calculateCartPriceByCoupon(Long userId, List<CartSkuInfoDTO> skuList) {
        // 计算商品总价格（未减免优惠券）
        Integer totalPrice = calculateCartPrice(skuList);
        // 获取优惠金额
        Integer couponAmount = getCouponAmount(userId, totalPrice);

        // 构造结算信息DTO
        return BillingDTO.builder()
                .totalPrice(totalPrice - couponAmount)
                .salePrice(couponAmount)
                .build();
    }

    /**
     * 获取优惠金额
     *
     * @param userId
     * @param totalPrice
     * @return
     */
    private Integer getCouponAmount(Long userId, Integer totalPrice) {
        // 如果购物车金额为0
        if (totalPrice == 0) {
            return 0;
        }

        // 调用优惠服务匹配最优优惠券
        SelectedOptimalCouponDTO couponDTO = cookbookCouponService.selectedOptimalCoupon(userId, totalPrice);
        if (Objects.isNull(couponDTO)) {
            return 0;
        }

        // 返回优惠券金额
        return couponDTO.getCouponAmount();
    }

    /**
     * 计算购物车中选中商品的金额，未减免优惠券
     *
     * @param skuList
     * @return
     */
    private Integer calculateCartPrice(List<CartSkuInfoDTO> skuList) {
        // 获取选中的购物车商品列表
        List<CartSkuInfoDTO> checkedSkuList = skuList.stream()
                .filter(cartSkuInfoDTO -> Objects.nonNull(cartSkuInfoDTO.getCheckStatus()))
                .filter(cartSkuInfoDTO -> cartSkuInfoDTO.getCheckStatus().equals(YesOrNoEnum.YES.getCode()))
                .collect(Collectors.toList());

        // 根据未失效的商品列表计算价格
        Integer totalPrice = 0;
        for (CartSkuInfoDTO skuInfoDTO : checkedSkuList) {
            Integer price = skuInfoDTO.getPrice() * skuInfoDTO.getCount();
            totalPrice += price;
        }
        return totalPrice;
    }

    /**
     * 检查入参
     *
     * @param userId
     */
    private void checkParams(Long userId) {
        if (ObjectUtils.isEmpty(userId)) {
            throw new CookbookCartBizException(CookbookCartErrorCodeEnum.PARAM_ERROR,
                    CookbookCartErrorCodeEnum.PARAM_ERROR.getErrorCode());
        }
    }

    /**
     * 修改选中状态
     * @param request
     * @return
     */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public CookBookCartInfoDTO checkedCartGoods(CheckedCartRequest request) {
        String updateCartLockKey = RedisLockKeyConstants.UPDATE_CART_LOCK_KEY + request.getUserId() + ":" + request.getSkuId();
        boolean locked = redisLock.blockedLock(updateCartLockKey);

        if(!locked) {
            throw new BaseBizException("商品加入购物车失败");
        }

        try {
            // 对象转换，获取购物车商品
            CartSkuInfoDTO cartSkuInfoDTO = getCartSkuInfoDTO(request);
            // 更新商品扩展信息缓存
            updateCartExtraCache(cartSkuInfoDTO);
            // 异步更新MySQL
            sendAsyncUpdateCheckStatusMessage(cartSkuInfoDTO);

        } finally {
            redisLock.unlock(updateCartLockKey);
        }
        // 获取新的购物车数据
        return queryCart(request.getUserId());
    }

    private void sendAsyncUpdateCheckStatusMessage(CartSkuInfoDTO cartSkuInfoDTO) {
        // 需要落库的购物车实体对象
        CookBookCartDO cartDO = cookbookCartConverter.dtoToDO(cartSkuInfoDTO);

        // 发送消息到MQ
        log.info("发送更新选中状态购物车消息到MQ, topic: {}, cartSkuInfo: {}",
                RocketMqConstant.COOKBOOK_ASYNC_CHECKED_STATUS_MESSAGE_SEND_TOPIC, JsonUtil.object2Json(cartDO));
        defaultProducer.sendMessage(RocketMqConstant.COOKBOOK_ASYNC_CHECKED_STATUS_MESSAGE_SEND_TOPIC,
                JsonUtil.object2Json(cartDO), "COOKBOOK购物车异步更新消息");
    }

    /**
     * 获取购物车商品
     * @param request
     * @return
     */
    private CartSkuInfoDTO getCartSkuInfoDTO(CheckedCartRequest request) {
        CartSkuInfoDTO cartSkuInfoDTO = cookbookCartConverter.requestToDTO(request);
        // 从缓存中获取购物车中的商品
        CartSkuInfoDTO oldCartSkuInfoDTO = getCartSkuInfoDTOFromCache(cartSkuInfoDTO);
        // 重新设置选中状态
        oldCartSkuInfoDTO.setCheckStatus(request.getCheckStatus());
        return oldCartSkuInfoDTO;
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public Integer calculateCartPrice(Long userId) {
        // 从缓存中查询出有序的购物车商品集合
        List<CartSkuInfoDTO> totalSkuList = getCartInfoDTOFromCache(userId);
        // 未失效的商品列表
        List<CartSkuInfoDTO> skuList = new ArrayList<>();
        // 拆分购物车商品列表为：未失效的商品列表
        splitCartSkuList(totalSkuList, skuList, null);
        // 根据未失效的商品列表计算商品总价格（不见面优惠券）
        return calculateCartPrice(skuList);
    }
}
