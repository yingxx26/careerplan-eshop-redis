package com.ruyuan.careerplan.social.constants;

/**
 * 社交常量类
 *
 * @author zhonghuashishan
 */
public class SocialConstant {

    /**
     * 社交活动配置
     */
    public static final String SOCIAL_ACTIVITY_CONFIG = "social_activity_config";

    /**
     * 团活动信息缓存
     */
    public final static String SOCIAL_MASTER_INFO_KEY = "social_master_info:";

    /**
     * 当前阶段缓存
     */
    public final static String CURRENT_STAGE_KEY = "current_stage:";

    /**
     * 助力阶段需要邀请的人数
     */
    public final static String HELP_STAGE_MEMBER_KEY = "help_stage_member:";

    /**
     * 社交活动金额提现倒计时x小时
     */
    public static final String WITHDRAWAL_COUNTDOWN_KEY = "withdrawal_countdown:";

    /**
     * 已分享群聊/好友次数
     */
    public static final String COOKBOOK_SHARE_COUNT_KEY = "cookbook_share_count:";

    /**
     * 是否已助力，有值则助力
     */
    public static final String JOIN_HELP_FLAG_KEY = "join_help_flag:";

    /**
     * 助力次数，针对助力成功的统计
     */
    public static final String SUCCESS_HELP_COUNT_KEY = "success_help_count:";

    /**
     * x天内不能给同一好友助力（读取配置）
     */
    public static final String HELP_INTERVAL_LIMIT_KEY = "help_interval_limit:";

    /**
     * 当前阶段是否弹额外奖励金额
     */
    public static final String CURRENT_STAGE_PREMIUMS_KEY = "current_stage_premiums:";

    /**
     * 活动初始化锁
     */
    public static final String INIT_SOCIAL_ACTIVITY_LOCK_KEY = "init_social_activity_lock:";

    /**
     * 参团活动入口分布式锁，防止同一个人多次助力
     */
    public static final String JOIN_HELP_SAME_LOCK_KEY = "join_help_same_lock:";

    /**
     * 参团助力人分布式锁，防止并发助力超额
     */
    public static final String JOIN_HELP_CONCURRENCE_LOCK_KEY = "join_help_concurrence_lock:";

    /**
     * 触发活动开始次数
     */
    public static final String ACTIVITY_START_COUNT_KEY = "activity_start_count:";

    /**
     * 发券有效期间隔x天（读取配置）
     */
    public static final String SEND_COUPON_INTERVAL_LIMIT_KEY = "send_coupon_interval_limit:";

    /**
     * 额外奖励默认昵称
     */
    public static final String PREMIUMS_NICKNAME = "额外奖励";

    /**
     * 额外奖励默认头像
     */
    public static final String PREMIUMS_AVATAR = "https://image.png";

    /**
     * 发送打款消息
     */
    public static final String PAYING_AMOUNT_TOPIC = "PAYING_AMOUNT_TOPIC";

    /**
     * 发送活动完成消息
     */
    public static final String ACTIVITY_FINISH_TOPIC = "ACTIVITY_FINISH_TOPIC";

    /**
     * 发送活动过期消息
     */
    public static final String ACTIVITY_EXPIRED_TOPIC = "ACTIVITY_EXPIRED_TOPIC";

}
