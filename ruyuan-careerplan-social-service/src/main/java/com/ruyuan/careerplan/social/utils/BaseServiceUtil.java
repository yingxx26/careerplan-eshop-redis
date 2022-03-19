package com.ruyuan.careerplan.social.utils;

import lombok.extern.slf4j.Slf4j;

import javax.servlet.http.HttpServletRequest;

/**
 * 基本服务工具
 *
 * @author zhonghuashishan
 */
@Slf4j
public abstract class BaseServiceUtil {

    private static final String unknown = "unknown";

    private static final int length = 10;
    /**
     * 获取IP地址
     *
     * @param request
     * @return java.lang.String
     * @author zhonghuashishan
     */
    public static  String getIpAddress(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");

        if (ip != null && ip.length() != 0 && !unknown.equalsIgnoreCase(ip)) {
            if (ip.length() < length) {
                return ip;
            }

            String[] ips = ip.split(",");
            for (String strIp : ips) {
                if (!unknown.equalsIgnoreCase(strIp)) {
                    ip = strIp;
                    break;
                }
            }

            return ip;
        }

        if (ip == null || ip.length() == 0 || unknown.equalsIgnoreCase(ip)) {
            ip = request.getHeader("Proxy-Client-IP");
        }

        if (ip == null || ip.length() == 0 || unknown.equalsIgnoreCase(ip)) {
            ip = request.getHeader("WL-Proxy-Client-IP");
        }

        if (ip == null || ip.length() == 0 || unknown.equalsIgnoreCase(ip)) {
            ip = request.getHeader("HTTP_CLIENT_IP");
        }

        if (ip == null || ip.length() == 0 || unknown.equalsIgnoreCase(ip)) {
            ip = request.getHeader("HTTP_X_FORWARDED_FOR");
        }

        if (ip == null || ip.length() == 0 || unknown.equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }

        return ip;
    }
}
