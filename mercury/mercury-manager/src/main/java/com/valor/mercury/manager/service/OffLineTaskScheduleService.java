package com.valor.mercury.manager.service;

import com.cronutils.model.definition.CronDefinitionBuilder;
import com.cronutils.model.time.ExecutionTime;
import com.cronutils.parser.CronParser;
import com.valor.mercury.manager.config.MercuryConstants;
import com.valor.mercury.manager.dao.MercuryManagerDao;
import com.valor.mercury.manager.model.ddo.*;
import org.apache.logging.log4j.util.Strings;
import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.Restrictions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.*;

import static com.cronutils.model.CronType.QUARTZ;

/**
 * @Author: Gavin
 * @Date: 2020/2/21 15:58
 * 负责调度工作，生成任务实例
 */
@Service
public class OffLineTaskScheduleService extends BaseDBService {

    private CronParser parser = new CronParser(CronDefinitionBuilder.instanceDefinitionFor(QUARTZ));
    private final MercuryManagerDao dao;
    private final LogAlarmService logService;
    private final HAService haService;
    private Logger logger = LoggerFactory.getLogger(this.getClass());
    private Calendar calendar = Calendar.getInstance();

    @Autowired
    public OffLineTaskScheduleService(MercuryManagerDao dao, LogAlarmService logService, HAService haService) {
        this.dao = dao;
        this.logService = logService;
        this.haService = haService;
    }

    public CronParser getParser() {
        return parser;
    }

    @Override
    public MercuryManagerDao getSchedulerDao() {
        return dao;
    }

    //每天的00:02~00:10作为业务隔离期，生成当天需要执行的任务实例
    @Scheduled(cron = "0 2 0 1/1 * ? ")
    private void schedule() {
        if (haService.getNodeStatus().equals(HAService.NodeStatus.LEADER)) {
            logger.info("schedule start!");
            logService.addLog(MercuryConstants.LOG_LEVEL_INFO, this.getClass().getName(), "schedule", "schedule start!");
            List<OffLineTask> offLineTasks = dao.listEntity(OffLineTask.class, Restrictions.eq("enable", "on"));
            for (OffLineTask offLineTask : offLineTasks) {
                try {
                    if (Strings.isNotEmpty(offLineTask.getDag()))
                        taskSchedule(offLineTask);
                    else {
                        logService.addLog(MercuryConstants.LOG_LEVEL_ERROR, this.getClass().getName(), "schedule", "task dag info is null:" + offLineTask.getName());
                        logger.error("task dag info is null:{}", offLineTask.getName());
                    }
                } catch (Exception e) {
                    logger.error("taskSchedule error: {}", e);
                }
            }
        } else if (haService.getNodeStatus().equals(HAService.NodeStatus.LOOKING)) {
            try {
                logger.info("waiting for elect master node");
                Thread.sleep(60_000);
                schedule();
            } catch (Exception e) {
                logger.error("waiting for elect master node error:{}", e);
                schedule();
            }
        }
    }

    public void taskSchedule(OffLineTask offLineTask) {
        calendar.setTime(new Date(System.currentTimeMillis() + 24 * 3600_000));
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        Integer runningBatch = calendar.get(Calendar.YEAR) * 10000 + (calendar.get(Calendar.MONTH) + 1) * 100 + calendar.get(Calendar.DAY_OF_MONTH) - 1;
        ExecutionTime executionTime = ExecutionTime.forCron(parser.parse(offLineTask.getCron()));
//        ZonedDateTime lastExecuteTime;
//        if (offLineTask.getLastExecuteTime() == null)
//            lastExecuteTime = ZonedDateTime.now();
//        else
//            lastExecuteTime =
//                    ZonedDateTime.of(LocalDateTime.ofInstant(offLineTask.getLastExecuteTime().toInstant(), ZoneId.systemDefault()), ZoneId.systemDefault());
        offLineTask.setRunningBatch(runningBatch);
        dao.updateEntity(offLineTask);
        allDay(offLineTask, executionTime, ZonedDateTime.now());
    }

    private void allDay(OffLineTask offLineTask, ExecutionTime executionTime, ZonedDateTime lastExecuteTime) {
        Optional<ZonedDateTime> optional = executionTime.nextExecution(lastExecuteTime);
        if (optional.isPresent()) {
            Date nextExecuteTime = Date.from((optional.get().toInstant()));
            if (nextExecuteTime.getTime() < calendar.getTimeInMillis()) {
                if (offLineTask.getNextExecuteTime() == null ||
                        offLineTask.getNextExecuteTime().getTime() < calendar.getTimeInMillis() - 24 * 3600_000)
                    offLineTask.setNextExecuteTime(nextExecuteTime);
                generateInstances(offLineTask, nextExecuteTime);
                allDay(offLineTask, executionTime,
                        ZonedDateTime.of(LocalDateTime.ofInstant(nextExecuteTime.toInstant(), ZoneId.systemDefault()), ZoneId.systemDefault()));
            }
        }
    }

