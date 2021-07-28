package com.valor.mercury.manager.service;

import com.valor.mercury.manager.config.MercuryConstants;
import com.valor.mercury.manager.dao.MercuryManagerDao;
import com.valor.mercury.manager.model.ddo.ETLDataDispatch;
import com.valor.mercury.manager.model.ddo.ETLDataReceive;
import com.valor.mercury.manager.model.ddo.ETLDataTraffic;
import com.valor.mercury.manager.model.system.ExecutorCommand;
import com.valor.mercury.manager.model.system.ExecutorReport;
import com.valor.mercury.manager.tool.JsonUtil;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.logging.log4j.util.Strings;
import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.Restrictions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;

/**
 * @author Gavin
 * 2020/8/17 9:39
 * 数据接收与分发服务
 */
@Service
public class ETLTaskService extends BaseDBService {

    private final MercuryManagerDao dao;
    private final HAService haService;
    private final LogAlarmService logService;
    private Logger logger = LoggerFactory.getLogger(this.getClass());
    private Map<String, Long> receiveMap = new HashMap<>();
    private Map<String, Long> dispatchMap = new HashMap<>();


    @Autowired
    public ETLTaskService(MercuryManagerDao dao, HAService haService, LogAlarmService logService) {
        this.dao = dao;
        this.haService = haService;
        this.logService = logService;
    }

    @Override
    public MercuryManagerDao getSchedulerDao() {
        return dao;
    }

    @Scheduled(cron = "0 0 0/1 1/1 * ? ")
//    @Async("taskScheduler")
    private void saveETLInfo() {
        if (haService.getNodeStatus().equals(HAService.NodeStatus.LEADER)) {
            String batch = LocalDateTime.now().getDayOfMonth() + "" + LocalDateTime.now().getHour();
            Criterion criterion = Restrictions.between("lastReportTime", new Date(System.currentTimeMillis() - 3600_000), new Date());
            List<ETLDataReceive> receives = listEntity(ETLDataReceive.class, criterion);
            List<ETLDataDispatch> dispatches = listEntity(ETLDataDispatch.class, criterion);
            for (ETLDataReceive receive : receives) {
                String name = receive.getReceiveName();
                ETLDataTraffic dataTraffic = new ETLDataTraffic();
                dataTraffic.setId(name + batch);
                dataTraffic.setType("Receive");
                dataTraffic.setTypeName(name);
                dataTraffic.setCreateTime(new Date());
                if (receiveMap.containsKey(name))
                    dataTraffic.setNum((int) (receive.getForwardNum() - receiveMap.get(name)));
                else
                    dataTraffic.setNum(0);
                receiveMap.put(name, receive.getForwardNum());
                if (dao.getById(ETLDataTraffic.class, dataTraffic.getId()) == null)
                    dao.saveEntity(dataTraffic);
                else
                    dao.updateEntity(dataTraffic);
            }
            if (receives.size() == 0) {
                ETLDataTraffic dataTraffic = new ETLDataTraffic();
                dataTraffic.setId("Receive" + batch);
                dataTraffic.setNum(0);
                dataTraffic.setType("Receive");
                dataTraffic.setTypeName("Receive");
                dataTraffic.setCreateTime(new Date());
                if (dao.getById(ETLDataTraffic.class, dataTraffic.getId()) == null)
                    dao.saveEntity(dataTraffic);
                else
                    dao.updateEntity(dataTraffic);
            }
            for (ETLDataDispatch dispatch : dispatches) {
                String name = dispatch.getTypeName() + "-" + dispatch.getConsumerType();
                ETLDataTraffic dataTraffic = new ETLDataTraffic();
                dataTraffic.setId(name + batch);
                dataTraffic.setType("Dispatch");
                dataTraffic.setTypeName(name);
                dataTraffic.setCreateTime(new Date());
                if (dispatchMap.containsKey(name))
                    dataTraffic.setNum((int) (dispatch.getConsumeNum() - dispatchMap.get(name)));
                else
                    dataTraffic.setNum(0);
                dispatchMap.put(name, dispatch.getConsumeNum());
                if (dao.getById(ETLDataTraffic.class, dataTraffic.getId()) == null)
                    dao.saveEntity(dataTraffic);
                else
                    dao.updateEntity(dataTraffic);
            }
            if (dispatches.size() == 0) {
                ETLDataTraffic dataTraffic = new ETLDataTraffic();
                dataTraffic.setId("Dispatch" + batch);
                dataTraffic.setNum(0);
                dataTraffic.setType("Dispatch");
                dataTraffic.setTypeName("Dispatch");
                dataTraffic.setCreateTime(new Date());
                if (dao.getById(ETLDataTraffic.class, dataTraffic.getId()) == null)
                    dao.saveEntity(dataTraffic);
                else
                    dao.updateEntity(dataTraffic);
            }
        }
    }

