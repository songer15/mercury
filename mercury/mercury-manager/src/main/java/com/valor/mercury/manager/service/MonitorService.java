package com.valor.mercury.manager.service;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.fasterxml.jackson.core.type.TypeReference;
import com.valor.mercury.manager.config.MercuryConstants;
import com.valor.mercury.manager.config.ConstantConfig;
import com.valor.mercury.manager.dao.MercuryManagerDao;
import com.valor.mercury.manager.model.ddo.*;
import com.valor.mercury.manager.model.system.*;
import com.valor.mercury.manager.tool.JsonUtil;
import com.valor.mercury.manager.tool.PostUtil;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.Restrictions;
import org.influxdb.InfluxDB;
import org.influxdb.InfluxDBFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import static com.valor.mercury.manager.config.MercuryConstants.EXECUTOR_OVER_TIME_LIMIT;
import static com.valor.mercury.manager.config.MercuryConstants.TASK_OVER_TIME_LIMIT;


/**
 * @author Gavin
 * 2020/9/2 15:00
 * 监听任务和客户端的上报情况
 */
@Service
public class MonitorService extends BaseDBService {
    private final MercuryManagerDao dao;
    private final LogAlarmService logService;
    private final HAService haService;
    private final HiveService hiveService;
    private Logger logger = LoggerFactory.getLogger(this.getClass());
    private Map<String, MonitorMetric> metricMap = new HashMap<>();
    private Producer<String, String> producer;

    @Autowired
    public MonitorService(MercuryManagerDao dao, LogAlarmService logService, HAService haService, HiveService hiveService) {
        this.dao = dao;
        this.logService = logService;
        this.haService = haService;
        this.hiveService = hiveService;
        metricMap.put("hdfs", new HDFSMetric());
        metricMap.put("yarn", new YarnMetric());
        metricMap.put("flink", new FlinkMetric());
        metricMap.put("kafka", new KafkaMetric());
        metricMap.put("influxDB", new InfluxDBMetric());
        metricMap.put("hive", new HiveMetric());
        metricMap.put("elasticsearch", new ElasticSearchMetric());
    }

    @Override
    public MercuryManagerDao getSchedulerDao() {
        return dao;
    }

    @Scheduled(cron = "0 0/4 * * * ?")
    private void monitorService() {
        logger.info("monitorService");
        if (haService.getNodeStatus().equals(HAService.NodeStatus.LEADER)) {
            try {
                offLineTaskMonitor();
                realTimeTaskMonitor();
//            ETLTaskMonitor();
                executorMonitor();
//            hiveConnectMonitor();
            } catch (Exception e) {
                logger.error("monitorService error:{}", e);
                logService.addLog(MercuryConstants.LOG_LEVEL_ERROR, this.getClass().getName(), "monitorService", "monitorService error:" + e.getMessage());
            }
        } else if (haService.getNodeStatus().equals(HAService.NodeStatus.LOOKING)) {
            try {
                logger.info("waiting for elect master node");
            } catch (Exception e) {
                logger.error("waiting for elect master node error:{}", e);
            }
        }
    }

    @Scheduled(cron = "0 0/5 * * * ?")
    private void monitorComponent() {
        logger.info("monitorComponent");
        if (haService.getNodeStatus().equals(HAService.NodeStatus.LEADER)) {
            try {
                ElasticSearchMonitor();
                FlinkMonitor();
                HDFSMonitor();
                KafkaMonitor();
                InfluxDbMonitor();
                YarnMonitor();
                HiveMonitor();
            } catch (Exception e) {
                logger.error("monitorComponent error:{}", e);
                logService.addLog(MercuryConstants.LOG_LEVEL_ERROR, this.getClass().getName(), "monitorComponent", "monitorComponent error:" + e.getMessage());
            }
        } else if (haService.getNodeStatus().equals(HAService.NodeStatus.LOOKING)) {
            try {
                logger.info("waiting for elect master node");
            } catch (Exception e) {
                logger.error("waiting for elect master node error:{}", e);
            }
        }
    }

    public Map<String, MonitorMetric> getMetricMap() {
        return metricMap;
    }

    private void hiveConnectMonitor() {
        if (System.currentTimeMillis() - hiveService.lastActionTime > 30 * 60_000)
            hiveService.closeConnection();
    }

