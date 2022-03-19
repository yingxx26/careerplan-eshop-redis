package com.ruyuan.careerplan.common.utils;

import org.springframework.beans.BeanUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

/**
 * 数据拷贝工具类
 *
 * @author zhonghuashishan
 */
public class BeanCopyUtil extends BeanUtils {
    /**
     * List数据拷贝
     *
     * @param sources
     * @param target
     * @return java.util.List<T>
     * @author zhonghuashishan
     */
    public static <S, T> List<T> copyListProperties(List<S> sources, Supplier<T> target) {
        return copyListProperties(sources, target, null);
    }

    /**
     * 可回调List数据拷贝
     *
     * @param sources
     * @param target
     * @param callBack
     * @return java.util.List<T>
     * @author zhonghuashishan
     */
    public static <S, T> List<T> copyListProperties(List<S> sources, Supplier<T> target, BeanCopyUtilCallBack<S, T> callBack) {
        List<T> list = new ArrayList<>(sources.size());
        for (S source : sources) {
            T t = target.get();
            copyProperties(source, t);
            list.add(t);
            if (callBack != null) {
                callBack.callBack(source, t);
            }
        }
        return list;
    }
}
