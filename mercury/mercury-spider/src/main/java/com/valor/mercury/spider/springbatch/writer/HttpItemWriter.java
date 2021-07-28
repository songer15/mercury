package com.valor.mercury.spider.springbatch.writer;

import com.mfc.config.ConfigTools3;
import com.valor.mercury.sender.service.MercurySender;
import com.valor.mercury.spider.model.SpiderJob;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

/**
 * Http写入模块
 */
public class HttpItemWriter extends BaseWriter {

    private static Logger logger = LoggerFactory.getLogger(HttpItemWriter.class);

    @Override
    public void init(SpiderJob spiderJob) {
        this.spiderJob = spiderJob;
        List<String> urlList = ConfigTools3.getAsList("mercury.receiver.url");
        Map<String, String[]> urls = new HashMap<>();
        urls.put("remote", urlList.toArray(new String[urlList.size()]));
        MercurySender.init(urls, "spider", "spider", 5000, 100, 2, 5, 200000, true, false);
        logger.info("HttpItemWriter initialized. ");
    }

    @Override
    public void write(List<? extends HashMap> items) {
        long start = System.currentTimeMillis();
        if (items.size() != 0) {
            for (HashMap map : items) {
                MercurySender.put("remote", spiderJob.outPutName, map);
            }
            spiderJob.sendNumber = spiderJob.sendNumber + items.size();
        }
        long end = System.currentTimeMillis();
        logger.info("mercury sender cost {} miliseconds to put data into queue", end - start);
        logger.info("output_name: {},  write size: {}", spiderJob.outPutName, items.size());
    }
}
