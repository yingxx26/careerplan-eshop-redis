package com.ruyuan.careerplan.goodscart;

import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.ruyuan.careerplan.common.redis.RedisCache;
import com.ruyuan.careerplan.goodscart.constants.RedisKeyConstant;
import com.ruyuan.careerplan.goodscart.dao.CookBookCartDAO;
import com.ruyuan.careerplan.goodscart.domain.dto.CookBookCartInfoDTO;
import com.ruyuan.careerplan.goodscart.domain.entity.CookBookCartDO;
import com.ruyuan.careerplan.goodscart.domain.request.AddCookBookCartRequest;
import com.ruyuan.careerplan.goodscart.domain.request.CheckedCartRequest;
import com.ruyuan.careerplan.goodscart.domain.request.UpdateCookBookCartRequest;
import com.ruyuan.careerplan.goodscart.service.CookBookCartService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.concurrent.TimeUnit;

/**
 * @author zhonghuashishan
 */
@SpringBootTest(classes = CookBookCartApplication.class)
@RunWith(SpringJUnit4ClassRunner.class)
public class CookBookCartServiceTest {

    @Autowired
    private RedisCache redisCache;

    @Autowired
    private CookBookCartService cookBookCartService;

    @Autowired
    private CookBookCartDAO cookBookCartDAO;

    private static final Long USER_ID = 1001L;
    private static final String NUM_KEY = RedisKeyConstant.SHOPPING_CART_HASH + USER_ID;
    private static final String EXTRA_KEY = RedisKeyConstant.SHOPPING_CART_EXTRA_HASH + USER_ID;
    private static final String ORDER_KEY = RedisKeyConstant.SHOPPING_CART_ZSET + USER_ID;
    private static final String EMPTY_KEY = RedisKeyConstant.SHOPPING_CART_EMPTY + USER_ID;

    /**
     * 购物车加购
     */
    @Test
    public void addCartGoods() {
        // 构造请求参数
        AddCookBookCartRequest request = AddCookBookCartRequest.builder()
                .skuId(6000000011L)
                .userId(USER_ID)
                .warehouse("北京")
                .build();

        // 首次加购
        cookBookCartService.addCartGoods(request);
        // 数量为1
        assert "1".equals(redisCache.hGet(NUM_KEY, "6000000011"));

        // 再次加购
        cookBookCartService.addCartGoods(request);
        assert "2".equals(redisCache.hGet(NUM_KEY, "6000000011"));
    }

    /**
     * 修改购物车
     */
    @Test
    public void updateCartGoods() {
//        addCartGoods();

        UpdateCookBookCartRequest request = UpdateCookBookCartRequest.builder()
                .skuId(6000000011L)
                .userId(USER_ID)
                .count(15)
                .build();

        // 修改存在的商品
        cookBookCartService.updateCartGoods(request);

        // 数量为15
        assert "15".equals(redisCache.hGet(NUM_KEY, "6000000011"));

        // 删除商品
        request.setCount(0);
        cookBookCartService.updateCartGoods(request);
        assert !redisCache.hasKey(NUM_KEY);
        try {
            // 如果不在这里睡眠，单测方法将直接结束，消息有可能未被消费掉
            TimeUnit.MINUTES.sleep(1);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    /**
     * 查询购物车
     */
    @Test
    public void queryCart() throws InterruptedException {
        // 加购商品，加购两次之后count是2
        addCartGoods();

        // case1. 缓存和数据库中都有数据：如果缓存中存在，那么就从缓存中查询到后返回
        CookBookCartInfoDTO dto1 = cookBookCartService.queryCart(USER_ID);
        assert dto1 != null;
        assert dto1.getSkuList().size() == 1;
        assert dto1.getSkuList().get(0).getCount() == 2;

        // 在这里睡眠10秒是为了确保消息被消费到数据库
        TimeUnit.SECONDS.sleep(10);

        // case2. 缓存没有，数据库有：如果缓存中不存在，那么就添加分布式锁，然后再查询MySQL，查询到数据后更新到缓存中，最后返回
        // 删除缓存
        assert redisCache.delete(NUM_KEY);
        assert redisCache.delete(EXTRA_KEY);
        assert redisCache.delete(ORDER_KEY);

        // 再次查询之后，缓存中会更新
        CookBookCartInfoDTO dto2 = cookBookCartService.queryCart(USER_ID);
        assert dto2 != null;
        assert dto2.getSkuList().size() == 1;
        assert dto2.getSkuList().get(0).getCount() == 2;

        // 重新获取缓存
        assert redisCache.hasKey(NUM_KEY);
        assert redisCache.hasKey(EXTRA_KEY);
        assert redisCache.hasKey(ORDER_KEY);

        // 在这里睡眠10秒是为了确保消息被消费到数据库
        TimeUnit.SECONDS.sleep(10);

        // case3. 缓存和数据库中都没有：
        // 如果缓存、MySQL都不存在，那么就再查询MySQL后，给缓存设置一个空值，设置一个随机的过期时间，最后返回一个空数据
        // 当下次再来查询购物车时，会先判断缓存中的空值是否存在，如果存在就不查数据库了

        // 删除缓存
        assert redisCache.delete(NUM_KEY);
        assert redisCache.delete(EXTRA_KEY);
        assert redisCache.delete(ORDER_KEY);

        // 删除数据库记录
        UpdateWrapper<CookBookCartDO> updateWrapper = new UpdateWrapper<>();
        updateWrapper.set("user_id", USER_ID);
        cookBookCartDAO.remove(updateWrapper);

        // 查询购物车
        CookBookCartInfoDTO dto3 = cookBookCartService.queryCart(USER_ID);
        assert dto3 != null;
        assert dto3.getSkuList().size() == 0;

        // 这个时候会存在一个空缓存
        assert redisCache.hasKey(EMPTY_KEY);

        try {
            // 如果不在这里睡眠，单测方法将直接结束，消息有可能未被消费掉
            TimeUnit.MINUTES.sleep(1);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void checkedCartGoods() {
        CheckedCartRequest request = CheckedCartRequest.builder()
                .skuId(6000000011L)
                .userId(USER_ID)
                .checkStatus(1)
                .build();

        cookBookCartService.checkedCartGoods(request);

        try {
            // 如果不在这里睡眠，单测方法将直接结束，消息有可能未被消费掉
            TimeUnit.MINUTES.sleep(1);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

}
