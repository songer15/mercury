package com.valor.mercury.common.client;

import com.google.common.collect.Lists;
import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.clients.admin.NewTopic;
import org.apache.kafka.clients.producer.Callback;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.springframework.util.concurrent.FailureCallback;
import org.springframework.util.concurrent.ListenableFuture;
import org.springframework.util.concurrent.SuccessCallback;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutionException;

import static org.apache.kafka.common.config.TopicConfig.RETENTION_BYTES_CONFIG;
import static org.apache.kafka.common.config.TopicConfig.RETENTION_MS_CONFIG;

public class KafkaProducerClient {
    private AdminClient adminClient;
    private KafkaProducer<String, String> kafkaProducer;

    public KafkaProducerClient(AdminClient adminClient, KafkaProducer<String, String> kafkaProducer) {
        this.adminClient = adminClient;
        this.kafkaProducer = kafkaProducer;
    }

    public void sendAsyncWithCallback(String data, String topic, Callback callback) { kafkaProducer.send(new ProducerRecord<>(topic, data), callback); }

    public void sendAsync(String data, String topic) {
        kafkaProducer.send(new ProducerRecord<>(topic, data));
    }

    public void createTopic(String topic, int partition, short replicaFactors, long retentionMillis, long retentionBytes) throws ExecutionException, InterruptedException {
        NewTopic newTopic = new NewTopic(topic, partition, replicaFactors);
        Map<String, String> prop = new HashMap<>();
        prop.put(RETENTION_MS_CONFIG, String.valueOf(retentionMillis));
        prop.put(RETENTION_BYTES_CONFIG, String.valueOf(retentionBytes));
        newTopic.configs(prop);
        adminClient.createTopics(Lists.newArrayList(newTopic)).all().get();
    }

    public Set<String> listAllTopics() throws ExecutionException, InterruptedException {
        return adminClient.listTopics().names().get();
    }
    public void closeAdminClient() {
        adminClient.close();
    }
}
