package com.ruyuan.careerplan.cookbook.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.ruyuan.careerplan.cookbook.domain.dto.CookbookDTO;
import com.ruyuan.careerplan.cookbook.domain.entity.CookbookDO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 *
 * @author zhonghuashishan
 */
@Mapper
public interface CookbookMapper extends BaseMapper<CookbookDO> {

    /**
     * 根据作者id查询当前作者菜谱列表
     *
     * @param userId
     * @return
     */
    List<CookbookDTO> listByUserId(@Param("userId") Long userId);

    /**
     * 根据作者id分页查询当前作者菜谱列表
     *
     * @param userId
     * @param start
     * @param size
     * @return
     */
    List<CookbookDTO> pageByUserId(@Param("userId") Long userId,
                                   @Param("start") int start,
                                   @Param("size") int size);

    /**
     * 根据id查询菜谱详情
     *
     * @param cookbookId
     * @return
     */
    CookbookDTO getCookbookInfoById(@Param("cookbookId") Long cookbookId);

    /**
     * 根据菜谱状态分页查询菜谱列表
     *
     * @param cookbookStatus
     * @param start
     * @param size
     * @return
     */
    List<CookbookDTO> pageByCookbookStatus(@Param("cookbookStatus") Integer cookbookStatus,
                                   @Param("start") int start,
                                   @Param("size") int size);

}