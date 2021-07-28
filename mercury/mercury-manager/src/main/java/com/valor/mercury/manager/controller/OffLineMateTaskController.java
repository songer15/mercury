package com.valor.mercury.manager.controller;

import com.valor.mercury.manager.config.MercuryConstants;
import com.valor.mercury.manager.model.ddo.*;
import com.valor.mercury.manager.model.system.*;
import com.valor.mercury.manager.service.OffLineMetaTaskService;
import com.valor.mercury.manager.service.OffLineTaskService;
import com.valor.mercury.manager.tool.JsonUtil;
import org.apache.commons.lang3.StringEscapeUtils;
import org.apache.logging.log4j.util.Strings;
import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.Restrictions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.*;

/**
 * @author Gavin
 * 2020/7/29 11:08
 */
@Controller
@RequestMapping("/offLineMetaTask")
public class OffLineMateTaskController {

    private Logger logger = LoggerFactory.getLogger(this.getClass());
    private final OffLineTaskService taskService;
    private final OffLineMetaTaskService metaTaskService;

    @Autowired
    public OffLineMateTaskController(OffLineTaskService taskService, OffLineMetaTaskService metaTaskService) {
        this.taskService = taskService;
        this.metaTaskService = metaTaskService;
    }

    @RequestMapping("spider")
    public String page(Model model) {
        List<Executor> taskExecutors = taskService.listEntity(Executor.class,
                Restrictions.eq("executorType", MercuryConstants.TYPE_OFFLINE_SPIDER));
        List<String> executors = new ArrayList<>();
        taskExecutors.forEach(v -> executors.add(v.getClientName()));
        model.addAttribute("executors", executors);
        return "main/spider_meta_task.html";
    }

    @RequestMapping("script")
    public String scriptPage(Model model) {
        List<Executor> taskExecutors = taskService.listEntity(Executor.class,
                Restrictions.eq("executorType", MercuryConstants.TYPE_OFFLINE_SCRIPT));
        List<String> executors = new ArrayList<>();
        taskExecutors.forEach(v -> executors.add(v.getClientName()));
        model.addAttribute("executors", executors);
        return "main/script_meta_task.html";
    }

    @RequestMapping("hive")
    public String hivePage(Model model) {
        List<Executor> taskExecutors = taskService.listEntity(Executor.class,
                Restrictions.eq("executorType", MercuryConstants.TYPE_OFFLINE_HIVE));
        List<String> executors = new ArrayList<>();
        taskExecutors.forEach(v -> executors.add(v.getClientName()));
        model.addAttribute("executors", executors);
        return "main/hive_meta_task.html";
    }

    @RequestMapping("spider/editForm")
    public String editFormPage(String id, Model model) throws Exception {
        List<Executor> taskExecutors = taskService.listEntity(Executor.class,
                Restrictions.eq("executorType", MercuryConstants.TYPE_OFFLINE_SPIDER));
        List<String> executors = new ArrayList<>();
        taskExecutors.forEach(v -> executors.add(v.getClientName()));
        model.addAttribute("executors", executors);

        List<DataBaseConfig> dataBaseConfigs = taskService.listEntity(DataBaseConfig.class);
        List<String> databases = new ArrayList<>();
        dataBaseConfigs.forEach(v -> databases.add(v.getName()));
        model.addAttribute("databases", databases);

        List<ETLDataReceive> ETLDataReceives = taskService.listEntity(ETLDataReceive.class);
        List<String> receivers = new ArrayList<>();
        ETLDataReceives.forEach(v -> receivers.add(v.getReceiveName()));
        model.addAttribute("receivers", receivers);
        if (Strings.isNotEmpty(id)) {
            OffLineMetaTask task = taskService.getEntityById(OffLineMetaTask.class, Long.parseLong(id));
            model.addAttribute("meta_task",
                    StringEscapeUtils.escapeJson(task.getConfig()).replace("'", "\\'"));
        } else
            model.addAttribute("meta_task", "null");
        return "main/spider_meta_task_form.html";
    }

