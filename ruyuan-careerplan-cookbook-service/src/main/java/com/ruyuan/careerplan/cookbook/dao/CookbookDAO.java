package com.ruyuan.careerplan.cookbook.dao;

import com.ruyuan.careerplan.common.dao.BaseDAO;
import com.ruyuan.careerplan.cookbook.domain.dto.CookbookDTO;
import com.ruyuan.careerplan.cookbook.domain.entity.CookbookDO;
import com.ruyuan.careerplan.cookbook.mapper.CookbookMapper;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 菜谱信息
 *
 * @author zhonghuashishan
 */
@Repository
public class CookbookDAO extends BaseDAO<CookbookMapper, CookbookDO> {

    /**
     * 根据作者id查询当前作者菜谱列表
     *
     * @param userId
     * @return
     */
    public List<CookbookDTO> listByUserId(Long userId) {
        return this.baseMapper.listByUserId(userId);
    }

    /**
     * 根据作者id分页查询当前作者菜谱列表
     *
     * @param userId
     * @param pageNo
     * @param pageSize
     * @return
     */
    public List<CookbookDTO> pageByUserId(Long userId, Integer pageNo, Integer pageSize) {
        return this.baseMapper.pageByUserId(userId, (pageNo - 1) * pageSize, pageSize);
    }

    public CookbookDTO getCookbookInfoById(Long cookbookId) {
        return this.baseMapper.getCookbookInfoById(cookbookId);
    }

    /**
     * 根据菜谱状态分页查询菜谱列表
     *
     * @param cookbookStatus
     * @param pageNo
     * @param pageSize
     * @return java.util.List<com.ruyuan.careerplan.cookbook.domain.dto.CookbookDTO>
     */
    public List<CookbookDTO> pageByCookbookStatus(Integer cookbookStatus, Integer pageNo, Integer pageSize) {
        return this.baseMapper.pageByCookbookStatus(cookbookStatus, (pageNo - 1) * pageSize, pageSize);
    }
}
