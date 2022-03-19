package com.ruyuan.careerplan.home.service.impl;

import com.google.common.collect.Lists;
import com.ruyuan.careerplan.home.domain.dto.RecommendCookbookDTO;
import com.ruyuan.careerplan.home.domain.request.CookbookViewRequestDTO;
import com.ruyuan.careerplan.home.service.HomeCommonService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * 此服务模拟各个中台提供接口，调用后返回业务结果
 *
 * @author zhonghuashishan
 */
@Slf4j
@Service
public class HomeCommonServiceImpl implements HomeCommonService {

    /**
     * 用户中台：用户授权的AccessToken对应用户ID
     */
    private static Map<String, Long> accessTokenUserMap = new HashMap<String, Long>() {{
        put("accesstokenaaa", 1001L);
        put("accesstokenbbb", 1002L);
        put("accesstokenccc", 1003L);
        put("accesstokenddd", 1004L);
        put("accesstokeneee", 1005L);
        put("accesstokenfff", 1006L);
        put("accesstokenggg", 1007L);
        put("accesstokenhhh", 1008L);
    }};

    /**
     * 大数据中台：用户是否命中推荐内容
     */
    private static Map<Long, Boolean> userRecommendMap = new HashMap<Long, Boolean>() {{
        put(1001L, true);
        put(1002L, false);
    }};

    /**
     * 大数据中台：给用户推荐内容
     */
    private static Map<Long, RecommendCookbookDTO> userRecommendCookbookMap = new HashMap<Long, RecommendCookbookDTO>() {{
        put(1001L, new RecommendCookbookDTO(Lists.newArrayList("1", "9", "18", "4", "13", "6", "16", "8", "2", "10", "20", "12", "5", "14", "15", "7", "17", "3", "19", "11"), false));
    }};

    /**
     * 社区电商中心：用户对应的菜谱
     */
    private static Map<Long, List<String>> userCookbookMap = new HashMap<Long, List<String>>() {{
        put(1001L, Lists.newArrayList("cookbooka1", "cookbooka2"));
        put(1002L, Lists.newArrayList("cookbookb1"));
        put(1003L, Lists.newArrayList("cookbookc1", "cookbookc2"));
        put(1004L, Lists.newArrayList("cookbookd1"));
    }};

    /**
     * 获取用户ID
     *
     * 模拟用户中台接口
     *  1、前端http请求传入授权accessToken
     *  2、根据token换取用户ID
     *
     * @param accessToken
     * @return java.lang.Long
     * @author zhonghuashishan
     */
    @Override
    public Long getUserIdByToken(String accessToken){
        return accessTokenUserMap.get(accessToken);
    }

    /**
     * 是否命中推荐内容
     * <p>
     * 模拟大数据中台接口
     * 1、根据用户画像：点赞、收藏、分享等行为推荐菜谱，用于feed流展示
     * 2、这里不做大数据中台业务，只提供设计思路
     *
     * @param userId
     * @return java.lang.Boolean
     * @author zhonghuashishan
     */
    @Override
    public Boolean isRecommend(Long userId) {
        Boolean flag = userRecommendMap.get(userId);
        if(Objects.isNull(flag)) {
            return false;
        }
        return flag;
    }

    /**
     * 推荐菜谱内容
     * <p>
     * 模拟大数据中台接口
     * 1、根据用户画像：点赞、收藏、分享等行为推荐菜谱
     * 2、这里不做大数据中台业务，只提供设计思路
     *
     * @param cookbookViewRequestDTO
     * @return com.ruyuan.careerplan.home.domain.dto.RecommendCookbookDTO
     */
    @Override
    public RecommendCookbookDTO getRecommendCookbook(CookbookViewRequestDTO cookbookViewRequestDTO) {
        return userRecommendCookbookMap.get(cookbookViewRequestDTO.getUserId());
    }

}
