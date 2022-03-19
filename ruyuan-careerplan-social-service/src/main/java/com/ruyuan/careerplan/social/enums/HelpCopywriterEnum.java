package com.ruyuan.careerplan.social.enums;

/**
 * 助力文案枚举
 *
 * @author zhonghuashishan
 */
public enum HelpCopywriterEnum {

    /**
     * 助力成功
     */
    HELP_SUCCESS(1, "谢谢你帮我领了%s元"),

    /**
     * 已助力-再次进入
     */
    HELP_SUCCESS_AGAIN(2, "你已经帮我领了%s元"),

    /**
     * 助力失败-活动已完成
     */
    HELP_FAILURE_FINISH(3, "我已经领完%s元"),

    /**
     * 助力失败-达助力上限
     */
    HELP_FAILURE_COUNT_LIMIT(4, "你今天不能再帮人领钱了"),

    /**
     * 助力失败-不能给同一好友助力
     */
    HELP_FAILURE_SAME_LIMIT(5, "%s天内不能给同一朋友助力"),

    /**
     * 助力失败-命中风控
     */
    HELP_FAILURE_RISK(6, "你的账号有风险不能助力"),

    /**
     * 新用户
     */
    NEW_USER_HELP_SUCCESS(7, "谢谢你帮我领了%s元"),

    /**
     * 已助力-再次进入
     */
    NEW_USER_HELP_SUCCESS_AGAIN(8, "你已经帮我领了%s元"),

    /**
     * 助力失败-已完成
     */
    NEW_USER_HELP_FAILURE_FINISH(9, "我已经领完了%s元"),

    /**
     * 助力失败-达生命周期助力上限
     */
    NEW_USER_HELP_FAILURE_COUNT_LIMIT(10, "作为新用户已经不能再帮人领钱了"),

    /**
     * 助力失败-不能给同一好友助力
     */
    NEW_USER_HELP_FAILURE_SAME_LIMIT(11, "%s天内不能给同一朋友助力"),

    /**
     * 助力失败-命中风控
     */
    NEW_USER_HELP_FAILURE_RISK(12, "你的账号有风险，不能助力"),

    /**
     * 助力失败
     */
    HELP_FAILURE(13, "谢谢你帮我助力"),
    ;

    private int code;
    private String desc;

    HelpCopywriterEnum(int code, String desc) {
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