    protected void generateInstances(OffLineTask offLineTask, Date nextExecuteTime) {
        OffLineTaskInstance instances = new OffLineTaskInstance();
        instances.setCreateTime(new Date());
        instances.setStatus(MercuryConstants.OFFLINE_TASK_INSTANCE_STATUS_WAITING);
        instances.setName(offLineTask.getName());
        instances.setCron(offLineTask.getCron());
        instances.setDesc(offLineTask.getDesc());
        instances.setTaskId(offLineTask.getId());
        instances.setExecuteTime(nextExecuteTime);
        instances.setRunningBatch(offLineTask.getRunningBatch());
        instances.setResult("INIT");
        instances.setId((Long) dao.saveEntity(instances));
        Criterion criterion = Restrictions.eq("taskId", offLineTask.getId());
        List<DAG> dags = dao.listEntity(DAG.class, criterion);
        Map<Long, List<DAG>> map = new HashMap<>();
        for (DAG dag : dags) {
            if (!map.containsKey(dag.getNodeId())) {
                List<DAG> child = new ArrayList<>();
                child.add(dag);
                map.put(dag.getNodeId(), child);
            } else {
                List<DAG> child = map.get(dag.getNodeId());
                child.add(dag);
            }
        }
        List<Long> runningList = new ArrayList<>();
        List<DAG> startNodes = map.get(MercuryConstants.TASK_NODE_START);
        //对任务节点进行排序，得到任务流
        appendDAGList(runningList, startNodes, map);
        Long nodeID = null;
        List<OffLineMetaTaskInstance> list = new ArrayList<>();
        for (Long dagNode : runningList) {
            OffLineMetaTask offLineMetaTask = dao.getById(OffLineMetaTask.class, dagNode);
            OffLineMetaTaskInstance offLineMetaTaskInstance = new OffLineMetaTaskInstance();
            offLineMetaTaskInstance.setExecuteTime(nextExecuteTime);
            offLineMetaTaskInstance.setCreateTime(new Date());
            offLineMetaTaskInstance.setDesc(offLineMetaTask.getDesc());
            offLineMetaTaskInstance.setExecutor(offLineMetaTask.getExecutor());
            offLineMetaTaskInstance.setMetaTaskId(offLineMetaTask.getId());
            offLineMetaTaskInstance.setName(offLineMetaTask.getName());
            offLineMetaTaskInstance.setPrimaryValue(offLineMetaTask.getInc());
            offLineMetaTaskInstance.setType(offLineMetaTask.getType());
            offLineMetaTaskInstance.setStatus(nodeID == null ? MercuryConstants.OFFLINE_TASK_INSTANCE_STATUS_READY
                    : MercuryConstants.OFFLINE_TASK_INSTANCE_STATUS_WAITING);
            offLineMetaTaskInstance.setTaskInstanceId(instances.getId());
            offLineMetaTaskInstance.setDepNode(nodeID == null ? 0L : nodeID);
            offLineMetaTaskInstance.setRunningBatch(offLineTask.getRunningBatch());
            offLineMetaTaskInstance.setNextNode(0L);
            nodeID = (Long) dao.saveEntity(offLineMetaTaskInstance);
            offLineMetaTaskInstance.setId(nodeID);
            offLineMetaTaskInstance.setLastActionTime(new Date());
            list.add(offLineMetaTaskInstance);
        }
        for (int i = list.size() - 2; i >= 0; i--) {
            OffLineMetaTaskInstance offLineMetaTaskInstance = list.get(i);
            offLineMetaTaskInstance.setNextNode(nodeID);
            dao.updateEntity(offLineMetaTaskInstance);
            nodeID = offLineMetaTaskInstance.getId();
        }
    }

    private void appendDAGList(List<Long> runningList, List<DAG> startNodes, Map<Long, List<DAG>> map) {
        for (DAG startNode : startNodes) {
            if (!startNode.getNextNode().equals(MercuryConstants.TASK_NODE_END)) {
                runningList.remove(startNode.getNextNode());
                runningList.add(startNode.getNextNode());
            }
        }
        List<DAG> nodes = new ArrayList<>();
        for (DAG startNode : startNodes) {
            List<DAG> children = map.get(startNode.getNextNode());
            if (children != null && children.size() > 0)
                nodes.addAll(children);
        }
        if (nodes.size() > 0)
            appendDAGList(runningList, nodes, map);
    }

    public boolean isValidCronExpression(String cron) {
        try {
            parser.parse(cron);
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}
