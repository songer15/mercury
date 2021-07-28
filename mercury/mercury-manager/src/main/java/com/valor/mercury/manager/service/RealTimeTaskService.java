package com.valor.mercury.manager.service;

import com.valor.mercury.manager.config.MercuryConstants;
import com.valor.mercury.manager.dao.MercuryManagerDao;
import com.valor.mercury.manager.model.ddo.RealTimeTask;
import com.valor.mercury.manager.model.ddo.RealTimeTaskInstance;
import com.valor.mercury.manager.model.ddo.UploadFileConfig;
import com.valor.mercury.manager.model.system.ExecutorCommand;
import com.valor.mercury.manager.model.system.ExecutorReport;
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
 * @Date: 2020/2/19 16:50
 * 实时任务管理
 */
@Service
public class RealTimeTaskService extends BaseDBService {

    private final MercuryManagerDao dao;
    private final LogAlarmService logService;
    private Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    public RealTimeTaskService(MercuryManagerDao dao, LogAlarmService logService) {
        this.dao = dao;
        this.logService = logService;
    }

    @Override
    public MercuryManagerDao getSchedulerDao() {
        return dao;
    }


    public Pair<Boolean, String> publishTask(Long taskId) {
        RealTimeTask task = getEntityById(RealTimeTask.class, taskId);
        if (task == null)
            return ImmutablePair.of(false, "未找到此任务");
        else if (task.getStatus().equals(MercuryConstants.REALTIME_TASK_STATUS_PUBLISHING))
            return ImmutablePair.of(false, "任务正在发布");
        else if (task.getStatus().equals(MercuryConstants.REALTIME_TASK_STATUS_RUNNING))
            return ImmutablePair.of(false, "任务还在执行中");
        else if (task.getStatus().equals(MercuryConstants.REALTIME_TASK_STATUS_CANCELED) ||
                task.getStatus().equals(MercuryConstants.REALTIME_TASK_STATUS_CANCELLING))
            return ImmutablePair.of(false, "任务正在取消");

        logService.addLog(MercuryConstants.LOG_LEVEL_INFO, this.getClass().getName(), "realTimeTask", "publish task:" + task.toString());
        RealTimeTaskInstance instances = new RealTimeTaskInstance();
        instances.setName(task.getName());
        instances.setDesc(task.getDesc());
        instances.setCreateTime(new Date());
        instances.setEntryClass(task.getEntryClass());
        instances.setExecutor(task.getExecutor());
        instances.setFileId(task.getFileId());
        instances.setParallelism(task.getParallelism());
        instances.setStatus(MercuryConstants.REALTIME_TASK_STATUS_PUBLISHING);
        instances.setProgramArguments(task.getProgramArguments());
        instances.setTaskId(taskId);
        Long instanceId = (long) dao.saveEntity(instances);

        task.setLastPublishTime(new Date());
        task.setStatus(MercuryConstants.REALTIME_TASK_STATUS_PUBLISHING);
        task.setLastPublishInstanceId(instanceId);
        task.setLastActionTime(new Date());
        update(task);
        return ImmutablePair.of(true, null);
    }

    public Pair<Boolean, String> editTask(RealTimeTask task) {
        RealTimeTask preTask = getEntityById(RealTimeTask.class, task.getId());
//        if (preTask.getStatus().equals(MercuryConstants.REALTIME_TASK_STATUS_PUBLISHING) ||
//                preTask.getStatus().equals(MercuryConstants.REALTIME_TASK_STATUS_RUNNING) ||
//                preTask.getStatus().equals(MercuryConstants.REALTIME_TASK_STATUS_CANCELLING) ||
//                preTask.getStatus().equals(MercuryConstants.REALTIME_TASK_STATUS_CANCELED))
//            return ImmutablePair.of(false, "任务正在执行中！");
        task.setCreateTime(preTask.getCreateTime());
        update(task);
        return ImmutablePair.of(true, null);
    }

    public Pair<Boolean, String> stopTask(Long taskId) {
        RealTimeTask task = getEntityById(RealTimeTask.class, taskId);
        if (task == null)
            return ImmutablePair.of(false, "未找到此任务");
        else if (task.getStatus().equals(MercuryConstants.REALTIME_TASK_STATUS_INIT))
            return ImmutablePair.of(false, "任务未在执行状态");
        else if (task.getStatus().equals(MercuryConstants.REALTIME_TASK_STATUS_CANCELED) ||
                task.getStatus().equals(MercuryConstants.REALTIME_TASK_STATUS_CANCELLING))
            return ImmutablePair.of(false, "任务正在暂停");
        else if (task.getStatus().equals(MercuryConstants.REALTIME_TASK_STATUS_FINISH))
            return ImmutablePair.of(false, "任务未在执行");
        else if (task.getStatus().equals(MercuryConstants.REALTIME_TASK_STATUS_PUBLISHING)) {
            task.setStatus(MercuryConstants.REALTIME_TASK_STATUS_INIT);
            update(task);
            RealTimeTaskInstance instances = getEntityById(RealTimeTaskInstance.class, task.getLastPublishInstanceId());
            instances.setStatus(MercuryConstants.REALTIME_TASK_STATUS_INIT);
            instances.setResult("Stop");
            update(instances);
            return ImmutablePair.of(true, null);
        } else {
            RealTimeTaskInstance instances = getEntityById(RealTimeTaskInstance.class, task.getLastPublishInstanceId());
            instances.setStatus(MercuryConstants.REALTIME_TASK_STATUS_CANCELLING);
            update(instances);
            task.setStatus(MercuryConstants.REALTIME_TASK_STATUS_CANCELLING);
            update(task);
            return ImmutablePair.of(true, null);
        }
    }

