package com.valor.mercury.manager.controller;

import com.valor.mercury.manager.config.MercuryConstants;
import com.valor.mercury.manager.model.system.JsonResult;
import com.valor.mercury.manager.model.system.PageResult;
import com.valor.mercury.manager.model.ddo.*;
import com.valor.mercury.manager.service.LogAlarmService;
import com.valor.mercury.manager.service.OffLineTaskScheduleService;
import com.valor.mercury.manager.service.OffLineTaskService;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.logging.log4j.util.Strings;
import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.Restrictions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;

/**
 * @Author: Gavin
 * @Date: 2020/2/20 20:49
 */
@Controller
@RequestMapping("/offLineTask")
public class OffLineTaskController {

    private Logger logger = LoggerFactory.getLogger(this.getClass());
    private final LogAlarmService logService;
    private final OffLineTaskService taskService;
    private final OffLineTaskScheduleService scheduleService;

    @Autowired
    public OffLineTaskController(LogAlarmService logService, OffLineTaskService taskService, OffLineTaskScheduleService scheduleService) {
        this.logService = logService;
        this.taskService = taskService;
        this.scheduleService = scheduleService;
    }

    @RequestMapping
    public String page() {
        return "main/offline-task.html";
    }

    @RequestMapping("editForm")
    public String editFormPage() {
        return "main/offline-task_form.html";
    }

    @RequestMapping("viewForm")
    public String viewFormPage() {
        return "main/offline-task_view_form.html";
    }

    @RequestMapping("viewInstanceForm")
    public String viewInstanceFormPage() {
        return "main/offline-taskInstance_view_form.html";
    }

    @RequestMapping("editDAGForm")
    public String editDAGFormPage() {
        return "main/offline-task_form_dag.html";
    }

    @RequestMapping("listInstances")
    @ResponseBody
    public PageResult<OffLineTaskInstance> listInstances(@RequestParam(value = "page", defaultValue = "1") Integer page
            , @RequestParam(value = "limit", defaultValue = "10") Integer limit, @RequestParam Long id) {
        OffLineTask offLineTask = taskService.getEntityById(OffLineTask.class, id);
        if (offLineTask == null) {
            logger.error("OffLineTaskController listInstances empty id:{}", id);
            return null;
        }
        List<OffLineTaskInstance> instances = taskService.getTaskInstancesByBatch(page, limit, offLineTask.getRunningBatch(), offLineTask.getId());
        return new PageResult<>(instances.size(), instances);
    }

    @RequestMapping("listMetaInstances")
    @ResponseBody
    public PageResult<OffLineMetaTaskInstance> listMetaInstances(@RequestParam(value = "page", defaultValue = "1") Integer page
            , @RequestParam(value = "limit", defaultValue = "10") Integer limit, @RequestParam Long id) {
        OffLineTask offLineTask = taskService.getEntityById(OffLineTask.class, id);
        if (offLineTask == null) {
            logger.error("OffLineTaskController listMetaInstances empty id:{}", id);
            return null;
        }
        List<OffLineMetaTaskInstance> instances = taskService.getMetaTaskInstancesByBatch(page, limit, offLineTask.getRunningBatch(), offLineTask.getId());
        return new PageResult<>(instances.size(), instances);
    }

    @RequestMapping("list")
    @ResponseBody
    public PageResult<OffLineTask> list(@RequestParam(value = "page", defaultValue = "1") Integer page
            , @RequestParam(value = "limit", defaultValue = "10") Integer limit) {
        logger.info("request task list. pageIndex:{},pageSize:{}", page, limit);
        PageResult<OffLineTask> pageResult = taskService.list(OffLineTask.class, page, limit);
        for (OffLineTask offLineTask : pageResult.getData()) {
            Criterion criterion = Restrictions.eq("taskId", offLineTask.getId());
            List<DAG> dags = taskService.listEntity(DAG.class, criterion);
            offLineTask.setDags(dags);
        }
        return pageResult;
    }

