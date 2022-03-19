package com.ruyuan.careerplan.goodscart.domain.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.io.Serializable;

/**
 * @author zhonghuashishan
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateCookBookCartRequest implements Serializable {
    /**
     * 商品编码
     */
    @NotBlank
    private Long skuId;

    /**
     * 用户ID
     */
    @NotNull
    private Long userId;

    /**
     * 商品数量
     */
    @NotNull
    @Min(0)
    private Integer count;

    /**
     * 卖家仓库地址
     */
    @NotBlank
    private String warehouse;
}
