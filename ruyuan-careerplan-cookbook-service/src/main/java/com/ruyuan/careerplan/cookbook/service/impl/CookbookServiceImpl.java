package com.ruyuan.careerplan.cookbook.service.impl;

import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.github.benmanes.caffeine.cache.Cache;
import com.jd.platform.hotkey.client.callback.JdHotKeyStore;
import com.ruyuan.careerplan.common.cache.CacheSupport;
import com.ruyuan.careerplan.common.enums.DeleteStatusEnum;
import com.ruyuan.careerplan.common.exception.BaseBizException;
import com.ruyuan.careerplan.common.page.PagingInfo;
import com.ruyuan.careerplan.common.redis.RedisCache;
import com.ruyuan.careerplan.common.redis.RedisLock;
import com.ruyuan.careerplan.common.utils.JsonUtil;
import com.ruyuan.careerplan.cookbook.constants.RedisKeyConstants;
import com.ruyuan.careerplan.cookbook.constants.RocketMqConstant;
import com.ruyuan.careerplan.cookbook.converter.CookbookConverter;
import com.ruyuan.careerplan.cookbook.dao.CookbookDAO;
import com.ruyuan.careerplan.cookbook.dao.CookbookSkuRelationDAO;
import com.ruyuan.careerplan.cookbook.dao.CookbookUserDAO;
import com.ruyuan.careerplan.cookbook.domain.dto.CookbookDTO;
import com.ruyuan.careerplan.cookbook.domain.dto.Food;
import com.ruyuan.careerplan.cookbook.domain.dto.SaveOrUpdateCookbookDTO;
import com.ruyuan.careerplan.cookbook.domain.dto.StepDetail;
import com.ruyuan.careerplan.cookbook.domain.entity.CookbookDO;
import com.ruyuan.careerplan.cookbook.domain.entity.CookbookSkuRelationDO;
import com.ruyuan.careerplan.cookbook.domain.entity.CookbookUserDO;
import com.ruyuan.careerplan.cookbook.domain.request.CookbookQueryRequest;
import com.ruyuan.careerplan.cookbook.domain.request.SaveOrUpdateCookbookRequest;
import com.ruyuan.careerplan.cookbook.message.CookbookUpdateMessage;
import com.ruyuan.careerplan.cookbook.mq.producer.DefaultProducer;
import com.ruyuan.careerplan.cookbook.service.CookbookService;
import com.ruyuan.careerplan.cookbook.service.GoodsService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.Collectors;

import static com.ruyuan.careerplan.common.constants.CoreConstant.REDIS_CONNECTION_FAILED;

/**
 * 菜谱服务
 *
 * @author zhonghuashishan
 */
@Service
@Slf4j
public class CookbookServiceImpl implements CookbookService {

    private static final Long USER_COOKBOOK_LOCK_TIMEOUT = 200L;

    /**
     * 商品修改锁
     */
    private static final Long COOKBOOK_UPDATE_LOCK_TIMEOUT = 200L;

    @Autowired
    private RedisCache redisCache;

    @Autowired
    private RedisLock redisLock;

    @Autowired
    private CookbookDAO cookbookDAO;

    @Autowired
    private CookbookSkuRelationDAO cookbookSkuRelationDAO;

    @Autowired
    private CookbookUserDAO cookbookUserDAO;

    @Autowired
    private CookbookConverter cookbookConverter;

    @Autowired
    private GoodsService goodsService;

    @Autowired
    private DefaultProducer defaultProducer;

    @Autowired
    private Cache<String, Object> caffeineCache;

    private Lock localLock = new ReentrantLock();

