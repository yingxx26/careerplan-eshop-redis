package com.ruyuan.careerplan.home.service;

import com.ruyuan.careerplan.home.domain.dto.RecommendCookbookDTO;
import com.ruyuan.careerplan.home.domain.request.CookbookViewRequestDTO;

/**
 * 此服务模拟各个中台提供接口，调用后返回业务结果
 *
 * @author zhonghuashishan
 */
public interface HomeCommonService {

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
    Long getUserIdByToken(String accessToken);

    /**
     * 是否命中推荐内容
     *
     * 模拟大数据中台接口
     *  1、根据用户画像：点赞、收藏、分享等行为推荐菜谱
     *  2、这里不做大数据中台业务，只提供设计思路
     *
     * @param userId
     * @return java.lang.Boolean
     * @author zhonghuashishan
     */
    Boolean isRecommend(Long userId);

    /**
     * 推荐菜谱
     *
     * 模拟大数据中台接口
     *  1、根据用户画像：点赞、收藏、分享等行为推荐菜谱
     *  2、这里不做大数据中台业务，只提供设计思路
     *
     * @param cookbookViewRequestDTO
     * @return com.ruyuan.careerplan.home.domain.dto.RecommendCookbookDTO
     */
    RecommendCookbookDTO getRecommendCookbook(CookbookViewRequestDTO cookbookViewRequestDTO);

}
