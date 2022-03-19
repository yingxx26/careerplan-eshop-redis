package com.ruyuan.careerplan.home.api.impl;

import com.ruyuan.careerplan.common.core.JsonResult;
import com.ruyuan.careerplan.common.utils.PageUtil;
import com.ruyuan.careerplan.home.api.HomeFeedApi;
import com.ruyuan.careerplan.home.constants.HomeConstant;
import com.ruyuan.careerplan.home.domain.dto.CookbookViewDTO;
import com.ruyuan.careerplan.home.domain.dto.CookbookViewResultDTO;
import com.ruyuan.careerplan.home.domain.dto.RecommendCookbookDTO;
import com.ruyuan.careerplan.home.domain.request.CookbookViewRequestDTO;
import com.ruyuan.careerplan.home.enums.ErrorMsgEnum;
import com.ruyuan.careerplan.home.service.HomeCommonService;
import com.ruyuan.careerplan.home.service.HomeCookbookService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.RandomUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.util.List;
import java.util.Objects;


@Slf4j
@Service
public class HomeFeedApiImpl implements HomeFeedApi {

    @Resource
    private HomeCookbookService homeCookbookService;

    @Resource
    private HomeCommonService homeCommonService;

    /**
     * 首页feed流
     *
     * @param cookbookViewRequestDTO
     * @return com.ruyuan.careerplan.common.core.JsonResult<com.ruyuan.careerplan.home.domain.dto.CookbookViewResultDTO>
     */
    @Override
    public JsonResult<CookbookViewResultDTO> getCookbookViewList(CookbookViewRequestDTO cookbookViewRequestDTO) {
        if (Objects.isNull(cookbookViewRequestDTO)) {
            return JsonResult.buildError(ErrorMsgEnum.PARAM_ERROR.getMsg());
        }

        CookbookViewResultDTO cookbookViewResultDTO;
        Boolean isRecommend = homeCommonService.isRecommend(cookbookViewRequestDTO.getUserId());
        if (isRecommend) {
            cookbookViewResultDTO = getRecommendCookbookList(cookbookViewRequestDTO);
        } else {
            cookbookViewResultDTO = getCacheCookbookList(cookbookViewRequestDTO);
        }

        if (Objects.nonNull(cookbookViewResultDTO) && !CollectionUtils.isEmpty(cookbookViewResultDTO.getCookbookViewDTOList())) {
            return JsonResult.buildSuccess(cookbookViewResultDTO);
        }

        return JsonResult.buildError(ErrorMsgEnum.SERVICE_ERROR.getMsg());
    }

    /**
     * 获取大数据推荐菜谱
     *
     * @param cookbookViewRequestDTO
     * @return com.ruyuan.careerplan.home.domain.dto.CookbookViewResultDTO
     */
    private CookbookViewResultDTO getRecommendCookbookList(CookbookViewRequestDTO cookbookViewRequestDTO) {
        RecommendCookbookDTO recommendCookbookDTO = homeCommonService.getRecommendCookbook(cookbookViewRequestDTO);
        if (Objects.isNull(recommendCookbookDTO)) {
            return null;
        }

        List<CookbookViewDTO> cookbookViewDTOList = homeCookbookService.getCookbookViewFromCache(recommendCookbookDTO.getCookbookIdList());

        CookbookViewResultDTO cookbookViewResultDTO = new CookbookViewResultDTO();
        cookbookViewResultDTO.setHasNextPage(recommendCookbookDTO.getHasNextPage());
        cookbookViewResultDTO.setCurrentPageNo(cookbookViewRequestDTO.getPageNo());
        cookbookViewResultDTO.setCookbookViewDTOList(cookbookViewDTOList);
        return cookbookViewResultDTO;
    }

    /**
     * 获取缓存菜谱
     *
     * @param cookbookViewRequestDTO
     * @return com.ruyuan.careerplan.home.domain.dto.CookbookViewResultDTO
     */
    private CookbookViewResultDTO getCacheCookbookList(CookbookViewRequestDTO cookbookViewRequestDTO) {
        String feedVersion = cookbookViewRequestDTO.getFeedVersion();
        Integer pageNo = cookbookViewRequestDTO.getPageNo();
        Boolean pullToRefresh = cookbookViewRequestDTO.getPullToRefresh();
        List<String> cookbookIdList;

        // 下拉刷新：获取随机范围页码
        if (pullToRefresh) {
            pageNo = RandomUtils.nextInt(HomeConstant.DOWN_REFRESH_PAGE_START, HomeConstant.DOWN_REFRESH_PAGE_END);
        }
        // 是否存在首页feed流缓存
        Boolean isExistHomeFeedCache = homeCookbookService.isExistHomeFeedCache(feedVersion);
        if (!isExistHomeFeedCache) {
            // 获取最新缓存
            feedVersion = homeCookbookService.getHomeCookLatestVersion();
            if (!pullToRefresh) {
                pageNo = HomeConstant.PAGE_NO_START;
            }
        }
        cookbookIdList = homeCookbookService.getHomeFeedCookbookList(feedVersion, pageNo);

        // 判定是否有下一页
        boolean hasNextPage = false;
        if (!CollectionUtils.isEmpty(cookbookIdList)) {
            Long count = homeCookbookService.getHomeFeedCookbookListSize(feedVersion);
            if (Objects.nonNull(count) && count.intValue() > 0) {
                hasNextPage = PageUtil.hasNextPage(pageNo, cookbookViewRequestDTO.getPageSize(), count.intValue());
            }
        }

        List<CookbookViewDTO> cookbookViewDTOList = homeCookbookService.getCookbookViewFromCache(cookbookIdList);
        CookbookViewResultDTO cookbookViewResultDTO = new CookbookViewResultDTO();
        cookbookViewResultDTO.setFeedVersion(feedVersion);
        cookbookViewResultDTO.setCurrentPageNo(pageNo);
        cookbookViewResultDTO.setHasNextPage(hasNextPage);
        cookbookViewResultDTO.setCookbookViewDTOList(cookbookViewDTOList);
        return cookbookViewResultDTO;
    }

}
