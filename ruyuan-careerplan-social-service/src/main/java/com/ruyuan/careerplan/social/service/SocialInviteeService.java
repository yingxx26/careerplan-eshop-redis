package com.ruyuan.careerplan.social.service;

import com.ruyuan.careerplan.social.domain.entity.SocialInviteeDO;

import java.util.List;


/**
 * 助力信息
 *
 * @author zhonghuashishan
 */
public interface SocialInviteeService {

	SocialInviteeDO selectSocialInviteeByCondition(String cookbookId, Long inviteeId);

	int insertSocialInvitee(SocialInviteeDO socialInviteeDO);

	List<SocialInviteeDO> selectSocialInviteeList(SocialInviteeDO socialInviteeDO);

	int deleteHelpFailInvitee(String cookbookId, Long inviteeId);

}
