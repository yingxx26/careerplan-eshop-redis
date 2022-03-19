package com.ruyuan.careerplan.inventory.dao;

import com.ruyuan.careerplan.common.dao.BaseDAO;
import com.ruyuan.careerplan.inventory.domain.entity.MqIdempotentLogDO;
import com.ruyuan.careerplan.inventory.mapper.MqIdempotentMapper;
import org.springframework.stereotype.Repository;

/**
 * @author zhonghuashishan
 */
@Repository
public class MqIdempotentDAO extends BaseDAO<MqIdempotentMapper, MqIdempotentLogDO> {
}
