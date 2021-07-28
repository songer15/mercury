package com.valor.mercury.manager.model.system.dispatcherMappingConfig;

/**
 * @author Gavin
 * 2020/8/18 14:47
 */
public class KafkaConfig {

    private String topicName;
    private int partitions;
    private int replications = 1;
    private long retentionMillis = 604800000;
    private int retentionBytes = 1073741824;
    private int messageMaxBytes = 52428700;
    private String cleanupPolicy;

    public KafkaConfig() {
    }

    public KafkaConfig(String topicName, int partitions, int replications, long retentionMillis, int retentionBytes, int messageMaxBytes, String cleanupPolicy) {
        this.topicName = topicName;
        this.partitions = partitions;
        this.replications = replications;
        this.retentionMillis = retentionMillis;
        this.retentionBytes = retentionBytes;
        this.messageMaxBytes = messageMaxBytes;
        this.cleanupPolicy = cleanupPolicy;
    }

    @Override
    public String toString() {
        return "KafkaConfig{" +
                "topicName='" + topicName + '\'' +
                ", partitions=" + partitions +
                ", replications=" + replications +
                ", retentionMillis=" + retentionMillis +
                ", retentionBytes=" + retentionBytes +
                ", messageMaxBytes=" + messageMaxBytes +
                ", cleanupPolicy='" + cleanupPolicy + '\'' +
                '}';
    }

    public String getTopicName() {
        return topicName;
    }

    public void setTopicName(String topicName) {
        this.topicName = topicName;
    }

    public int getPartitions() {
        return partitions;
    }

    public void setPartitions(int partitions) {
        this.partitions = partitions;
    }

    public int getReplications() {
        return replications;
    }

    public void setReplications(int replications) {
        this.replications = replications;
    }

    public long getRetentionMillis() {
        return retentionMillis;
    }

    public void setRetentionMillis(long retentionMillis) {
        this.retentionMillis = retentionMillis;
    }

    public int getRetentionBytes() {
        return retentionBytes;
    }

    public void setRetentionBytes(int retentionBytes) {
        this.retentionBytes = retentionBytes;
    }

    public int getMessageMaxBytes() {
        return messageMaxBytes;
    }

    public void setMessageMaxBytes(int messageMaxBytes) {
        this.messageMaxBytes = messageMaxBytes;
    }

    public String getCleanupPolicy() {
        return cleanupPolicy;
    }

    public void setCleanupPolicy(String cleanupPolicy) {
        this.cleanupPolicy = cleanupPolicy;
    }
}
