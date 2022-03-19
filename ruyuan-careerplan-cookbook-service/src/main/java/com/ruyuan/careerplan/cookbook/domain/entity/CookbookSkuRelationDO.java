package com.ruyuan.careerplan.cookbook.domain.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.ruyuan.careerplan.common.domain.BaseEntity;
import lombok.Builder;
import lombok.Data;
import lombok.experimental.Tolerate;

import java.io.Serializable;


/**
 * @author zhonghuashishan
 */
@Data
@TableName("cookbook_sku_relation")
@Builder
public class CookbookSkuRelationDO extends BaseEntity implements Serializable {

    /**
     * 菜谱ID
     */
    private Long cookbookId;

    /**
     * 商品编码
     */
    private Long skuId;

    /**
     * 删除标识  0:有效  1:删除
     */
    private Integer delFlag;

    /**
     * 创建人
     */
    private Integer createUser;

    /**
     * 修改人
     */
    private Integer updateUser;

    @Tolerate
    public CookbookSkuRelationDO() {

    }
}