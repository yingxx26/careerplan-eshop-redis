package com.ruyuan.careerplan.home.utils;

import java.util.*;

public class CollectionUtils {

    /**
     * 从集合随机取若干元素
     *
     * @param collection 待随机的元素
     * @param num        随机取的数量
     * @param <T>        类型
     * @return 随机集合
     */
    public static <T> List<T> random(Collection<T> collection, int num) {
        if (collection == null || collection.isEmpty()) {
            return new ArrayList<>();
        }
        if (collection.size() <= num) {
            return new ArrayList<>(collection);
        }
        List<T> randomList = new ArrayList<>(collection);
        Collections.shuffle(randomList);
        return randomList.subList(0, num);
    }

    public static void main(String[] args) {
        Collection<Integer> collection = Arrays.asList(1, 2, 3, 4, 5, 6);
        System.out.println(CollectionUtils.random(collection, 2));
    }
}
