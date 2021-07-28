package com.valor.mercury.manager.service;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.cronutils.model.time.ExecutionTime;
import com.google.common.collect.Lists;
import com.valor.mercury.manager.config.MercuryConstants;
import com.valor.mercury.manager.dao.MercuryManagerDao;
import com.valor.mercury.manager.model.ddo.*;
import com.valor.mercury.manager.model.system.*;
import com.valor.mercury.manager.tool.JsonUtil;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.logging.log4j.util.Strings;
import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.Restrictions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * @Author: Gavin
 * @Date: 2020/2/20 20:50
 * 离线任务的管理
 */
@Service
public class OffLineTaskService extends BaseDBService {

    private final MercuryManagerDao dao;
    private final LogAlarmService logService;
    private final OffLineTaskScheduleService scheduleService;
    private final OffLineMetaTaskService metaTaskService;
    private Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    public OffLineTaskService(MercuryManagerDao dao, LogAlarmService logService, OffLineTaskScheduleService scheduleService, OffLineMetaTaskService metaTaskService) {
        this.dao = dao;
        this.logService = logService;
        this.scheduleService = scheduleService;
        this.metaTaskService = metaTaskService;
    }

    @Override
    public MercuryManagerDao getSchedulerDao() {
        return dao;
    }

    //将有向无环图json信息解析并存储到本地
    public Pair<Boolean, String> setTaskDAG(String dags, OffLineTask offLineTask) {
        Set<String> nodes = new HashSet<>();
        try {
            JSONObject jsonObject = JSONObject.parseObject(dags);
            JSONArray jsonArray = jsonObject.getJSONArray("connections");
            if (jsonArray.size() <= 0)
                return ImmutablePair.of(false, "DAG设置错误");
            else {
                List<DAG> dagList = new ArrayList<>();
                for (int i = 0; i < jsonArray.size(); i++) {
                    JSONObject object = jsonArray.getJSONObject(i);
                    String source = object.getString("pageSourceId");
                    String target = object.getString("pageTargetId");
                    if (source.equals("chart-node-start") && target.equals("chart-node-end"))
                        return ImmutablePair.of(false, "存在无用连线");
                    nodes.add(source);
                    nodes.add(target);
                    DAG dag = new DAG();
                    if (source.equals("chart-node-start"))
                        dag.setNodeId(MercuryConstants.TASK_NODE_START);
                    else if (source.equals("chart-node-end"))
                        return ImmutablePair.of(false, "以end作为起点");
                    else
                        dag.setNodeId(Long.parseLong(source));
                    if (target.equals("chart-node-start"))
                        return ImmutablePair.of(false, "以start作为终点");
                    else if (target.equals("chart-node-end"))
                        dag.setNextNode(MercuryConstants.TASK_NODE_END);
                    else
                        dag.setNextNode(Long.parseLong(target));
                    dag.setCreateTime(new Date());
                    dag.setTaskId(offLineTask.getId());
                    dagList.add(dag);
                }
                //判断是否有重复连线
                for (int i = 0; i < dagList.size(); i++) {
                    for (int j = i + 1; j < dagList.size(); j++) {
                        if (dagList.get(i).getNodeId().equals(dagList.get(j).getNodeId())
                                && dagList.get(i).getNextNode().equals(dagList.get(j).getNextNode()))
                            return ImmutablePair.of(false, "存在重复连线");
                    }
                }
                Map<Long, LinkedList<DAG>> map = new HashMap<>();
                for (DAG dag : dagList) {
                    if (!map.containsKey(dag.getNodeId())) {
                        LinkedList<DAG> child = Lists.newLinkedList();
                        child.add(dag);
                        map.put(dag.getNodeId(), child);
                    } else {
                        LinkedList<DAG> child = map.get(dag.getNodeId());
                        child.add(dag);
                    }
                }
                LinkedList<DAG> startNodes = map.get(MercuryConstants.TASK_NODE_START);
                //判断是否流向到终点
                for (DAG startNode : startNodes) {
                    if (!verifyDAG(startNode, map))
                        return ImmutablePair.of(false, "流程图没有回到终点");
                }
                //判断是否有回环
                Set<String> uniqueKeys = new HashSet<>();
                while (startNodes.size() > 0) {
                    DAG startNode = startNodes.poll();
                    String uniqueKey = startNode.getNodeId() + "-" + startNode.getNextNode();
                    uniqueKeys.add(uniqueKey);
                    for (DAG dag : dagList) {
                        if (startNode.getNodeId().equals(dag.getNodeId()) && startNode.getNextNode().equals(dag.getNextNode()))
                            continue;
                        if (startNode.getNextNode().equals(dag.getNodeId()) && !startNodes.contains(dag)) {
                            uniqueKey = dag.getNodeId() + "-" + dag.getNextNode();
                            if (uniqueKeys.contains(uniqueKey))
                                return ImmutablePair.of(false, "存在回环");
                            uniqueKeys.add(uniqueKey);
                            startNodes.add(dag);
                        }
                    }
                }
                //保存和清理之前的调度痕迹
                Criterion idCriterion = Restrictions.eq("taskId", offLineTask.getId());
                List<DAG> preDags = listEntity(DAG.class, idCriterion);
                for (DAG preDag : preDags)
                    deleteEntity(preDag);
                dao.storeAll(dagList);
                //删除无用node
                JSONArray nodeArray = jsonObject.getJSONArray("nodes");
                List list = new ArrayList();
                for (int i = 0; i < nodeArray.size(); i++) {
                    JSONObject object = nodeArray.getJSONObject(i);
                    list.add(object);
                    String nodeId = object.getString("nodeId");
                    if (!nodes.contains(nodeId))
                        list.remove(object);
                    else
                        //去除重复出现的节点
                        nodes.remove(nodeId);
                }
                JSONObject newJsonObject = new JSONObject();
                newJsonObject.fluentPut("connections", jsonArray);
                newJsonObject.put("nodes", new JSONArray(list));
                offLineTask.setDag(newJsonObject.toJSONString());
                dao.updateEntity(offLineTask);
                List<OffLineTaskInstance> taskInstances = listEntity(OffLineTaskInstance.class,
                        Restrictions.eq("runningBatch", offLineTask.getRunningBatch()));
                for (OffLineTaskInstance taskInstance : taskInstances) {
                    deleteEntity(taskInstance);
//                    taskInstance.setStatus(MercuryConstants.OFFLINE_TASK_INSTANCE_STATUS_FINISH);
//                    taskInstance.setResult("CANCELED");
//                    update(taskInstance);
                    List<OffLineMetaTaskInstance> metaTaskInstances = listEntity(OffLineMetaTaskInstance.class,
                            Restrictions.eq("taskInstanceId", taskInstance.getId()));
                    for (OffLineMetaTaskInstance metaTaskInstance : metaTaskInstances) {
//                        metaTaskInstance.setStatus(MercuryConstants.OFFLINE_TASK_INSTANCE_STATUS_FINISH);
//                        metaTaskInstance.setResult("CANCELED");
//                        update(metaTaskInstance);
                        deleteEntity(metaTaskInstance);
                    }
                }
//                scheduleService.taskSchedule(offLineTask);
                return ImmutablePair.of(true, null);
            }
        } catch (Exception e) {
            logger.info("setTaskDAG error:{}", e);
            return ImmutablePair.of(false, "DAG设置错误");
        }
    }

