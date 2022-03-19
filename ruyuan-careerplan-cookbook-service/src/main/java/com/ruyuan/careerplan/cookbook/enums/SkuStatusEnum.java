package com.ruyuan.careerplan.cookbook.enums;

import com.google.common.collect.Maps;

import java.util.Map;

/**
 * 商品状态枚举
 *
 * @author zhonghuashishan
 */
public enum SkuStatusEnum {

    /**
     * 上架
     */
    UP(1, "上架"),

    /**
     * 下架
     */
    DOWN(2, "下架");

    private Integer code;

    private String value;

    SkuStatusEnum(Integer code, String value) {
        this.code = code;
        this.value = value;
    }

    public Integer getCode() {
        return code;
    }

    public String getValue() {
        return value;
    }

    public static Map<Integer, String> toMap() {
        Map<Integer, String> map = Maps.newHashMap();
        for (SkuStatusEnum element : SkuStatusEnum.values()) {
            map.put(element.getCode(), element.getValue());
        }
        return map;
    }

    public static SkuStatusEnum getByCode(Integer code) {
        if (code == null) {
            return null;
        }
        for (SkuStatusEnum element : SkuStatusEnum.values()) {
            if (code.equals(element.getCode())) {
                return element;
            }
        }
        return null;
    }

}