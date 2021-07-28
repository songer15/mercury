package com.valor.mercury.spider.springbatch.processor;

import com.valor.mercury.spider.model.SpiderJob;

import java.util.HashMap;

public class IDIncItemProcessor extends BaseProcessor {

    private Long incTab;

    @Override
    public void init(SpiderJob spiderJob) {
        this.spiderJob = spiderJob;
        incTab = spiderJob.incValue == null ? 0 : Long.parseLong(spiderJob.incValue);
    }

    @Override
    public HashMap<String, Object> process(HashMap<String, Object> item) throws IllegalArgumentException {
        Long tab = ((Number) item.get(spiderJob.incTag.split(",")[0])).longValue();
        if (incTab < tab) {
            incTab = tab;
            spiderJob.incValue = (incTab.toString());
        }
        spiderJob.processNumber = spiderJob.processNumber + 1;
        return item;
    }
}
