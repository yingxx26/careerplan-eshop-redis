package com.ruyuan.careerplan.common.aspect;

import com.alibaba.fastjson.JSONObject;
import com.jd.platform.hotkey.client.callback.JdHotKeyStore;
import com.ruyuan.careerplan.common.cache.CacheSupport;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.Signature;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;

import java.util.Objects;

import static com.ruyuan.careerplan.common.constants.CoreConstant.REDIS_CONNECTION_FAILED;

/**
 * @author zhonghuashishan
 */
@Aspect
@Slf4j
@Component
public class RedisAspect {

    /**
     * 切入点，RedisCache的所有方法
     */
    @Pointcut("execution(* com.ruyuan.careerplan.common.redis.RedisCache.*(..))")
    public void redisCachePointcut() {
    }

    /**
     * 切入点，RedisLock的所有方法
     */
    @Pointcut("execution(* com.ruyuan.careerplan.common.redis.RedisLock.*(..))")
    public void redisLockPointcut() {
    }



    /**
     * 环绕通知，在方法执行前后
     *
     * @param point 切入点
     * @return 结果
     * @throws Throwable
     */
    @Around("redisCachePointcut() || redisLockPointcut()")
    public Object around(ProceedingJoinPoint point) {
        // 签名信息
        Signature signature = point.getSignature();
        // 强转为方法信息
        MethodSignature methodSignature = (MethodSignature) signature;
        // 参数名称
        String[] parameterNames = methodSignature.getParameterNames();

        //执行的对象
        Object target = point.getTarget();

        log.debug("处理方法:{}.{}", target.getClass().getName() , methodSignature.getMethod().getName());
        Object[] parameterValues = point.getArgs();

        //查看入参
        log.debug("参数名:{}，参数值:{}", JSONObject.toJSONString(parameterNames), JSONObject.toJSONString(parameterValues));

        Class returnType = methodSignature.getReturnType();

        // 返回类型是否布尔类型
        boolean booleanType = boolean.class.equals(returnType) || Boolean.class.equals(returnType);
        try {
            if (Objects.nonNull(JdHotKeyStore.get(REDIS_CONNECTION_FAILED))) {
                // 值不为空表示redis连接失败，这里就不再继续请求redis了，直接返回false或者null
                log.error("获取缓存失败，redis连接失败，直接返回 false 或者 null");
                if (booleanType) {
                    return false;
                }
                return null;
            }
            return point.proceed();
        } catch (Throwable throwable) {
            log.error("执行方法:{}失败，异常信息:{}", methodSignature.getMethod().getName(), throwable);

            /*
             * redis连接失败，不抛异常，返回空值，
             * 继续用数据库提供服务，避免整个服务异常
             * 一分钟之内或者30秒之内出现了几次redis连接失败
             * 此时可以设置一个key，告诉hotkey，redis连接不上了，指定1分钟左右的过期时间
             * 下次获取缓存的时候，先根据hotkey来判断，redis是否异常了
             * hotkey在1分钟之后，会删除key，下次再有redis请求过来，重新去看redis能否连接
             * 这样可以简单的实现redis挂掉之后直接走数据库的降级
             */
            if (JdHotKeyStore.isHotKey(REDIS_CONNECTION_FAILED)) {
                JdHotKeyStore.smartSet(REDIS_CONNECTION_FAILED, CacheSupport.EMPTY_CACHE);
            }

            // 让后续操作继续，判断返回类型是Boolean则返回false，其他类型返回null
            log.error("缓存操作失败，直接返回 false 或者 null");
            if (booleanType) {
                return false;
            }
            return null;
        }
    }


}