    @Transactional(rollbackFor = Exception.class)
    @Override
    public SaveOrUpdateCookbookDTO saveOrUpdateCookbook(SaveOrUpdateCookbookRequest request) {
        String cookbookUpdateLockKey = RedisKeyConstants.COOKBOOK_UPDATE_LOCK_PREFIX + request.getOperator();
        Boolean lock = null;

        if(request.getId() != null && request.getId() > 0) {
            lock = redisLock.lock(cookbookUpdateLockKey);
        }

        if (lock != null && !lock) {
            log.info("操作菜谱获取锁失败，operator:{}", request.getOperator());
            throw new BaseBizException("新增/修改失败");
        }
        try {
            // 构建菜谱信息
            CookbookDO cookbookDO = buildCookbookDO(request);

            // 保存菜谱信息
            // 菜谱 = 美食分享，关于美食、菜品，图，视频，如何做，原材料，信息
            cookbookDAO.saveOrUpdate(cookbookDO);
            // 菜谱商品关联信息，一个菜谱可以种草多个商品，可以保存菜品跟多个商品关联关系
            List<CookbookSkuRelationDO> cookbookSkuRelationDOS = buildCookbookSkuRelationDOS(cookbookDO, request);
            // 保存菜谱商品关联信息
            cookbookSkuRelationDAO.saveBatch(cookbookSkuRelationDOS);

            // 更新缓存信息
            updateCookbookCache(cookbookDO, request);

            // 发布菜谱数据更新事件消息
            publishCookbookUpdatedEvent(cookbookDO);

            // 返回信息
            SaveOrUpdateCookbookDTO dto = SaveOrUpdateCookbookDTO.builder()
                    .success(true)
                    .cookbookId(cookbookDO.getId())
                    .build();
            return dto;
        }finally {
            if(lock != null) {
                redisLock.unlock(cookbookUpdateLockKey);
            }
        }
    }

    private void publishCookbookUpdatedEvent(CookbookDO cookbookDO) {
        // 发消息通知作者的菜谱信息变更
        CookbookUpdateMessage message = CookbookUpdateMessage.builder()
                .cookbookId(cookbookDO.getId())
                .userId(cookbookDO.getUserId())
                .build();
        defaultProducer.sendMessage(RocketMqConstant.COOKBOOK_UPDATE_MESSAGE_TOPIC,
                JsonUtil.object2Json(message), "作者菜谱变更消息");
    }

    private CookbookDO buildCookbookDO(SaveOrUpdateCookbookRequest request) {
        CookbookDO cookbookDO = cookbookConverter.convertCookbookDO(request);
        cookbookDO.setFoods(JsonUtil.object2Json(request.getFoods()));
        cookbookDO.setCookbookDetail(JsonUtil.object2Json(request.getCookbookDetail()));
        cookbookDO.setUpdateUser(request.getOperator());

        // 新增数据
        if (Objects.isNull(cookbookDO.getId())) {
            // 菜谱状态为空，则设置为未删除
            if (Objects.isNull(cookbookDO.getCookbookStatus())) {
                cookbookDO.setCookbookStatus(DeleteStatusEnum.NO.getCode());
            }
            // 设置创建人
            cookbookDO.setCreateUser(request.getOperator());
        }
        return cookbookDO;
    }

    private void updateCookbookCache(CookbookDO cookbookDO, SaveOrUpdateCookbookRequest request) {
        CookbookDTO cookbookDTO = buildCookbookDTO(cookbookDO, request.getSkuIds());

        // 修改菜谱信息缓存数据
        String cookbookKey = RedisKeyConstants.COOKBOOK_PREFIX + cookbookDO.getId();
        redisCache.setCache(cookbookKey, cookbookDTO, CacheSupport.generateCacheExpireSecond());

        // 将作者的菜谱总数信息存储在缓存中
        String userCookbookCountKey = RedisKeyConstants.USER_COOKBOOK_COUNT_PREFIX + cookbookDO.getUserId();
//        LambdaQueryWrapper<CookbookDO> queryWrapper = Wrappers.lambdaQuery();
//        queryWrapper.eq(CookbookDO::getUserId, cookbookDO.getUserId())
//                .eq(CookbookDO::getCookbookStatus, DeleteStatusEnum.NO.getCode());
//        int count = cookbookDAO.count(queryWrapper);
//        redisCache.set(userCookbookCountKey, String.valueOf(count), -1);
        redisCache.increment(userCookbookCountKey, 1);
    }

    private CookbookDTO buildCookbookDTO(CookbookDO cookbookDO, List<Long> skuIds) {
        CookbookDTO cookbookDTO = cookbookConverter.convertCookbookDTO(cookbookDO);
        CookbookUserDO userDO = cookbookUserDAO.getById(cookbookDO.getUserId());
        cookbookDTO.setUserName(userDO.getUserName());
        cookbookDTO.setCookbookDetail(
                JSON.parseArray(cookbookDO.getCookbookDetail(), StepDetail.class));
        cookbookDTO.setFoods(JSON.parseArray(cookbookDO.getFoods(), Food.class));
        cookbookDTO.setSkuIds(skuIds);
        return cookbookDTO;
    }