    private void executorMonitor() {
        List<Executor> executors = dao.listEntity(Executor.class, Restrictions.eq("enable", "on"));
        for (Executor executor : executors) {
            if ((MercuryConstants.EXECUTOR_STATUS_WORKING.equals(executor.getStatus())
                    || (MercuryConstants.EXECUTOR_STATUS_WAITING.equals(executor.getStatus()))) &&
                    (System.currentTimeMillis() - executor.getLastActionTime().getTime()) > EXECUTOR_OVER_TIME_LIMIT) {
                executor.setStatus(MercuryConstants.EXECUTOR_STATUS_DISCONNECTED);
                update(executor);
                //超过10分钟没有取任务指令，认为客户端程序挂掉
                logger.error("executor :{} time out", executor.getClientName());
                logService.addLog(MercuryConstants.LOG_LEVEL_ERROR, this.getClass().getName(), "executor",
                        "executor  :" + executor.getClientName() + " time out!");
            }
        }
    }

    private void ETLTaskMonitor() {
        Criterion enable = Restrictions.eq("enable", "on");
        List<ETLDataReceive> receives = dao.listEntity(ETLDataReceive.class, enable);
        for (ETLDataReceive receive : receives) {
            if (receive.getLastReportTime() != null &&
                    (System.currentTimeMillis() - receive.getLastReportTime().getTime()) > TASK_OVER_TIME_LIMIT) {
                //超过10分钟没有取任务指令，认为客户端程序挂掉
                logger.error("ETL receive :{} time out", receive.getReceiveName());
                logService.addLog(MercuryConstants.LOG_LEVEL_ERROR, this.getClass().getName(), "ETL",
                        "ETL receiver report  :" + receive.getReceiveName() + " time out!");
            }
        }

        Criterion status = Restrictions.eq("status", "finished");
        List<ETLDataDispatch> dispatches = dao.listEntity(ETLDataDispatch.class, status, enable);
        for (ETLDataDispatch dispatch : dispatches) {
            if (dispatch.getLastReportTime() != null
                    && (System.currentTimeMillis() - dispatch.getLastReportTime().getTime()) > TASK_OVER_TIME_LIMIT) {
                //超过20分钟没有取任务指令，认为客户端程序挂掉
                logger.error("ETL dispatch :{} time out", dispatch.getTypeName());
                logService.addLog(MercuryConstants.LOG_LEVEL_ERROR, this.getClass().getName(), "ETL",
                        "ETL dispatcher report :" + dispatch.getTypeName() + " time out!");
            }
        }
    }

    private void realTimeTaskMonitor() {
        Criterion status = Restrictions.in("status", MercuryConstants.REALTIME_TASK_STATUS_PUBLISHING,
                MercuryConstants.REALTIME_TASK_STATUS_RUNNING, MercuryConstants.REALTIME_TASK_STATUS_CANCELED,
                MercuryConstants.REALTIME_TASK_STATUS_CANCELLING);
        List<RealTimeTaskInstance> instances = dao.listEntity(RealTimeTaskInstance.class, status);
        for (RealTimeTaskInstance instance : instances) {
            if (instance.getLastActionTime() == null
                    || (System.currentTimeMillis() - instance.getLastActionTime().getTime()) > TASK_OVER_TIME_LIMIT) {
                //超过10分钟没有取任务指令，认为客户端程序挂掉
                logger.error("metaTask :{} time out", instance.getName());
                logService.addLog(MercuryConstants.LOG_LEVEL_ERROR, this.getClass().getName(), "realTimeTask",
                        "metaTask :" + instance.getName() + " time out!");
                instance.setStatus(MercuryConstants.REALTIME_TASK_STATUS_FINISH);
                update(instance);
                RealTimeTask task = getEntityById(RealTimeTask.class, instance.getTaskId());
                task.setStatus(MercuryConstants.REALTIME_TASK_STATUS_INIT);
                update(task);
            }
        }
    }

