package com.valor.mercury.spider.springbatch.processor;

;
import com.valor.mercury.common.util.DateUtils;
import com.valor.mercury.spider.model.SpiderJob;

import java.util.Date;
import java.util.HashMap;

public class TimeIncItemProcessor extends BaseProcessor {

    private Date latestDate;

    @Override
    public void init(SpiderJob spiderJob){
        this.spiderJob = spiderJob;
        latestDate = spiderJob.incValue == null ? new Date(0) : DateUtils.tryParse(spiderJob.incValue);
        spiderJob.incValue = DateUtils.tryFormat(latestDate);
    }

    @Override
    public HashMap<String, Object> process(HashMap<String, Object> item) {
        //更新增量值
        Object dateField = item.get(spiderJob.incTag.split(",")[0]);
        if (dateField instanceof Date) {
            Date date = (Date) dateField;
            if (latestDate.before(date)) {
                latestDate = date;
                String timeString = DateUtils.tryFormat(latestDate);
                if (timeString != null)
                    spiderJob.incValue = timeString;
            }
        } else if (dateField instanceof Long) {
            Long millis = (Long) dateField;
            Date date = new Date(millis);
            if (latestDate.before(date)) {
                latestDate = date;
                spiderJob.incValue = millis.toString();
            }
        } else if (dateField instanceof String) {
            String dateString = (String) dateField;
            Date date = DateUtils.tryParse(dateString);
            if (date != null && latestDate.before(date)) {
                latestDate = date;
                spiderJob.incValue = dateString;
            }
        }
        spiderJob.processNumber = spiderJob.processNumber + 1;
        return item;
    }
}
