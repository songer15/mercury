package com.valor.mercury.spider.springbatch.reader;
import java.util.HashMap;
import java.util.Map;

public class NumberIncPageItemReader extends AbstractJdbcReader {

    @Override
    public Map<String, Object> getParamsMap() {
        Map<String, Object> map = new HashMap<>();
        map.put("pre", spiderJob.incValue);
        return map;
    }
}
