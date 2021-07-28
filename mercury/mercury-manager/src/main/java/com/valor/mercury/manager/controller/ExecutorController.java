package com.valor.mercury.manager.controller;

import com.valor.mercury.manager.config.MercuryConstants;
import com.valor.mercury.manager.model.ddo.OffLineMetaTask;
import com.valor.mercury.manager.model.ddo.RealTimeTask;
import com.valor.mercury.manager.model.system.ExecutorCommand;
import com.valor.mercury.manager.model.system.ExecutorReport;
import com.valor.mercury.manager.model.system.JsonResult;
import com.valor.mercury.manager.model.system.PageResult;
import com.valor.mercury.manager.model.ddo.Executor;
import com.valor.mercury.manager.service.*;
import com.valor.mercury.manager.tool.HttpTool;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.logging.log4j.util.Strings;
import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.Restrictions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static com.valor.mercury.manager.config.MercuryConstants.EXECUTOR_STATUS_INIT;

/**
 * @Author: Gavin
 * @Date: 2020/2/24 12:44
 */
@Controller
@RequestMapping("/executor")
public class ExecutorController {

    private Logger logger = LoggerFactory.getLogger(this.getClass());
    private final ExecutorService executorService;
    private final OffLineTaskService offLineTaskService;
    private final ETLTaskService etlTaskService;
    private final RealTimeTaskService realTimeTaskService;

    @Autowired
    public ExecutorController(ExecutorService executorService, OffLineTaskService offLineTaskService, ETLTaskService etlTaskService, RealTimeTaskService realTimeTaskService) {
        this.executorService = executorService;
        this.offLineTaskService = offLineTaskService;
        this.etlTaskService = etlTaskService;
        this.realTimeTaskService = realTimeTaskService;
    }

    @RequestMapping()
    public String page() {
        return "main/executor.html";
    }

    @RequestMapping("editForm")
    public String editFormPage() {
        return "main/executor_form.html";
    }

    @RequestMapping("list")
    @ResponseBody
    public PageResult<Executor> list(@RequestParam(value = "page", defaultValue = "1") Integer page
            , @RequestParam(value = "limit", defaultValue = "10") Integer limit) {
        return executorService.list(Executor.class, page, limit);
    }

    @RequestMapping("queryList")
    @ResponseBody
    public PageResult<Executor> queryList(@RequestParam(value = "page", defaultValue = "1") Integer page
            , @RequestParam(value = "limit", defaultValue = "10") Integer limit, String clientName, String clientIP) {
        logger.info("ExecutorController queryList. name:{},ip:{}", clientName, clientIP);
        Criterion clientNameCriterion = Strings.isEmpty(clientName) ? null : Restrictions.like("clientName", clientName);
        Criterion clientIPCriterion = Strings.isEmpty(clientIP) ? null : Restrictions.like("clientIP", clientIP);
        return executorService.list(Executor.class, page, limit, clientNameCriterion, clientIPCriterion);
    }

    @RequestMapping("add")
    @ResponseBody
    public JsonResult add(Executor executor) {
        logger.info("request add executor, executor name:{},ip:{}", executor.getClientName(), executor.getClientIP());
        if (executorService.getClientByName(executor.getClientName()) == null) {
            executor.setCreateTime(new Date());
            executor.setStatus(EXECUTOR_STATUS_INIT);
            executor.setLastActionTime(new Date());
            executorService.addEntity(executor);
            logger.info("new executor request to add:{}", executor.toString());
            return JsonResult.ok();
        } else
            return JsonResult.error("executor Name conflict");
    }

    @RequestMapping("edit")
    @ResponseBody
    public JsonResult edit(Executor executor) {
        logger.info("executor request to edit:{}", executor.toString());
        Executor cExecutor = executorService.getClientByName(executor.getClientName());
        if (cExecutor != null && cExecutor.getExecutorType().equals(executor.getExecutorType())) {
            executor.setCreateTime(cExecutor.getCreateTime());
            executor.setStatus(cExecutor.getStatus());
            executorService.update(executor);
            return JsonResult.ok();
        } else
            return JsonResult.error("不支持修改客户端名字和类型");
    }

