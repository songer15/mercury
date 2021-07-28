package com.valor.mercury.spider.springbatch.reader;

import com.valor.mercury.common.util.DateUtils;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class TimeIncPageItemReader extends AbstractJdbcReader {

    @Override
    public Map<String, Object> getParamsMap() {
        Map<String, Object> map = new HashMap<>();
        Date date = DateUtils.tryParse(spiderJob.incValue);
        if (date != null) {
            map.put("pre", date);
        } else  {
            long millis = Long.parseLong(spiderJob.incValue);
            map.put("pre", new Date(millis));
        }
        return map;
    }
}