    private void offLineTaskMonitor() {
        Criterion status = Restrictions.eq("status", MercuryConstants.OFFLINE_TASK_INSTANCE_STATUS_RUNNING);
        List<OffLineMetaTaskInstance> metaTasks = dao.listEntity(OffLineMetaTaskInstance.class, status);
        for (OffLineMetaTaskInstance metaTask : metaTasks) {
            //超过10分钟没有得到状态上报，认为客户端程序挂掉
            if (System.currentTimeMillis() - metaTask.getLastActionTime().getTime() > TASK_OVER_TIME_LIMIT) {
                logger.error("metaTask :{} time out", metaTask.getName());
                logService.addLog(MercuryConstants.LOG_LEVEL_ERROR, this.getClass().getName(), "offLineTask",
                        "metaTask :" + metaTask.getName() + " time out!");
                metaTask.setResult("Time Out");
                metaTask.setStatus(MercuryConstants.OFFLINE_TASK_INSTANCE_STATUS_FINISH);
                update(metaTask);

                //更新任务流信息
                OffLineTaskInstance taskInstance = getEntityById(OffLineTaskInstance.class, metaTask.getTaskInstanceId());
                taskInstance.setStatus(MercuryConstants.OFFLINE_TASK_INSTANCE_STATUS_FINISH);
                taskInstance.setEndTime(new Date());
                taskInstance.setResult("Time Out");
                dao.updateEntity(taskInstance);

                //更新任务信息
                OffLineTask task = getEntityById(OffLineTask.class, taskInstance.getTaskId());
                task.setStatus(MercuryConstants.OFFLINE_TASK_STATUS_WAITING);
                dao.updateEntity(task);

                //取消其他子任务实例
                List<OffLineMetaTaskInstance> allMetaTaskInstances = listEntity(OffLineMetaTaskInstance.class, Restrictions.eq("taskInstanceId", taskInstance.getId()));
                for (OffLineMetaTaskInstance offLineMetaTaskInstance : allMetaTaskInstances) {
                    if (!offLineMetaTaskInstance.getStatus().equals(MercuryConstants.OFFLINE_TASK_INSTANCE_STATUS_FINISH)) {
                        offLineMetaTaskInstance.setResult("Time Out");
                        offLineMetaTaskInstance.setStatus(MercuryConstants.OFFLINE_TASK_INSTANCE_STATUS_FINISH);
                        dao.updateEntity(offLineMetaTaskInstance);
                    }
                }
            }
        }

        Criterion cancelStatus = Restrictions.in("status",
                MercuryConstants.OFFLINE_TASK_INSTANCE_STATUS_CANCELLING, MercuryConstants.OFFLINE_TASK_INSTANCE_STATUS_CANCELED);
        List<OffLineMetaTaskInstance> cancelMetaTasks = dao.listEntity(OffLineMetaTaskInstance.class, cancelStatus);
        for (OffLineMetaTaskInstance cancelMetaTask : cancelMetaTasks) {
            //超过10分钟没有得到状态上报，认为客户端程序挂掉
            if (cancelMetaTask.getLastActionTime() == null
                    || System.currentTimeMillis() - cancelMetaTask.getLastActionTime().getTime() > TASK_OVER_TIME_LIMIT) {
                logger.error("metaTask :{} time out", cancelMetaTask.getName());
                logService.addLog(MercuryConstants.LOG_LEVEL_ERROR, this.getClass().getName(), "offLineTask",
                        "metaTask :" + cancelMetaTask.getName() + " time out!");
                cancelMetaTask.setResult("Cancel&Time Out");
                cancelMetaTask.setStatus(MercuryConstants.OFFLINE_TASK_INSTANCE_STATUS_FINISH);
                update(cancelMetaTask);

                //更新任务流实例信息
                OffLineTaskInstance taskInstance = getEntityById(OffLineTaskInstance.class, cancelMetaTask.getTaskInstanceId());
                taskInstance.setStatus(MercuryConstants.OFFLINE_TASK_INSTANCE_STATUS_FINISH);
                taskInstance.setResult("Cancel&Time Out");
                dao.updateEntity(taskInstance);

                //更新任务流信息
                OffLineTask task = getEntityById(OffLineTask.class, taskInstance.getTaskId());
                task.setStatus(MercuryConstants.OFFLINE_TASK_STATUS_WAITING);
                dao.updateEntity(task);
            }
        }

        Criterion readyStatus = Restrictions.eq("status", MercuryConstants.OFFLINE_TASK_INSTANCE_STATUS_READY);
        List<OffLineMetaTaskInstance> readyMetaTasks = dao.listEntity(OffLineMetaTaskInstance.class, readyStatus);
        for (OffLineMetaTaskInstance readyMetaTask : readyMetaTasks) {
            if (System.currentTimeMillis() - readyMetaTask.getExecuteTime().getTime() > 600_000) {
                logger.error("metaTask :{} time out", readyMetaTask.getName());
                logService.addLog(MercuryConstants.LOG_LEVEL_WARN, this.getClass().getName(), "offLineTask",
                        "metaTask :" + readyMetaTask.getName() + " time out!");
                readyMetaTask.setResult("Time Out");
                readyMetaTask.setStatus(MercuryConstants.OFFLINE_TASK_INSTANCE_STATUS_FINISH);
                update(readyMetaTask);

                //更新任务流信息
                OffLineTaskInstance taskInstance = getEntityById(OffLineTaskInstance.class, readyMetaTask.getTaskInstanceId());
                taskInstance.setStatus(MercuryConstants.OFFLINE_TASK_INSTANCE_STATUS_FINISH);
                taskInstance.setEndTime(new Date());
                taskInstance.setResult("Time Out");
                dao.updateEntity(taskInstance);

                //更新任务信息
                OffLineTask task = getEntityById(OffLineTask.class, taskInstance.getTaskId());
                task.setStatus(MercuryConstants.OFFLINE_TASK_STATUS_WAITING);
                dao.updateEntity(taskInstance);

                //取消其他子任务实例
                List<OffLineMetaTaskInstance> allMetaTaskInstances = listEntity(OffLineMetaTaskInstance.class, Restrictions.eq("taskInstanceId", taskInstance.getId()));
                for (OffLineMetaTaskInstance offLineMetaTaskInstance : allMetaTaskInstances) {
                    if (!offLineMetaTaskInstance.getStatus().equals(MercuryConstants.OFFLINE_TASK_INSTANCE_STATUS_FINISH)) {
                        offLineMetaTaskInstance.setResult("Time Out");
                        offLineMetaTaskInstance.setStatus(MercuryConstants.OFFLINE_TASK_INSTANCE_STATUS_FINISH);
                        dao.updateEntity(offLineMetaTaskInstance);
                    }
                }
            }
        }
    }

