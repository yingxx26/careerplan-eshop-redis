package com.ruyuan.careerplan.cookbook.enums;

import com.google.common.collect.Maps;

import java.util.Map;

/**
 * @author zhonghuashishan
 */
public enum CookbookTypeEnum {

    /**
     * Professionally-generated Content的缩写，专业生产内容
     */
    PGC(1, "专业生产内容"),

    /**
     * User-generated Content的缩写，用户生产内容
     */
    UGC(2, "用户生产内容");

    private Integer code;

    private String value;

    CookbookTypeEnum(Integer code, String value) {
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
        for (CookbookTypeEnum element : CookbookTypeEnum.values()) {
            map.put(element.getCode(), element.getValue());
        }
        return map;
    }

    public static CookbookTypeEnum getByCode(Integer code) {
        if (code == null) {
            return null;
        }
        for (CookbookTypeEnum element : CookbookTypeEnum.values()) {
            if (code.equals(element.getCode())) {
                return element;
            }
        }
        return null;
    }

}
