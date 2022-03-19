package com.ruyuan.careerplan.social.enums;

import com.google.common.collect.Maps;

import java.util.Map;

/**
 * 优惠券状态枚举
 *
 * @author zhonghuashishan
 */
public enum CouponStatusEnum {

    NOT_USE(0, "未使用"),

    USED(1, "已使用"),

    EXPIRED(2, "已过期");

    private Integer code;

    private String value;

    CouponStatusEnum(Integer code, String value) {
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
        for (CouponStatusEnum element : CouponStatusEnum.values()) {
            map.put(element.getCode(), element.getValue());
        }
        return map;
    }

    public static CouponStatusEnum getByCode(Integer code) {
        if (code == null) {
            return null;
        }
        for (CouponStatusEnum element : CouponStatusEnum.values()) {
            if (code.equals(element.getCode())) {
                return element;
            }
        }
        return null;
    }

}