package com.valor.mercury.manager.controller;

import com.valor.mercury.manager.model.ddo.HiveTable;
import com.valor.mercury.manager.model.system.JsonResult;
import com.valor.mercury.manager.model.system.PageResult;
import com.valor.mercury.manager.service.HiveService;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.logging.log4j.util.Strings;
import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.Restrictions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * @author Gavin
 * 2020/10/22 13:49
 */
@Controller
@RequestMapping("/hive")
public class HiveController {

    private Logger logger = LoggerFactory.getLogger(this.getClass());
    private final HiveService hiveService;

    public HiveController(HiveService hiveService) {
        this.hiveService = hiveService;
    }

    @RequestMapping()
    public String page(@RequestParam(value = "database") String database, Model model) {
        model.addAttribute("database", database);
        return "main/hive.html";
    }

    @RequestMapping("addForm")
    public String addForm(@RequestParam(value = "database") String database, Model model) {
        model.addAttribute("database", database);
        return "main/hive_add_form.html";
    }

    @RequestMapping("editForm")
    public String editForm() {
        return "main/hive_edit_form.html";
    }

    @RequestMapping("list")
    @ResponseBody
    public PageResult<HiveTable> list(@RequestParam(value = "database") String database,
                                      @RequestParam(value = "page", defaultValue = "1") Integer page, @RequestParam(value = "limit", defaultValue = "10") Integer limit) {
        logger.info("HiveController list. database:{}", database);
        return hiveService.listTables(page, limit, database);
    }

    @RequestMapping("queryList")
    @ResponseBody
    public PageResult<HiveTable> queryList(@RequestParam(value = "page", defaultValue = "1") Integer page
            , @RequestParam(value = "limit", defaultValue = "10") Integer limit, String database, String name) {
        logger.info("HiveController queryList. name:{},database:{}", name, database);
        Criterion nameCriterion = Strings.isEmpty(name) ? null : Restrictions.like("name", name);
        Criterion databaseCriterion = Strings.isEmpty(database) ? null : Restrictions.eq("database", database);
        return hiveService.list(HiveTable.class, page, limit, nameCriterion, databaseCriterion);
    }

    @RequestMapping("refresh")
    @ResponseBody
    public JsonResult refresh(@RequestParam(value = "database") String database) {
        logger.info("HiveController refresh. database:{}", database);
        ImmutablePair<Boolean, String> result = hiveService.refreshHiveTables(database, 0);
        if (result.left)
            return JsonResult.ok();
        else
            return JsonResult.error(result.right);
    }

    @RequestMapping("add")
    @ResponseBody
    public JsonResult add(@RequestParam(value = "database") String database, @RequestParam(value = "command") String command) {
        logger.info("request add hive table, command:{}", command);
        ImmutablePair<Boolean, String> result = hiveService.addTable(database, command);
        if (result.left)
            return JsonResult.ok();
        else
            return JsonResult.error(result.right);
    }

    @RequestMapping("delete")
    @ResponseBody
    public JsonResult delete(@RequestParam(value = "database") String database, @RequestParam(value = "id") Long id) {
        logger.info("request HiveTable delete. id:{}", id);
        ImmutablePair<Boolean, String> result = hiveService.dropTable(database, id);
        if (result.left)
            return JsonResult.ok();
        else
            return JsonResult.error(result.right);
    }

    @RequestMapping("edit")
    @ResponseBody
    public JsonResult editCron(@RequestParam Long id, String cron, String command) {
        logger.info("request edit Hive table. cron:{},command:{}", cron, command);
        ImmutablePair<Boolean, String> result = hiveService.editHiveTable(id, cron, command);
        if (result.left)
            return JsonResult.ok();
        else
            return JsonResult.error(result.right);
    }
}