    //验证有向无环图是否正确
    private boolean verifyDAG(DAG dag, Map<Long, LinkedList<DAG>> map) {
        if (dag.getNextNode().equals(MercuryConstants.TASK_NODE_END))
            return true;
        LinkedList<DAG> children = map.get(dag.getNextNode());
        if (children == null || children.size() == 0)
            return false;
        else
            for (DAG child : children) {
                if (verifyDAG(child, map))
                    return true;
            }
        return false;
    }

    //生成即刻执行的任务流实例
    public boolean runTaskImmediately(OffLineTask offLineTask) {
        offLineTask.setNextExecuteTime(new Date());
        update(offLineTask);
        scheduleService.generateInstances(offLineTask, new Date());
        return true;
    }

    //停止当天所有任务流实例
    public boolean clearTaskImmediately(OffLineTask offLineTask) {
        Criterion batchCriterion = Restrictions.eq("runningBatch", offLineTask.getRunningBatch());
        Criterion idCriterion = Restrictions.eq("taskId", offLineTask.getId());
        List<OffLineTaskInstance> instances = listEntity(OffLineTaskInstance.class, batchCriterion, idCriterion);
        if (instances.size() == 0)
            return false;
        for (OffLineTaskInstance instance : instances) {
            if (instance.getStatus().equals(MercuryConstants.OFFLINE_TASK_INSTANCE_STATUS_RUNNING)) {
                stopTaskImmediately(instance);
            } else if (instance.getStatus().equals(MercuryConstants.OFFLINE_TASK_INSTANCE_STATUS_READY)
                    || instance.getStatus().equals(MercuryConstants.OFFLINE_TASK_INSTANCE_STATUS_WAITING)) {
                instance.setResult("CANCELED");
                instance.setStatus(MercuryConstants.OFFLINE_TASK_INSTANCE_STATUS_FINISH);
                update(instance);
                idCriterion = Restrictions.eq("taskInstanceId", instance.getId());
                List<OffLineMetaTaskInstance> metaTaskInstances = listEntity(OffLineMetaTaskInstance.class, idCriterion);
                for (OffLineMetaTaskInstance metaTaskInstance : metaTaskInstances) {
                    metaTaskInstance.setLastActionTime(new Date());
                    metaTaskInstance.setStatus(MercuryConstants.OFFLINE_TASK_INSTANCE_STATUS_FINISH);
                    metaTaskInstance.setResult("CANCELED");
                    update(metaTaskInstance);
                }
            }
        }
        return true;
    }


