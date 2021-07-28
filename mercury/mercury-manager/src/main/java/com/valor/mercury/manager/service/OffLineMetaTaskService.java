package com.valor.mercury.manager.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.valor.mercury.manager.config.MercuryConstants;
import com.valor.mercury.manager.model.ddo.UploadFileConfig;
import com.valor.mercury.manager.model.system.ExecutorReport;
import com.valor.mercury.manager.model.system.HiveTask;
import com.valor.mercury.manager.model.system.ScriptTask;
import com.valor.mercury.manager.model.system.SpiderTask;
import com.valor.mercury.manager.dao.MercuryManagerDao;
import com.valor.mercury.manager.model.ddo.DataBaseConfig;
import com.valor.mercury.manager.model.ddo.OffLineMetaTask;
import com.valor.mercury.manager.model.ddo.OffLineMetaTaskInstance;
import com.valor.mercury.manager.tool.DateUtils;
import com.valor.mercury.manager.tool.JsonUtil;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.statement.select.*;
import org.apache.logging.log4j.util.Strings;
import org.hibernate.criterion.Restrictions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * @author Gavin
 * 2020/7/30 14:08
 * 负责子任务的解析，校验和封装
 */
@Service
public class OffLineMetaTaskService extends BaseDBService {

    private final MercuryManagerDao dao;
    private Logger logger = LoggerFactory.getLogger(this.getClass());
    private static ObjectMapper objectMapper = new ObjectMapper();
    private Calendar calendar = Calendar.getInstance();
    private final LogAlarmService logService;

    @Autowired
    public OffLineMetaTaskService(MercuryManagerDao dao, LogAlarmService logService) {
        this.dao = dao;
        this.logService = logService;
    }

    @Override
    public MercuryManagerDao getSchedulerDao() {
        return dao;
    }

    public OffLineMetaTask verifySpiderTask(OffLineMetaTask offLineMetaTask, SpiderTask spiderTask) {
        try {
            if (offLineMetaTask == null) {
                offLineMetaTask = new OffLineMetaTask();
                offLineMetaTask.setCreateTime(new Date());
            }
            //校验SQL
            Select stmt = (Select) CCJSqlParserUtil.parse(spiderTask.getSql());
            PlainSelect selectBody = (PlainSelect) stmt.getSelectBody();
            selectBody.getSelectItems();
            selectBody.getFromItem();
            if (selectBody.getJoins() != null)
                selectBody.getJoins();
            selectBody.getWhere();

            offLineMetaTask.setConfig(objectMapper.writeValueAsString(spiderTask));
            offLineMetaTask.setDesc(spiderTask.getDescription());
            offLineMetaTask.setExecutor(spiderTask.getExecutor());
            offLineMetaTask.setInc(spiderTask.getIncValue());
            offLineMetaTask.setLastModifyTime(new Date());
            offLineMetaTask.setName(spiderTask.getName());
            offLineMetaTask.setType(MercuryConstants.TYPE_OFFLINE_SPIDER);
            return offLineMetaTask;
        } catch (Exception e) {
            logger.error("verifySpiderTask error:{}", e);
            return null;
        }
    }

    public Map<String, Object> setMetaTaskMetric(OffLineMetaTask metaTask) {
        switch (metaTask.getType()) {
            case MercuryConstants.TYPE_OFFLINE_SPIDER:
                return setSpiderMetaTaskMetric(metaTask);
            case MercuryConstants.TYPE_OFFLINE_SCRIPT:
                return setScriptMetaTaskMetric(metaTask);
            case MercuryConstants.TYPE_OFFLINE_HIVE:
                return setHiveMetaTaskMetric(metaTask);
            default:
                logger.error("unKnown task type:{}", metaTask.getType());
                return null;
        }
    }

    private Map<String, Object> setScriptMetaTaskMetric(OffLineMetaTask metaTask) {
        try {
            Map<String, Object> metric = new HashMap<>();
            ScriptTask scriptTask = objectMapper.readValue(metaTask.getConfig(), ScriptTask.class);
            metric.put("config", scriptTask.getConfig());
            UploadFileConfig fileConfig = getEntityById(UploadFileConfig.class, scriptTask.getFileId());
            metric.put("fileMd5", fileConfig.getFileMd5());
            metric.put("fileName", fileConfig.getFileName());
            metric.put("incValue", metaTask.getInc());
            metric.put("entryClass", scriptTask.getEntryClass());
            return metric;
        } catch (Exception e) {
            logger.error("setScriptMetaTaskMetric error:{}", e);
            return null;
        }
    }

