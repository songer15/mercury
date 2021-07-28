package com.valor.mercury.manager.controller;

import com.valor.mercury.manager.model.system.JsonResult;
import com.valor.mercury.manager.model.system.dispatcherMappingConfig.ElasticSearchConfig;
import com.valor.mercury.manager.model.system.dispatcherMappingConfig.HDFSConfig;
import com.valor.mercury.manager.model.system.dispatcherMappingConfig.InfluxDBConfig;
import com.valor.mercury.manager.model.system.dispatcherMappingConfig.KafkaConfig;
import com.valor.mercury.manager.model.system.PageResult;
import com.valor.mercury.manager.model.ddo.ETLDataDispatch;
import com.valor.mercury.manager.model.ddo.ETLDataReceive;
import com.valor.mercury.manager.service.ETLTaskService;
import com.valor.mercury.manager.service.OffLineTaskService;
import com.valor.mercury.manager.tool.JsonUtil;
import org.apache.commons.lang3.tuple.Pair;
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

import java.util.Date;
import java.util.List;

/**
 * @author Gavin
 * 2020/7/28 13:26
 */
@Controller
@RequestMapping("/ETLTask")
public class ETLTaskController {


    private Logger logger = LoggerFactory.getLogger(this.getClass());
    private final OffLineTaskService taskService;
    private final ETLTaskService etlTaskService;

    @Autowired
    public ETLTaskController(OffLineTaskService taskService, ETLTaskService etlTaskService) {
        this.taskService = taskService;
        this.etlTaskService = etlTaskService;
    }

    @RequestMapping("dispatch")
    public String dispatchPage() {
        return "main/etl-dispatch.html";
    }

    @RequestMapping("receive")
    public String receivePage() {
        return "main/etl-receive.html";
    }

    @RequestMapping("dispatch/editForm")
    public String dispatchEditForm() {
        return "main/etl-dispatch_form.html";
    }

    @RequestMapping("dispatch/editMapping")
    public String dispatchEditMapping() {
        return "main/etl-dispatch_mapping_form.html";
    }

    @RequestMapping("receive/editForm")
    public String receiveEditForm() {
        return "main/etl-receive_form.html";
    }

    @RequestMapping("dispatch/editConfigForm")
    public String dispatchEditConfigForm(Model model, String consumerType, Long id) {
        try {
            ETLDataDispatch dispatch = etlTaskService.getEntityById(ETLDataDispatch.class, id);
            if (Strings.isNotEmpty(dispatch.getConsumerConfig())) {
                if (dispatch.getConsumerType().equals("ElasticSearch")) {
                    ElasticSearchConfig config = JsonUtil.objectMapper.readValue(dispatch.getConsumerConfig(), ElasticSearchConfig.class);
                    config.setIndexMapping(config.getIndexMapping().replace("\"", "\\\"").replace("\n", ""));
                    model.addAttribute("config", JsonUtil.objectMapper.writeValueAsString(config));
                } else
                    model.addAttribute("config", dispatch.getConsumerConfig());
            } else
                model.addAttribute("config", "null");
            model.addAttribute("id", id);
            switch (consumerType) {
                case "HDFS":
                    return "main/etl-dispatch_config_form_hdfs.html";
                case "ElasticSearch":
                    return "main/etl-dispatch_config_form_elastic.html";
                case "Kafka":
                    return "main/etl-dispatch_config_form_kafka.html";
                default:
                    return "main/etl-dispatch_config_form_influx.html";
            }
        } catch (Exception e) {
            logger.error("dispatchEditConfigForm error:{}", e);
            return null;
        }
    }

    @RequestMapping("receive/add")
    @ResponseBody
    public JsonResult addReceiveConfig(ETLDataReceive etlDataReceive) {
        logger.info("request addReceiveConfig:{}", etlDataReceive.toString());
        if (etlDataReceive.getReceiveName().contains(" "))
            return JsonResult.error("包含空格字符");
        Pair<Boolean, String> result = etlTaskService.addReceiveTask(etlDataReceive);
        if (result.getKey())
            return JsonResult.ok();
        else
            return JsonResult.error(result.getValue());
    }

    @RequestMapping("dispatch/verifyMapping")
    @ResponseBody
    public JsonResult verifyMapping(String value) {
        logger.info("request verifyMapping:{}", value);
        String json = etlTaskService.verifyMapping(value);
        if (json != null)
            return JsonResult.ok(json);
        else
            return JsonResult.error("数据错误");
    }