    @RequestMapping("delete")
    @ResponseBody
    public JsonResult delete(@RequestParam Long id) {
        logger.info("request Executor delete. Executor:{}", id);
        Executor executor = executorService.getEntityById(Executor.class, id);
        if (executor.getExecutorType().equals(MercuryConstants.TYPE_OFFLINE_SPIDER) ||
                executor.getExecutorType().equals(MercuryConstants.TYPE_OFFLINE_HIVE) ||
                executor.getExecutorType().equals(MercuryConstants.TYPE_OFFLINE_SCRIPT)) {
            List<OffLineMetaTask> metaTasks = executorService.listEntity(OffLineMetaTask.class, Restrictions.eq("executor", executor.getClientName()));
            if (metaTasks.size() > 0)
                return JsonResult.error("删除失败：请先修改此客户端关联的离线任务");
        } else if (executor.getExecutorType().equals(MercuryConstants.TYPE_REALTIME_FLINK)) {
            List<RealTimeTask> metaTasks = executorService.listEntity(RealTimeTask.class, Restrictions.eq("executor", executor.getClientName()));
            if (metaTasks.size() > 0)
                return JsonResult.error("删除失败：请先修改此客户端关联的实时任务");
        }
        if (executorService.deleteById(Executor.class, id))
            return JsonResult.ok("删除成功");
        else
            return JsonResult.error("删除失败：数据库错误");
    }

    @RequestMapping("getTask")
    @ResponseBody
    public JsonResult getTask(@RequestParam String clientName, @RequestParam String clientPsd, @RequestParam String status, HttpServletRequest request) {
        String clientIP = HttpTool.getIpAddress(request);
        logger.info("request getTask, executor name:{},ip:{},status:{}", clientName, clientIP, status);
        Pair<Boolean, String> verifyResult = executorService.verifyExecutor(clientName, clientPsd, status, clientIP);
        if (verifyResult.getKey()) {
            List<ExecutorCommand> commands;
            switch (verifyResult.getValue()) {
                case MercuryConstants.TYPE_ETL_RECEIVER:
                    commands = etlTaskService.getReceiveCommandByExecutor();
                    break;
                case MercuryConstants.TYPE_ETL_DISPATCHER:
                    commands = etlTaskService.getDispatchCommandByExecutor();
                    break;
                case MercuryConstants.TYPE_REALTIME_FLINK:
                    commands = realTimeTaskService.getRealTimeTaskCommandByExecutor(clientName);
                    break;
                default:
                    commands = offLineTaskService.getOffLineTaskCommandByExecutor(clientName);
                    break;
            }
            if (commands == null)
                commands = new ArrayList<>();
            JsonResult result = JsonResult.ok();
            result.put("commands", commands);
            logger.info("getTask response:{}", commands.toArray());
            return result;
        } else
            return JsonResult.error(verifyResult.getValue());

    }

    @RequestMapping("taskReport")
    @ResponseBody
    public JsonResult taskReport(@RequestBody ExecutorReport report, HttpServletRequest request) {
        String clientIP = HttpTool.getIpAddress(request);
        logger.info("request taskReport, report info:{},ip:{}", report.toString(), clientIP);
        Pair<Boolean, String> verifyResult = executorService.verifyExecutor(report.getClientName(), report.getClientPsd(), null, clientIP);
        if (verifyResult.getKey()) {
            boolean result;
            switch (verifyResult.getValue()) {
                case MercuryConstants.TYPE_ETL_RECEIVER:
                    result = etlTaskService.updateReceiveTaskStatus(report);
                    break;
                case MercuryConstants.TYPE_ETL_DISPATCHER:
                    result = etlTaskService.updateDispatchTaskStatus(report);
                    break;
                case MercuryConstants.TYPE_REALTIME_FLINK:
                    result = realTimeTaskService.updateRealTimeTaskStatus(report);
                    break;
                default:
                    result = offLineTaskService.updateOffLineTaskStatus(report);
                    break;
            }
            if (result)
                return JsonResult.ok();
            else
                return JsonResult.error("taskResponse error");
        }
        return JsonResult.error(verifyResult.getValue());
    }
}
