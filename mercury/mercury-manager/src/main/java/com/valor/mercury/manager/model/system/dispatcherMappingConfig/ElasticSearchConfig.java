package com.valor.mercury.manager.model.system.dispatcherMappingConfig;

/**
 * @author Gavin
 * 2020/8/18 14:47
 */
public class ElasticSearchConfig {
    private String indexName;      //索引名
    private Integer shardNum;       //分片数
    private Integer replicaNum;     //副本数
    private String indexMapping;    //索引mapping表
    private String idField;        //ID字段
    private String indexNameStrategy; //索引命名策略

    public ElasticSearchConfig() {
    }

    public ElasticSearchConfig(String indexName, Integer shardNum, Integer replicaNum, String indexMapping, String idField, String indexNameStrategy) {
        this.indexName = indexName;
        this.shardNum = shardNum;
        this.replicaNum = replicaNum;
        this.indexMapping = indexMapping;
        this.idField = idField;
        this.indexNameStrategy = indexNameStrategy;
    }

    @Override
    public String toString() {
        return "ElasticSearchConfig{" +
                "indexName='" + indexName + '\'' +
                ", shardNum=" + shardNum +
                ", replicaNum=" + replicaNum +
                ", indexMapping='" + indexMapping + '\'' +
                ", idField='" + idField + '\'' +
                ", indexNameStrategy='" + indexNameStrategy + '\'' +
                '}';
    }

    public String getIndexName() {
        return indexName;
    }

    public void setIndexName(String indexName) {
        this.indexName = indexName;
    }

    public Integer getShardNum() {
        return shardNum;
    }

    public void setShardNum(Integer shardNum) {
        this.shardNum = shardNum;
    }

    public Integer getReplicaNum() {
        return replicaNum;
    }

    public void setReplicaNum(Integer replicaNum) {
        this.replicaNum = replicaNum;
    }

    public String getIndexMapping() {
        return indexMapping;
    }

    public void setIndexMapping(String indexMapping) {
        this.indexMapping = indexMapping;
    }

    public String getIdField() {
        return idField;
    }

    public void setIdField(String idField) {
        this.idField = idField;
    }

    public String getIndexNameStrategy() {
        return indexNameStrategy;
    }

    public void setIndexNameStrategy(String indexNameStrategy) {
        this.indexNameStrategy = indexNameStrategy;
    }
}
