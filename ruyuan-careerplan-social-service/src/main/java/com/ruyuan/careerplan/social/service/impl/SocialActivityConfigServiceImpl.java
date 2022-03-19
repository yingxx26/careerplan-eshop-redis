package com.ruyuan.careerplan.social.service.impl;

import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.ruyuan.careerplan.common.redis.RedisCache;
import com.ruyuan.careerplan.social.constants.SocialConstant;
import com.ruyuan.careerplan.social.dao.SocialActivityConfigDAO;
import com.ruyuan.careerplan.social.domain.dto.SocialActivityConfigDTO;
import com.ruyuan.careerplan.social.domain.entity.SocialActivityConfigDO;
import com.ruyuan.careerplan.social.service.SocialActivityConfigService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Objects;
import java.util.concurrent.TimeUnit;


@Slf4j
@Service
public class SocialActivityConfigServiceImpl implements SocialActivityConfigService {

    @Resource
    private RedisCache redisCache;

    @Resource
    private SocialActivityConfigDAO socialActivityConfigDAO;

    /**
     * 过期时间
     */
    private static final int SECONDS_60 = 60;

    /**
     * 获取社交活动配置
     *
     * @param
     * @return com.ruyuan.careerplan.social.domain.dto.SocialActivityConfigDTO
     * @author zhonghuashishan
     */
    @Override
    public SocialActivityConfigDTO getSocialActivityConfig() {
        String configStr = redisCache.get(SocialConstant.SOCIAL_ACTIVITY_CONFIG);
        SocialActivityConfigDTO socialActivityConfigDTO = JSON.parseObject(configStr, SocialActivityConfigDTO.class);
        if (Objects.nonNull(socialActivityConfigDTO)) {
            return socialActivityConfigDTO;
        }

        // 必须先在数据库中执行活动配置脚本，没有配置意味着活动无法进行
        QueryWrapper<SocialActivityConfigDO> query = new QueryWrapper<>();
        query.eq("ID", 1);
        SocialActivityConfigDO socialActivityConfigDO = socialActivityConfigDAO.getOne(query);
        if (Objects.nonNull(socialActivityConfigDO)) {
            socialActivityConfigDTO = new SocialActivityConfigDTO();
            BeanUtils.copyProperties(socialActivityConfigDO, socialActivityConfigDTO);
            redisCache.setex(SocialConstant.SOCIAL_ACTIVITY_CONFIG, JSON.toJSONString(socialActivityConfigDTO), SECONDS_60, TimeUnit.SECONDS);
        }

        return socialActivityConfigDTO;
    }

}
