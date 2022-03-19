package com.ruyuan.careerplan.cookbook.enums;

import com.google.common.collect.Maps;

import java.util.Map;

/**
 * @author zhonghuashishan
 */
public enum CategoryTagEnum {

    /**
     * 私房菜
     */
    PRIVATE_HOME(1, "私房菜"),
    /**
     * 下饭菜
     */
    DELICIOUS(2, "下饭菜"),
    /**
     * 快手菜
     */
    QUICK(3, "快手菜");

    private Integer code;

    private String value;

    CategoryTagEnum(Integer code, String value) {
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
        for (CategoryTagEnum element : CategoryTagEnum.values()) {
            map.put(element.getCode(), element.getValue());
        }
        return map;
    }

    public static CategoryTagEnum getByCode(Integer code) {
        if (code == null) {
            return null;
        }
        for (CategoryTagEnum element : CategoryTagEnum.values()) {
            if (code.equals(element.getCode())) {
                return element;
            }
        }
        return null;
    }

}