    @RequestMapping("script/editForm")
    public String editScriptFormPage(String id, Model model) throws Exception {
        List<Executor> taskExecutors = taskService.listEntity(Executor.class,
                Restrictions.eq("executorType", MercuryConstants.TYPE_OFFLINE_SCRIPT));
        List<String> executors = new ArrayList<>();
        taskExecutors.forEach(v -> executors.add(v.getClientName()));
        model.addAttribute("executors", executors);
        List<UploadFileConfig> files = taskService.listEntity(UploadFileConfig.class, Restrictions.eq("targetType", "Script"));
        model.addAttribute("files", files);
        if (Strings.isNotEmpty(id)) {
            OffLineMetaTask task = taskService.getEntityById(OffLineMetaTask.class, Long.parseLong(id));
            model.addAttribute("meta_task",
                    StringEscapeUtils.escapeJson(task.getConfig()).replace("'", "\\'"));
        } else
            model.addAttribute("meta_task", "null");
        return "main/script_meta_task_form.html";
    }

    @RequestMapping("hive/editForm")
    public String editHiveFormPage(String id, Model model) throws Exception {
        List<Executor> taskExecutors = taskService.listEntity(Executor.class,
                Restrictions.eq("executorType", MercuryConstants.TYPE_OFFLINE_HIVE));
        List<String> executors = new ArrayList<>();
        taskExecutors.forEach(v -> executors.add(v.getClientName()));
        model.addAttribute("executors", executors);

        if (Strings.isNotEmpty(id)) {
            OffLineMetaTask task = taskService.getEntityById(OffLineMetaTask.class, Long.parseLong(id));
            model.addAttribute("meta_task",
                    StringEscapeUtils.escapeJson(task.getConfig()).replace("'", "\\'"));
        } else
            model.addAttribute("meta_task", "null");
        return "main/hive_meta_task_form.html";
    }

    @RequestMapping("listMetaInstances")
    @ResponseBody
    public PageResult<OffLineMetaTaskInstance> listMetaInstances(@RequestParam(value = "page", defaultValue = "1") Integer page
            , @RequestParam(value = "limit", defaultValue = "10") Integer limit, @RequestParam Long id) {
        OffLineTaskInstance offLineTaskInstance = taskService.getEntityById(OffLineTaskInstance.class, id);
        if (offLineTaskInstance == null) {
            logger.error("OffLineMateTaskController listMetaInstances empty id:{}", id);
            return null;
        }
        return taskService.list(OffLineMetaTaskInstance.class, page, limit,
                Restrictions.eq("taskInstanceId", id));
    }

    @RequestMapping("spider/list")
    @ResponseBody
    public PageResult<OffLineMetaTask> listSpiderTask(@RequestParam(value = "page", defaultValue = "1") Integer page
            , @RequestParam(value = "limit", defaultValue = "10") Integer limit) {
        logger.info("request spider task list. pageIndex:{},pageSize:{}", page, limit);
        Criterion criterion = Restrictions.eq("type", MercuryConstants.TYPE_OFFLINE_SPIDER);
        return taskService.list(OffLineMetaTask.class, page, limit, criterion);
    }

    @RequestMapping("spider/listInstance")
    @ResponseBody
    public PageResult<OffLineMetaTaskInstance> listSpiderTaskInstance(@RequestParam(value = "page", defaultValue = "1") Integer page
            , @RequestParam(value = "limit", defaultValue = "10") Integer limit) {
        logger.info("request spider task instances list. pageIndex:{},pageSize:{}", page, limit);
        Criterion criterion = Restrictions.eq("type", MercuryConstants.TYPE_OFFLINE_SPIDER);
        PageResult<OffLineMetaTaskInstance> result = taskService.list(OffLineMetaTaskInstance.class, page, limit, criterion);
        for (OffLineMetaTaskInstance instance : result.getData()) {
            if (Strings.isNotEmpty(instance.getMetrics())) {
                try {
                    Map<String, Object> map = JsonUtil.jsonToMap(instance.getMetrics());
                    instance.setExtValue(map.get("sendNumber").toString());
                } catch (Exception e) {
                    logger.error("listSpiderTaskInstance error:{}", e);
                }
            }
        }
        return result;
    }

