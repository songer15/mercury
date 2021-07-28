package com.valor.mercury.manager.controller;

import com.valor.mercury.manager.config.MercuryConstants;
import com.valor.mercury.manager.model.ddo.*;
import com.valor.mercury.manager.model.system.*;
import com.valor.mercury.manager.service.ETLTaskService;
import com.valor.mercury.manager.service.HAService;
import com.valor.mercury.manager.service.MonitorService;
import com.valor.mercury.manager.tool.DateUtils;
import org.hibernate.criterion.Restrictions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.servlet.http.HttpServletRequest;
import java.util.*;

@Controller()
@RequestMapping("/home")
public class PageController {

    private Calendar calendar = Calendar.getInstance();
    private final ETLTaskService service;
    private final HAService haService;
    private final MonitorService monitorService;

    @Autowired
    public PageController(ETLTaskService service, HAService haService, MonitorService monitorService) {
        this.service = service;
        this.haService = haService;
        this.monitorService = monitorService;
    }

    /**
     * 控制台
     */
    @RequestMapping("/console")
    public String console(Model model) {
        model.addAttribute("nodeStatus", haService.getNodeStatus().toString());
        model.addAttribute("nodeInfo", haService.getManagerNodes());
        model.addAttribute("nodeNumber", haService.getManagerNodes().size());

        calendar.setTime(new Date(System.currentTimeMillis() + 24 * 3600_000));
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        Integer runningBatch = calendar.get(Calendar.YEAR) * 10000 + (calendar.get(Calendar.MONTH) + 1) * 100 + calendar.get(Calendar.DAY_OF_MONTH) - 1;
        int totalOffLineTasks = service.listEntity(OffLineTask.class, Restrictions.eq("enable", "on")).size();
        List<OffLineTaskInstance> instances = service.listEntity(OffLineTaskInstance.class,
                Restrictions.eq("runningBatch", runningBatch));
        model.addAttribute("totalOffLineTasks", totalOffLineTasks);
        model.addAttribute("totalOffLineTaskInstances", instances.size());
        int finishOffLineTaskInstances = 0;
        int runningOffLineTaskInstances = 0;
        int initOffLineTaskInstances = 0;
        int cancelOffLineTaskInstances = 0;
        int failOffLineTaskInstances = 0;
        for (OffLineTaskInstance instance : instances) {
            if (instance.getResult().equals("RUNNING"))
                runningOffLineTaskInstances++;
            else if (instance.getResult().equals("INIT"))
                initOffLineTaskInstances++;
            else if (instance.getResult().equals("FAIL"))
                failOffLineTaskInstances++;
            else if (instance.getResult().equals("SUCCESS"))
                finishOffLineTaskInstances++;
            else
                cancelOffLineTaskInstances++;
        }
        model.addAttribute("finishOffLineTaskInstances", finishOffLineTaskInstances);
        model.addAttribute("runningOffLineTaskInstances", runningOffLineTaskInstances);
        model.addAttribute("initOffLineTaskInstances", initOffLineTaskInstances);
        model.addAttribute("cancelOffLineTaskInstances", cancelOffLineTaskInstances);
        model.addAttribute("failOffLineTaskInstances", failOffLineTaskInstances);

        List<RealTimeTask> realTimeTasks = service.listEntity(RealTimeTask.class);
        model.addAttribute("totalRealTimeTasks", realTimeTasks.size());
        int runningRealTimeTaskInstances = 0;
        int finishRealTimeTaskInstances = 0;
        int initRealTimeTaskInstances = 0;
        for (RealTimeTask realTimeTask : realTimeTasks) {
            if (realTimeTask.getStatus().equals("INIT") || realTimeTask.getStatus().equals("PUBLISHING"))
                initRealTimeTaskInstances++;
            else if (realTimeTask.getStatus().equals("FINISH"))
                finishRealTimeTaskInstances++;
            else
                runningRealTimeTaskInstances++;
        }
        model.addAttribute("runningRealTimeTaskInstances", runningRealTimeTaskInstances);
        model.addAttribute("finishRealTimeTaskInstances", finishRealTimeTaskInstances);
        model.addAttribute("initRealTimeTaskInstances", initRealTimeTaskInstances);

        List<ETLDataTraffic> last24Hour = service.listEntity(ETLDataTraffic.class,
                Restrictions.between("createTime", new Date(System.currentTimeMillis() - 24 * 3600_000), new Date()));
        Map<String, Integer> receiveMap = new HashMap<>();
        Map<String, Integer> dispatchMap = new HashMap<>();
        int receiveNum = 0;
        int dispatchNum = 0;
        for (ETLDataTraffic dataTraffic : last24Hour) {
            String time = DateUtils.getDateStr(dataTraffic.getCreateTime());
            if (dataTraffic.getType().equals("Receive")) {
                Integer traffic = receiveMap.get(time);
                receiveNum = receiveNum + dataTraffic.getNum();
                if (traffic == null)
                    receiveMap.put(time, dataTraffic.getNum());
                else
                    receiveMap.put(time, traffic + dataTraffic.getNum());
            } else {
                Integer traffic = dispatchMap.get(time);
                dispatchNum = dispatchNum + dataTraffic.getNum();
                if (traffic == null)
                    dispatchMap.put(time, dataTraffic.getNum());
                else
                    dispatchMap.put(time, traffic + dataTraffic.getNum());
            }
        }
        List<String> xSerial = new ArrayList<>();
        List<Integer> y1Serial = new ArrayList<>();
        List<Integer> y2Serial = new ArrayList<>();
        receiveMap.keySet().stream().sorted(Comparator.comparing(String::toString)).forEach(v -> {
            y1Serial.add(receiveMap.get(v));
            y2Serial.add(dispatchMap.get(v));
            xSerial.add(v);
        });
        model.addAttribute("xSerial", xSerial);
        model.addAttribute("y1Serial", y1Serial);
        model.addAttribute("y2Serial", y2Serial);
        model.addAttribute("receiveNum", receiveNum);
        model.addAttribute("sendNum", dispatchNum);

        List<Executor> executors = service.listEntity(Executor.class, Restrictions.eq("enable", "on"));
        int workingExecutors = 0;
        int waitingExecutors = 0;
        int disconnectedExecutors = 0;
        int initExecutors = 0;
        for (Executor executor : executors) {
            switch (executor.getStatus()) {
                case MercuryConstants.EXECUTOR_STATUS_INIT:
                    initExecutors++;
                    break;
                case MercuryConstants.EXECUTOR_STATUS_DISCONNECTED:
                    disconnectedExecutors++;
                    break;
                case MercuryConstants.EXECUTOR_STATUS_WAITING:
                    waitingExecutors++;
                    break;
                case MercuryConstants.EXECUTOR_STATUS_WORKING:
                    workingExecutors++;
                    break;
            }
        }
        model.addAttribute("workingExecutors", workingExecutors);
        model.addAttribute("waitingExecutors", waitingExecutors);
        model.addAttribute("disconnectedExecutors", disconnectedExecutors);
        model.addAttribute("initExecutors", initExecutors);

        Map<String, MonitorMetric> metricMap = monitorService.getMetricMap();
        metricMap.forEach((k, v) -> {
            switch (k) {
                case "hdfs":
                    HDFSMetric hdfsMetric = (HDFSMetric) v;
                    model.addAttribute("hdfsLiveNodes", hdfsMetric.getLiveNodes());
                    model.addAttribute("hdfsTotalBlocks", hdfsMetric.getTotalBlocks());
                    model.addAttribute("hdfsUsed", hdfsMetric.getUsed());
                    model.addAttribute("hdfsFree", hdfsMetric.getFree());
                    model.addAttribute("hdfsThreads", hdfsMetric.getThreads());
                    model.addAttribute("hdfsTotal", hdfsMetric.getTotal());
                    model.addAttribute("hdfsMissingBlocks", hdfsMetric.getMissingBlocks());
                    break;
                case "flink":
                    FlinkMetric flinkMetric = (FlinkMetric) v;
                    model.addAttribute("flinkRunningJobs", flinkMetric.getRunningJobs());
                    model.addAttribute("flinkTaskSlotsAvailable", flinkMetric.getTaskSlotsAvailable());
                    model.addAttribute("flinkNodeNumber", flinkMetric.getNodeNumber());
                    model.addAttribute("flinkMemoryUsed", flinkMetric.getMemoryUsed());
                    model.addAttribute("flinkTaskSlotsTotal", flinkMetric.getTaskSlotsTotal());
                    model.addAttribute("flinkMemoryMax", flinkMetric.getMemoryMax());
                    break;
                case "yarn":
                    YarnMetric yarnMetric = (YarnMetric) v;
                    model.addAttribute("yarnRunningJobs", yarnMetric.getRunningJobs());
                    model.addAttribute("yarnAppsSubmitted", yarnMetric.getAppsSubmitted());
                    model.addAttribute("yarnNodeNumber", yarnMetric.getNodeNumber());
                    model.addAttribute("yarnActiveNodes", yarnMetric.getActiveNodes());
                    model.addAttribute("yarnAvailableVirtualCores", yarnMetric.getAvailableVirtualCores());
                    model.addAttribute("yarnAvailableMB", yarnMetric.getAvailableMB());
                    model.addAttribute("yarnContainersAllocated", yarnMetric.getContainersAllocated());
                    break;
                case "hive":
                    break;
                case "influxDB":
                    break;
                case "kafka":
                    break;
                case "elasticsearch":
                    ElasticSearchMetric elasticSearchMetric = (ElasticSearchMetric) v;
                    model.addAttribute("elasticsearchNodeNumber", elasticSearchMetric.getNodeNumber());
                    break;
            }
            model.addAttribute(k + "Status", v.getStatus());
            model.addAttribute(k + "ActionTime", DateUtils.sdf.format(v.getActionTime()));
            model.addAttribute(k + "Enable", v.getEnable());
        });
        return "home/console.html";
    }

    /**
     * 消息弹窗
     */
    @RequestMapping("/message")
    public String message() {
        return "tpl/message.html";
    }

    /**
     * 修改密码弹窗
     */
    @RequestMapping("/password")
    public String password() {
        return "tpl/password.html";
    }

    /**
     * 主题设置弹窗
     */
    @RequestMapping("/theme")
    public String theme() {
        return "tpl/theme.html";
    }

    /**
     * 设置主题
     */
    @RequestMapping("/setTheme")
    public String setTheme(String themeName, HttpServletRequest request) {
        if (null == themeName) {
            request.getSession().removeAttribute("theme");
        } else {
            request.getSession().setAttribute("theme", themeName);
        }
        return "redirect:/";
    }
}
