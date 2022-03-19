package com.ruyuan.careerplan.social.utils;

import com.ruyuan.careerplan.social.domain.dto.SocialMasterExtendDTO;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;

/**
 * 社交工具类
 *
 * @author zhonghuashishan
 */
public class SocialUtil {

    /**
     * 当前阶段还需邀请人数
     *
     * @param currentStage
     * @param socialMasterExtendDTO
     * @param stageMemberList
     * @return int
     * @author zhonghuashishan
     */
    public static int stageMembers(int currentStage, SocialMasterExtendDTO socialMasterExtendDTO, List<Integer> stageMemberList) {
        int stageMemberCount = 0;

        if (currentStageMemberSuccess(currentStage, socialMasterExtendDTO, stageMemberList)) {
            currentStage += 1;
        }

        for (int i = 0; i < currentStage; i++) {
            stageMemberCount += stageMemberList.get(i);
        }
        return stageMemberCount;
    }

    /**
     * 判断是否将要满足阶段
     * 忽略最后一阶段
     *
     * @param currentStage
     * @param socialMasterExtendDTO
     * @param memberCountList
     * @return boolean
     * @author zhonghuashishan
     */
    public static boolean currentStageMemberSuccess(int currentStage, SocialMasterExtendDTO socialMasterExtendDTO, List<Integer> memberCountList) {
        int stageMemberCount = 0;
        for (int i = 0; i < currentStage; i++) {
            stageMemberCount += memberCountList.get(i);
        }
        return stageMemberCount == socialMasterExtendDTO.getHelpCount() && currentStage != memberCountList.size();
    }

    /**
     * 获取当天最后时间
     *
     * @param
     * @return java.util.Date
     * @author zhonghuashishan
     */
    public static Date getCurrentEndDate() {
        Calendar currentDay = Calendar.getInstance();
        currentDay.set(currentDay.get(Calendar.YEAR), currentDay.get(Calendar.MONTH), currentDay.get(Calendar.DAY_OF_MONTH),
                23, 59, 59);
        return currentDay.getTime();
    }

    /**
     * 获取指定天数日期
     *
     * @param amount 整数增加天数，负数减少天数
     * @return java.util.Date
     * @author zhonghuashishan
     */
    public static Date getNextDay(int amount) {
        Date date = new Date();
        Calendar calendar = new GregorianCalendar();
        calendar.setTime(date);
        // 天数增加或减少
        calendar.add(Calendar.DATE, amount);
        return calendar.getTime();
    }

    /**
     * 获取指定小时数日期
     *
     * @param amount 整数增加小时数，负数减少小时数
     * @return java.util.Date
     * @author zhonghuashishan
     */
    public static Date getNextHour(int amount) {
        Date date = new Date();
        Calendar calendar = new GregorianCalendar();
        calendar.setTime(date);
        // 小时数增加或减少
        calendar.add(Calendar.HOUR, amount);
        return calendar.getTime();
    }

    /**
     * 剩余指定天数-毫秒
     *
     * @param days
     * @return java.lang.Long
     * @author zhonghuashishan
     */
    public static Long getCustomdayLastTimeMillisecond(int days) {
        if (days > 0) {
            days--;
        } else {
            days = 0;
        }
        return days * 86400000 + getTodayLastTimeMillisecond();
    }

    /**
     * 剩余当天-毫秒
     *
     * @param
     * @return java.lang.Long
     * @author zhonghuashishan
     */
    public static Long getTodayLastTimeMillisecond() {
        Calendar cal = Calendar.getInstance();
        cal.setTime(new Date());
        cal.add(Calendar.DAY_OF_YEAR, 1);
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        return cal.getTimeInMillis() - System.currentTimeMillis();
    }

}

