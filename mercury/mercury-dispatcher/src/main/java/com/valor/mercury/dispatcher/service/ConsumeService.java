package com.valor.mercury.dispatcher.service;

import com.mfc.config.ConfigTools3;
import com.valor.mercury.common.model.WrapperEntity;
import com.valor.mercury.common.model.Router;
import org.apache.commons.lang3.StringUtils;
import org.apache.kafka.clients.consumer.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.time.Duration;
import java.util.Collections;
import java.util.Map;
import java.util.Properties;


/**
 * 消费缓冲topic里的数据，写入对应的地方
 */
@Service
public class ConsumeService {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    private EsService esService;
    private InfluxDBService influxDBService;
    private KafkaService kafkaService;
    private HDFSService hdfsService;
    private Consumer<String, WrapperEntity> consumer;

    @PostConstruct
    private void init() {
        try {
            String cacheTopicName = ConfigTools3.getConfigAsString("cache.topic.name");
            if (StringUtils.isEmpty(cacheTopicName))
                throw new IllegalArgumentException("cache topic name is empty");
            Properties props = new Properties();
            props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, ConfigTools3.getAsList( "cache.bootstrap.servers", ","));
            props.put(ConsumerConfig.GROUP_ID_CONFIG,  ConfigTools3.getConfigAsString("cache.group.id", "mercury-dispatcher"));
            props.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, "true");
            props.put(ConsumerConfig.AUTO_COMMIT_INTERVAL_MS_CONFIG, "1000");
            props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "latest");
            props.put(ConsumerConfig.MAX_POLL_RECORDS_CONFIG, "2000");
            props.put(ConsumerConfig.SESSION_TIMEOUT_MS_CONFIG, "30000");
            props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, "org.apache.kafka.common.serialization.StringDeserializer");
            props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, "com.valor.mercury.dispatcher.serializer.WrapperEntityDeserializer");
            consumer = new KafkaConsumer<>(props);
            consumer.subscribe(Collections.singleton(cacheTopicName));
            new Thread(this::consume, "ConsumeThread").start();
            influxDBService = new InfluxDBService();
            esService = new EsService();
            kafkaService = new KafkaService();
            hdfsService = new HDFSService();
            logger.info("All Service Started");
        } catch (Throwable ex) {
            logger.error("init error:", ex);
            System.exit(1);
        }
    }

    /**
     * 所有服务实例并行消费缓存在kafka中的数据，保持同一个group_id, 把数据放进对应的RouteService中等待发送。
     */
    private void consume() {
        while (true) {
            ConsumerRecords<String, WrapperEntity> consumerRecords = consumer.poll(Duration.ofSeconds(10));
            for (ConsumerRecord<String, WrapperEntity> record : consumerRecords) {
                WrapperEntity value = record.value();
                String type = value.getType();
                Map<Router.Dest, Router> routers = ConfigManager.routerMap.get(type);
                Map<String, Object> object = value.getObject();
                if (routers != null && object != null) {
                    for (Router router : routers.values()) {
                        RouteService routeService = getRouteService(router.dest);
                        if (routeService != null) {
                            routeService.collectData(object, router);
                        }
                    }
                }
            }
//            logger.info("consumed {} message", consumerRecords.count());
        }
    }

    private RouteService getRouteService(Router.Dest destination) {
        switch (destination) {
            case ES: {
                return esService;
            }
            case KAFKA:{
               return kafkaService;
            }
            case INFLUXDB: {
                return influxDBService;
            }
            case HDFS: {
                return hdfsService;
            }
            default: return null;
        }
    }

}