    @RequestMapping("script/listInstance")
    @ResponseBody
    public PageResult<OffLineMetaTaskInstance> listScriptTaskInstance(@RequestParam(value = "page", defaultValue = "1") Integer page
            , @RequestParam(value = "limit", defaultValue = "10") Integer limit) {
        logger.info("request script task list. pageIndex:{},pageSize:{}", page, limit);
        Criterion criterion = Restrictions.eq("type", MercuryConstants.TYPE_OFFLINE_SCRIPT);
        return taskService.list(OffLineMetaTaskInstance.class, page, limit, criterion);
    }

    @RequestMapping("script/list")
    @ResponseBody
    public PageResult<OffLineMetaTask> listScriptTask(@RequestParam(value = "page", defaultValue = "1") Integer page
            , @RequestParam(value = "limit", defaultValue = "10") Integer limit) {
        logger.info("request script task list. pageIndex:{},pageSize:{}", page, limit);
        Criterion criterion = Restrictions.eq("type", MercuryConstants.TYPE_OFFLINE_SCRIPT);
        return taskService.list(OffLineMetaTask.class, page, limit, criterion);
    }

    @RequestMapping("hive/list")
    @ResponseBody
    public PageResult<OffLineMetaTask> listHiveTask(@RequestParam(value = "page", defaultValue = "1") Integer page
            , @RequestParam(value = "limit", defaultValue = "10") Integer limit) {
        logger.info("request hive task list. pageIndex:{},pageSize:{}", page, limit);
        Criterion criterion = Restrictions.eq("type", MercuryConstants.TYPE_OFFLINE_HIVE);
        return taskService.list(OffLineMetaTask.class, page, limit, criterion);
    }

    @RequestMapping("hive/listInstance")
    @ResponseBody
    public PageResult<OffLineMetaTaskInstance> listHiveTaskInstance(@RequestParam(value = "page", defaultValue = "1") Integer page
            , @RequestParam(value = "limit", defaultValue = "10") Integer limit) {
        logger.info("request hive task instances list. pageIndex:{},pageSize:{}", page, limit);
        Criterion criterion = Restrictions.eq("type", MercuryConstants.TYPE_OFFLINE_HIVE);
        return taskService.list(OffLineMetaTaskInstance.class, page, limit, criterion);
    }

    @RequestMapping("spider/queryList")
    @ResponseBody
    public PageResult<OffLineMetaTask> queryListSpiderTask(@RequestParam(value = "page", defaultValue = "1") Integer page
            , @RequestParam(value = "limit", defaultValue = "10") Integer limit, Long id, String name, String executor, String config) {
        logger.info("request queryListSpiderTask list. pageIndex:{},pageSize:{}", page, limit);
        Criterion idCriterion = id == null ? null : Restrictions.eq("id", id);
        Criterion nameCriterion = Strings.isEmpty(name) ? null : Restrictions.eq("name", name);
        Criterion executorCriterion = Strings.isEmpty(executor) ? null : Restrictions.eq("executor", executor);
        Criterion configCriterion = Strings.isEmpty(config) ? null : Restrictions.like("config", config);
        Criterion criterion = Restrictions.eq("type", MercuryConstants.TYPE_OFFLINE_SPIDER);
        return taskService.list(OffLineMetaTask.class, page, limit,
                idCriterion, nameCriterion, executorCriterion, configCriterion, criterion);
    }

