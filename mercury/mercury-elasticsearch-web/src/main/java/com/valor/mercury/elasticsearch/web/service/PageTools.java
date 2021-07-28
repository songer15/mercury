package com.valor.mercury.elasticsearch.web.service;



import com.valor.mercury.elasticsearch.web.model.PageResult;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;


public class PageTools {

    public static PageResult pageResult(List data, int pagerSize, int pagerId) {
        List list = new ArrayList<>();
        if (data == null || data.isEmpty())
            return new PageResult(Collections.emptyList());
        int pagerNo = (int) Math.ceil((double) data.size() / pagerSize);

        if (pagerId <= 0)
            pagerId = 1;
        if (pagerId >= pagerNo)
            pagerId = pagerNo;


        for (int i = (pagerId - 1) * pagerSize; i < pagerId * pagerSize; i++) {
            if (i < data.size())
                list.add(data.get(i));
            else
                break;
        }

        return new PageResult(data.size(),list);
    }
}
