package com.valor.mercury.manager.service;

import com.dingtalk.api.DefaultDingTalkClient;
import com.dingtalk.api.DingTalkClient;
import com.dingtalk.api.request.OapiRobotSendRequest;
import com.valor.mercury.manager.config.ConstantConfig;
import com.valor.mercury.manager.dao.MercuryManagerDao;
import com.valor.mercury.manager.model.ddo.MonitorLog;
import org.hibernate.criterion.Restrictions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;

import static com.valor.mercury.manager.service.HAService.NodeStatus.LEADER;
import static com.valor.mercury.manager.service.HAService.NodeStatus.LOOKING;

/**
 * @Author: Gavin
 * @Date: 2020/2/20 13:31
 * 监控与报警
 */
@Service
public class LogAlarmService extends BaseDBService {
    private final MercuryManagerDao dao;
    private final HAService haService;
    private DingTalkClient client;
    private Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    public LogAlarmService(MercuryManagerDao dao, HAService haService) {
        this.dao = dao;
        this.haService = haService;
        client = new DefaultDingTalkClient(ConstantConfig.dingdingOath());
    }

    @Override
    public MercuryManagerDao getSchedulerDao() {
        return dao;
    }

    public void addLog(int logLevel, String source, String targetObject, String detail) {
        MonitorLog monitorLog = new MonitorLog();
        monitorLog.setCreateTime(new Date());
        monitorLog.setLevel(logLevel);
        monitorLog.setSource(source);
        monitorLog.setTargetObject(targetObject);
        monitorLog.setDetail(detail);
        dao.saveEntity(monitorLog);
        alarm(monitorLog);
    }

    public void addLog(int logLevel, String source, String targetObject, String detail, String uniqueValue) {
        MonitorLog preLog = getEntityByCriterion(MonitorLog.class, Restrictions.eq("uniqueValue", uniqueValue));
        if (preLog == null) {
            MonitorLog monitorLog = new MonitorLog();
            monitorLog.setCreateTime(new Date());
            monitorLog.setLevel(logLevel);
            monitorLog.setSource(source);
            monitorLog.setTargetObject(targetObject);
            monitorLog.setDetail(detail);
            monitorLog.setUniqueValue(uniqueValue);
            dao.saveEntity(monitorLog);
            alarm(monitorLog);
        } else {
            preLog.setCreateTime(new Date());
            preLog.setLevel(logLevel);
            preLog.setSource(source);
            preLog.setTargetObject(targetObject);
            preLog.setDetail(detail);
            preLog.setUniqueValue(uniqueValue);
            dao.updateEntity(preLog);
            alarm(preLog);
        }
    }

    private void alarm(MonitorLog log) {
        try {
            if (haService.getNodeStatus().equals(LEADER)) {
                if (log.getLevel() >= ConstantConfig.dingdingOathLevel()) {
                    OapiRobotSendRequest request = new OapiRobotSendRequest();
                    OapiRobotSendRequest.Text message = new OapiRobotSendRequest.Text();
                    message.setContent(log.toString());
                    request.setText(message);
                    client.execute(request);
                }
            }
        } catch (Exception e) {
            logger.error("alarm error:{}", e);
        }
    }
}