    //停止正在执行的任务流实例
    public void stopTaskImmediately(OffLineTaskInstance instance) {
        Criterion idCriterion = Restrictions.eq("taskInstanceId", instance.getId());
        List<OffLineMetaTaskInstance> metaTaskInstances = dao.listEntity(OffLineMetaTaskInstance.class, idCriterion);
        for (OffLineMetaTaskInstance metaTaskInstance : metaTaskInstances) {
            if (metaTaskInstance.getStatus().equals(MercuryConstants.OFFLINE_TASK_INSTANCE_STATUS_RUNNING)) {
                metaTaskInstance.setLastActionTime(new Date());
                metaTaskInstance.setStatus(MercuryConstants.OFFLINE_TASK_INSTANCE_STATUS_CANCELLING);
                update(metaTaskInstance);
            } else if (metaTaskInstance.getStatus().equals(MercuryConstants.OFFLINE_TASK_INSTANCE_STATUS_WAITING)
                    || metaTaskInstance.getStatus().equals(MercuryConstants.OFFLINE_TASK_INSTANCE_STATUS_READY)) {
                metaTaskInstance.setLastActionTime(new Date());
                metaTaskInstance.setStatus(MercuryConstants.OFFLINE_TASK_INSTANCE_STATUS_FINISH);
                metaTaskInstance.setResult("CANCELED");
                update(metaTaskInstance);
            }
        }
        instance.setStatus(MercuryConstants.OFFLINE_TASK_INSTANCE_STATUS_CANCELLING);
        update(instance);
    }

