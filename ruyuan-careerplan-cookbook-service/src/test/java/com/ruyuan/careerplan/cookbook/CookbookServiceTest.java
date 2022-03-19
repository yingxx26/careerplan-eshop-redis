package com.ruyuan.careerplan.cookbook;

import com.google.common.collect.Sets;
import com.ruyuan.careerplan.common.page.PagingInfo;
import com.ruyuan.careerplan.cookbook.domain.dto.CookbookDTO;
import com.ruyuan.careerplan.cookbook.domain.dto.Food;
import com.ruyuan.careerplan.cookbook.domain.dto.SaveOrUpdateCookbookDTO;
import com.ruyuan.careerplan.cookbook.domain.dto.StepDetail;
import com.ruyuan.careerplan.cookbook.domain.request.CookbookQueryRequest;
import com.ruyuan.careerplan.cookbook.domain.request.SaveOrUpdateCookbookRequest;
import com.ruyuan.careerplan.cookbook.enums.CategoryTagEnum;
import com.ruyuan.careerplan.cookbook.enums.CookbookTypeEnum;
import com.ruyuan.careerplan.cookbook.enums.CookingTimeEnum;
import com.ruyuan.careerplan.cookbook.enums.DifficultyEnum;
import com.ruyuan.careerplan.cookbook.service.CookbookService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@SpringBootTest(classes = CookbookApplication.class)
@RunWith(SpringJUnit4ClassRunner.class)
public class CookbookServiceTest {

    @Autowired
    private CookbookService cookbookService;


    @Test
    public void saveOrUpdateCookbook() {
        List<StepDetail> stepDetails = new ArrayList<>();
        for (int i = 1; i < 10; i++) {
            StepDetail stepDetail = StepDetail.builder()
                    .step(i)
                    .content("第" + i + "步，首先需要。。。然后需要。。。最后需要。。。")
                    .img("step" + i + "url")
                    .build();
            stepDetails.add(stepDetail);
        }
        List<Food> foods = new ArrayList<>();
        for (int i = 1; i < 10; i++) {
            Food food = Food.builder()
                    .sort(i)
                    .foodName("食材" + i)
                    .foodSpecs("食材" + i + "用量")
                    .tag("菜")
                    .build();
            foods.add(food);
        }

        Set<String> skuIds = Sets.newHashSet("6000000011",
                "6000000074", "6000000121");

        SaveOrUpdateCookbookRequest request = SaveOrUpdateCookbookRequest.builder()
                .userId(1L)
                .cookbookName("清蒸鲈鱼")
                .cookbookType(CookbookTypeEnum.UGC.getCode())
                .description("清蒸鲈鱼，做法简单，但是成品美观，味道特别的出色。成为家宴中不可缺少的一道主菜。" +
                        "选用一斤左右的鲈鱼，蒸的时间恰到火候，鱼肉刚熟，细嫩爽滑，鱼肉的鲜美完全的呈现。" +
                        "汤汁带着米酒的甜，豉油的香，吃到嘴里绝对每一口都是享受。")
                .categoryTag(CategoryTagEnum.PRIVATE_HOME.getCode())
                .mainUrl("mainUrl")
                .videoUrl("videoUrl")
                .cookingTime(CookingTimeEnum.WITHIN_TEN_MINUTES.getCode())
                .difficulty(DifficultyEnum.EASY.getCode())
                .cookbookDetail(stepDetails)
                .foods(foods)
                .tips("1，鲈鱼肉质细嫩，一斤的鲈鱼切了花刀，七分钟正好，八九分钟就老了。一斤的误差范围在二两。\n" +
                        "2，米酒在里面，吃起来无比的鲜美。可以试试！猪油的作用也不可小视，很香，没有的话可以放一片五花肉铺在鱼背上。\n" +
                        "3，这个鱼，千万不要自作主张放很多蒸鱼豉油，其实不放都可以，我为了成品美观，所以放了一点改变一下太白的颜色。\n" +
                        "吃的时候，拨下一块肉，沾一下汤汁，美得无与伦比....")
                .operator(1)
                .build();

        SaveOrUpdateCookbookDTO dto = cookbookService.saveOrUpdateCookbook(request);
        System.out.println(dto);
    }


    @Test
    public void getCookbookInfo() {
        CookbookQueryRequest request = CookbookQueryRequest.builder()
                .cookbookId(10L)
                .build();

        CookbookDTO dto = cookbookService.getCookbookInfo(request);
        System.out.println(dto);
    }

    @Test
    public void listCookbookInfo() {
        CookbookQueryRequest request = CookbookQueryRequest.builder()
                .userId(1L)
                .pageNo(1)
                .pageSize(10)
                .build();
        PagingInfo<CookbookDTO> pagingInfo = cookbookService.listCookbookInfo(request);
        System.out.println(pagingInfo);
    }


}