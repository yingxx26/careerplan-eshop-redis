package com.ruyuan.careerplan.common.utils;

import com.google.common.collect.Lists;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class PageUtil {

    private static final int PAGE_SIZE = 20;

    /**
     * 获取当前页的数据
     *
     * @param list   需要分页的list
     * @param pageNo 页码
     * @return java.util.List<T>
     */
    public static <T> List<T> pageListByPageNo(List<T> list, int pageNo) {
        List<T> pageCookIds = new ArrayList<>();
        if (CollectionUtils.isEmpty(list)) {
            return pageCookIds;
        }

        if (pageNo < 0) {
            pageNo = 0;
        }
        List<List<T>> partionList = Lists.partition(list, PAGE_SIZE);
        if (pageNo < partionList.size()) {
            pageCookIds = partionList.get(pageNo);
        }
        return pageCookIds;
    }

    /**
     * 将列表进行分页
     *
     * @param list     待分页的列表
     * @param pageSize 每页数量
     * @return java.util.List<java.util.List < T>>
     */
    public static <T> List<List<T>> pageListByPageSize(List<T> list, int pageSize) {
        if (CollectionUtils.isEmpty(list)) {
            return Collections.emptyList();
        }
        List<List<T>> result = new ArrayList<>();
        int totalSize = list.size();
        int pageNo = -1;
        do {
            int start = pageStart(pageNo, pageSize);
            int end = Math.min(pageEnd(pageNo, pageSize), totalSize);
            List<T> subList = list.subList(start, end);
            result.add(subList);
            pageNo++;
        } while (hasNextPage(pageNo, pageSize, totalSize));
        return result;
    }

    /**
     * 分页起始位置
     *
     * @param pageNo   页码
     * @param pageSize 每页数量
     */
    public static int pageStart(int pageNo, int pageSize) {
        return pageNo * pageSize;
    }

    /**
     * 分页结束位置
     *
     * @param pageNo   页码
     * @param pageSize 每页数量
     */
    public static int pageEnd(int pageNo, int pageSize) {
        return pageStart(pageNo, pageSize) + pageSize;
    }

    /**
     * redis分页结束位置
     *
     * @param pageNo   页码
     * @param pageSize 每页数量
     */
    public static int redisPageEnd(int pageNo, int pageSize) {
        return pageStart(pageNo, pageSize) + pageSize - 1;
    }

    /**
     * 判断是否有下一页
     *
     * @param pageNo   页码
     * @param pageSize 每页数量
     * @param count    总数
     */
    public static boolean hasNextPage(int pageNo, int pageSize, int count) {
        return redisPageEnd(pageNo, pageSize) < count - 1;
    }

}