    private void HiveMonitor() {
        HiveMetric metric = new HiveMetric();
        metric.setActionTime(new Date());
        try {
            String value = PostUtil.httpGet(Collections.singletonList(ConstantConfig.hiveMonitorUrl()), "");
            if (value != null) {
                metric.setEnable("true");
                metric.setStatus("green");
            } else {
                metric.setEnable("false");
                metric.setStatus("red");
            }
        } catch (Exception e) {
            logger.error("Hive Monitor error:{}", e);
            metric.setEnable("false");
            metric.setStatus("red");
            logService.addLog(MercuryConstants.LOG_LEVEL_DISASTER, this.getClass().getName(),
                    "hive", "hive disabled:" + e.getMessage(), "hive");
        }
        metricMap.put("hive", metric);
    }

    private void YarnMonitor() {
        YarnMetric metric = new YarnMetric();
        metric.setActionTime(new Date());
        try {
            String value = PostUtil.httpGet(Collections.singletonList(ConstantConfig.yarnMonitorUrl()), "");
            JSONObject object = JSONObject.parseObject(value).getJSONObject("clusterMetrics");
            HashMap map = JSONObject.parseObject(object.toJSONString(), HashMap.class);
            metric.setRunningJobs(Integer.parseInt(map.get("appsRunning").toString()));
            metric.setNodeNumber(Integer.parseInt(map.get("totalNodes").toString()));
            metric.setActiveNodes(Integer.parseInt(map.get("activeNodes").toString()));
            metric.setAppsSubmitted(Integer.parseInt(map.get("appsSubmitted").toString()));
            metric.setAvailableVirtualCores(Integer.parseInt(map.get("availableVirtualCores").toString()));
            metric.setAvailableMB(Integer.parseInt(map.get("availableMB").toString()));
            metric.setContainersAllocated(Integer.parseInt(map.get("containersAllocated").toString()));
            if (metric.getActiveNodes() == metric.getNodeNumber())
                metric.setStatus("green");
            else
                metric.setStatus("yellow");
            metric.setEnable("true");
        } catch (Exception e) {
            logger.error("Yarn Monitor error:{}", e);
            metric.setEnable("false");
            metric.setStatus("red");
            logService.addLog(MercuryConstants.LOG_LEVEL_DISASTER, this.getClass().getName(),
                    "yarn", "yarn disabled:" + e.getMessage(), "yarn");
        }
        metricMap.put("yarn", metric);
    }

