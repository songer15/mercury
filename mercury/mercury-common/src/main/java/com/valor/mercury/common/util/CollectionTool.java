package com.valor.mercury.common.util;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.*;

public class CollectionTool {

//    @SuppressWarnings("unchecked")
//    public static <T, U extends SplitWorkThread> void splitCollectionThenApply(Iterable<T> collection, int n, U u, RouterDdo router) {
//        CountDownLatch countDownLatch = new CountDownLatch(n);
//        Map<Integer, List<T>> map = new HashMap<>();
//        int total = 0;
//        for (T t : collection) {
//            total++;
//        }
//        int splitSize = (total / n) + 1;
//        for (int i = 0; i < n; i++) {
//            map.put(i, new ArrayList<>(splitSize));
//        }
//
//        //添加元素到list
//        int i = 0;
//        for (T t : collection ) {
//            int index = i % n;
//            map.get(index).add(t);
//            i++;
//        }
//
//        for (List<T> subList : map.values()) {
//            u.initThread(subList, countDownLatch, router);
//            u.start();
//        }
//        try {
//            countDownLatch.await();
//        } catch (InterruptedException ex) {
//
//        }
//    }
    public static <T> List<List<T>> splitList(List<T> list, int len) {

    if (list == null || list.isEmpty() || len < 1) {
        return Collections.emptyList();
    }

    List<List<T>> result = new ArrayList<>();

    int size = list.size();
    int count = (size + len - 1) / len;

    for (int i = 0; i < count; i++) {
        List<T> subList = list.subList(i * len, ((i + 1) * len > size ? size : len * (i + 1)));
        result.add(subList);
    }

    return result;

    }


}
