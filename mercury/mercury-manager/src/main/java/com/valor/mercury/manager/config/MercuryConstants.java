package com.valor.mercury.manager.config;

/**
 * @Author: Gavin
 * @Date: 2020/2/19 12:26
 */
public class MercuryConstants {

    //type
    public static final String TYPE_OFFLINE_SPIDER = "SPIDER";
    public static final String TYPE_OFFLINE_SCRIPT = "SCRIPT";
    public static final String TYPE_OFFLINE_HIVE = "HIVE";
    public static final String TYPE_REALTIME_FLINK = "FLINK";
    public static final String TYPE_ETL_RECEIVER = "RECEIVER";
    public static final String TYPE_ETL_DISPATCHER = "DISPATCHER";

    //Real-Time task
    public static final String REALTIME_TASK_STATUS_INIT = "INIT";//初始化
    public static final String REALTIME_TASK_STATUS_PUBLISHING = "PUBLISHING";//正在发布
    public static final String REALTIME_TASK_STATUS_RUNNING = "RUNNING";//执行中
    public static final String REALTIME_TASK_STATUS_CANCELLING = "CANCELLING";//正在取消
    public static final String REALTIME_TASK_STATUS_CANCELED = "CANCELED"; //已取消
    public static final String REALTIME_TASK_STATUS_FINISH = "FINISH"; //已结束

    //offLine-Task
    public static final String OFFLINE_TASK_STATUS_RUNNING = "RUNNING";
    public static final String OFFLINE_TASK_STATUS_WAITING = "WAITING";

    //offLine-Task Instance
    public static final String OFFLINE_TASK_INSTANCE_STATUS_WAITING = "WAITING";//创建实例
    public static final String OFFLINE_TASK_INSTANCE_STATUS_READY = "READY";//等待客户端取走（meta-instance独有此状态）
    public static final String OFFLINE_TASK_INSTANCE_STATUS_RUNNING = "RUNNING";//执行中
    public static final String OFFLINE_TASK_INSTANCE_STATUS_CANCELLING = "CANCELLING";//正在取消
    public static final String OFFLINE_TASK_INSTANCE_STATUS_CANCELED = "CANCELED";//已取消
    public static final String OFFLINE_TASK_INSTANCE_STATUS_FINISH = "FINISH";//执行完毕

    //Executor instance status
    public static final String EXECUTOR_INSTANCE_STATUS_LINING = "LINING";  //排队中
    public static final String EXECUTOR_INSTANCE_STATUS_RUNNING = "RUNNING"; //执行
    public static final String EXECUTOR_INSTANCE_STATUS_SUCCESS = "SUCCESS"; //成功
    public static final String EXECUTOR_INSTANCE_STATUS_FAIL = "FAIL";  //失败
    public static final String EXECUTOR_INSTANCE_STATUS_CANCELED = "CANCELED";  //取消

    //commandType
    public static final String EXECUTOR_COMMAND_START = "START";
    public static final String EXECUTOR_COMMAND_STOP = "STOP";

    //DAG
    public static final Long TASK_NODE_START = -100L;
    public static final Long TASK_NODE_END = -200L;

    //log
    public static final int LOG_LEVEL_INFO = 1;
    public static final int LOG_LEVEL_WARN = 2;
    public static final int LOG_LEVEL_ERROR = 3;
    public static final int LOG_LEVEL_DISASTER = 4;

    //executor
    public static final Integer EXECUTOR_OVER_TIME_LIMIT = 10 * 60_000; //客户端超时时间
    public static final Integer TASK_OVER_TIME_LIMIT = 20 * 60_000; //任务超时时间
    public static final String EXECUTOR_STATUS_INIT = "INIT";
    public static final String EXECUTOR_STATUS_WAITING = "WAITING";
    public static final String EXECUTOR_STATUS_WORKING = "WORKING";
    public static final String EXECUTOR_STATUS_DISCONNECTED = "DISCONNECTED";
    public static final String BaseFilePath = "./file";

    //receiver
    public static final String DEFAULT_IP_FIELD = "preset_custom_ip";
}
