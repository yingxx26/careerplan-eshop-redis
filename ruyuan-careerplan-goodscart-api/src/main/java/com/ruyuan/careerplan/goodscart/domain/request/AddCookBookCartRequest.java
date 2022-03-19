package com.ruyuan.careerplan.goodscart.domain.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.io.Serializable;

/**
 * @author zhonghuashishan
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AddCookBookCartRequest implements Serializable {
    /**
     * 商品编码
     * 商品skuId，代表了是一个商品，购买一个商品
     */
    @NotBlank
    private Long skuId;

    /**
     * 用户ID
     * 加入到哪个用户的购物车里去，userId就可以了
     */
    @NotNull
    private Long userId;

    /**
     * 卖家仓库地址
     */
    @NotBlank
    private String warehouse;
}
