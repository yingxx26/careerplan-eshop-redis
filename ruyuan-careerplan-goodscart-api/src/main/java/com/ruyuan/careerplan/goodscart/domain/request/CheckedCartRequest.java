package com.ruyuan.careerplan.goodscart.domain.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.util.List;

/**
 * @author zhonghuashishan
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CheckedCartRequest implements Serializable {
    /**
     * 用户ID
     */
    @NotNull
    private Long userId;

    /**
     * 商品编号
     */
    @NotBlank
    private Long skuId;

    /**
     * 选中状态
     */
    @NotNull
    @Min(0)
    @Max(1)
    private Integer checkStatus;
}