    private void InfluxDbMonitor() {
        InfluxDBMetric metric = new InfluxDBMetric();
        metric.setActionTime(new Date());
        try {
            InfluxDB influxDB = InfluxDBFactory.connect(ConstantConfig.influxDBMonitorUrl());
            if (influxDB.ping() != null) {
                metric.setEnable("true");
                metric.setStatus("green");
            } else {
                metric.setEnable("false");
                metric.setStatus("red");
            }
            influxDB.close();
        } catch (Exception e) {
            logger.error("influxDB Monitor error:{}", e);
            metric.setEnable("false");
            metric.setStatus("red");
            logService.addLog(MercuryConstants.LOG_LEVEL_DISASTER, this.getClass().getName(),
                    "influxDB", "influxDB disabled:" + e.getMessage(), "influxDB");
        }
        metricMap.put("influxDB", metric);
    }

    private void KafkaMonitor() {
        KafkaMetric metric = new KafkaMetric();
        metric.setActionTime(new Date());
        try {
            if (producer == null) {
                Properties props = new Properties();
                props.put("bootstrap.servers", ConstantConfig.kafkaMonitorUrl());
                props.put("acks", "all");
                props.put("retries", 0);
                props.put("batch.size", 1);
                props.put("linger.ms", 1);
                props.put("buffer.memory", 33554432);
                props.put("key.serializer", "org.apache.kafka.common.serialization.StringSerializer");
                props.put("value.serializer", "org.apache.kafka.common.serialization.StringSerializer");
                producer = new KafkaProducer<>(props);
            }
            Future<RecordMetadata> result = producer.send(new ProducerRecord<>("system_monitor_log",
                    System.currentTimeMillis() + "", System.currentTimeMillis() + "_monitor"));
            result.get(30, TimeUnit.SECONDS);
            metric.setEnable("true");
            metric.setStatus("green");
        } catch (Exception e) {
            logger.error("Kafka Monitor error:{}", e);
            metric.setEnable("false");
            metric.setStatus("red");
            logService.addLog(MercuryConstants.LOG_LEVEL_DISASTER, this.getClass().getName(),
                    "kafka", "kafka disabled:" + e.getMessage(), "kafka");
        }
        metricMap.put("kafka", metric);
    }

    private void HDFSMonitor() {
        HDFSMetric metric = new HDFSMetric();
        metric.setActionTime(new Date());
        try {
            String value = PostUtil.httpGet(Collections.singletonList(ConstantConfig.hdfsMonitorUrl()), "");
            JSONObject object = JSONObject.parseObject(value);
            JSONArray array = object.getJSONArray("beans");
            HashMap result = JSONArray.parseArray(array.toJSONString(), HashMap.class).get(0);
            metric.setLiveNodes(result.get("LiveNodes").toString().split("infoAddr").length - 1);
            metric.setEnable("true");
            metric.setStatus(result.get("DeadNodes").toString().length() > 2 ? "yellow" : "green");
            metric.setTotalBlocks(Integer.parseInt(result.get("TotalBlocks").toString()));
            metric.setTotal((int) (Long.parseLong(result.get("Total").toString()) / (1024 * 1024)));
            metric.setUsed((int) (Long.parseLong(result.get("Used").toString()) / (1024 * 1024)));
            metric.setFree((int) (Long.parseLong(result.get("Free").toString()) / (1024 * 1024)));
            metric.setThreads(Integer.parseInt(result.get("Threads").toString()));
            metric.setMissingBlocks(Integer.parseInt(result.get("NumberOfMissingBlocks").toString()));
        } catch (Exception e) {
            logger.error("HDFSMonitor error:{}", e);
            metric.setEnable("false");
            metric.setStatus("red");
            logService.addLog(MercuryConstants.LOG_LEVEL_DISASTER, this.getClass().getName(),
                    "hdfs", "hdfs disabled:" + e.getMessage(), "hdfs");
        }
        metricMap.put("hdfs", metric);
    }

