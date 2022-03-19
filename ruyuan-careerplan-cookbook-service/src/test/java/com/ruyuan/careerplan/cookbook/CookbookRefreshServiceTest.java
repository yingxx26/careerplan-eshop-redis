package com.ruyuan.careerplan.cookbook;

import com.ruyuan.careerplan.cookbook.service.CookbookRefreshService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@SpringBootTest(classes = CookbookApplication.class)
@RunWith(SpringJUnit4ClassRunner.class)
public class CookbookRefreshServiceTest {

    @Autowired
    private CookbookRefreshService cookbookRefreshService;

    /**
     * 刷新首页菜谱feed缓存数据
     */
    @Test
    public void refreshHomeFeedCookbook() {
        cookbookRefreshService.refreshHomeFeedCookbook();
    }

    /**
     * 刷新首页菜谱视图缓存数据
     */
    @Test
    public void refreshHomeViewCookbook() {
        cookbookRefreshService.refreshHomeViewCookbook();
    }

}