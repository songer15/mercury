package com.valor.mercury.task.flink;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Main {

    private static Logger logger = LoggerFactory.getLogger(Main.class);

    public static void main(String[] args) throws Exception {
        try {
            Class clazz = Class.forName(args[0]);
            MetricAnalyse metricAnalyse = (MetricAnalyse) clazz.newInstance();
            metricAnalyse.run(args);
        } catch (Exception e) {
            logger.error("execute job error", e);
        }
    }
}