    private Map<String, Object> setHiveMetaTaskMetric(OffLineMetaTask metaTask) {
        try {
            Map<String, Object> metric = new HashMap<>();
            HiveTask hiveTask = objectMapper.readValue(metaTask.getConfig(), HiveTask.class);
            String SQL = hiveTask.getSql();
            if (Strings.isNotEmpty(hiveTask.getParameterMap())) {
                List<Map<String, String>> parameters = JsonUtil.objectMapper.readValue(hiveTask.getParameterMap(), new TypeReference<List<Map>>() {
                });
                for (Map<String, String> parameter : parameters) {
                    String incName = parameter.get("parameterName");
                    String incValue = parameter.get("parameterValue");
                    if (incValue.contains("-")) {
                        try {
                            int index = 8;
                            if (incValue.charAt(5) == '0') {
                                incValue = incValue.substring(0, 5) + incValue.substring(6);
                                index = 7;
                            }
                            if (incValue.charAt(index) == '0')
                                incValue = incValue.substring(0, index) + incValue.substring(index + 1);
                        } catch (Exception e) {
                        }
                    }
                    SQL = SQL.replace(incName, incValue);
                }
            }
            metric.put("SQL", SQL);
            metric.put("outPutName", metaTask.getName());
            return metric;
        } catch (Exception e) {
            logger.error("setHiveMetaTaskMetric error:{}", e);
            return null;
        }
    }

    private Map<String, Object> setSpiderMetaTaskMetric(OffLineMetaTask metaTask) {
        try {
            SpiderTask spiderTask = objectMapper.readValue(metaTask.getConfig(), SpiderTask.class);
            Map<String, Object> metric = new HashMap<>();
            Map<String, Object> configs = new HashMap<>();
            Select stmt = (Select) CCJSqlParserUtil.parse(spiderTask.getSql());
            PlainSelect selectBody = (PlainSelect) stmt.getSelectBody();
            StringBuilder selectClause = new StringBuilder();
            for (SelectItem item : selectBody.getSelectItems()) {
                selectClause.append(item.toString());
                selectClause.append(",");
            }
            StringBuilder fromClause = new StringBuilder();
            fromClause.append(selectBody.getFromItem().toString());
            if (selectBody.getJoins() != null)
                for (Join item : selectBody.getJoins()) {
                    fromClause.append(" ");
                    fromClause.append(item.toString());
                }
            configs.put("SELECT_CLAUSE", selectClause.toString().subSequence(0, selectClause.toString().length() - 1));
            configs.put("FROM_CLAUSE", fromClause.toString());
            configs.put("WHERE_CLAUSE", selectBody.getWhere().toString());
            configs.put("SORT_KEYS", spiderTask.getIncTag());
            configs.put("PAGE_SIZE", Integer.parseInt(spiderTask.getPageSize()));
            DataBaseConfig dataBaseConfig = getEntityByCriterion(DataBaseConfig.class,
                    Restrictions.eq("name", spiderTask.getDatabase()));
            Map<String, Object> dataBaseMap = new HashMap<>();
            dataBaseMap.put("name", dataBaseConfig.getName());
            dataBaseMap.put("url", dataBaseConfig.getDbUrl());
            dataBaseMap.put("userName", dataBaseConfig.getDbUsername());
            dataBaseMap.put("password", dataBaseConfig.getDbPassword());
            dataBaseMap.put("driver", dataBaseConfig.getDbDriver());
            configs.put("DATASOURCE", objectMapper.writeValueAsString(dataBaseMap));

            metric.put("config", objectMapper.writeValueAsString(configs));
            metric.put("taskName", metaTask.getName());
            metric.put("outPutName", spiderTask.getOutPutName());
            metric.put("reader", spiderTask.getReader());
            metric.put("processor", spiderTask.getProcessor());
            metric.put("listener", spiderTask.getListener());
            metric.put("incTag", spiderTask.getIncTag().split("\\.").length > 1 ?
                    spiderTask.getIncTag().split("\\.")[1] : spiderTask.getIncTag());
            metric.put("incValue", metaTask.getInc());
            return metric;
        } catch (Exception e) {
            logger.error("setSpiderMetaTaskMetric error:{}", e);
            return null;
        }
    }

    public void updateMetaTaskInstanceMetric(ExecutorReport report, OffLineMetaTaskInstance metaTaskInstance) throws Exception {
        metaTaskInstance.setMetrics(JsonUtil.objectMapper.writeValueAsString(report.getMetrics()));
        metaTaskInstance.setResult(report.getInstanceStatus());
        metaTaskInstance.setErrorMsg(report.getErrorMessage());
        metaTaskInstance.setLastActionTime(new Date());
        if (report.getMetrics().containsKey("startTime"))
            metaTaskInstance.setStartTime(new Date(((Number) report.getMetrics().get("startTime")).longValue()));
        if (report.getMetrics().containsKey("endTime"))
            metaTaskInstance.setEndTime(new Date(((Number) report.getMetrics().get("endTime")).longValue()));
        update(metaTaskInstance);
    }

