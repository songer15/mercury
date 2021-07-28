package com.valor.mercury.common.util;

import com.google.gson.Gson;
import com.mfc.config.ConfigTools3;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PropertyUtil {

    private static Logger logger = LoggerFactory.getLogger(PropertyUtil.class);
    private static Map<String, Object> map = new HashMap<>();
    private static List<String> requestUrls = new ArrayList<>();
    private static List<String> responseUrls = new ArrayList<>();


    /**************************************************************************************************************************/

    public static String getDBDriverName(String name) {
        String pro = ConfigTools3.getConfigAsString("metric.database." + name + ".driver");
        logger.info("getDBDriverName:{}", pro);
        return pro;
    }

    public static String getDBUrl(String name) {
        String pro = ConfigTools3.getConfigAsString("metric.database." + name + ".url");
        logger.info("getDBUrl:{}", pro);
        return pro;
    }

    public static String getDBUserName(String name) {
        String pro = ConfigTools3.getConfigAsString("metric.database." + name + ".username");
        logger.info("getDBUserName:{}", pro);
        return pro;
    }

    public static String getDBPassword(String name) {
        return  ConfigTools3.getConfigAsString("metric.database." + name + ".password");
    }


    /**************************************************************************************************************************/

    public static Map<String, Object> initConfig(String json) {
        Gson gson = new Gson();
        return gson.fromJson(json, HashMap.class);
    }

    public static <T> T getConfig(String configName, Class<T> z) {
        if (map.containsKey(configName))
            return (T) map.get(configName);
        else {
            logger.info("getConfig error", configName);
            return null;
        }
    }

    public static String getHttpAuthenticationPassword() {
        return ConfigTools3.getConfigAsString("metric.task.authentication.password");
    }

    public static String getHttpAuthenticationKey() {
        return ConfigTools3.getConfigAsString("metric.task.authentication.key");
    }

    public static List<String> getTaskRequestUrls() {
        if (requestUrls.isEmpty()) {
            List<String> ips = ConfigTools3.getAsList("mercury.url.ips");
            for (String ip : ips) {
                requestUrls.add("http://" + ip + ConfigTools3.getConfigAsString("metric.url.task.request.path"));
            }
        }
        return requestUrls;
    }

    public static List<String> getConfigUrls() {
        if (requestUrls.isEmpty()) {
            List<String> ips = ConfigTools3.getAsList("mercury.url.ips");
            for (String ip : ips) {
                requestUrls.add("http://" + ip + ConfigTools3.getConfigAsString("metric.url.config.request.path"));
            }
        }
        return requestUrls;
    }

    public static List<String> getTaskReportUrls() {
        if (responseUrls.isEmpty()) {
            List<String> ips = ConfigTools3.getAsList("mercury.url.ips");
            for (String ip : ips) {
                responseUrls.add("http://" + ip + ConfigTools3.getConfigAsString("metric.url.task.response.path"));
            }
        }
        return responseUrls;
    }

    public static long getLocalServerId() {
        return ConfigTools3.getConfigAsLong("metric.task.localServerId");
    }

    public static List<String> getEsHost(String name) {
        return ConfigTools3.getConfigAsList("metric.reader.elastic." + name + ".host");
    }

}
