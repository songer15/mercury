package com.vms.metric.analyse.config;

import com.mfc.config.ConfigTools3;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public class MetricAnalyseConfig {

    private static Logger logger = LoggerFactory.getLogger(MetricAnalyseConfig.class);


    public static String getServerUrlRequest() {
        return ConfigTools3.getConfigAsString("metric.task.url.request");
    }

    public static String getServerUrlResponse() {
        return ConfigTools3.getConfigAsString("metric.task.url.response");
    }

    public static Long getLocalServerId() {
        return ConfigTools3.getConfigAsLong("metric.task.localServerName");
    }

    public static String getHttpAuthenticationPassword() {
        return ConfigTools3.getConfigAsString("metric.task.authentication.password");
    }

    public static String getHttpAuthenticationKey() {
        return ConfigTools3.getConfigAsString("metric.task.authentication.key");
    }

    public static List<String> getElsHost() {
        return ConfigTools3.getAsList("metric.elasticsearch.host",",");
    }

    public static Integer getElsPost() {
        return ConfigTools3.getConfigAsInt("metric.elasticsearch.port",9200);
    }
}