    public List<ExecutorCommand> getRealTimeTaskCommandByExecutor(String clientName) {
        List<ExecutorCommand> result = new ArrayList<>();
        Criterion criterion = Restrictions.eq("executor", clientName);
        Criterion runCriterion = Restrictions.eq("status", MercuryConstants.REALTIME_TASK_STATUS_PUBLISHING);
        Criterion stopCriterion = Restrictions.eq("status", MercuryConstants.REALTIME_TASK_STATUS_CANCELLING);
        List<RealTimeTaskInstance> runInstances = dao.listEntity(RealTimeTaskInstance.class, criterion, runCriterion);
        for (RealTimeTaskInstance instance : runInstances) {
            ExecutorCommand command = new ExecutorCommand();
            command.setActionTime(new Date());
            command.setCommandType(MercuryConstants.EXECUTOR_COMMAND_START);
            command.setInstanceID(instance.getId());
            Map<String, Object> taskConfig = new HashMap<>();
            taskConfig.put("entryClass", instance.getEntryClass());
            taskConfig.put("parallelism", instance.getParallelism());
            taskConfig.put("programArguments", instance.getProgramArguments());
            UploadFileConfig fileConfig = getEntityById(UploadFileConfig.class, instance.getFileId());
            taskConfig.put("jarName", fileConfig.getFileName());
            taskConfig.put("jarMD5", fileConfig.getFileMd5());
            command.setTaskConfig(taskConfig);
            result.add(command);

            //更新任务状态
            instance.setStatus(MercuryConstants.REALTIME_TASK_STATUS_RUNNING);
            instance.setLastActionTime(new Date());
            update(instance);
            RealTimeTask task = getEntityById(RealTimeTask.class, instance.getTaskId());
            task.setStatus(MercuryConstants.REALTIME_TASK_STATUS_RUNNING);
            update(task);
        }
        List<RealTimeTaskInstance> stopInstances = dao.listEntity(RealTimeTaskInstance.class, criterion, stopCriterion);
        for (RealTimeTaskInstance instance : stopInstances) {
            ExecutorCommand command = new ExecutorCommand();
            command.setActionTime(new Date());
            command.setCommandType(MercuryConstants.EXECUTOR_COMMAND_STOP);
            command.setInstanceID(instance.getId());
            Map<String, Object> taskConfig = new HashMap<>();
            taskConfig.put("jobId", instance.getJobId());
            command.setTaskConfig(taskConfig);
            result.add(command);

            //更新任务状态
            instance.setStatus(MercuryConstants.REALTIME_TASK_STATUS_CANCELED);
            instance.setLastActionTime(new Date());
            update(instance);
            RealTimeTask task = getEntityById(RealTimeTask.class, instance.getTaskId());
            task.setStatus(MercuryConstants.REALTIME_TASK_STATUS_CANCELED);
            update(task);
        }
        return result;
    }

    //客户端返回指标
    public boolean updateRealTimeTaskStatus(ExecutorReport report) {
        RealTimeTaskInstance instance;
        String jobId = null;
        if (report.getMetrics().containsKey("jobId"))
            jobId = report.getMetrics().get("jobId").toString();
        if (report.getInstanceID() == null || report.getInstanceID() == 0) {
            if (jobId == null) {
                logger.error("instance id is null,jobId is null:{}", report.toString());
                logService.addLog(MercuryConstants.LOG_LEVEL_ERROR, this.getClass().getName(), "realTimeTask", "instance id is null,jobId is null:" + report.toString());
                return true;
            }
            Criterion criterion = Restrictions.eq("jobId", jobId);
            instance = getEntityByCriterion(RealTimeTaskInstance.class, criterion);
        } else instance = getEntityById(RealTimeTaskInstance.class, report.getInstanceID());
        RealTimeTask task = getEntityById(RealTimeTask.class, instance.getTaskId());
        if (Strings.isEmpty(instance.getJobId())) {
            instance.setJobId(jobId);
            task.setJobId(jobId);
        }
        instance.setLastActionTime(report.getActionTime());
        task.setLastActionTime(report.getActionTime());
        instance.setStatus(report.getInstanceStatus());
        switch (report.getInstanceStatus()) {
            case MercuryConstants.EXECUTOR_INSTANCE_STATUS_CANCELED:
                task.setStatus(MercuryConstants.REALTIME_TASK_STATUS_INIT);
                break;
            case MercuryConstants.EXECUTOR_INSTANCE_STATUS_FAIL:
                logger.error("instance fail:{}", report.toString());
                logService.addLog(MercuryConstants.LOG_LEVEL_ERROR, this.getClass().getName(), "realTimeTask", "instance fail:" + report.toString());
                task.setStatus(MercuryConstants.REALTIME_TASK_STATUS_FINISH);
                break;
            case MercuryConstants.EXECUTOR_INSTANCE_STATUS_RUNNING:
                break;
            case MercuryConstants.EXECUTOR_INSTANCE_STATUS_LINING:
                break;
            case MercuryConstants.EXECUTOR_INSTANCE_STATUS_SUCCESS:
                break;
            default:
                logger.error("unknown report status:{}", report.toString());
                logService.addLog(MercuryConstants.LOG_LEVEL_ERROR, this.getClass().getName(), "realTimeTask", "unknown report status:" + report.toString());
        }
        update(instance);
        update(task);
        return true;
    }
}
