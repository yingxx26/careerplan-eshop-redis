package com.ruyuan.careerplan.social.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.ruyuan.careerplan.social.domain.entity.SocialInviteeDO;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface SocialInviteeMapper extends BaseMapper<SocialInviteeDO> {

    SocialInviteeDO selectSocialInviteeByCondition(SocialInviteeDO socialInviteeDO);

    int insertSocialInvitee(SocialInviteeDO socialInviteeDO);

    List<SocialInviteeDO> selectSocialInviteeList(SocialInviteeDO socialInviteeDO);

    int deleteFailInvitee(SocialInviteeDO socialInviteeDO);

}
