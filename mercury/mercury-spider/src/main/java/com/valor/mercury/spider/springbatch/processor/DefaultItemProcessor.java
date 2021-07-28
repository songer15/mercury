package com.valor.mercury.spider.springbatch.processor;

import com.valor.mercury.spider.model.SpiderJob;

import java.util.HashMap;

/**
 * 不对原始数据和增量标记做任何操作
 */
public class DefaultItemProcessor extends BaseProcessor {

    @Override
    public void init(SpiderJob spiderJob) {
        this.spiderJob = spiderJob;
    }

    @Override
    public HashMap<String,Object> process(HashMap<String,Object> item){
        spiderJob.processNumber = spiderJob.processNumber + 1;
        return item;
    }
}