    @RequestMapping("spider/queryListInstance")
    @ResponseBody
    public PageResult<OffLineMetaTaskInstance> queryListSpiderTaskInstance(@RequestParam(value = "page", defaultValue = "1") Integer page
            , @RequestParam(value = "limit", defaultValue = "10") Integer limit, Long id, String name, String executor, String status, String result, Integer batch) {
        logger.info("request queryListSpiderTaskInstance list. pageIndex:{},pageSize:{}", page, limit);
        Criterion idCriterion = id == null ? null : Restrictions.eq("id", id);
        Criterion nameCriterion = Strings.isEmpty(name) ? null : Restrictions.eq("name", name);
        Criterion executorCriterion = Strings.isEmpty(executor) ? null : Restrictions.eq("executor", executor);
        Criterion statusCriterion = Strings.isEmpty(status) ? null : Restrictions.like("status", status);
        Criterion resultCriterion = Strings.isEmpty(result) ? null : Restrictions.like("result", result);
        Criterion batchCriterion = batch == null ? null : Restrictions.like("batch", batch);
        Criterion criterion = Restrictions.eq("type", MercuryConstants.TYPE_OFFLINE_SPIDER);
        PageResult<OffLineMetaTaskInstance> queryResult = taskService.list(OffLineMetaTaskInstance.class, page, limit,
                idCriterion, nameCriterion, executorCriterion, statusCriterion, criterion, resultCriterion, batchCriterion);
        for (OffLineMetaTaskInstance instance : queryResult.getData()) {
            if (Strings.isNotEmpty(instance.getMetrics())) {
                try {
                    Map<String, Object> map = JsonUtil.jsonToMap(instance.getMetrics());
                    instance.setExtValue(map.get("sendNumber").toString());
                } catch (Exception e) {
                    logger.error("listSpiderTaskInstance error:{}", e);
                }
            }
        }
        return queryResult;
    }

    @RequestMapping("script/queryListInstance")
    @ResponseBody
    public PageResult<OffLineMetaTaskInstance> queryListScriptTaskInstance(@RequestParam(value = "page", defaultValue = "1") Integer page
            , @RequestParam(value = "limit", defaultValue = "10") Integer limit, Long id, String name, String executor, String status, String result, Integer batch) {
        logger.info("request queryListSpiderTaskInstance list. pageIndex:{},pageSize:{}", page, limit);
        Criterion idCriterion = id == null ? null : Restrictions.eq("id", id);
        Criterion nameCriterion = Strings.isEmpty(name) ? null : Restrictions.eq("name", name);
        Criterion executorCriterion = Strings.isEmpty(executor) ? null : Restrictions.eq("executor", executor);
        Criterion statusCriterion = Strings.isEmpty(status) ? null : Restrictions.like("status", status);
        Criterion resultCriterion = Strings.isEmpty(result) ? null : Restrictions.like("result", result);
        Criterion batchCriterion = batch == null ? null : Restrictions.like("batch", batch);
        Criterion criterion = Restrictions.eq("type", MercuryConstants.TYPE_OFFLINE_SCRIPT);
        return taskService.list(OffLineMetaTaskInstance.class, page, limit,
                idCriterion, nameCriterion, executorCriterion, statusCriterion, criterion, resultCriterion, batchCriterion);
    }

    @RequestMapping("script/queryList")
    @ResponseBody
    public PageResult<OffLineMetaTask> queryListScriptTask(@RequestParam(value = "page", defaultValue = "1") Integer page
            , @RequestParam(value = "limit", defaultValue = "10") Integer limit, Long id, String name, String executor, String config) {
        logger.info("request queryListScriptTask list. pageIndex:{},pageSize:{}", page, limit);
        Criterion idCriterion = id == null ? null : Restrictions.eq("id", id);
        Criterion nameCriterion = Strings.isEmpty(name) ? null : Restrictions.eq("name", name);
        Criterion executorCriterion = Strings.isEmpty(executor) ? null : Restrictions.eq("executor", executor);
        Criterion configCriterion = Strings.isEmpty(config) ? null : Restrictions.like("config", config);
        Criterion criterion = Restrictions.eq("type", MercuryConstants.TYPE_OFFLINE_SCRIPT);
        return taskService.list(OffLineMetaTask.class, page, limit, idCriterion, nameCriterion, executorCriterion, configCriterion, criterion);
    }