    //客户端获取指令
    public List<ExecutorCommand> getOffLineTaskCommandByExecutor(String name) {
        Criterion criterion = Restrictions.eq("executor", name);
        Criterion runCriterion = Restrictions.eq("status", MercuryConstants.OFFLINE_TASK_INSTANCE_STATUS_READY);
        Criterion stopCriterion = Restrictions.eq("status", MercuryConstants.OFFLINE_TASK_INSTANCE_STATUS_CANCELLING);
        List<OffLineMetaTaskInstance> runList = dao.listEntity(OffLineMetaTaskInstance.class, criterion, runCriterion);
        List<OffLineMetaTaskInstance> stopList = dao.listEntity(OffLineMetaTaskInstance.class, criterion, stopCriterion);
        List<ExecutorCommand> result = new ArrayList<>();
        for (OffLineMetaTaskInstance offLineMetaTaskInstance : runList) {
            if (Math.abs(offLineMetaTaskInstance.getExecuteTime().getTime() - System.currentTimeMillis()) < 600_000) {
                OffLineMetaTask metaTask = dao.getById(OffLineMetaTask.class, offLineMetaTaskInstance.getMetaTaskId());
                //执行指令
                offLineMetaTaskInstance.setStatus(MercuryConstants.OFFLINE_TASK_INSTANCE_STATUS_RUNNING);
                offLineMetaTaskInstance.setLastActionTime(new Date());
                offLineMetaTaskInstance.setPrimaryValue(metaTask.getInc());
                dao.updateEntity(offLineMetaTaskInstance);
                //如果是任务流的第一个节点
                if (offLineMetaTaskInstance.getDepNode() == 0L) {
                    //更新任务流实例信息
                    OffLineTaskInstance taskInstance = dao.getById(OffLineTaskInstance.class, offLineMetaTaskInstance.getTaskInstanceId());
                    taskInstance.setStatus(MercuryConstants.OFFLINE_TASK_INSTANCE_STATUS_RUNNING);
                    dao.updateEntity(taskInstance);
                    //更新任务信息
                    OffLineTask task = dao.getById(OffLineTask.class, taskInstance.getTaskId());
                    task.setStatus(MercuryConstants.OFFLINE_TASK_STATUS_RUNNING);
//                    task.setLastExecuteTime(task.getNextExecuteTime());
                    dao.updateEntity(task);
                }
                ExecutorCommand command = new ExecutorCommand();
                command.setActionTime(new Date());
                command.setCommandType(MercuryConstants.EXECUTOR_COMMAND_START);
                command.setInstanceID(offLineMetaTaskInstance.getId());
                command.setTaskConfig(metaTaskService.setMetaTaskMetric(metaTask));
                result.add(command);
            }
        }
        for (OffLineMetaTaskInstance offLineMetaTaskInstance : stopList) {
            //暂停指令
            offLineMetaTaskInstance.setStatus(MercuryConstants.OFFLINE_TASK_INSTANCE_STATUS_CANCELED);
            offLineMetaTaskInstance.setLastActionTime(new Date());
            dao.updateEntity(offLineMetaTaskInstance);
            ExecutorCommand command = new ExecutorCommand();
            command.setActionTime(new Date());
            command.setCommandType(MercuryConstants.EXECUTOR_COMMAND_STOP);
            command.setInstanceID(offLineMetaTaskInstance.getId());
            result.add(command);
            //更新任务流实例信息
            OffLineTaskInstance taskInstance = getEntityById(OffLineTaskInstance.class, offLineMetaTaskInstance.getTaskInstanceId());
            taskInstance.setStatus(MercuryConstants.OFFLINE_TASK_INSTANCE_STATUS_CANCELED);
            dao.updateEntity(offLineMetaTaskInstance);
        }
        return result;
    }

