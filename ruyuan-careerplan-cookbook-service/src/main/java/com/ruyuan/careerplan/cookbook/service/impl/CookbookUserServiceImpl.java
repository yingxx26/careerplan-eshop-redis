package com.ruyuan.careerplan.cookbook.service.impl;

import com.ruyuan.careerplan.common.cache.CacheSupport;
import com.ruyuan.careerplan.common.exception.BaseBizException;
import com.ruyuan.careerplan.common.redis.RedisCache;
import com.ruyuan.careerplan.common.redis.RedisLock;
import com.ruyuan.careerplan.common.utils.JsonUtil;
import com.ruyuan.careerplan.cookbook.constants.RedisKeyConstants;
import com.ruyuan.careerplan.cookbook.converter.CookbookUserConverter;
import com.ruyuan.careerplan.cookbook.dao.CookbookUserDAO;
import com.ruyuan.careerplan.cookbook.domain.dto.CookbookUserDTO;
import com.ruyuan.careerplan.cookbook.domain.dto.SaveOrUpdateUserDTO;
import com.ruyuan.careerplan.cookbook.domain.entity.CookbookUserDO;
import com.ruyuan.careerplan.cookbook.domain.request.CookbookUserQueryRequest;
import com.ruyuan.careerplan.cookbook.domain.request.SaveOrUpdateUserRequest;
import com.ruyuan.careerplan.cookbook.service.CookbookUserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.Objects;
import java.util.concurrent.TimeUnit;

/**
 * 菜谱作者服务
 *
 * @author zhonghuashishan
 */
@Slf4j
@Service
public class CookbookUserServiceImpl implements CookbookUserService {

    private static final long USER_UPDATE_LOCK_TIMEOUT = 200;

    @Autowired
    private CookbookUserDAO cookbookUserDAO;

    @Autowired
    private CookbookUserConverter cookbookUserConverter;

    @Autowired
    private RedisCache redisCache;

    @Autowired
    private RedisLock redisLock;

    @Override
    public SaveOrUpdateUserDTO saveOrUpdateUser(SaveOrUpdateUserRequest request) {
        // 加入用户，要先取得一把分布式锁，针对的是操作人
        // 同一个操作人，同时间只能新增用户，避免说重复请求短时间内发生，数据重复灌入
        // 加分布式锁
        String userUpdateLockKey = RedisKeyConstants.USER_UPDATE_LOCK_PREFIX + request.getOperator();
        boolean lock = redisLock.lock(userUpdateLockKey);

        if (!lock) {
            log.info("操作作者信息获取锁失败，operator:{}", request.getOperator());
            throw new BaseBizException("新增/修改失败");
        }
        try {
            // 这个时候有一个线程来更新了，先写了db，此时db已经成了新数据
            CookbookUserDO cookbookUserDO = cookbookUserConverter.convertCookbookUserDO(request);
            cookbookUserDO.setUpdateUser(request.getOperator());
            if (Objects.isNull(cookbookUserDO.getId())) {
                cookbookUserDO.setCreateUser(request.getOperator());
            }
            cookbookUserDAO.saveOrUpdate(cookbookUserDO);

            CookbookUserDTO cookbookUserDTO = cookbookUserConverter.convertCookbookUserDTO(cookbookUserDO);

            // 要去把我们的社区电商APP用户，把他的数据在redis缓存里去写一份
            // 生成随机的过期时间，把用户数据写入到缓存里去，每条用户数据都会在redis里缓存个2天多
            // 用户数据后续高并发读取的时候，就是可以直接从缓存里提取用户数据，用户数据一般是不会变化的
            // 第一次注册，偶尔修改，写少读多，写0.01%，读99.99%，非常适合用缓存来支持用户数据高并发的读取的
            // 设置过期时间，用户数据可能不是说经常会被读取的，有些用户是冷门个人用户，他发表的东西，一般不会有很多人来看
            // 默认来说，2天多过期掉，如果后面有人要访问，缓存里没找到数据，从库里加载出来写缓存就可以了
            // 不断的延长他的过期时间也是可以的

            // 同时这个线程在这里，把新数据写入了redis缓存里，此时我认为没问题的，缓存是最新的
            redisCache.setCache(RedisKeyConstants.USER_INFO_PREFIX + cookbookUserDO.getId(), cookbookUserDTO,
                    CacheSupport.generateCacheExpireSecond());

            SaveOrUpdateUserDTO dto = SaveOrUpdateUserDTO.builder()
                    .userId(cookbookUserDO.getId())
                    .success(true)
                    .build();
            return dto;
        } finally {
            redisLock.unlock(userUpdateLockKey);
        }
    }

    @Override
    public CookbookUserDTO getUserInfo(CookbookUserQueryRequest request) {
        // 如果说在读操作的入口这里加，分布式锁，非常不靠谱的
        // 没有必要，纯粹就是大量的高并发的，多线程的，直接对redis发起一个读操作
        // 无非就是多个线程并发的去expire时间延期，不改变我们的数据，不属于缓存写操作，缓存元数据（expire time）的更新
        // 坑，大量的线程串行化在之类读redis，没有必要，导致你的性能巨差

        Long userId = request.getUserId();

        CookbookUserDTO user = getUserFromCache(userId);
        if (user != null) {
            return user;
        }

        // 未在内存和缓存中获取到值，从数据库中获取
        return getUserInfoFromDB(userId);
    }


