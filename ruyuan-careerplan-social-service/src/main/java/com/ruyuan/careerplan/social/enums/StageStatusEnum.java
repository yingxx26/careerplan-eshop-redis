package com.ruyuan.careerplan.social.enums;

public enum StageStatusEnum {

    SHARE_GROUP("群分享", 1),
    BUITY_ING("任务中", 2),
    RED_PACK("弹红包", 3),
    BUITY_NOT("无任务", 4),
    BUITY_NOT_TRIGGER("最后一个阶段", 5);

    private String desc;
    private int code;

    StageStatusEnum(String desc, int code) {
        this.desc = desc;
        this.code = code;
    }

    public String getDesc() {
        return desc;
    }

    public int getCode() {
        return code;
    }

}
