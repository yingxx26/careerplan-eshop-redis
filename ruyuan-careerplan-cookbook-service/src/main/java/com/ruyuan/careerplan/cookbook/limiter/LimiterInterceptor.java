package com.ruyuan.careerplan.cookbook.limiter;

import com.google.common.util.concurrent.RateLimiter;
import com.jd.platform.hotkey.client.callback.JdHotKeyStore;
import com.ruyuan.careerplan.common.core.JsonResult;
import com.ruyuan.careerplan.common.utils.ServletUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Objects;

import static com.ruyuan.careerplan.common.constants.CoreConstant.REDIS_CONNECTION_FAILED;

/**
 * @author zhonghuashishan
 */
@Slf4j
public class LimiterInterceptor implements HandlerInterceptor {

    @Autowired
    private LimiterProperties limiterProperties;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        if (Objects.nonNull(JdHotKeyStore.get(REDIS_CONNECTION_FAILED))) {
            if (!limiterProperties.getNoRedisRateLimiter().tryAcquire()) {
                log.warn("redis 连接失败，全局限流");
                ServletUtil.writeJsonMessage(response, JsonResult.buildError("系统繁忙，请稍后重试"));
                return false;
            }
        } else {
            // 获取请求的requestMapping
            String requestMapping = getRequestMapping((HandlerMethod) handler);
            RateLimiter rateLimiter = limiterProperties.getApiRateLimiterMap().get(requestMapping);
            if (rateLimiter == null) {
                String property = limiterProperties.getProperties().getProperty(requestMapping);
                rateLimiter = RateLimiter.create(StringUtils.isEmpty(property) ? limiterProperties.getDefaultLimiter() : Double.valueOf(property));
                limiterProperties.getApiRateLimiterMap().put(requestMapping, rateLimiter);
            }
            if (!rateLimiter.tryAcquire()) {
                log.warn("全局限流");
                ServletUtil.writeJsonMessage(response, JsonResult.buildError("系统繁忙，请稍后重试"));
                return false;
            }
        }
        return true;
    }

    /**
     * 拼接处理方法对应的类的RequestMapping中的路径和方法中的RequestMapping中的路径
     *
     * @param handler
     * @return
     */
    private String getRequestMapping(HandlerMethod handler) {
        // 请求处理类的RequestMapping注解
        RequestMapping annotation = handler.getBean().getClass().getAnnotation(RequestMapping.class);
        String[] classMapping;
        if (annotation == null) {
            classMapping = new String[0];
        } else {
            classMapping = annotation.value();
        }
        // 请求处理方法的RequestMapping注解
        RequestMapping methodAnnotation = handler.getMethodAnnotation(RequestMapping.class);
        String[] methodMapping;
        if (methodAnnotation == null) {
            methodMapping = new String[0];
        } else {
            methodMapping = methodAnnotation.value();
        }

        /*
         * fixme 如果RequestMapping注解中配置了多个路径，那这里返回的路径可能就会与配置文件中的不一致
         * 这里我们只是简单的认为RequestMapping注解里面的路径只有一个
         * 其实也简单，把RequestMapping中的路径做一个排列组合，然后与请求的URI做一个比较，得出实际请求的路径
         * 那这样会有另外一个问题，
         * 就是限流配置文件中，设置的是路径 a,但是实际上调用的是路径 b,最终处理请求的是同一个方法，
         * 那么这个方法就会对应多个限流器。所以这里还能优化成，排列组合出来的结果中有一个与配置文件中相同，就返回这个结果。
         * 确保同一个处理请求的方法，对应的限流器只有一个
         */
        String requestMapping = (classMapping.length==0 ? "" : classMapping[0])
                + (methodMapping.length==0 ? "" : methodMapping[0]);
        return requestMapping;
    }
}
