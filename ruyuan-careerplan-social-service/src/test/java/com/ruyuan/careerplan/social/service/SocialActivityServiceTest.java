package com.ruyuan.careerplan.social.service;

import com.ruyuan.careerplan.social.SocialApplication;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import javax.annotation.Resource;

@SpringBootTest(classes = SocialApplication.class)
@RunWith(SpringJUnit4ClassRunner.class)
public class SocialActivityServiceTest {

    @Resource
    private SocialActivityService socialActivityService;

    /**
     * 初始化社交活动
     *
     * @param
     * @return void
     * @author zhonghuashishan
     */
    @Test
    public void initSocialActivity() {
        // 团长ID
        Long userId = 1001L;
        // 菜谱ID
        String cookbookId = "cookbookaaa";
        Boolean result = socialActivityService.initSocialActivity(userId, cookbookId);
        System.out.println(result);
    }
}