    private void FlinkMonitor() {
        FlinkMetric metric = new FlinkMetric();
        metric.setActionTime(new Date());
        try {
            List<HashMap> result;
            result = JsonUtil.objectMapper.readValue(PostUtil.httpGet(Collections.singletonList(ConstantConfig.flinkMonitorUrl()
                    + "numRegisteredTaskManagers"), ""), new TypeReference<List<HashMap>>() {
            });
            metric.setNodeNumber(Integer.parseInt(result.get(0).get("value").toString()));
            result = JsonUtil.objectMapper.readValue(PostUtil.httpGet(Collections.singletonList(ConstantConfig.flinkMonitorUrl()
                    + "taskSlotsAvailable"), ""), new TypeReference<List<HashMap>>() {
            });
            metric.setTaskSlotsAvailable(Integer.parseInt(result.get(0).get("value").toString()));
            result = JsonUtil.objectMapper.readValue(PostUtil.httpGet(Collections.singletonList(ConstantConfig.flinkMonitorUrl()
                    + "numRunningJobs"), ""), new TypeReference<List<HashMap>>() {
            });
            metric.setRunningJobs(Integer.parseInt(result.get(0).get("value").toString()));
            result = JsonUtil.objectMapper.readValue(PostUtil.httpGet(Collections.singletonList(ConstantConfig.flinkMonitorUrl()
                    + "taskSlotsTotal"), ""), new TypeReference<List<HashMap>>() {
            });
            metric.setTaskSlotsTotal(Integer.parseInt(result.get(0).get("value").toString()));
            result = JsonUtil.objectMapper.readValue(PostUtil.httpGet(Collections.singletonList(ConstantConfig.flinkMonitorUrl()
                    + "Status.JVM.Memory.Heap.Used"), ""), new TypeReference<List<HashMap>>() {
            });
            metric.setMemoryUsed((int) (Long.parseLong(result.get(0).get("value").toString()) / (1024 * 1024)));
            result = JsonUtil.objectMapper.readValue(PostUtil.httpGet(Collections.singletonList(ConstantConfig.flinkMonitorUrl()
                    + "Status.JVM.Memory.Heap.Max"), ""), new TypeReference<List<HashMap>>() {
            });
            metric.setMemoryMax((int) (Long.parseLong(result.get(0).get("value").toString()) / (1024 * 1024)));
            metric.setStatus("green");
            metric.setEnable("true");
        } catch (Exception e) {
            logger.error("Flink Monitor error:{}", e);
            metric.setEnable("false");
            metric.setStatus("red");
            logService.addLog(MercuryConstants.LOG_LEVEL_DISASTER, this.getClass().getName(),
                    "flink", "flink disabled:" + e.getMessage(), "flink");
        }
        metricMap.put("flink", metric);
    }

    private void ElasticSearchMonitor() {
        ElasticSearchMetric metric = new ElasticSearchMetric();
        metric.setActionTime(new Date());
        try {
            String result = PostUtil.httpGet(Collections.singletonList(ConstantConfig.elasticMonitorUrl()), "");
            String[] metrics = result.split(" ");
            metric.setStatus(metrics[3]);
            metric.setNodeNumber(Integer.parseInt(metrics[4]));
            if (!metric.getStatus().equals("red"))
                metric.setEnable("true");
            else {
                metric.setEnable("false");
                logger.error("elastic search disabled:{}", result);
                logService.addLog(MercuryConstants.LOG_LEVEL_DISASTER, this.getClass().getName(),
                        "elasticsearch", "elastic search disabled:" + result, "elasticsearch");
            }
        } catch (Exception e) {
            logger.error("ElasticSearch Monitor error:{}", e);
            metric.setEnable("false");
            metric.setStatus("red");
            logService.addLog(MercuryConstants.LOG_LEVEL_DISASTER, this.getClass().getName(),
                    "elasticsearch", "elastic search disabled:" + e.getMessage(), "elasticsearch");

        }
        metricMap.put("elasticsearch", metric);
    }
}
