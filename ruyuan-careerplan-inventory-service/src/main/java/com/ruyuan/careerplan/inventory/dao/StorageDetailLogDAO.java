package com.ruyuan.careerplan.inventory.dao;

import com.ruyuan.careerplan.common.dao.BaseDAO;
import com.ruyuan.careerplan.inventory.domain.entity.InventoryDO;
import com.ruyuan.careerplan.inventory.domain.entity.StorageDetailLogDO;
import com.ruyuan.careerplan.inventory.mapper.InventoryMapper;
import com.ruyuan.careerplan.inventory.mapper.StorageDetailLogMapper;
import org.springframework.stereotype.Repository;

/**
 * @author zhonghuashishan
 */
@Repository
public class StorageDetailLogDAO extends BaseDAO<StorageDetailLogMapper, StorageDetailLogDO> {
}
