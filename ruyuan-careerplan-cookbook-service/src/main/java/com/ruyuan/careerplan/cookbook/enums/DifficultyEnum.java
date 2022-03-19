package com.ruyuan.careerplan.cookbook.enums;

import com.google.common.collect.Maps;

import java.util.Map;

/**
 * @author zhonghuashishan
 */
public enum DifficultyEnum {

    /**
     * 简单
     */
    EASY(1, "简单"),
    /**
     * 一般
     */
    AVERAGE(2, "一般"),
    /**
     * 较难
     */
    DIFFICULT(3, "较难"),
    /**
     * 极难
     */
    EXTREMELY_DIFFICULT(4, "极难");

    private Integer code;

    private String value;

    DifficultyEnum(Integer code, String value) {
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
        for (DifficultyEnum element : DifficultyEnum.values()) {
            map.put(element.getCode(), element.getValue());
        }
        return map;
    }

    public static DifficultyEnum getByCode(Integer code) {
        if (code == null) {
            return null;
        }
        for (DifficultyEnum element : DifficultyEnum.values()) {
            if (code.equals(element.getCode())) {
                return element;
            }
        }
        return null;
    }

}