    //客户端返回指标
    public boolean updateOffLineTaskStatus(ExecutorReport report) {
        OffLineMetaTaskInstance metaTaskInstance = dao.getById(OffLineMetaTaskInstance.class, report.getInstanceID());
        if (metaTaskInstance == null)
            return false;
        else if (metaTaskInstance.getStatus().equals(MercuryConstants.OFFLINE_TASK_INSTANCE_STATUS_FINISH))
            //此子任务实例反馈信息无效，因为消息超时，任务已经判定为失败
            return true;
        try {
            //更新任务节点信息
            OffLineTaskInstance taskInstance = dao.getById(OffLineTaskInstance.class, metaTaskInstance.getTaskInstanceId());
            OffLineTask task = dao.getById(OffLineTask.class, taskInstance.getTaskId());
            Map<String, Object> metric = report.getMetrics();
            switch (report.getInstanceStatus()) {
                case MercuryConstants.EXECUTOR_INSTANCE_STATUS_SUCCESS: //任务成功
                    //更新子任务实例信息
                    metaTaskInstance.setStatus(MercuryConstants.OFFLINE_TASK_INSTANCE_STATUS_FINISH);
                    metaTaskService.updateMetaTaskInstanceMetric(report, metaTaskInstance);

                    //更新子任务信息
                    OffLineMetaTask metaTask = getEntityById(OffLineMetaTask.class, metaTaskInstance.getMetaTaskId());
                    if (report.getMetrics().containsKey("incValue"))
                        metaTask.setInc(report.getMetrics().get("incValue").toString());
                    if (metaTask.getType().equals(MercuryConstants.TYPE_OFFLINE_SPIDER)) {
                        SpiderTask spider = JsonUtil.objectMapper.readValue(metaTask.getConfig(), SpiderTask.class);
                        spider.setIncValue(metaTask.getInc());
                        metaTask.setConfig(JsonUtil.objectMapper.writeValueAsString(spider));
                    } else if (metaTask.getType().equals(MercuryConstants.TYPE_OFFLINE_SCRIPT)) {
                        ScriptTask scriptTask = JsonUtil.objectMapper.readValue(metaTask.getConfig(), ScriptTask.class);
                        scriptTask.setIncValue(metaTask.getInc());
                        metaTask.setConfig(JsonUtil.objectMapper.writeValueAsString(scriptTask));
                    } else if (metaTask.getType().equals(MercuryConstants.TYPE_OFFLINE_HIVE)) {
                        HiveTask hiveTask = JsonUtil.objectMapper.readValue(metaTask.getConfig(), HiveTask.class);
                        hiveTask = metaTaskService.updateHiveMetaTask(hiveTask);
                        metaTask.setConfig(JsonUtil.objectMapper.writeValueAsString(hiveTask));
                        metaTask.setInc(hiveTask.getParameterMap());
                    }
                    update(metaTask);

                    //更新任务流信息
                    if (metaTaskInstance.getDepNode() == 0L) {
                        // 如果是任务流第一个节点
                        taskInstance.setStartTime(new Date(metric.containsKey("startTime") ?
                                ((Number) metric.get("startTime")).longValue() : 0));
                        dao.updateEntity(taskInstance);
                    }
                    if (metaTaskInstance.getNextNode() == 0L) {
                        // 如果是任务流最后一个节点
                        taskInstance.setStatus(MercuryConstants.OFFLINE_TASK_INSTANCE_STATUS_FINISH);
                        taskInstance.setEndTime(new Date(metric.containsKey("endTime") ?
                                ((Number) metric.get("endTime")).longValue() : 0));
                        taskInstance.setResult("SUCCESS");
                        dao.updateEntity(taskInstance);
                        //更新任务信息
                        task.setStatus(MercuryConstants.OFFLINE_TASK_STATUS_WAITING);
                        dao.updateEntity(task);
                    } else {
                        //更新下一个子任务节点信息
                        OffLineMetaTaskInstance nextNode = dao.getById(OffLineMetaTaskInstance.class, metaTaskInstance.getNextNode());
                        nextNode.setExecuteTime(new Date());
                        nextNode.setStatus(MercuryConstants.OFFLINE_TASK_INSTANCE_STATUS_READY);
                        dao.updateEntity(nextNode);
                    }
                    break;
                case MercuryConstants.EXECUTOR_INSTANCE_STATUS_FAIL: //任务失败
                    logService.addLog(MercuryConstants.LOG_LEVEL_ERROR, this.getClass().getName(), "offLineTask",
                            "offLineTask fail :" + report.getErrorMessage());

                    //更新子任务实例信息
                    metaTaskInstance.setStatus(MercuryConstants.OFFLINE_TASK_INSTANCE_STATUS_FINISH);
                    metaTaskService.updateMetaTaskInstanceMetric(report, metaTaskInstance);

                    //更新任务流信息
                    if (metaTaskInstance.getDepNode() != 0L)
                        // 如果是任务流第一个节点
                        if (metric.containsKey("startTime"))
                            taskInstance.setStartTime(new Date(((Number) metric.get("startTime")).longValue()));
                    taskInstance.setStatus(MercuryConstants.OFFLINE_TASK_INSTANCE_STATUS_FINISH);
                    taskInstance.setEndTime(new Date());
                    taskInstance.setResult("FAIL");
                    taskInstance.setErrorMsg(report.getErrorMessage());
                    dao.updateEntity(taskInstance);

                    //更新任务信息
                    task.setStatus(MercuryConstants.OFFLINE_TASK_STATUS_WAITING);
                    dao.updateEntity(task);

                    //取消其他子任务实例
                    List<OffLineMetaTaskInstance> allMetaTaskInstances = listEntity(OffLineMetaTaskInstance.class, Restrictions.eq("taskInstanceId", taskInstance.getId()));
                    for (OffLineMetaTaskInstance offLineMetaTaskInstance : allMetaTaskInstances) {
                        if (!offLineMetaTaskInstance.getStatus().equals(MercuryConstants.OFFLINE_TASK_INSTANCE_STATUS_FINISH)) {
                            offLineMetaTaskInstance.setResult(report.getInstanceStatus());
                            offLineMetaTaskInstance.setStatus(MercuryConstants.OFFLINE_TASK_INSTANCE_STATUS_FINISH);
                            dao.updateEntity(offLineMetaTaskInstance);
                        }
                    }
                    break;
                case MercuryConstants.EXECUTOR_INSTANCE_STATUS_CANCELED: //任务已被取消
                    if (!metaTaskInstance.getStatus().equals(MercuryConstants.OFFLINE_TASK_INSTANCE_STATUS_CANCELED)) {
                        //出现错误的反馈
                        logService.addLog(MercuryConstants.LOG_LEVEL_ERROR, this.getClass().getName(), "offLineTask",
                                "offLine-MetaTaskInstance report CANCELED,but status is not CANCELED:" + metaTaskInstance.getId());
                        break;
                    }
                    //更新子任务实例信息
                    metaTaskInstance.setStatus(MercuryConstants.OFFLINE_TASK_INSTANCE_STATUS_FINISH);
                    metaTaskService.updateMetaTaskInstanceMetric(report, metaTaskInstance);

                    //更新任务流信息
                    if (metaTaskInstance.getDepNode() != 0L)
                        // 如果是任务流第一个节点
                        taskInstance.setStartTime(new Date(metric.containsKey("startTime") ?
                                ((Number) metric.get("startTime")).longValue() : 0));
                    taskInstance.setStatus(MercuryConstants.OFFLINE_TASK_INSTANCE_STATUS_FINISH);
                    taskInstance.setEndTime(new Date(metric.containsKey("endTime") ?
                            ((Number) metric.get("endTime")).longValue() : 0));
                    taskInstance.setResult("CANCELED");
                    dao.updateEntity(taskInstance);

                    //更新任务信息
                    task.setStatus(MercuryConstants.OFFLINE_TASK_STATUS_WAITING);
                    dao.updateEntity(task);
                    break;
                case MercuryConstants.EXECUTOR_INSTANCE_STATUS_RUNNING: //任务还在执行
                    //更新子任务实例信息
//                    metaTaskInstance.setStatus(MercuryConstants.OFFLINE_TASK_INSTANCE_STATUS_RUNNING);
                    metaTaskService.updateMetaTaskInstanceMetric(report, metaTaskInstance);

                    //更新任务流信息
                    if (metaTaskInstance.getDepNode() != 0L) {
                        // 如果是任务流第一个节点
                        taskInstance.setStartTime(new Date(metric.containsKey("startTime") ?
                                ((Number) metric.get("startTime")).longValue() : 0));
                        dao.updateEntity(taskInstance);
                    }
                    break;
                case MercuryConstants.EXECUTOR_INSTANCE_STATUS_LINING:  //任务还在排队中
                    metaTaskInstance.setResult("LINING");
                    update(metaTaskInstance);
                    break;
            }
        } catch (Exception e) {
            logger.error("updateOffLineTaskStatus error:{}", e);
            return false;
        }
        return true;
    }