    @RequestMapping("dispatch/editInfluxConfig")
    @ResponseBody
    public JsonResult editInfluxConfig(@RequestParam(name = "id") Long id, @RequestParam(name = "database") String database, @RequestParam String measurement
            , @RequestParam String tags, @RequestParam String retentionPolicy) {
        InfluxDBConfig config = new InfluxDBConfig(database, measurement, tags, retentionPolicy);
        logger.info("request editInfluxConfig:{}", config.toString());
        try {
            ETLDataDispatch dispatch = etlTaskService.getEntityById(ETLDataDispatch.class, id);
            dispatch.setConsumerConfig(JsonUtil.objectMapper.writeValueAsString(config));
            dispatch.setStatus("finished");
            if (etlTaskService.update(dispatch))
                return JsonResult.ok();
            else
                return JsonResult.error("数据错误");
        } catch (Exception e) {
            logger.error("editInfluxConfig error:{}", e);
            return JsonResult.error("数据错误");
        }
    }

    @RequestMapping("dispatch/editKafkaConfig")
    @ResponseBody
    public JsonResult editKafkaConfig(@RequestParam(name = "id") Long id, @RequestParam String topicName, @RequestParam Integer partitions,
                                      @RequestParam Integer replications, @RequestParam Long retentionMillis,
                                      @RequestParam Integer retentionBytes, @RequestParam Integer messageMaxBytes,
                                      @RequestParam String cleanupPolicy) {
        KafkaConfig config = new KafkaConfig(topicName, partitions, replications, retentionMillis, retentionBytes, messageMaxBytes, cleanupPolicy);
        logger.info("request editInfluxConfig:{}", config.toString());
        try {
            ETLDataDispatch dispatch = etlTaskService.getEntityById(ETLDataDispatch.class, id);
            dispatch.setConsumerConfig(JsonUtil.objectMapper.writeValueAsString(config));
            dispatch.setStatus("finished");
            if (etlTaskService.update(dispatch))
                return JsonResult.ok();
            else
                return JsonResult.error("数据错误");
        } catch (Exception e) {
            logger.error("editInfluxConfig error:{}", e);
            return JsonResult.error("数据错误");
        }
    }

    @RequestMapping("dispatch/editElasticConfig")
    @ResponseBody
    public JsonResult editElasticConfig(@RequestParam(name = "id") Long id, @RequestParam String indexName, @RequestParam Integer shardNum,
                                        @RequestParam Integer replicaNum, @RequestParam String indexMapping,
                                        @RequestParam String idField, @RequestParam String indexNameStrategy) {
        ElasticSearchConfig config = new ElasticSearchConfig(indexName, shardNum, replicaNum, indexMapping, idField, indexNameStrategy);
        logger.info("request editInfluxConfig:{}", config.toString());
        try {
            ETLDataDispatch dispatch = etlTaskService.getEntityById(ETLDataDispatch.class, id);
            dispatch.setConsumerConfig(JsonUtil.objectMapper.writeValueAsString(config));
            dispatch.setStatus("finished");
            if (etlTaskService.update(dispatch))
                return JsonResult.ok();
            else
                return JsonResult.error("数据错误");
        } catch (Exception e) {
            logger.error("editInfluxConfig error:{}", e);
            return JsonResult.error("数据错误");
        }
    }

    @RequestMapping("dispatch/editHDFSConfig")
    @ResponseBody
    public JsonResult editHDFSConfig(@RequestParam(name = "id") Long id, @RequestParam(name = "filePath") String filePath
            , @RequestParam(name = "namedStrategy") Integer namedStrategy, @RequestParam(name = "replacePath") String replacePath) {
        HDFSConfig config = new HDFSConfig(filePath, namedStrategy, replacePath);
        logger.info("request editInfluxConfig:{}", config.toString());
        if (replacePath.equals(filePath))
            return JsonResult.error("主路径和备用路径不能一致");
        try {
            ETLDataDispatch dispatch = etlTaskService.getEntityById(ETLDataDispatch.class, id);
            dispatch.setConsumerConfig(JsonUtil.objectMapper.writeValueAsString(config));
            dispatch.setStatus("finished");
            if (etlTaskService.update(dispatch))
                return JsonResult.ok();
            else
                return JsonResult.error("数据错误");
        } catch (Exception e) {
            logger.error("editInfluxConfig error:{}", e);
            return JsonResult.error("数据错误");
        }
    }

