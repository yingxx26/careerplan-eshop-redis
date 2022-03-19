package com.ruyuan.careerplan.social.dao;

import com.ruyuan.careerplan.common.dao.BaseDAO;
import com.ruyuan.careerplan.social.domain.entity.SocialInviteeDO;
import com.ruyuan.careerplan.social.mapper.SocialInviteeMapper;
import org.springframework.stereotype.Repository;

import javax.annotation.Resource;
import java.util.List;

@Repository
public class SocialInviteeDAO extends BaseDAO<SocialInviteeMapper, SocialInviteeDO> {

    @Resource
    private SocialInviteeMapper socialInviteeMapper;

    public SocialInviteeDO selectSocialInviteeByCondition(SocialInviteeDO socialInviteeDO) {
        return socialInviteeMapper.selectSocialInviteeByCondition(socialInviteeDO);
    }

    public int insertSocialInvitee(SocialInviteeDO socialInviteeDO) {
        return socialInviteeMapper.insertSocialInvitee(socialInviteeDO);
    }

    public List<SocialInviteeDO> selectSocialInviteeList(SocialInviteeDO socialInviteeDO) {
        return socialInviteeMapper.selectSocialInviteeList(socialInviteeDO);
    }

    public int deleteFailInvitee(SocialInviteeDO socialInviteeDO) {
        return socialInviteeMapper.deleteFailInvitee(socialInviteeDO);
    }

}
