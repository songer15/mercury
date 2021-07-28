package com.valor.mercury.task.flink.util;

/**
 * @author Gavin
 * 2019/10/12 16:32
 */
public class Constants {

    public static String groupID = "Metric";
    public static String bootstrapServer = "172.16.0.210:9092,172.16.0.211:9092,172.16.0.212:9092";
    public static String PMBootstrapServer = "51.161.13.211:9092";
    public static String esHost = "172.16.0.201,172.16.0.202";
    public static String influxDbUrl = "http://147.135.46.20:8087";
    public static String PMInfluxDbUrl = "http://147.135.46.219:8086";
    public static String MysqlUserName = "root";
    public static String MysqlPassword = "hAbxj4iq#h@yMf&j";
    public static String MysqlUrl = "jdbc:mysql://172.16.0.210:3306/metric_server?useUnicode=true&characterEncoding=UTF-8&rewriteBatchedStatements=true&useServerPrepStmts=false";


//    public static String groupID = "TEST";
//    public static String bootstrapServer = "144.217.74.120:9092";
//    public static String PMBootstrapServer = "144.217.74.120:9092";
//    public static String esHost = "144.217.74.120";
//    public static String influxDbUrl = "http://144.217.74.120:8087";
//    public static String PMInfluxDbUrl = "http://144.217.74.120:8086";
//    public static String MysqlUserName = "root";
//    public static String MysqlPassword = "6666";
//    public static String MysqlUrl = "jdbc:mysql://144.217.74.120:3306/metric_server?useUnicode=true&characterEncoding=UTF-8&rewriteBatchedStatements=true&useServerPrepStmts=false";


    //MFC登录统计
    public static String mfcLoginTopicName = "mfc_user_action_log";
    public static String mfcLoginEsIndexName = "flink_mfc_metric_login";

    //MFC播放统计
    public static String mfcPlayTopicName = "mfc_app_play_action";
    public static String mfcPlayEsIndexName = "flink_mfc_metric_play";
    public static String mfcPlayTableName = "mfc_play_action_table";

    //MFC页面浏览统计
    public static String mfcViewTopicName = "mfc_app_page_action";
    public static String mfcViewEsIndexName = "flink_mfc_metric_view";
    public static String mfcViewTableName = "mfc_page_action_table";

    //MFC搜索统计
    public static String mfcSearchTopicName = "mfc_app_search_action";
    public static String mfcSearchEsIndexName = "flink_mfc_metric_search";
    public static String mfcSearchTableName = "mfc_search_action_table";

    //MFC缓存统计
    public static String mfcCacheTopicName = "mfc_app_cache_action";
    public static String mfcCacheEsIndexName = "flink_mfc_metric_cache";
    public static String mfcCacheTableName = "mfc_cache_action_table";

    //MFC网盘统计
    public static String mfcCloudTopicName = "mfc_cloud_action";
    public static String mfcCloudEsIndexName = "flink_mfc_metric_cloud";
    public static String mfcCloudTableName = "mfc_cloud_action_table";

    //MFC PRT统计
    public static String mfcPrtTopicName = "mfc_prt_metric_log";
    public static String mfcPrtEsIndexName = "flink_mfc_metric_prt";
    public static String mfcPrtTableName = "mfc_prt_action_table";

    //MFC缓存统计
    public static String mfcPMTopicName = "mfc_pm_engine_action";
    public static String mfcPMBufferingTopicName = "mfc_prt_engine_buffering_action_log";

    public static String mfcLocalPMTableName = "mfc_local_pm_action_table";
    public static String mfcPlayingPMTableName = "mfc_playing_pm_action_table";


}
