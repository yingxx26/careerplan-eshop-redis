package com.ruyuan.careerplan.social.service;

import com.ruyuan.careerplan.social.domain.entity.SocialMasterDO;

import java.util.List;


/**
 * 团长信息
 *
 * @author zhonghuashishan
 */
public interface SocialMasterService {

	int insertSocialMaster(SocialMasterDO socialMasterDO);

	int updateSocialMasterByIdSelective(SocialMasterDO socialMasterDO);

	SocialMasterDO selectSocialMasterByCookbookId(String cookbookId);

}
