package com.ruyuan.careerplan.cookbook.aspect;

import com.alibaba.fastjson.JSONObject;
import com.ruyuan.careerplan.common.exception.BaseBizException;
import com.ruyuan.careerplan.cookbook.exception.CookbookErrorCodeEnum;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.Signature;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import javax.validation.ConstraintViolation;
import javax.validation.Validator;
import java.util.Set;

/**
 * @author zhonghuashishan
 */
@Aspect
@Slf4j
@Component
public class ParamsValidatorAspect {


    @Autowired
    private Validator validator;


    /**
     * 切入点，@ParamsValidate 注解标注的
     */
    @Pointcut("@annotation(com.ruyuan.careerplan.cookbook.annotation.ParamsValidate)")
    public void pointcut() {
    }



    /**
     * 环绕通知，在方法执行前后
     *
     * @param point 切入点
     * @return 结果
     * @throws Throwable
     */
    @Around("pointcut()")
    public Object around(ProceedingJoinPoint point) throws Throwable {
        // 签名信息
        Signature signature = point.getSignature();
        // 强转为方法信息
        MethodSignature methodSignature = (MethodSignature) signature;
        // 参数名称
        String[] parameterNames = methodSignature.getParameterNames();
        //执行的对象
        Object target = point.getTarget();

        log.info("请求处理方法:{}.{}", target.getClass().getName() , methodSignature.getMethod().getName());
        Object[] parameterValues = point.getArgs();

        //查看入参
        log.info("请求参数名:{}，请求参数值:{}", JSONObject.toJSONString(parameterNames), JSONObject.toJSONString(parameterValues));

        // 参数校验
        for (Object parameterValue : parameterValues) {
            Set<ConstraintViolation<Object>> validateResult = validator.validate(parameterValue);

            if (!CollectionUtils.isEmpty(validateResult)) {
                StringBuffer sb = new StringBuffer();
                for (ConstraintViolation<Object> objectConstraintViolation : validateResult) {
                    sb.append(objectConstraintViolation.getMessage());
                    sb.append(",");
                }
                throw new BaseBizException(CookbookErrorCodeEnum.PARAM_CHECK_ERROR, sb.substring(0, sb.length()-1));
            }
        }
        try {
            Object response = point.proceed();
            return response;
        } catch (Throwable throwable) {
            log.error("执行方法:{}失败，异常信息:{}", methodSignature.getMethod().getName(), throwable);
            throw throwable;
        }
    }


}