    public List<OffLineTaskInstance> getTaskInstancesByBatch(Integer page, Integer limit, Integer runningBatch, Long id) {
        Criterion batchCriterion = Restrictions.eq("runningBatch", runningBatch);
        Criterion idCriterion = Restrictions.eq("taskId", id);
        Criterion statusCriterion = Restrictions.not(Restrictions.eq("status", MercuryConstants.OFFLINE_TASK_INSTANCE_STATUS_FINISH));
        return dao.listEntity(OffLineTaskInstance.class, page, limit, "executeTime", batchCriterion, idCriterion, statusCriterion);
    }

    public Pair<Boolean, String> addOffLineTask(OffLineTask offLineTask) {
        try {
            ExecutionTime.forCron(scheduleService.getParser().parse(offLineTask.getCron()));
        } catch (Exception e) {
            return ImmutablePair.of(false, "Cron字段解析错误");
        }
        if (Strings.isEmpty(offLineTask.getName()))
            return ImmutablePair.of(false, "标识字段解析错误");
        if (Strings.isEmpty(offLineTask.getEnable()))
            return ImmutablePair.of(false, "enable字段解析错误");

        offLineTask.setCreateTime(new Date());
        offLineTask.setLastModifyTime(new Date());
        offLineTask.setStatus(MercuryConstants.OFFLINE_TASK_STATUS_WAITING);
        offLineTask.setSuccessTimes(0);
        offLineTask.setFailTimes(0);
        if (dao.saveEntity(offLineTask) != null) {
            return ImmutablePair.of(true, null);
        } else
            return ImmutablePair.of(false, "数据添加失败");
    }