    private List<CookbookSkuRelationDO> buildCookbookSkuRelationDOS(CookbookDO cookbookDO, SaveOrUpdateCookbookRequest request) {
        /*
         * 这里逻辑为
         * 前端页面选择了菜谱对应的商品，则直接使用所选择的商品，
         * 如果没有选择，则根据食材的标签对应的商品来匹配。
         * 一般情况下，除非是社区电商自己后台人员去创建的菜谱会去关联商品，否则都不会去关联商品。
         * 因为关联商品是自己后台人员需要去关心的事情，用户创建一个菜谱分享出来，并不会去关心对应的商品信息。
         */
        if (Objects.isNull(request.getSkuIds())) {
             List<String> tags = request.getFoods().stream().map(food -> food.getTag()).collect(Collectors.toList());
             List<Long> skuIds = goodsService.getSkuIdsByTags(tags);
             request.setSkuIds(skuIds);
        }

        List<CookbookSkuRelationDO> cookbookSkuRelationDOS = new ArrayList<>();
        for (long skuId : request.getSkuIds()) {
            CookbookSkuRelationDO cookbookSkuRelationDO =
                    buildCookbookSkuRelationDO(cookbookDO.getId(), skuId, request.getOperator());
            cookbookSkuRelationDOS.add(cookbookSkuRelationDO);
        }
        return cookbookSkuRelationDOS;
    }

    /**
     * 构建菜谱商品关联对象
     * @param cookbookId
     * @param skuId
     * @param operator
     * @return
     */
    private CookbookSkuRelationDO buildCookbookSkuRelationDO(Long cookbookId,
                                                             Long skuId,
                                                             Integer operator) {

        CookbookSkuRelationDO cookbookSkuRelationDO = CookbookSkuRelationDO.builder()
                .cookbookId(cookbookId)
                .skuId(skuId)
                .delFlag(DeleteStatusEnum.NO.getCode())
                .createUser(operator)
                .updateUser(operator)
                .build();
        return cookbookSkuRelationDO;
    }

    @Override
    public CookbookDTO getCookbookInfo(CookbookQueryRequest request) {
        Long cookbookId = request.getCookbookId();

        CookbookDTO cookbook = getCookbookFromCache(cookbookId);
        if(cookbook != null) {
            return cookbook;
        }

        // 未在内存和缓存中获取到值，从数据库中获取
        return getCookbookFromDB(cookbookId);
    }

    private CookbookDTO getCookbookFromCache(Long cookbookId) {
        String cookbookKey = RedisKeyConstants.COOKBOOK_PREFIX + cookbookId;
        // 从内存或者缓存中获取数据
        Object cookbookValue = redisCache.getCache(cookbookKey);
        if (Objects.equals(CacheSupport.EMPTY_CACHE, cookbookValue)) {
            // 如果是空缓存，则是防止缓存穿透的，直接返回null
            return new CookbookDTO();
        } else if (cookbookValue instanceof CookbookDTO) {
            // 如果是对象，则是从内存中获取到的数据，直接返回
            return (CookbookDTO) cookbookValue;
        } else if (cookbookValue instanceof String) {
            // 如果是字符串，则是从缓存中获取到的数据，转换成对象之后返回
            CookbookDTO dto = JsonUtil.json2Object((String) cookbookValue, CookbookDTO.class);

            Long expire = redisCache.getExpire(cookbookKey, TimeUnit.SECONDS);
            /*
             * 对于临期缓存两种做法：
             * 1、临期再续期
             * 2、不续期，随机过期时间，过期了直接加锁查库，然后放入缓存
             * 这里采用第一种做法，如果缓存过期时间小于一小时，则重新设置过期时间
             */
            if (expire < CacheSupport.ONE_HOURS_SECONDS) {
                redisCache.expire(cookbookKey, CacheSupport.generateCacheExpireSecond());
            }
            return dto;
        }

        return null;
    }


