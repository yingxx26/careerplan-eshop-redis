package com.ruyuan.careerplan.social.service.impl;

import com.ruyuan.careerplan.social.dao.SocialMasterDAO;
import com.ruyuan.careerplan.social.domain.entity.SocialMasterDO;
import com.ruyuan.careerplan.social.service.SocialMasterService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;


/**
 *
 *
 * @author zhonghuashishan
 */
@Service
public class SocialMasterServiceImpl implements SocialMasterService {

	private static Logger logger = LoggerFactory.getLogger(SocialMasterServiceImpl.class);

	@Resource
	private SocialMasterDAO socialMasterDAO;

	@Override
	public int insertSocialMaster(SocialMasterDO socialMasterDO) {
		return socialMasterDAO.insertSocialMaster(socialMasterDO);
	}

	@Override
	public int updateSocialMasterByIdSelective(SocialMasterDO socialMasterDO) {
		return socialMasterDAO.updateSocialMasterByIdSelective(socialMasterDO);
	}

	@Override
	public SocialMasterDO selectSocialMasterByCookbookId(String cookbookId) {
		return socialMasterDAO.selectSocialMasterByCookbookId(cookbookId);
	}

}
