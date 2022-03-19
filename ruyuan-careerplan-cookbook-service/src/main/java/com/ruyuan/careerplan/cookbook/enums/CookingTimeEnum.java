package com.ruyuan.careerplan.cookbook.enums;

import com.google.common.collect.Maps;

import java.util.Map;

/**
 * @author zhonghuashishan
 */
public enum CookingTimeEnum {

    /**
     * 10分钟之内
     */
    WITHIN_TEN_MINUTES(1, "10分钟之内"),
    /**
     * 10-30分钟
     */
    TEN_TO_THIRTY_MINUTES(2, "30分钟之内"),
    /**
     * 30-60分钟
     */
    THIRTY_TO_SIXTY_MINUTES(3, "30-60分钟"),
    /**
     * 1小时以上
     */
    MORE_THEN_AN_HOUR(4, "1小时以上");

    private Integer code;

    private String value;

    CookingTimeEnum(Integer code, String value) {
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
        for (CookingTimeEnum element : CookingTimeEnum.values()) {
            map.put(element.getCode(), element.getValue());
        }
        return map;
    }

    public static CookingTimeEnum getByCode(Integer code) {
        if (code == null) {
            return null;
        }
        for (CookingTimeEnum element : CookingTimeEnum.values()) {
            if (code.equals(element.getCode())) {
                return element;
            }
        }
        return null;
    }

}