    @RequestMapping("listInstance")
    @ResponseBody
    public PageResult<OffLineTaskInstance> listInstance(@RequestParam(value = "page", defaultValue = "1") Integer page
            , @RequestParam(value = "limit", defaultValue = "10") Integer limit) {
        logger.info("request OffLineTaskInstance list. pageIndex:{},pageSize:{}", page, limit);
        return taskService.list(OffLineTaskInstance.class, page, limit);
    }

    @RequestMapping("queryList")
    @ResponseBody
    public PageResult<OffLineTask> queryList(@RequestParam(value = "page", defaultValue = "1") Integer page
            , @RequestParam(value = "limit", defaultValue = "10") Integer limit, Long id, String name) {
        logger.info("request task queryList. id:{},name:{}", id, name);
        Criterion idCriterion = id == null ? null : Restrictions.eq("id", id);
        Criterion nameCriterion = Strings.isEmpty(name) ? null : Restrictions.like("name", name);
        return taskService.list(OffLineTask.class, page, limit, idCriterion, nameCriterion);
    }

    @RequestMapping("queryListInstance")
    @ResponseBody
    public PageResult<OffLineTaskInstance> queryListInstance(@RequestParam(value = "page", defaultValue = "1") Integer page
            , @RequestParam(value = "limit", defaultValue = "10") Integer limit, Long id, String name, Integer batch, String status, String result) {
        logger.info("request task queryList. id:{},name:{},batch:{},status:{},result:{}", id, name, batch, status, result);
        Criterion idCriterion = id == null ? null : Restrictions.eq("id", id);
        Criterion nameCriterion = Strings.isEmpty(name) ? null : Restrictions.like("name", name);
        Criterion batchCriterion = batch == null ? null : Restrictions.like("runningBatch", batch);
        Criterion statusCriterion = Strings.isEmpty(status) ? null : Restrictions.eq("status", status);
        Criterion resultCriterion = Strings.isEmpty(result) ? null : Restrictions.eq("result", result);
        return taskService.list(OffLineTaskInstance.class, page, limit, idCriterion, nameCriterion, batchCriterion, statusCriterion, resultCriterion);
    }

    @RequestMapping("add")
    @ResponseBody
    public JsonResult add(OffLineTask offLineTask) {
        logger.info("request offLineTask add. offLineTask:{}", offLineTask.toString());
        logService.addLog(MercuryConstants.LOG_LEVEL_INFO, this.getClass().getName(), offLineTask.getName(), "add offLineTask");
        Pair<Boolean, String> result = taskService.addOffLineTask(offLineTask);
        if (result.getKey())
            return JsonResult.ok("添加成功");
        else
            return JsonResult.error("添加失败：" + result.getValue());
    }

    @RequestMapping("editDAG")
    @ResponseBody
    public JsonResult editDAG(String jsonValue, String taskId) {
        logger.info("request offLineTask modifyDAG. taskId:{},dags:{}", taskId, jsonValue);
        if (Strings.isEmpty(taskId))
            return JsonResult.error("关键字段为空");
        OffLineTask offLineTask = taskService.getEntityById(OffLineTask.class, Long.parseLong(taskId));
        if (offLineTask == null)
            return JsonResult.error("找不到关键数据");
        else if (offLineTask.getStatus().equals(MercuryConstants.OFFLINE_TASK_STATUS_RUNNING))
            return JsonResult.error("任务流正在执行，请停止后修改");
//        if (jsonValue.equals(offLineTask.getDag()))
//            return JsonResult.ok("未发生变化，不需要修改");
        Pair<Boolean, String> result = taskService.setTaskDAG(jsonValue, offLineTask);
        if (result.getKey())
            return JsonResult.ok("配置成功");
        else
            return JsonResult.error(result.getValue());
    }

