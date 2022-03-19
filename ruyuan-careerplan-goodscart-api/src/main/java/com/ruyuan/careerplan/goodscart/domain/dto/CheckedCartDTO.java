package com.ruyuan.careerplan.goodscart.domain.dto;

import com.ruyuan.careerplan.goodscart.domain.request.CookBookCartSkuRequest;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * @author zhonghuashishan
 */
@Data
public class CheckedCartDTO implements Serializable {
    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 选中的商品项
     */
    private List<CookBookCartSkuRequest> checkedList;
}
