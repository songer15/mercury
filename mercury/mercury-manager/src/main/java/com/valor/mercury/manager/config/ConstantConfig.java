package com.valor.mercury.manager.config;

import com.mfc.config.ConfigTools3;

/**
 * @author Gavin
 * 2019/12/2 14:40
 */
public class ConstantConfig {

    public static String localDBDriver() {
        return ConfigTools3.getConfigAsString("mercury.manager.db.driver");
    }

    public static String localDBUrl() {
        return ConfigTools3.getConfigAsString("mercury.manager.db.url");
    }

    public static String localDBName() {
        return ConfigTools3.getConfigAsString("mercury.manager.db.username");
    }

    public static String localDBPsw() {
        return ConfigTools3.getConfigAsString("mercury.manager.db.password");
    }

    public static String zkFilePath() {
        return ConfigTools3.getConfigAsString("mercury.manager.zk.file.path", "/MercuryManager-Election");
    }

    public static String zkClusterPath() {
        return ConfigTools3.getConfigAsString("mercury.manager.zk.cluster.path");
    }

    public static String localIP() {
        return ConfigTools3.getConfigAsString("mercury.manager.localIP");
    }

    public static String hiveUrl() {
        return ConfigTools3.getConfigAsString("mercury.manager.hive.url");
    }

    public static String hiveUserName() {
        return ConfigTools3.getConfigAsString("mercury.manager.hive.username", "");
    }

    public static String hivePassword() {
        return ConfigTools3.getConfigAsString("mercury.manager.hive.password", "");
    }

    public static String elasticMonitorUrl() {
        return ConfigTools3.getConfigAsString("mercury.manager.monitor.elasticsearch", "");
    }

    public static String flinkMonitorUrl() {
        return ConfigTools3.getConfigAsString("mercury.manager.monitor.flink", "");
    }

    public static String hdfsMonitorUrl() {
        return ConfigTools3.getConfigAsString("mercury.manager.monitor.hdfs", "");
    }

    public static String kafkaMonitorUrl() {
        return ConfigTools3.getConfigAsString("mercury.manager.monitor.kafka", "");
    }

    public static String influxDBMonitorUrl() {
        return ConfigTools3.getConfigAsString("mercury.manager.monitor.influxDB", "");
    }

    public static String yarnMonitorUrl() {
        return ConfigTools3.getConfigAsString("mercury.manager.monitor.yarn", "");
    }

    public static String hiveMonitorUrl() {
        return ConfigTools3.getConfigAsString("mercury.manager.monitor.hive", "");
    }

    public static String dingdingOath() {
        return ConfigTools3.getConfigAsString("mercury.manager.monitor.oauthKey.dingding", "");
    }

    public static Integer dingdingOathLevel() {
        return ConfigTools3.getConfigAsInt("mercury.manager.monitor.level.dingding", 3);
    }
}
