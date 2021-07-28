package com.valor.mercury.receiver.service;

import com.mfc.config.ConfigTools3;
import com.valor.mercury.common.client.ServiceMonitor;
import com.valor.mercury.common.model.ExecutorCommand;
import com.valor.mercury.common.model.Router;
import com.valor.mercury.common.util.PostUtil;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static com.valor.mercury.common.constant.MercuryConstants.EXECUTOR_STATUS_WORKING;


@Service
public class ServiceManger {
    private static final Logger logger = LoggerFactory.getLogger(ServiceManger.class);
    public static ServiceMonitor serviceMonitor = ServiceMonitor.getDefault();
    public Map<String, Router> routerMap = new ConcurrentHashMap<>();

    @Scheduled(fixedDelay = 60_000, initialDelay = 0L)
    private void scheduledTask() {
        fetchConfigs();
        serviceMonitor.reportCount("receiver");
        serviceMonitor.reportErrors();
    }

    /**
     * 从mercury-manager获得 Router configs
     */
    private void fetchConfigs() {
        String cacheTopicName = ConfigTools3.getConfigAsString("cache.topic.name");
        if (StringUtils.isEmpty(cacheTopicName))
            throw new IllegalArgumentException("cache topic name is empty");
        List<ExecutorCommand> commands = PostUtil.getCommandsFromMercury(EXECUTOR_STATUS_WORKING);
        Map<String, Router> newRouterMap = new ConcurrentHashMap<>();
        if (!commands.isEmpty()) {
            for (ExecutorCommand executorCommand : commands) {
                Map<String, Object> config = executorCommand.getTaskConfig();
                String typeName = (String) config.get("typeName");
                if (typeName != null)
                    newRouterMap.put(typeName, new Router(executorCommand.getInstanceID(), typeName, cacheTopicName));
            }
            routerMap = newRouterMap;
            logger.info("configs updated, size: [{}]", routerMap.size());
        }
    }
}
