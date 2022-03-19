package com.ruyuan.careerplan.social.service.impl;

import com.ruyuan.careerplan.social.dao.SocialInviteeDAO;
import com.ruyuan.careerplan.social.domain.entity.SocialInviteeDO;
import com.ruyuan.careerplan.social.service.SocialInviteeService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;


/**
 *
 *
 * @author zhonghuashishan
 */
@Service
public class SocialInviteeServiceImpl implements SocialInviteeService {

	private static Logger logger = LoggerFactory.getLogger(SocialInviteeServiceImpl.class);

	@Resource
	private SocialInviteeDAO socialInviteeDAO;

	@Override
	public SocialInviteeDO selectSocialInviteeByCondition(String cookbookId, Long inviteeId) {
		SocialInviteeDO socialInviteeDO = new SocialInviteeDO();
		socialInviteeDO.setCookbookId(cookbookId);
		socialInviteeDO.setInviteeId(inviteeId);
		return socialInviteeDAO.selectSocialInviteeByCondition(socialInviteeDO);
	}

	@Override
	public int insertSocialInvitee(SocialInviteeDO socialInviteeDO) {
		return socialInviteeDAO.insertSocialInvitee(socialInviteeDO);
	}

	@Override
	public List<SocialInviteeDO> selectSocialInviteeList(SocialInviteeDO socialInviteeDO){
		return socialInviteeDAO.selectSocialInviteeList(socialInviteeDO);
	}

	@Override
	public int deleteHelpFailInvitee(String cookbookId, Long inviteeId) {
		SocialInviteeDO socialInviteeDO = new SocialInviteeDO();
		socialInviteeDO.setCookbookId(cookbookId);
		socialInviteeDO.setInviteeId(inviteeId);
		return socialInviteeDAO.deleteFailInvitee(socialInviteeDO);
	}

}
