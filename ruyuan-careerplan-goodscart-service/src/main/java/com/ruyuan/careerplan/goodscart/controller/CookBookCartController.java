package com.ruyuan.careerplan.goodscart.controller;

import com.ruyuan.careerplan.common.core.JsonResult;
import com.ruyuan.careerplan.goodscart.domain.dto.BillingDTO;
import com.ruyuan.careerplan.goodscart.domain.dto.CookBookCartInfoDTO;
import com.ruyuan.careerplan.goodscart.domain.request.AddCookBookCartRequest;
import com.ruyuan.careerplan.goodscart.domain.request.CheckedCartRequest;
import com.ruyuan.careerplan.goodscart.domain.request.UpdateCookBookCartRequest;
import com.ruyuan.careerplan.goodscart.service.CookBookCartService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;

/**
 * @author zhonghuashishan
 */
@RestController
@RequestMapping("/api/goodscart")
public class CookBookCartController {

    @Autowired
    private CookBookCartService cookBookCartService;

    /**
     * 购物车加购
     * @param request
     * @return
     */
    @RequestMapping("/addCartGoods")
    public JsonResult<String> addCartGoods(@RequestBody @Valid AddCookBookCartRequest request){
        cookBookCartService.addCartGoods(request);
        return JsonResult.buildSuccess("success");
    }

    /**
     * 更新购物车
     * @param request
     * @return
     */
    @RequestMapping("/updateCartGoods")
    public JsonResult<CookBookCartInfoDTO> updateCartGoods(@RequestBody @Valid UpdateCookBookCartRequest request){
        CookBookCartInfoDTO dto = cookBookCartService.updateCartGoods(request);
        return JsonResult.buildSuccess(dto);
    }

    /**
     * 查询购物车
     * @param userId
     * @return
     */
    @RequestMapping("/queryCart")
    public JsonResult<CookBookCartInfoDTO> queryCart(Long userId){
        // sorted set + hash来实现的
        // 先查询按时间排序的商品skuId集合，每个skuId对应商品信息
        CookBookCartInfoDTO dto = cookBookCartService.queryCart(userId);
        return JsonResult.buildSuccess(dto);
    }

    /**
     * 选中购物车中的商品项
     * @param request
     * @return
     */
    @RequestMapping("/checkedCartGoods")
    public JsonResult<CookBookCartInfoDTO> checkedCartGoods(@RequestBody @Valid CheckedCartRequest request){
        CookBookCartInfoDTO dto = cookBookCartService.checkedCartGoods(request);
        return JsonResult.buildSuccess(dto);
    }

}