    private CookbookDTO getCookbookFromLocalCache(Long cookbookId) {
        String cookbookKey = RedisKeyConstants.COOKBOOK_PREFIX + cookbookId;
        // 先查看本地缓存是否有，有就返回，没有，则需要获取锁，然后查询
        Object value = caffeineCache.getIfPresent(cookbookKey);
        log.warn("redis 连接失败，降级处理，从本地缓存中获取数据 key {}，value {}", cookbookKey, value);
        if (value != null) {
            if (CacheSupport.EMPTY_CACHE.equals(value)){
                return null;
            }
            return (CookbookDTO) value;
        }
        // 缓存中没有数据，则查询数据库，并将结果放至数据库
        if (localLock.tryLock()) {
            try {
                log.info("缓存数据为空，从数据库中获取数据，cookbookId:{}", cookbookId);
                CookbookDTO dto = cookbookDAO.getCookbookInfoById(cookbookId);
                caffeineCache.put(cookbookKey, Objects.isNull(dto)? CacheSupport.EMPTY_CACHE : dto);
                return dto;
            }finally {
                localLock.unlock();
            }
        }
        // 如果没能拿到锁，直接抛异常返回
        throw new BaseBizException("系统繁忙，请稍后再试");
    }


    /**
     * 从数据库中获取菜谱信息
     *
     * @param cookbookId
     * @return
     */
    private CookbookDTO getCookbookFromDB(Long cookbookId) {
        // 我们主要针对的是菜谱数据的更新操作
        // 对某个菜谱进行更新操作，同时在读取这个菜谱的详情，缓存过期，锁粒度，其实cookbookId
        String cookbookLockKey = RedisKeyConstants.COOKBOOK_UPDATE_LOCK_PREFIX + cookbookId;
        String cookbookKey = RedisKeyConstants.COOKBOOK_PREFIX + cookbookId;
        // 如果redis连接失败，降级为从本地缓存获取，本地获取锁
        if (Objects.nonNull(JdHotKeyStore.get(REDIS_CONNECTION_FAILED))) {
            return getCookbookFromLocalCache(cookbookId);
        }

        // 以上是降级流程，这里是正常流程，使用redisson获取锁
        boolean lock = false;
        try {
            lock = redisLock.tryLock(cookbookLockKey, COOKBOOK_UPDATE_LOCK_TIMEOUT);
        } catch(InterruptedException e) {
            CookbookDTO cookbook = getCookbookFromCache(cookbookId);
            if(cookbook != null) {
                return cookbook;
            }

            log.error(e.getMessage(), e);
        }

        if (!lock) {
            CookbookDTO cookbook = getCookbookFromCache(cookbookId);
            if(cookbook != null) {
                return cookbook;
            }

            log.info("缓存数据为空，从数据库查询菜谱信息时获取锁失败，cookbookId:{}", cookbookId);
            throw new BaseBizException("查询失败");
        }

        try {
            CookbookDTO cookbook = getCookbookFromCache(cookbookId);
            if(cookbook != null) {
                return cookbook;
            }

            log.info("缓存数据为空，从数据库中获取数据，cookbookId:{}", cookbookId);
            CookbookDTO dto = cookbookDAO.getCookbookInfoById(cookbookId);
            if (Objects.isNull(dto)) {
                redisCache.setCache(cookbookKey, CacheSupport.EMPTY_CACHE, CacheSupport.generateCachePenetrationExpireSecond());
                return null;
            }

            redisCache.setCache(cookbookKey, dto, CacheSupport.generateCacheExpireSecond());
            return dto;
        } finally {
            redisLock.unlock(cookbookLockKey);
        }
    }

    @Override
    public PagingInfo<CookbookDTO> listCookbookInfo(CookbookQueryRequest request) {
        // 从redis获取
//        String userCookbookKey = RedisKeyConstants.USER_COOKBOOK_PREFIX + request.getUserId();

        // redis的list类型的数据结构，lrange针对list类型的数据结构，范围查询，指定key，起始位置，每页数据量
        // 把一页数据给查出来
//        List<String> cookbookDTOJsonString =
//                redisCache.lRange(userCookbookKey,
//                        (request.getPageNo() - 1) * request.getPageSize(), request.getPageSize());

//        log.info("从缓存中获取菜谱信息列表,request:{},value:{}", request, JsonUtil.object2Json(cookbookDTOS));

        // 前端界面里，随意进行跳转，查第几页都可以，不用说一定按页顺序来查询
        PagingInfo<CookbookDTO> page = listCookbookInfoFromCache(request);
        if(page != null) {
            return page;
        }

        return listCookbookInfoFromDB(request);
    }

