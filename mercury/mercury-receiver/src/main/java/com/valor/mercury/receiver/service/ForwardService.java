package com.valor.mercury.receiver.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.valor.mercury.common.model.Constants;
import com.valor.mercury.common.model.MetricMessage;
import com.valor.mercury.common.model.Router;
import com.valor.mercury.common.model.WrapperEntity;
import com.valor.mercury.common.model.exception.CustomException;
import com.valor.mercury.common.util.DateUtils;
import com.valor.mercury.common.util.JsonUtils;
import com.valor.mercury.receiver.config.CommonConfig;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * 接收到的所有数据直接发送到kafka作为缓冲层，然后由其他RouteService根据配置相应的消费并发往中间层或结果层。
 */
@Service
public class ForwardService {
    private static final Logger logger = LoggerFactory.getLogger(ForwardService.class);
    @Autowired
    private ServiceManger serviceManager;
    @Autowired
    private KafkaService kafkaService ;
    private LinkedBlockingQueue<WrapperEntity> queue = new LinkedBlockingQueue<>(CommonConfig.getMessageQueueSize());
//    private final Router kafkaCacheRouter = new Router(CommonConfig.getKafkaCacheTopic(), CommonConfig.getKafkaCacheTopic(), CommonConfig.getKafkaNodesNum());


    @PostConstruct
    public void init() {
        for (int i = 0; i < CommonConfig.getForwardThreadSize(); i++) {
            Thread thread = new Thread(this::forwardData, "ForwardThread" + i);
            thread.start();
        }
    }

    @Scheduled(fixedDelay = 60_000)
    public void monitorCacheQueueSize() {
        logger.info("Cache queue size : {}", queue.size());
    }

    private void forwardData() {
        while (true) {
            try {
                WrapperEntity msg = queue.take();
                String type = msg.getType();
                if (!serviceManager.routerMap.containsKey(type)) {
                    ServiceManger.serviceMonitor.saveException(1, "Message Without Router, type:  " + type, String.format("Message value: %s", msg.toString()));
                    continue;
                }
                Router router = serviceManager.routerMap.get(type);
                ServiceManger.serviceMonitor.addReceiveCount(router.instanceId, 1);
                kafkaService.forward(msg, router);
            } catch (Exception e) {
                ServiceManger.serviceMonitor.saveException(2, "Forward Data Error: " + DateUtils.getCurrentDateStr(), e);
            }
        }
    }

    private void processDataAsync(WrapperEntity data) throws CustomException {
        //没有type的message直接丢弃
        if (StringUtils.isEmpty(data.getType()))
            return;
        if (!queue.offer(data)) {
            throw new CustomException(Constants.CustomCode.QUEUE_FULL_ERROR.getValue(), String.format("Pushing data while Queue is full. queue size : {%s}", queue.size()));
        }
    }

    public void processDataAsync(String value, String ip, Class<?> clazz) throws Exception {
        List<WrapperEntity> dataList;
        //兼容metric-receiver的接口
        if (clazz != null && clazz.getName().contains("MetricMessage")) {
            List<MetricMessage> metricMessageList = JsonUtils.objectMapper.readValue(value, new TypeReference<List<MetricMessage>>() {
            });
            dataList = new LinkedList<>();
            for (MetricMessage metricMessage : metricMessageList) {
                dataList.add(new WrapperEntity(metricMessage.getName(), metricMessage.getFieldsMap()));
            }
        } else {
            dataList = JsonUtils.objectMapper.readValue(value, new TypeReference<List<WrapperEntity>>() {
            });
        }
        for (WrapperEntity e : dataList) {
            e.getObject().put("preset_custom_ip", ip);
            processDataAsync(e);
        }
    }
}
