package com.ruyuan.careerplan.common.utils;

/**
 * 函数式接口
 *
 * @author zhonghuashishan
 */
@FunctionalInterface
public interface BeanCopyUtilCallBack<S, T> {
    /**
     * 定义默认回调方法
     *
     * @param t
     * @param s
     * @return void
     * @author zhonghuashishan
     */
    void callBack(S t, T s);
}