    @RequestMapping("hive/queryListInstance")
    @ResponseBody
    public PageResult<OffLineMetaTaskInstance> queryListHiveTaskInstance(@RequestParam(value = "page", defaultValue = "1") Integer page
            , @RequestParam(value = "limit", defaultValue = "10") Integer limit, Long id, String name, String executor, String status, String result, Integer batch) {
        logger.info("request queryListHiveTaskInstance list. pageIndex:{},pageSize:{}", page, limit);
        Criterion idCriterion = id == null ? null : Restrictions.eq("id", id);
        Criterion nameCriterion = Strings.isEmpty(name) ? null : Restrictions.eq("name", name);
        Criterion executorCriterion = Strings.isEmpty(executor) ? null : Restrictions.eq("executor", executor);
        Criterion statusCriterion = Strings.isEmpty(status) ? null : Restrictions.like("status", status);
        Criterion resultCriterion = Strings.isEmpty(result) ? null : Restrictions.like("result", result);
        Criterion batchCriterion = batch == null ? null : Restrictions.like("batch", batch);
        Criterion criterion = Restrictions.eq("type", MercuryConstants.TYPE_OFFLINE_HIVE);
        return taskService.list(OffLineMetaTaskInstance.class, page, limit,
                idCriterion, nameCriterion, executorCriterion, statusCriterion, criterion, resultCriterion, batchCriterion);
    }

    @RequestMapping("hive/queryList")
    @ResponseBody
    public PageResult<OffLineMetaTask> queryListHiveTask(@RequestParam(value = "page", defaultValue = "1") Integer page
            , @RequestParam(value = "limit", defaultValue = "10") Integer limit, Long id, String name, String executor, String config) {
        logger.info("request queryListHiveTask list. pageIndex:{},pageSize:{}", page, limit);
        Criterion idCriterion = id == null ? null : Restrictions.eq("id", id);
        Criterion nameCriterion = Strings.isEmpty(name) ? null : Restrictions.eq("name", name);
        Criterion executorCriterion = Strings.isEmpty(executor) ? null : Restrictions.eq("executor", executor);
        Criterion configCriterion = Strings.isEmpty(config) ? null : Restrictions.like("config", config);
        Criterion criterion = Restrictions.eq("type", MercuryConstants.TYPE_OFFLINE_HIVE);
        return taskService.list(OffLineMetaTask.class, page, limit, idCriterion, nameCriterion, executorCriterion, configCriterion, criterion);
    }

    @RequestMapping("queryList")
    @ResponseBody
    public PageResult<OffLineMetaTask> queryList(@RequestParam(value = "page", defaultValue = "1") Integer page
            , @RequestParam(value = "limit", defaultValue = "10") Integer limit, String name, String type) {
        logger.info("request queryList meta-task list. pageIndex:{},pageSize:{},name:{},type:{}", page, limit, name, type);
        Criterion nameCriterion = Strings.isEmpty(name) ? null : Restrictions.like("name", name);
        Criterion typeCriterion = Strings.isEmpty(type) ? null : Restrictions.eq("type", type);
        return taskService.list(OffLineMetaTask.class, page, limit, nameCriterion, typeCriterion);
    }

    @RequestMapping("list")
    @ResponseBody
    public PageResult<OffLineMetaTask> list(@RequestParam(value = "page", defaultValue = "1") Integer page
            , @RequestParam(value = "limit", defaultValue = "10") Integer limit) {
        logger.info("request task list. pageIndex:{},pageSize:{}", page, limit);
        return taskService.list(OffLineMetaTask.class, page, limit);
    }

    @RequestMapping("spider/add")
    @ResponseBody
    public JsonResult add(SpiderTask spiderTask) {
        logger.info("request add spider:{}", spiderTask.toString());
        OffLineMetaTask offLineMetaTask = metaTaskService.verifySpiderTask(null, spiderTask);
        if (offLineMetaTask != null) {
            taskService.addEntity(offLineMetaTask);
            return JsonResult.ok("添加成功");
        } else
            return JsonResult.error("添加失败：数据解析错误");
    }

