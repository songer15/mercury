package com.valor.mercury.dispatcher.service;


import com.mfc.config.ConfigTools3;
import com.valor.mercury.common.client.KafkaProducerClient;
import com.valor.mercury.common.client.ServiceMonitor;
import com.valor.mercury.common.model.Router;
import com.valor.mercury.common.util.JsonUtils;
import org.apache.kafka.clients.CommonClientConfigs;
import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringSerializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PreDestroy;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class KafkaService extends RouteService {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    private KafkaProducerClient kafkaProducerClient;
    private Map<String, String> kafkaTopicSet = new ConcurrentHashMap<>();

    public KafkaService()  {
        dest = Router.Dest.KAFKA;
        routeThreads = new RouteThread[ConfigTools3.getConfigAsInt("thread.kafka.size",1)];
        kafkaProducerClient = new KafkaProducerClient(getAdminClient(), getKafkaProducer());
        updateTopicList();
        super.startRouteThread();
    }

    @Override
    public boolean route(List<Map<String, Object>> list, Router router) {
        try {
            createTopicIfAbsent(router);
            for (Map<String, Object> map : list) {
                kafkaProducerClient.sendAsync(JsonUtils.toJsonString(map), router.topicName);
            }
            return true;
        } catch (Exception e) {
            ServiceMonitor.getDefault().saveException(1, String.format("type: %s route error", router.type), e);
            return false;
        }
    }

    /**
     * 创建topic
     */
    public void createTopicIfAbsent(Router router) {
        String topic = router.topicName;
        if (!kafkaTopicSet.containsKey(topic)) {
            try {
                kafkaProducerClient.createTopic(topic, router.partitions, (short) (router.replications), router.retentionMillis, router.retentionBytes);
                logger.info("[KAFKA]Created topic:{}", topic);
            } catch (Exception ex) {
                ConfigManager.serviceMonitor.saveException(2, String.format("Create topic error: [%s]", topic), ex);
            }
            updateTopicList();
        }
    }

    private void updateTopicList()  {
        try {
            Set<String> set =  kafkaProducerClient.listAllTopics();
            if (set != null && !set.isEmpty()) {
                for (String topic : set) {
                    kafkaTopicSet.put(topic, "");
                }
            }
        } catch (Exception ex) {
            logger.error("kafka connection fails", ex);
        }
    }

    private AdminClient getAdminClient() {
        Properties props = new Properties();
        props.put(CommonClientConfigs.BOOTSTRAP_SERVERS_CONFIG, ConfigTools3.getAsList( "kafka.producer.bootstrap.servers", ","));
        return AdminClient.create(props);
    }

    private KafkaProducer<String, String> getKafkaProducer() {
        Map<String, Object> props = new HashMap<>();
        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, ConfigTools3.getAsList( "kafka.producer.bootstrap.servers", ","));
        props.put(ProducerConfig.ACKS_CONFIG, ConfigTools3.getConfigAsString( "kafka.producer.acks", "all"));
        props.put(ProducerConfig.BATCH_SIZE_CONFIG, ConfigTools3.getConfigAsInt("kafka.producer.batch.size", 100));
        props.put(ProducerConfig.REQUEST_TIMEOUT_MS_CONFIG, ConfigTools3.getConfigAsInt("kafka.producer.timeout", 120000));
        props.put(ProducerConfig.BUFFER_MEMORY_CONFIG, ConfigTools3.getConfigAsInt("kafka.producer.buffer.memory", 33554432));
        props.put(ProducerConfig.COMPRESSION_TYPE_CONFIG, ConfigTools3.getConfigAsString("kafka.producer.compression.type", "none"));
        props.put(ProducerConfig.RETRIES_CONFIG, ConfigTools3.getConfigAsInt("kafka.producer.retries", 5));
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        return new KafkaProducer<>(props);
    }

    @PreDestroy
    private void destroy() {
        kafkaProducerClient.closeAdminClient();
    }
}
