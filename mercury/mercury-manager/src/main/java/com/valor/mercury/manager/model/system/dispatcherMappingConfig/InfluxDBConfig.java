package com.valor.mercury.manager.model.system.dispatcherMappingConfig;

/**
 * @author Gavin
 * 2020/8/18 14:47
 */
public class InfluxDBConfig {
    private String database;
    private String measurement;
    private String tags;
    private String retentionPolicy;

    public InfluxDBConfig() {
    }

    public InfluxDBConfig(String database, String measurement, String tags, String retentionPolicy) {
        this.database = database;
        this.measurement = measurement;
        this.tags = tags;
        this.retentionPolicy = retentionPolicy;
    }

    public String getDatabase() {
        return database;
    }

    public void setDatabase(String database) {
        this.database = database;
    }

    public String getMeasurement() {
        return measurement;
    }

    public void setMeasurement(String measurement) {
        this.measurement = measurement;
    }

    public String getTags() {
        return tags;
    }

    public void setTags(String tags) {
        this.tags = tags;
    }

    public String getRetentionPolicy() {
        return retentionPolicy;
    }

    public void setRetentionPolicy(String retentionPolicy) {
        this.retentionPolicy = retentionPolicy;
    }

    @Override
    public String toString() {
        return "InfluxDBConfig{" +
                "database='" + database + '\'' +
                ", measurement='" + measurement + '\'' +
                ", tags='" + tags + '\'' +
                ", retentionPolicy='" + retentionPolicy + '\'' +
                '}';
    }
}