    public Pair<Boolean, String> addReceiveTask(ETLDataReceive etlDataReceive) {
        try {
            ETLDataReceive receive = getEntityByCriterion(ETLDataReceive.class, Restrictions.eq("receiveName", etlDataReceive.getReceiveName()));
            if (receive == null || !receive.getReceiveName().equals(etlDataReceive.getReceiveName())) {
                etlDataReceive.setCreateTime(new Date());
                etlDataReceive.setLastModifyTime(new Date());
                addEntity(etlDataReceive);
                return ImmutablePair.of(true, null);
            } else
                return ImmutablePair.of(false, "命名冲突");
        } catch (Exception e) {
            return ImmutablePair.of(false, e.getMessage());
        }
    }

    public Pair<Boolean, String> updateReceiveTask(ETLDataReceive etlDataReceive) {
        try {
            ETLDataReceive ctReceive = getEntityById(ETLDataReceive.class, etlDataReceive.getId());
            if (ctReceive != null) {
                ctReceive.setEnable(etlDataReceive.getEnable());
                ctReceive.setReceiveName(etlDataReceive.getReceiveName());
                ctReceive.setReceiveNum(etlDataReceive.getReceiveNum());
                ctReceive.setForwardNum(etlDataReceive.getForwardNum());
                update(ctReceive);
                return ImmutablePair.of(true, null);
            } else
                return ImmutablePair.of(false, "找不到对应数据");
        } catch (Exception e) {
            return ImmutablePair.of(false, e.getMessage());
        }
    }

    public <T> Pair<Boolean, String> deleteETLTask(Class<T> tClass, Long id) {
        if (deleteById(tClass, id))
            return ImmutablePair.of(true, null);
        else
            return ImmutablePair.of(false, "找不到对应数据");
    }

    public boolean updateReceiveTaskStatus(ExecutorReport report) {
        if (report.getInstanceID() == null || report.getInstanceID().equals(0L)) {
            //异常情况
            String value = report.getErrorMessage();
            if (Strings.isNotEmpty(value)) {
                logger.warn("message info is not config:{}", value);
                String[] values = value.split("\"");
                if (values.length >= 3)
                    logService.addLog(MercuryConstants.LOG_LEVEL_WARN, this.getClass().getName(),
                            "ETL", "message info is not config:" + value, values[3]);
                else
                    logService.addLog(MercuryConstants.LOG_LEVEL_WARN, this.getClass().getName(),
                            "ETL", "message info is not config:" + value);
            }
            return true;
        }
        try {
            ETLDataReceive receive = getEntityById(ETLDataReceive.class, report.getInstanceID());
            Map<String, Object> metric = report.getMetrics();
            Integer receiveNum = Integer.parseInt(metric.get("receiveNum").toString());
            Integer forwardNum = Integer.parseInt(metric.get("forwardNum").toString());
            receive.setForwardNum(receive.getForwardNum() + forwardNum);
            receive.setReceiveNum(receive.getReceiveNum() + receiveNum);
            receive.setLastReportTime(new Date());
            update(receive);
            return true;
        } catch (Exception e) {
            logger.error("updateReceiveTaskStatus error:{}", e);
            return false;
        }
    }

    public List<ExecutorCommand> getReceiveCommandByExecutor() {
        List<ExecutorCommand> result = new ArrayList<>();
        Criterion criterion = Restrictions.eq("enable", "on");
        List<ETLDataReceive> receives = listEntity(ETLDataReceive.class, criterion);
        for (ETLDataReceive receive : receives) {
            receive.setLastRequestTime(new Date());
            update(receive);
            ExecutorCommand command = new ExecutorCommand();
            command.setInstanceID(receive.getId());
            command.setActionTime(new Date());
            Map<String, Object> metric = new HashMap<>();
            metric.put("typeName", receive.getReceiveName());
            command.setTaskConfig(metric);
            result.add(command);
        }
        return result;
    }

