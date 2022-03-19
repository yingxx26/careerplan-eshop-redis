package com.ruyuan.careerplan.social.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.ruyuan.careerplan.social.domain.entity.SocialMasterDO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface SocialMasterMapper extends BaseMapper<SocialMasterDO> {

    int insertSocialMaster(SocialMasterDO socialMaster);

    int updateSocialMasterByIdSelective(SocialMasterDO socialMaster);

    SocialMasterDO selectSocialMasterByCookbookId(String cookbookId);

}
