package com.valor.mercury.manager.service;

import com.valor.mercury.manager.config.MercuryConstants;
import com.valor.mercury.manager.dao.MercuryManagerDao;
import com.valor.mercury.manager.model.ddo.Executor;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.logging.log4j.util.Strings;
import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.Restrictions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;

/**
 * @Author: Gavin
 * @Date: 2020/2/24 14:26
 * 客户端管理
 */
@Service
public class ExecutorService extends BaseDBService {

    private final MercuryManagerDao dao;
    private final LogAlarmService logService;
    private Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    public ExecutorService(MercuryManagerDao dao, LogAlarmService logService) {
        this.dao = dao;
        this.logService = logService;
    }

    @Override
    public MercuryManagerDao getSchedulerDao() {
        return dao;
    }

    public Executor getClientByName(String clientName) {
        Criterion criterion = Restrictions.eq("clientName", clientName);
        List<Executor> list = dao.listEntity(Executor.class, criterion);
        if (list == null || list.size() != 1) {
            return null;
        } else
            return list.get(0);
    }

    public Pair<Boolean, String> verifyExecutor(String clientName, String clientPsd, String status, String clientIP) {
        Executor executor = getClientByName(clientName);
        if (executor == null || !executor.getEnable().equals("on")) {
            logger.error("executor is disable:{}", clientName);
            logService.addLog(MercuryConstants.LOG_LEVEL_WARN, this.getClass().getName(),
                    "executor", "executor is disable:" + clientName);
            return ImmutablePair.of(false, "executor is disable");
        } else if (executor.getClientPsd().equals(clientPsd)) {
            if (Strings.isNotEmpty(executor.getClientIP()))
                executor.setClientIP(clientIP);
            else if (!executor.getClientIP().equals(clientIP)) {
                executor.setClientIP(clientIP);
                logger.warn("Executor ip changed:{}", clientName);
                logService.addLog(MercuryConstants.LOG_LEVEL_WARN, this.getClass().getName(),
                        "executor", "Executor ip changed:" + clientName);
            }
            if (status != null) executor.setStatus(status);
            executor.setLastActionTime(new Date());
            update(executor);
            return ImmutablePair.of(true, executor.getExecutorType());
        } else {
            logger.info("wrong password getTask:{}", clientName);
            logService.addLog(MercuryConstants.LOG_LEVEL_WARN, this.getClass().getName(),
                    "executor", "wrong password getTask:" + clientName);
            return ImmutablePair.of(false, "wrong password");
        }
    }
}