    public List<ExecutorCommand> getDispatchCommandByExecutor() {
        List<ExecutorCommand> result = new ArrayList<>();
        Criterion criterion = Restrictions.eq("enable", "on");
        Criterion statusCriterion = Restrictions.eq("status", "finished");
        List<ETLDataDispatch> dispatches = listEntity(ETLDataDispatch.class, criterion, statusCriterion);
        for (ETLDataDispatch dispatch : dispatches) {
            update(dispatch);
            ExecutorCommand command = new ExecutorCommand();
            command.setInstanceID(dispatch.getId());
            command.setActionTime(new Date());
            Map<String, Object> metric = new HashMap<>();
            metric.put("typeName", dispatch.getTypeName());
            metric.put("mapping", dispatch.getMapping());
            metric.put("mappingApplyLevel", dispatch.getMappingApplyLevel());
            metric.put("consumerType", dispatch.getConsumerType());
            metric.put("consumerConfig", dispatch.getConsumerConfig());
            metric.put("ipField", dispatch.getIpField());
            command.setTaskConfig(metric);
            result.add(command);
        }
        return result;
    }

    public boolean updateDispatchTaskStatus(ExecutorReport report) {
        try {
            ETLDataDispatch dispatch = getEntityById(ETLDataDispatch.class, report.getInstanceID());
            Map<String, Object> metric = report.getMetrics();
            Integer consumeNum = Integer.parseInt(metric.get("consumeNum").toString());
            Integer sendNum = Integer.parseInt(metric.get("sendNum").toString());
            Integer interceptNum = Integer.parseInt(metric.get("interceptNum").toString());
            dispatch.setConsumeNum(dispatch.getConsumeNum() + consumeNum);
            dispatch.setSendNum(dispatch.getSendNum() + sendNum);
            dispatch.setInterceptNum(dispatch.getInterceptNum() + interceptNum);
            dispatch.setLastReportTime(new Date());
            update(dispatch);
            return true;
        } catch (Exception e) {
            logger.error("updateReceiveTaskStatus error:{}", e);
            return false;
        }
    }

    public String verifyMapping(String value) {
        try {
            String[] values = value.split(",");
            if (values.length % 4 != 0)
                return null;
            List<Map<String, String>> result = new ArrayList<>();
            for (int i = 0; i < values.length; ) {
                Map<String, String> mappingMap = new HashMap<>();
                String acceptName = values[i++];
                String acceptType = values[i++];
                String outPutName = values[i++];
                String outPutType = values[i++];
                mappingMap.put("acceptName", acceptName);
                mappingMap.put("acceptType", acceptType);
                if (Strings.isEmpty(outPutName))
                    mappingMap.put("outputName", acceptName);
                else
                    mappingMap.put("outputName", outPutName);
                mappingMap.put("outputType", outPutType);
                result.add(mappingMap);
            }
            return JsonUtil.objectMapper.writeValueAsString(result);
        } catch (Exception e) {
            logger.error("verifyMapping error:{}", e);
            return null;
        }
    }

    public Pair<Boolean, String> updateDispatchTask(ETLDataDispatch etlDataDispatch) {
        ETLDataDispatch preDispatch = getEntityById(ETLDataDispatch.class, etlDataDispatch.getId());
        if (!preDispatch.getConsumerType().equals(etlDataDispatch.getConsumerType())) {
            preDispatch.setStatus("unfinished");
            preDispatch.setConsumerConfig(null);
        }
        preDispatch.setSendNum(etlDataDispatch.getSendNum());
        preDispatch.setInterceptNum(etlDataDispatch.getInterceptNum());
        preDispatch.setTypeName(etlDataDispatch.getTypeName());
        preDispatch.setConsumeNum(etlDataDispatch.getConsumeNum());
        preDispatch.setEnable(etlDataDispatch.getEnable());
        preDispatch.setMapping(etlDataDispatch.getMapping());
        preDispatch.setConsumerType(etlDataDispatch.getConsumerType());
        preDispatch.setMappingApplyLevel(etlDataDispatch.getMappingApplyLevel());
        preDispatch.setIpField(etlDataDispatch.getIpField());
        if (etlDataDispatch.getMappingApplyLevel() == 2 && Strings.isEmpty(etlDataDispatch.getMapping()))
            return ImmutablePair.of(false, "缺少Mapping");
        List<ETLDataDispatch> dispatches
                = listEntity(ETLDataDispatch.class, Restrictions.eq("typeName", preDispatch.getTypeName()));
        for (ETLDataDispatch dispatch : dispatches) {
            if (dispatch.getConsumerType().equals(preDispatch.getConsumerType()) && !dispatch.getId().equals(preDispatch.getId()))
                return ImmutablePair.of(false, "重复配置");
        }
        if (update(preDispatch))
            return ImmutablePair.of(true, null);
        else
            return ImmutablePair.of(false, "数据库错误");
    }
}
