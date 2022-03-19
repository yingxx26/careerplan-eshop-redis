package com.ruyuan.careerplan.social.dao;

import com.ruyuan.careerplan.common.dao.BaseDAO;
import com.ruyuan.careerplan.social.domain.entity.SocialMasterDO;
import com.ruyuan.careerplan.social.mapper.SocialMasterMapper;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import javax.annotation.Resource;

@Repository
public class SocialMasterDAO extends BaseDAO<SocialMasterMapper, SocialMasterDO> {

    @Resource
    private SocialMasterMapper socialMasterMapper;

    public int insertSocialMaster(SocialMasterDO socialMaster) {
        return socialMasterMapper.insertSocialMaster(socialMaster);
    }

    public int updateSocialMasterByIdSelective(SocialMasterDO socialMaster) {
        return socialMasterMapper.updateSocialMasterByIdSelective(socialMaster);
    }

    public SocialMasterDO selectSocialMasterByCookbookId(String cookbookId) {
        return socialMasterMapper.selectSocialMasterByCookbookId(cookbookId);
    }

}
