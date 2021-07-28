package com.valor.mercury.manager.controller;

import com.valor.mercury.manager.config.MercuryConstants;
import com.valor.mercury.manager.model.system.JsonResult;
import com.valor.mercury.manager.model.system.PageResult;
import com.valor.mercury.manager.model.ddo.Executor;
import com.valor.mercury.manager.model.ddo.RealTimeTask;
import com.valor.mercury.manager.model.ddo.RealTimeTaskInstance;
import com.valor.mercury.manager.model.ddo.UploadFileConfig;
import com.valor.mercury.manager.service.RealTimeTaskService;
import org.apache.commons.lang3.tuple.Pair;
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

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * @Author: Gavin
 * @Date: 2020/8/24 15:28
 */
@Controller
@RequestMapping("/realTimeTask")
public class RealTimeTaskController {

    private Logger logger = LoggerFactory.getLogger(this.getClass());
    private final RealTimeTaskService taskService;

    @Autowired
    public RealTimeTaskController(RealTimeTaskService taskService) {
        this.taskService = taskService;
    }

    @RequestMapping
    public String page() {
        return "main/realtime-task.html";
    }

    @RequestMapping("editForm")
    public String editFormPage(Model model) {
        List<Executor> taskExecutors = taskService.listEntity(Executor.class,
                Restrictions.eq("executorType", MercuryConstants.TYPE_REALTIME_FLINK));
        List<String> executors = new ArrayList<>();
        taskExecutors.forEach(v -> executors.add(v.getClientName()));
        model.addAttribute("executors", executors);
        List<UploadFileConfig> files = taskService.listEntity(UploadFileConfig.class, Restrictions.eq("targetType", "Flink"));
        model.addAttribute("files", files);
        return "main/realtime-task_form.html";
    }

    @RequestMapping("list")
    @ResponseBody
    public PageResult<RealTimeTask> list(@RequestParam(value = "page", defaultValue = "1") Integer page
            , @RequestParam(value = "limit", defaultValue = "10") Integer limit) {
        logger.info("request real-time task list. pageIndex:{},pageSize:{}", page, limit);
        return taskService.list(RealTimeTask.class, page, limit);
    }

    @RequestMapping("listInstance")
    @ResponseBody
    public PageResult<RealTimeTaskInstance> listInstance(@RequestParam(value = "page", defaultValue = "1") Integer page
            , @RequestParam(value = "limit", defaultValue = "10") Integer limit) {
        logger.info("request real-time task list. pageIndex:{},pageSize:{}", page, limit);
        return taskService.list(RealTimeTaskInstance.class, page, limit);
    }

    @RequestMapping("queryListInstance")
    @ResponseBody
    public PageResult<RealTimeTaskInstance> queryListInstance(@RequestParam(value = "page", defaultValue = "1") Integer page
            , @RequestParam(value = "limit", defaultValue = "10") Integer limit, String name) {
        Criterion criterion = Restrictions.eq("name", name);
        logger.info("request real-time task list. pageIndex:{},pageSize:{}", page, limit);
        return taskService.list(RealTimeTaskInstance.class, page, limit, criterion);
    }

    @RequestMapping("queryList")
    @ResponseBody
    public PageResult<RealTimeTask> queryList(@RequestParam(value = "page", defaultValue = "1") Integer page
            , @RequestParam(value = "limit", defaultValue = "10") Integer limit, String name) {
        Criterion criterion = Restrictions.eq("name", name);
        logger.info("request real-time task list. pageIndex:{},pageSize:{}", page, limit);
        return taskService.list(RealTimeTask.class, page, limit, criterion);
    }

    @RequestMapping("add")
    @ResponseBody
    public JsonResult add(RealTimeTask task) {
        logger.info("request RealTimeTask add. task:{}", task.toString());
        task.setCreateTime(new Date());
        task.setLastModifyTime(new Date());
        task.setStatus(MercuryConstants.REALTIME_TASK_STATUS_INIT);
        if (taskService.addEntity(task))
            return JsonResult.ok("添加成功");
        else
            return JsonResult.error("添加失败");
    }

    //做好任务的校验工作
    @RequestMapping("publish")
    @ResponseBody
    public JsonResult publish(@RequestParam(name = "id") Long taskId) {
        logger.info("request publish realtime task. task id:{}", taskId);
        Pair<Boolean, String> result = taskService.publishTask(taskId);
        if (result.getKey()) {
            logger.info("发布任务成功：{}", taskId);
            return JsonResult.ok("发布成功");
        } else {
            logger.error("发布任务失败：{}", result.getValue());
            return JsonResult.error(result.getValue());
        }
    }

    //做好任务的校验工作
    @RequestMapping("stop")
    @ResponseBody
    public JsonResult stop(@RequestParam(name = "id") Long taskId) {
        logger.info("request stop realtime task. task id:{}", taskId);
        Pair<Boolean, String> result = taskService.stopTask(taskId);
        if (result.getKey()) {
            logger.info("停止任务成功：{}", taskId);
            return JsonResult.ok("停止成功");
        } else {
            logger.error("停止任务失败：{}", result.getValue());
            return JsonResult.error(result.getValue());
        }
    }

    @RequestMapping("edit")
    @ResponseBody
    public JsonResult edit(RealTimeTask task) {
        logger.info("request edit realtime task. task name:{},value:{}", task.getName(),task.toString());
        Pair<Boolean, String> result = taskService.editTask(task);
        if (result.getKey())
            return JsonResult.ok("修改成功");
        else
            return JsonResult.error(result.getValue());
    }

    @RequestMapping("delete")
    @ResponseBody
    public JsonResult delete(@RequestParam(name = "id") Long id) {
        logger.info("request delete:{}", id);
        RealTimeTask task = taskService.getEntityById(RealTimeTask.class, id);
        if (task == null || task.getStatus().equals(MercuryConstants.REALTIME_TASK_STATUS_PUBLISHING) ||
                task.getStatus().equals(MercuryConstants.REALTIME_TASK_STATUS_RUNNING) ||
                task.getStatus().equals(MercuryConstants.REALTIME_TASK_STATUS_CANCELLING) ||
                task.getStatus().equals(MercuryConstants.REALTIME_TASK_STATUS_CANCELED)) {
            return JsonResult.error("任务不在空闲状态，请先停止任务");
        }
        if (taskService.deleteEntity(task))
            return JsonResult.ok();
        else
            return JsonResult.error("删除失败");
    }
}
