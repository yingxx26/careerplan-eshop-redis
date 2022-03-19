package com.ruyuan.careerplan.inventory.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.ruyuan.careerplan.inventory.dao.InventoryDAO;
import com.ruyuan.careerplan.inventory.domain.entity.InventoryDO;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * @author zhonghuashishan
 */
@Mapper
public interface InventoryMapper extends BaseMapper<InventoryDO> {
    /**
     * 查询库存的相关商品信息
     * @return
     */
    List<InventoryDO> queryInventoryList();
}
