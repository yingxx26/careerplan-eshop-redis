package com.ruyuan.careerplan.cookbook.enums;

import com.google.common.collect.Maps;

import java.util.Map;

/**
 * @author zhonghuashishan
 */
public enum SexEnum {

    /**
     * 男
     */
    MALE(1, "男"),

    /**
     * 女
     */
    FEMALE(2, "女");

    private Integer code;

    private String value;

    SexEnum(Integer code, String value) {
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
        for (SexEnum element : SexEnum.values()) {
            map.put(element.getCode(), element.getValue());
        }
        return map;
    }

    public static SexEnum getByCode(Integer code) {
        if (code == null) {
            return null;
        }
        for (SexEnum element : SexEnum.values()) {
            if (code.equals(element.getCode())) {
                return element;
            }
        }
        return null;
    }

}