    private PagingInfo<CookbookDTO> listCookbookInfoFromCache(CookbookQueryRequest request) {
        String userCookbookPageKey = RedisKeyConstants.USER_COOKBOOK_PAGE_PREFIX
                + request.getUserId() + ":" + request.getPageNo();
        String cookbooksJSON = redisCache.get(userCookbookPageKey);

        if (cookbooksJSON != null && !"".equals(cookbooksJSON)) {
//            Long size = redisCache.lsize(userCookbookKey);
            String userCookbookCountKey = RedisKeyConstants.USER_COOKBOOK_COUNT_PREFIX + request.getUserId();
            Long size = Long.valueOf(redisCache.get(userCookbookCountKey));
            List<CookbookDTO> cookbookDTOS = JSON.parseObject(cookbooksJSON, List.class);

            Long expire = redisCache.getExpire(userCookbookPageKey, TimeUnit.SECONDS);
            /*
             * 对于临期缓存两种做法：
             * 1、临期再续期
             * 2、不续期，随机过期时间，过期了直接加锁查库，然后放入缓存
             * 这里采用第一种做法，如果缓存过期时间小于一小时，则重新设置过期时间
             */
            if (expire < CacheSupport.ONE_HOURS_SECONDS) {
                redisCache.expire(userCookbookPageKey, CacheSupport.generateCacheExpireSecond());
            }

            return PagingInfo.toResponse(cookbookDTOS, size, request.getPageNo(), request.getPageSize());
        }

        return null;
    }


    private PagingInfo<CookbookDTO> listCookbookInfoFromDB(CookbookQueryRequest request) {
        String userCookbookLockKey = RedisKeyConstants.USER_COOKBOOK_PREFIX + request.getUserId();
        boolean lock = false;

        try {
            lock = redisLock.tryLock(userCookbookLockKey, USER_COOKBOOK_LOCK_TIMEOUT);
        } catch(InterruptedException e) {
            PagingInfo<CookbookDTO> page = listCookbookInfoFromCache(request);
            if(page != null) {
                return page;
            }

            log.error(e.getMessage(), e);
        }

        if (!lock) {
            PagingInfo<CookbookDTO> page = listCookbookInfoFromCache(request);
            if(page != null) {
                return page;
            }

            log.info("缓存数据为空，从数据库查询用户菜谱信息时获取锁失败，userId:{}", request.getUserId());
            throw new BaseBizException("查询失败");
        }

        try {
            PagingInfo<CookbookDTO> page = listCookbookInfoFromCache(request);
            if(page != null) {
                return page;
            }

            log.info("缓存数据为空，从数据库中获取数据，request:{}", request);

            LambdaQueryWrapper<CookbookDO> queryWrapper = Wrappers.lambdaQuery();
            queryWrapper.eq(CookbookDO::getUserId, request.getUserId());
            int count = cookbookDAO.count(queryWrapper);

            // 这里是从db里查到的一页数据
            List<CookbookDTO> cookbookDTOS =
                    cookbookDAO.pageByUserId(request.getUserId(), request.getPageNo(), request.getPageSize());

            // 基于redis的list数据结构，rpush，lrange
            // 把你的用户发布过这一页数据，给怼到list数据结构里去
            // 此时在list缓存里，仅仅只有第一页的数据而已，惰性分页list缓存构建
//            redisCache.rPushAll(userCookbookKey, JsonUtil.listObject2ListJson(cookbookDTOS));

            // 第一页的page缓存是没有包含刚才写入最新数据，旧数据
            // 数据库和缓存不一致了
            // 2天多之内，有人访问第一个page，缓存，读到的都是旧数据，没包含你最新发布的新数据
            String userCookbookPageKey = RedisKeyConstants.USER_COOKBOOK_PAGE_PREFIX
                    + request.getUserId() + ":" + request.getPageNo();
            redisCache.set(userCookbookPageKey,
                    JsonUtil.object2Json(cookbookDTOS),
                    CacheSupport.generateCacheExpireSecond());

            PagingInfo<CookbookDTO> pagingInfo =
                    PagingInfo.toResponse(cookbookDTOS, (long) count, request.getPageNo(), request.getPageSize());

            return pagingInfo;
        } finally {
            redisLock.unlock(userCookbookLockKey);
        }
    }
}
