package com.ruyuan.careerplan.social.constants;

/**
 * 助力文案
 *
 * @author zhonghuashishan
 */
public class WordRoundConstant {

    private static final String FIRST = "一分也是情";
    private static final String SECOND = "礼轻情意重";
    private static final String THIRD = "只能帮你到这了";
    private static final String FOURTH = "助你一臂之力";
    private static final String FIVETH = "快来感谢我";
    private static final String SIXTH = "朋友一生一起走";

    /**
     * 根据助力金额范围返回文档
     *
     * @param amount 助力金额（单位：分）
     * @return java.lang.String
     * @author zhonghuashishan
     */
    public static String getRoundWord(Integer amount) {
        if (amount == 1) {
            return FIRST;
        } else if (amount > 1 && amount < 30) {
            return SECOND;
        } else if (amount >= 30 && amount < 50) {
            return THIRD;
        } else if (amount >= 50 && amount < 80) {
            return FOURTH;
        } else if (amount >= 80 && amount < 200) {
            return FIVETH;
        } else if (amount >= 200) {
            return SIXTH;
        }
        return FIVETH;
    }
}
