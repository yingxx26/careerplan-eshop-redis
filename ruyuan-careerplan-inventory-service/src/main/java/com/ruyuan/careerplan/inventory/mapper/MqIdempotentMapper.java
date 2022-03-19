package com.ruyuan.careerplan.inventory.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.ruyuan.careerplan.inventory.domain.entity.MqIdempotentLogDO;
import org.apache.ibatis.annotations.Mapper;

/**
 * @author zhonghuashishan
 */
@Mapper
public interface MqIdempotentMapper extends BaseMapper<MqIdempotentLogDO> {
}
