package com.ruyuan.careerplan.inventory.dao;

import com.ruyuan.careerplan.common.dao.BaseDAO;
import com.ruyuan.careerplan.inventory.domain.entity.InventoryDO;
import com.ruyuan.careerplan.inventory.mapper.InventoryMapper;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * @author zhonghuashishan
 */
@Repository
public class InventoryDAO extends BaseDAO<InventoryMapper, InventoryDO> {
    /**
     * 查询库存的相关商品信息
     * @return
     */
    public List<InventoryDO> queryInventoryList(){
        return this.baseMapper.queryInventoryList();
    }

}
