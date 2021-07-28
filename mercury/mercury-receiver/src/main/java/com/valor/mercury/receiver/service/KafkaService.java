package com.valor.mercury.receiver.service;


import com.mfc.config.ConfigTools3;
import com.valor.mercury.common.client.KafkaProducerClient;
import com.valor.mercury.common.model.WrapperEntity;
import com.valor.mercury.receiver.config.CommonConfig;
import com.valor.mercury.common.model.Router;
import com.valor.mercury.common.util.JsonUtils;
import org.apache.kafka.clients.CommonClientConfigs;
import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.clients.producer.Callback;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.apache.kafka.common.serialization.StringSerializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class KafkaService {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    private KafkaProducerClient kafkaProducerClient;
//    private Map<String, String> kafkaTopicSet = new ConcurrentHashMap();

    @PostConstruct
    protected void init()  {
        kafkaProducerClient = new KafkaProducerClient(getAdminClient(), getKafkaProducer());
//        logger.info("[{}] initialized.Total topic [{}]", this.getClass().getSimpleName(),kafkaTopicSet.size());
    }

    /**
     * 缓存消息到kafka的中间topic
     */
    public void forward(WrapperEntity data, Router router) {
        Callback callback  = new Callback() {
            @Override
            public void onCompletion(RecordMetadata metadata, Exception exception) {
                if (exception == null) {
                    ServiceManger.serviceMonitor.addForwardCount(router.instanceId, 1);
                }
            }
        };
        kafkaProducerClient.sendAsyncWithCallback(JsonUtils.toJsonString(data),
                router.topicName,
                callback);
    }

    private AdminClient getAdminClient() {
        Properties props = new Properties();
        props.put(CommonClientConfigs.BOOTSTRAP_SERVERS_CONFIG, ConfigTools3.getAsList( "cache.bootstrap.servers", ","));
        return AdminClient.create(props);
    }

    private KafkaProducer<String, String> getKafkaProducer() {
        Map<String, Object> props = new HashMap<>();
        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, ConfigTools3.getAsList( "cache.bootstrap.servers", ","));
        props.put(ProducerConfig.ACKS_CONFIG,  ConfigTools3.getConfigAsString( "cache.producer.acks", "all"));
        props.put(ProducerConfig.BATCH_SIZE_CONFIG, ConfigTools3.getConfigAsInt("cache.producer.batch.size", 100));
        props.put(ProducerConfig.REQUEST_TIMEOUT_MS_CONFIG, ConfigTools3.getConfigAsInt("cache.producer.timeout", 120000));
        props.put(ProducerConfig.BUFFER_MEMORY_CONFIG, ConfigTools3.getConfigAsInt("cache.producer.buffer.memory", 33554432));
        props.put(ProducerConfig.COMPRESSION_TYPE_CONFIG, ConfigTools3.getConfigAsString("cache.producer.compression.type", "none"));
        props.put(ProducerConfig.RETRIES_CONFIG, ConfigTools3.getConfigAsInt("cache.producer.retries", 5));
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        return new KafkaProducer<>(props);
    }

    @PreDestroy
    private void destroy() {
        kafkaProducerClient.closeAdminClient();
    }
}
