package com.ruyuan.careerplan.inventory.service;

import com.ruyuan.careerplan.common.core.JsonResult;
import com.ruyuan.careerplan.inventory.InventoryApplication;
import com.ruyuan.careerplan.inventory.api.InventoryApi;
import com.ruyuan.careerplan.inventory.domain.request.InventoryRequest;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.util.Random;

@SpringBootTest(classes = InventoryApplication.class)
@RunWith(SpringJUnit4ClassRunner.class)
public class InventoryServiceTest {

    @Resource
    private InventoryApi inventoryApi;

    @Test
    public void putStorage(){
        InventoryRequest request = new InventoryRequest();
        request.setInventoryNum(29140000);
        request.setOperator(1);
        request.setSkuId(10000001L);
        request.setWarehouse("10001");
        request.setWarehouseCode("20000001");
        inventoryApi.putStorage(request);
    }
    @Test
    public void queryProductStock(){
        Long skuId = 10000001L;
        JsonResult<BigDecimal> integerJsonResult = inventoryApi.queryProductStock(skuId);

        System.out.println("当前商品："+skuId+"的总库存："+integerJsonResult.getData());
    }

    @Test
    public void deductProductStock(){
        InventoryRequest request = new InventoryRequest();
        request.setInventoryNum(300);
        request.setOperator(1);
        request.setSkuId(10000001L);
        request.setWarehouse("10001");
        request.setWarehouseCode("20000001");
        inventoryApi.deductProductStock(request);
        queryProductStock();
    }
}