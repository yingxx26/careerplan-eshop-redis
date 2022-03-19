package com.ruyuan.careerplan.goodscart.enums;

import com.google.common.collect.Maps;

import java.util.Map;

/**
 * 删除标记枚举
 *
 * @author zhonghuashishan
 */
public enum DelFlagEnum {

    /**
     * 有效
     */
    EFFECTIVE(1, "有效"),

    /**
     * 删除
     */
    DISABLED(0, "删除");

    private Integer code;

    private String value;

    DelFlagEnum(Integer code, String value) {
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
        for (DelFlagEnum element : DelFlagEnum.values()) {
            map.put(element.getCode(), element.getValue());
        }
        return map;
    }

    public static DelFlagEnum getByCode(Integer code) {
        if (code == null) {
            return null;
        }
        for (DelFlagEnum element : DelFlagEnum.values()) {
            if (code.equals(element.getCode())) {
                return element;
            }
        }
        return null;
    }

}