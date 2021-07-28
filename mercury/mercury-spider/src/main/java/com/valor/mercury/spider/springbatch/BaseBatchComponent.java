package com.valor.mercury.spider.springbatch;

import com.valor.mercury.spider.model.SpiderJob;

public abstract class BaseBatchComponent {
    protected SpiderJob spiderJob;
    abstract public void init(SpiderJob spiderJob) throws Exception ;
}
