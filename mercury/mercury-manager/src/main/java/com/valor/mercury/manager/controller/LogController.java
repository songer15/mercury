package com.valor.mercury.manager.controller;

import com.valor.mercury.manager.model.system.JsonResult;
import com.valor.mercury.manager.model.system.PageResult;
import com.valor.mercury.manager.model.ddo.MonitorLog;
import com.valor.mercury.manager.service.LogAlarmService;
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

/**
 * @author Gavin
 * 2020/8/6 9:38
 */
@Controller
@RequestMapping("/log")
public class LogController {
    private Logger logger = LoggerFactory.getLogger(this.getClass());
    private final LogAlarmService logService;

    @Autowired
    public LogController(LogAlarmService logService) {
        this.logService = logService;
    }

    @RequestMapping()
    public String page() {
        return "main/log.html";
    }

    @RequestMapping("view")
    public String viewPage() {
        return "main/log_form.html";
    }

    @RequestMapping("list")
    @ResponseBody
    public PageResult<MonitorLog> list(@RequestParam(value = "page", defaultValue = "1") Integer page
            , @RequestParam(value = "limit", defaultValue = "10") Integer limit) {
        return logService.list(MonitorLog.class, page, limit, "createTime");
    }

    @RequestMapping("listTop")
    @ResponseBody
    public PageResult<MonitorLog> listTop() {
        return logService.list(MonitorLog.class, 1, 6, "createTime", Restrictions.eq("level", 3));
    }

    @RequestMapping("queryList")
    @ResponseBody
    public PageResult<MonitorLog> queryList(@RequestParam(value = "page", defaultValue = "1") Integer page
            , @RequestParam(value = "limit", defaultValue = "10") Integer limit, String source, String level) {
        logger.info("LogController queryList. source:{},level:{}", source, level);
        Criterion sourceCriterion = Strings.isEmpty(source) ? null : Restrictions.like("source", source);
        Criterion levelCriterion = Strings.isEmpty(level) ? null : Restrictions.eq("level", Integer.parseInt(level));
        return logService.list(MonitorLog.class, page, limit, "createTime", sourceCriterion, levelCriterion);
    }

    @RequestMapping("delete")
    @ResponseBody
    public JsonResult delete(@RequestParam Long id) {
        logger.info("request MonitorLog delete. MonitorLog:{}", id);
        if (logService.deleteById(MonitorLog.class, id))
            return JsonResult.ok("删除成功");
        else
            return JsonResult.error("删除失败：数据库错误");
    }
}
