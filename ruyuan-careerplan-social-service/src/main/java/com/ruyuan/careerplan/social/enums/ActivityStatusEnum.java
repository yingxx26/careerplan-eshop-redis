package com.ruyuan.careerplan.social.enums;

/**
 * 活动状态
 *
 * @author zhonghuashishan
 */
public enum ActivityStatusEnum {

    UNDERWAY("活动中", 0),
    FINISH("活动完成", 1),
    EXPIRED("活动过期", 2),
    WAITING("活动未开始", 3),
    ;


    private String desc;
    private int code;

    ActivityStatusEnum(String desc, int code) {
        this.desc = desc;
        this.code = code;
    }

    public String getDesc() {
        return this.desc;
    }

    public int getCode() {
        return this.code;
    }
}