    @RequestMapping("dispatch/add")
    @ResponseBody
    public JsonResult addDispatchConfig(ETLDataDispatch etlDataDispatch) {
        logger.info("request addDispatchConfig:{}", etlDataDispatch.toString());
        etlDataDispatch.setStatus("unfinished");
        etlDataDispatch.setCreateTime(new Date());
        if (etlDataDispatch.getMappingApplyLevel() == 2 && Strings.isEmpty(etlDataDispatch.getMapping()))
            return JsonResult.error("缺少Mapping");
        List<ETLDataDispatch> dispatches
                = etlTaskService.listEntity(ETLDataDispatch.class, Restrictions.eq("typeName", etlDataDispatch.getTypeName()));
        for (ETLDataDispatch dispatch : dispatches) {
            if (dispatch.getConsumerType().equals(etlDataDispatch.getConsumerType()))
                return JsonResult.error("重复配置");
        }
        if (etlTaskService.addEntity(etlDataDispatch))
            return JsonResult.ok();
        else
            return JsonResult.error("数据添加错误");
    }

    @RequestMapping("dispatch/edit")
    @ResponseBody
    public JsonResult editDispatchConfig(ETLDataDispatch etlDataDispatch) {
        logger.info("request editDispatchConfig:{}", etlDataDispatch.toString());
        Pair<Boolean, String> result = etlTaskService.updateDispatchTask(etlDataDispatch);
        if (result.getKey())
            return JsonResult.ok();
        else
            return JsonResult.error(result.getValue());
    }

    @RequestMapping("receive/edit")
    @ResponseBody
    public JsonResult editReceiveConfig(ETLDataReceive etlDataReceive) {
        logger.info("request editReceiveConfig:{}", etlDataReceive.toString());
        Pair<Boolean, String> result = etlTaskService.updateReceiveTask(etlDataReceive);
        if (result.getKey())
            return JsonResult.ok();
        else
            return JsonResult.error(result.getValue());
    }

    @RequestMapping("dispatch/list")
    @ResponseBody
    public PageResult<ETLDataDispatch> listDispatchConfig(@RequestParam(value = "page", defaultValue = "1") Integer page
            , @RequestParam(value = "limit", defaultValue = "10") Integer limit) {
        return taskService.list(ETLDataDispatch.class, page, limit);
    }

    @RequestMapping("receive/list")
    @ResponseBody
    public PageResult<ETLDataReceive> listReceiveConfig(@RequestParam(value = "page", defaultValue = "1") Integer page
            , @RequestParam(value = "limit", defaultValue = "10") Integer limit) {
        return taskService.list(ETLDataReceive.class, page, limit);
    }

    @RequestMapping("dispatch/delete")
    @ResponseBody
    public JsonResult deleteDispatch(@RequestParam Long id) {
        logger.info("request deleteDispatchConfig:{}", id);
        Pair<Boolean, String> result = etlTaskService.deleteETLTask(ETLDataDispatch.class, id);
        if (result.getKey())
            return JsonResult.ok("删除成功");
        else
            return JsonResult.error(result.getValue());
    }

    @RequestMapping("receive/delete")
    @ResponseBody
    public JsonResult delete(@RequestParam Long id) {
        logger.info("request deleteReceiveConfig:{}", id);
        Pair<Boolean, String> result = etlTaskService.deleteETLTask(ETLDataReceive.class, id);
        if (result.getKey())
            return JsonResult.ok("删除成功");
        else
            return JsonResult.error(result.getValue());
    }

    @RequestMapping("dispatch/queryList")
    @ResponseBody
    public PageResult<ETLDataDispatch> queryDispatchList(@RequestParam(value = "page", defaultValue = "1") Integer page
            , @RequestParam(value = "limit", defaultValue = "10") Integer limit, Long id, String name, String consumer) {
        logger.info("ETLDataDispatch queryList. id:{},name:{},consumer:{}", id, name, consumer);
        Criterion idCriterion = (id == null || id == 0) ? null : Restrictions.eq("id", id);
        Criterion nameCriterion = Strings.isEmpty(name) ? null : Restrictions.like("typeName", name);
        Criterion typeNameCriterion = Strings.isEmpty(consumer) ? null : Restrictions.eq("consumerType", consumer);
        return taskService.list(ETLDataDispatch.class, page, limit, idCriterion, nameCriterion, typeNameCriterion);
    }

    @RequestMapping("receive/queryList")
    @ResponseBody
    public PageResult<ETLDataReceive> queryReceiveList(@RequestParam(value = "page", defaultValue = "1") Integer page
            , @RequestParam(value = "limit", defaultValue = "10") Integer limit, String name) {
        logger.info("ETLDataReceive queryList. name::{}", name);
        Criterion nameCriterion = Strings.isEmpty(name) ? null : Restrictions.like("receiveName", name);
        return taskService.list(ETLDataReceive.class, page, limit, nameCriterion);
    }
}