    @RequestMapping("script/add")
    @ResponseBody
    public JsonResult addScript(ScriptTask scriptTask) {
        logger.info("request add script:{}", scriptTask.toString());
        OffLineMetaTask offLineMetaTask = metaTaskService.verifyScriptTask(null, scriptTask);
        if (offLineMetaTask != null) {
            taskService.addEntity(offLineMetaTask);
            return JsonResult.ok("添加成功");
        } else
            return JsonResult.error("添加失败：数据解析错误");
    }

    @RequestMapping("hive/add")
    @ResponseBody
    public JsonResult addHive(HiveTask hiveTask) {
        logger.info("request add hive:{}", hiveTask.toString());
        OffLineMetaTask offLineMetaTask = metaTaskService.verifyHiveTask(null, hiveTask);
        if (offLineMetaTask != null) {
            taskService.addEntity(offLineMetaTask);
            return JsonResult.ok("添加成功");
        } else
            return JsonResult.error("添加失败：数据解析错误");
    }

    @RequestMapping("spider/edit")
    @ResponseBody
    public JsonResult edit(SpiderTask spiderTask) {
        OffLineMetaTask offLineMetaTask = taskService.getMetaTaskByName(spiderTask.getName(), MercuryConstants.TYPE_OFFLINE_SPIDER);
        if (offLineMetaTask != null) {
            offLineMetaTask = metaTaskService.verifySpiderTask(offLineMetaTask, spiderTask);
            taskService.update(offLineMetaTask);
            return JsonResult.ok("修改成功");
        } else
            return JsonResult.error("修改失败：数据解析错误");
    }

    @RequestMapping("script/edit")
    @ResponseBody
    public JsonResult editScript(ScriptTask scriptTask) {
        OffLineMetaTask offLineMetaTask = taskService.getMetaTaskByName(scriptTask.getName(), MercuryConstants.TYPE_OFFLINE_SCRIPT);
        if (offLineMetaTask != null) {
            offLineMetaTask = metaTaskService.verifyScriptTask(offLineMetaTask, scriptTask);
            taskService.update(offLineMetaTask);
            return JsonResult.ok("修改成功");
        } else
            return JsonResult.error("修改失败：找不到" + scriptTask.getName());
    }

    @RequestMapping("hive/edit")
    @ResponseBody
    public JsonResult editHive(HiveTask hiveTask) {
        OffLineMetaTask offLineMetaTask = taskService.getMetaTaskByName(hiveTask.getName(), MercuryConstants.TYPE_OFFLINE_HIVE);
        if (offLineMetaTask != null) {
            offLineMetaTask = metaTaskService.verifyHiveTask(offLineMetaTask, hiveTask);
            taskService.update(offLineMetaTask);
            return JsonResult.ok("修改成功");
        } else
            return JsonResult.error("修改失败：找不到" + hiveTask.getName());
    }

    @RequestMapping("hive/verifyParameter")
    @ResponseBody
    public JsonResult verifyParameter(@RequestParam(value = "value") String value) {
        logger.info("request verifyHiveParameter:{}", value);
        String json = metaTaskService.verifyHiveParameter(value);
        if (json != null)
            return JsonResult.ok(json);
        else
            return JsonResult.error("数据错误");
    }

    @RequestMapping("delete")
    @ResponseBody
    public JsonResult delete(Long id) {
        List<DAG> dagList = taskService.listEntity(DAG.class, Restrictions.eq("nodeId", id));
        if (dagList.size() != 0)
            return JsonResult.error("删除失败:请先删除包含此任务的任务流");
        if (taskService.deleteById(OffLineMetaTask.class, id))
            return JsonResult.ok("删除成功");
        else
            return JsonResult.error("删除失败");
    }
}