    private CookbookUserDTO getUserFromCache(Long userId) {
        String userInfoKey = RedisKeyConstants.USER_INFO_PREFIX + userId;
        // 从内存或者缓存中获取数据
        Object userInfoValue = redisCache.getCache(userInfoKey);

        if (Objects.equals(CacheSupport.EMPTY_CACHE, userInfoValue)) {
            // 如果是空缓存，则是防止缓存穿透的，返回空对象
            return new CookbookUserDTO();
        } else if (userInfoValue instanceof CookbookUserDTO) {
            // 如果是对象，则是从内存中获取到的数据，直接返回
            return (CookbookUserDTO) userInfoValue;
        } else if (userInfoValue instanceof String) {
            // 如果是字符串，则是从缓存中获取到的数据，转换成对象之后返回

            Long expire = redisCache.getExpire(userInfoKey, TimeUnit.SECONDS);
            /*
             * 对于临期缓存两种做法：
             * 1、临期再续期
             * 2、不续期，随机过期时间，过期了直接加锁查库，然后放入缓存
             * 这里采用第一种做法，如果缓存过期时间小于一小时，则重新设置过期时间
             */
            if (expire < CacheSupport.ONE_HOURS_SECONDS) {
                redisCache.expire(userInfoKey, CacheSupport.generateCacheExpireSecond());
            }
            return JsonUtil.json2Object((String) userInfoValue, CookbookUserDTO.class);
        }

        return null;
    }

    private CookbookUserDTO getUserInfoFromDB(Long userId) {
        // 有两个选择，load from db + write redis，加两把锁，user_lock，user_update_lock
        // 基于redisson，加多锁，multi lock
        // 共用一把锁，multi lock加锁，不同的锁应对的是不同的并发场景

//        String userLockKey = RedisKeyConstants.USER_LOCK_PREFIX + userId;

        // 有大量的线程突然读一个冷门的用户数据，都囤积在这里，在上面大家都没读到
        // 都在这个地方在排队等待获取锁，然后去尝试load db + write redis
        // 非常严重的锁竞争的问题，线程，串行化，一个一个的排队，一个人先拿锁，load一次db，写缓存
        // 下一个人拿到锁了，通过double check，直接读缓存，下一个人，短时间内突然有一个严重串行化，虽然每次读缓存，时间不多

        // 其实只要有第一个人，能够拿到锁，进去，laod db + wreite redis，redis里就已经有数据了
        // 后续的线程就不需要通过锁排队，串行化，一个一个load redis里的数据
        // 只要有一个人能够成功，其他的人，其实可以突然之间全部转换为上面的操作，无锁的情况下，大量的一起并发的读redis就可以了

        String userLockKey = RedisKeyConstants.USER_UPDATE_LOCK_PREFIX + userId;
        boolean lock = false;
        try {
            lock = redisLock.tryLock(userLockKey, USER_UPDATE_LOCK_TIMEOUT);
        } catch(InterruptedException e) {
            CookbookUserDTO user = getUserFromCache(userId);
            if(user != null) {
                return user;
            }
            log.error(e.getMessage(), e);
            throw new BaseBizException("查询失败");
        }

        if (!lock) {
            CookbookUserDTO user = getUserFromCache(userId);
            if(user != null) {
                return user;
            }
            log.info("缓存数据为空，从数据库查询作者信息时获取锁失败，userId:{}", userId);
            throw new BaseBizException("查询失败");
        }

        try {
            CookbookUserDTO user = getUserFromCache(userId);
            if(user != null) {
                return user;
            }

            log.info("缓存数据为空，从数据库中获取数据，userId:{}", userId);

            String userInfoKey = RedisKeyConstants.USER_INFO_PREFIX + userId;

            // 在这里先读到了db里的用户信息的旧数据
            // 这个线程刚刚读到，还没有来得及把旧数据写入缓存里去
            CookbookUserDO cookbookUserDO = cookbookUserDAO.getById(userId);
            if (Objects.isNull(cookbookUserDO)) {
                redisCache.setCache(userInfoKey, CacheSupport.EMPTY_CACHE,
                        CacheSupport.generateCachePenetrationExpireSecond());
                return null;
            }

            CookbookUserDTO dto = cookbookUserConverter.convertCookbookUserDTO(cookbookUserDO);

            // 此时这个线程，在上面的那个线程都已经把新数据写入缓存里去了，缓存里已经是最新数据了
            // 把旧数据库，写入了缓存做了一个覆盖操作，典型的，数据库+缓存双写的时候，写和读，并发的时候
            // db里是新数据，缓存里是旧数据，旧数据是覆盖了新数据的
            // db和缓存，数据是不一致的
            redisCache.setCache(userInfoKey, JsonUtil.object2Json(dto), CacheSupport.generateCacheExpireSecond());
            return dto;
        } finally {
            redisLock.unlock(userLockKey);
        }
    }
}
