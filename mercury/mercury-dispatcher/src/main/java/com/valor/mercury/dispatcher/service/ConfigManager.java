package com.valor.mercury.dispatcher.service;

import com.valor.mercury.common.client.ServiceMonitor;
import com.valor.mercury.common.model.ExecutorCommand;
import com.valor.mercury.common.model.Router;
import com.valor.mercury.common.util.JsonUtil;
import com.valor.mercury.common.util.PostUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

import static com.valor.mercury.common.constant.MercuryConstants.EXECUTOR_STATUS_WORKING;


@EnableScheduling
@Service
public class ConfigManager {
    private static final Logger logger = LoggerFactory.getLogger(ConfigManager.class);
    public static ServiceMonitor serviceMonitor = new ServiceMonitor();
    //<"tve_play_action", <"Kafka", Router>>
    public static Map<String, Map<Router.Dest, Router>> routerMap = new ConcurrentHashMap<>();

    @Scheduled(fixedDelay = 60_000, initialDelay = 0L)
    public void scheduledTask() {
        fetchConfigs();
        serviceMonitor.reportCount("dispatcher");
        serviceMonitor.reportErrors();
    }

    /**
     * 更新 configs
     */
    public void fetchConfigs() {
        List<ExecutorCommand> commands = PostUtil.getCommandsFromMercury(EXECUTOR_STATUS_WORKING);
        if (!commands.isEmpty()) {
            Map<String, Map<Router.Dest, Router>> newRouterMap = new ConcurrentHashMap<>();
            for (ExecutorCommand command : commands) {
                Long instanceId = command.getInstanceID();
                Map<String, Object> taskConfig = command.getTaskConfig();
                try {
                    Router router = new Router(instanceId);
                    loadRouter(router, taskConfig);
                    String type = router.type;
                    newRouterMap.putIfAbsent(type, new HashMap<>());
                    newRouterMap.get(type).put(router.dest, router);
                } catch (Exception ex) {
                    serviceMonitor.saveException(instanceId, ex);
                }
            }
            routerMap = newRouterMap;
            logger.info("valid type size: [{}]", routerMap.size());
        }
    }


    /**
     */
    private void loadRouter(Router router, Map<String, Object> config) throws IOException {
        router.type = (String) config.get("typeName");
        router.loadDest((String)config.get("consumerType"));

        router.mapping = (String) config.get("mapping");
        router.loadFilter();

        router.ipField = (String)config.get("ipField");

        router.mappingApplyLevel = (int)config.get("mappingApplyLevel");

        router.consumerConfig = JsonUtil.jsonToMap((String) config.get("consumerConfig"));
        router.loadConsumerConfig();

//        logger.info(router.toString());
    }
}