    public OffLineMetaTask verifyScriptTask(OffLineMetaTask offLineMetaTask, ScriptTask scriptTask) {
        try {
            if (offLineMetaTask == null) {
                offLineMetaTask = new OffLineMetaTask();
                offLineMetaTask.setCreateTime(new Date());
            }
            offLineMetaTask.setConfig(objectMapper.writeValueAsString(scriptTask));
            offLineMetaTask.setDesc(scriptTask.getDescription());
            offLineMetaTask.setExecutor(scriptTask.getExecutor());
            offLineMetaTask.setInc(scriptTask.getIncValue());
            offLineMetaTask.setLastModifyTime(new Date());
            offLineMetaTask.setName(scriptTask.getName());
            offLineMetaTask.setType(MercuryConstants.TYPE_OFFLINE_SCRIPT);
            return offLineMetaTask;
        } catch (Exception e) {
            logger.error("verifyScriptTask error:{}", e);
            return null;
        }
    }

    public OffLineMetaTask verifyHiveTask(OffLineMetaTask offLineMetaTask, HiveTask hiveTask) {
        try {
            if (offLineMetaTask == null) {
                offLineMetaTask = new OffLineMetaTask();
                offLineMetaTask.setCreateTime(new Date());
            }
            if (!hiveTask.getSql().endsWith(";"))
                hiveTask.setSql(hiveTask.getSql() + ";");
            offLineMetaTask.setConfig(objectMapper.writeValueAsString(hiveTask));
            offLineMetaTask.setDesc(hiveTask.getDescription());
            offLineMetaTask.setExecutor(hiveTask.getExecutor());
            offLineMetaTask.setInc(hiveTask.getParameterMap());
            offLineMetaTask.setLastModifyTime(new Date());
            offLineMetaTask.setName(hiveTask.getName());
            offLineMetaTask.setType(MercuryConstants.TYPE_OFFLINE_HIVE);
            return offLineMetaTask;
        } catch (Exception e) {
            logger.error("verifyHiveTask error:{}", e);
            return null;
        }
    }

    public String verifyHiveParameter(String value) {
        try {
            String[] values = value.split(",");
            if (values.length % 4 != 0)
                return null;
            List<Map<String, String>> result = new ArrayList<>();
            for (int i = 0; i < values.length; ) {
                Map<String, String> parameterMap = new HashMap<>();
                String parameterName = values[i++];
                String parameterValue = values[i++];
                String format = values[i++];
                String named = values[i++];
                parameterMap.put("parameterName", parameterName);
                parameterMap.put("parameterValue", parameterValue);
                parameterMap.put("format", format);
                parameterMap.put("named", named);
                result.add(parameterMap);
                if (!format.equals("timestamp"))
                    DateUtils.parseStringToDate(format, parameterValue);
                else
                    new Date(Long.parseLong(parameterValue));
            }
            return JsonUtil.objectMapper.writeValueAsString(result);
        } catch (Exception e) {
            logger.error("verifyHiveParameter error:{}", e);
            return null;
        }
    }

    public HiveTask updateHiveMetaTask(HiveTask hiveTask) {
        try {
            if (Strings.isEmpty(hiveTask.getParameterMap()))
                return hiveTask;
            List<Map<String, String>> parameters = JsonUtil.objectMapper.readValue(hiveTask.getParameterMap(), new TypeReference<List<Map>>() {
            });
            for (Map<String, String> parameter : parameters) {
                String format = parameter.get("format");
                Date incValue;
                if (format.equals("timestamp"))
                    incValue = new Date(Long.parseLong(parameter.get("parameterValue")));
                else
                    incValue = DateUtils.parseStringToDate(format, parameter.get("parameterValue"));
                switch (parameter.get("named")) {
                    case "Hour":
                        incValue = new Date(incValue.getTime() + 3600_000);
                        break;
                    case "Day":
                        incValue = new Date(incValue.getTime() + 24 * 3600_000);
                        break;
                    case "Week":
                        incValue = new Date(incValue.getTime() + 7 * 24 * 3600_000);
                        break;
                    case "Month":
                        calendar.setTime(incValue);
                        calendar.set(Calendar.MONTH, calendar.get(Calendar.MONTH) + 1);
                        incValue = calendar.getTime();
                        break;
                    case "Quarter":
                        calendar.setTime(incValue);
                        calendar.set(Calendar.MONTH, calendar.get(Calendar.MONTH) + 3);
                        incValue = calendar.getTime();
                        break;
                    case "Year":
                        calendar.setTime(incValue);
                        calendar.set(Calendar.YEAR, calendar.get(Calendar.YEAR) + 1);
                        incValue = calendar.getTime();
                        break;
                    default:
                        logger.error("setHiveMetaTaskMetric error:parseDate");
                        return null;
                }
                if (format.equals("timestamp"))
                    parameter.put("parameterValue", incValue.getTime() + "");
                else
                    parameter.put("parameterValue", DateUtils.parseDateToString(format, incValue));
            }
            hiveTask.setParameterMap(JsonUtil.objectMapper.writeValueAsString(parameters));
        } catch (Exception e) {
            logger.error("updateHiveMetaTask error:{}", e);
            logService.addLog(MercuryConstants.LOG_LEVEL_ERROR, this.getClass().getName(), "HiveTask",
                    "HiveTask :updateHiveMetaTask error " + e.getMessage());
        }
        return hiveTask;
    }
}