    public OffLineMetaTask getMetaTaskByName(String name, String type) {
        Criterion criterion = Restrictions.eq("name", name);
        Criterion typeCriterion = Restrictions.eq("type", type);
        List<OffLineMetaTask> value = dao.listEntity(OffLineMetaTask.class, criterion, typeCriterion);
        if (value.size() == 0)
            return null;
        else
            return value.get(0);
    }

    //获取最近批次的子任务实例
    public List<OffLineMetaTaskInstance> getMetaTaskInstancesByBatch(Integer page, Integer limit, Integer runningBatch, Long id) {
        Criterion batchCriterion = Restrictions.eq("runningBatch", runningBatch);
        Criterion idCriterion = Restrictions.eq("taskId", id);
        List<OffLineTaskInstance> taskInstances = dao.listEntity(OffLineTaskInstance.class, page, limit, batchCriterion, idCriterion);
        if (taskInstances.size() == 0)
            return new ArrayList<>();
        taskInstances.sort(Comparator.comparing(OffLineTaskInstance::getExecuteTime));
        List<OffLineMetaTaskInstance> result = null;
        for (OffLineTaskInstance taskInstance : taskInstances) {
            if (taskInstance.getStatus().equals(MercuryConstants.OFFLINE_TASK_INSTANCE_STATUS_RUNNING)) {
                Criterion metaCriterion = Restrictions.eq("taskInstanceId", taskInstance.getId());
                result = dao.listEntity(OffLineMetaTaskInstance.class, metaCriterion);
            }
        }
        if (result == null) {
            Long taskInstanceId = taskInstances.get(0).getStatus().equals(MercuryConstants.OFFLINE_TASK_INSTANCE_STATUS_WAITING) ?
                    taskInstances.get(0).getId() : taskInstances.get(taskInstances.size() - 1).getId();
            Criterion metaCriterion = Restrictions.eq("taskInstanceId", taskInstanceId);
            result = dao.listEntity(OffLineMetaTaskInstance.class, metaCriterion);
        }
        return result;
    }