    @RequestMapping("edit")
    @ResponseBody
    public JsonResult edit(@RequestParam Long id, String name, String desc, String cron, String enable) {
        logger.info("request offLineTask edit. offLineTask:{}", name);
        OffLineTask offLineTask = taskService.getEntityById(OffLineTask.class, id);
        if (offLineTask.isEmpty())
            return JsonResult.error("修改失败：关键字段为空");
        else if (scheduleService.isValidCronExpression(cron)) {
            offLineTask.setName(name);
            offLineTask.setDesc(desc);
            offLineTask.setCron(cron);
            offLineTask.setEnable(enable);
            if (taskService.update(offLineTask))
                return JsonResult.ok("修改成功");
            else
                return JsonResult.error("修改失败：数据库错误");
        } else
            return JsonResult.error("修改失败：cron表达式错误");
    }

    @RequestMapping("delete")
    @ResponseBody
    public JsonResult delete(@RequestParam Long id) {
        logger.info("request offLineTask delete. offLineTask:{}", id);
        Pair<Boolean, String> result = taskService.deleteTask(id);
        if (result.getKey())
            return JsonResult.ok("删除成功");
        else
            return JsonResult.error("删除失败：" + result.getValue());
    }

    @RequestMapping("run")
    @ResponseBody
    public JsonResult run(@RequestParam Long id) {
        logger.info("request offLineTask run. offLineTask:{}", id);
        OffLineTask offLineTask = taskService.getEntityById(OffLineTask.class, id);
        if (offLineTask.isEmpty())
            return JsonResult.error("立即执行失败：找不到任务流");
        else {
            if (taskService.runTaskImmediately(offLineTask))
                return JsonResult.ok("立即执行成功");
            else
                return JsonResult.error("立即执行失败：数据库错误");
        }
    }

    @RequestMapping("stop")
    @ResponseBody
    public JsonResult stop(@RequestParam Long id) {
        logger.info("request offLineTask stop. offLineTask:{}", id);
        OffLineTask offLineTask = taskService.getEntityById(OffLineTask.class, id);
        if (offLineTask.isEmpty())
            return JsonResult.error("停止失败：找不到任务流");
        else {
            Criterion statusCriterion = Restrictions.eq("status", MercuryConstants.OFFLINE_TASK_STATUS_RUNNING);
            Criterion idCriterion = Restrictions.eq("taskId", offLineTask.getId());
            List<OffLineTaskInstance> instances = taskService.listEntity(OffLineTaskInstance.class, statusCriterion, idCriterion);
            if (instances.size() == 0) {
                if (offLineTask.getStatus().equals(MercuryConstants.OFFLINE_TASK_STATUS_RUNNING)) {
                    offLineTask.setStatus(MercuryConstants.OFFLINE_TASK_STATUS_WAITING);
                    taskService.update(offLineTask);
                }
                return JsonResult.ok("没有正在执行的任务实例");
            }
            if (taskService.clearTaskImmediately(offLineTask))
                return JsonResult.ok("全部停止成功");
            else
                return JsonResult.error("全部停止失败");
        }
    }

    @RequestMapping("schedule")
    @ResponseBody
    public JsonResult clear(@RequestParam Long id) {
        logger.info("request offLineTask schedule. offLineTask:{}", id);
        OffLineTask offLineTask = taskService.getEntityById(OffLineTask.class, id);
        if (offLineTask.isEmpty())
            return JsonResult.error("调度失败：找不到任务流");
        else {
            try {
                scheduleService.taskSchedule(offLineTask);
                return JsonResult.ok("调度成功");
            } catch (Exception e) {
                return JsonResult.error("调度失败:" + e.getMessage());
            }
        }
    }

    @RequestMapping("instances/cancel")
    @ResponseBody
    public JsonResult cancelInstance(@RequestParam Long id) {
        logger.info("request offLineTask cancel. taskId:{}", id);
        Pair<Boolean, String> result = taskService.cancelTaskInstance(id);
        if (result.getKey())
            return JsonResult.ok("取消成功");
        else
            return JsonResult.error("取消失败：" + result.getValue());
    }

}