    //删除任务
    public Pair<Boolean, String> deleteTask(Long id) {
        OffLineTask offLineTask = getEntityById(OffLineTask.class, id);
        if (offLineTask == null)
            return ImmutablePair.of(false, "找不到实体对象");
        Criterion idCriterion = Restrictions.eq("taskId", id);
        Criterion batchCriterion = Restrictions.eq("runningBatch", offLineTask.getRunningBatch());
        List<OffLineTaskInstance> taskInstances = dao.listEntity(OffLineTaskInstance.class, idCriterion, batchCriterion);
        for (OffLineTaskInstance taskInstance : taskInstances) {
            taskInstance.setStatus(MercuryConstants.OFFLINE_TASK_INSTANCE_STATUS_CANCELED);
            dao.updateEntity(taskInstance);
            Criterion criterion = Restrictions.eq("taskInstanceId", taskInstance.getId());
            List<OffLineMetaTaskInstance> metaTaskInstances = dao.listEntity(OffLineMetaTaskInstance.class, criterion);
            for (OffLineMetaTaskInstance metaTaskInstance : metaTaskInstances) {
                metaTaskInstance.setStatus(MercuryConstants.OFFLINE_TASK_INSTANCE_STATUS_CANCELED);
                dao.updateEntity(metaTaskInstance);
            }
        }
        List<DAG> dags = dao.listEntity(DAG.class, idCriterion);
        for (DAG dag : dags)
            deleteEntity(dag);
        if (deleteEntity(offLineTask))
            return ImmutablePair.of(true, null);
        else
            return ImmutablePair.of(false, "数据库错误");
    }

    //取消某一个任务实例
    public Pair<Boolean, String> cancelTaskInstance(Long id) {
        OffLineTaskInstance taskInstance = getEntityById(OffLineTaskInstance.class, id);
        if (taskInstance == null)
            return ImmutablePair.of(false, "找不到关键数据");
        if (taskInstance.getStatus().equals(MercuryConstants.OFFLINE_TASK_INSTANCE_STATUS_FINISH))
            return ImmutablePair.of(false, "此任务实例已经完成");
        else if (taskInstance.getStatus().equals(MercuryConstants.OFFLINE_TASK_INSTANCE_STATUS_CANCELED))
            return ImmutablePair.of(false, "此任务实例已经被取消");
        else if (taskInstance.getStatus().equals(MercuryConstants.OFFLINE_TASK_INSTANCE_STATUS_WAITING)) {
            Criterion idCriterion = Restrictions.eq("taskInstanceId", id);
            List<OffLineMetaTaskInstance> metaTaskInstances = dao.listEntity(OffLineMetaTaskInstance.class, idCriterion);
            for (OffLineMetaTaskInstance metaTaskInstance : metaTaskInstances) {
                metaTaskInstance.setStatus(MercuryConstants.OFFLINE_TASK_INSTANCE_STATUS_FINISH);
                metaTaskInstance.setResult("CANCELED");
                metaTaskInstance.setLastActionTime(new Date());
                update(metaTaskInstance);
            }
            taskInstance.setStatus(MercuryConstants.OFFLINE_TASK_INSTANCE_STATUS_FINISH);
            taskInstance.setResult("CANCELED");
            update(taskInstance);
            return ImmutablePair.of(true, null);
        } else if (taskInstance.getStatus().equals(MercuryConstants.OFFLINE_TASK_INSTANCE_STATUS_RUNNING)) {
            stopTaskImmediately(taskInstance);
            return ImmutablePair.of(true, null);
        } else
            return ImmutablePair.of(false, "数据错误");
    }
}
