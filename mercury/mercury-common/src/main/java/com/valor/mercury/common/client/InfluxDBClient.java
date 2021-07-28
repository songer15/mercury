package com.valor.mercury.common.client;

import org.influxdb.InfluxDB;
import org.influxdb.InfluxDBFactory;
import org.influxdb.dto.BatchPoints;
import org.influxdb.dto.Point;
import org.influxdb.dto.Query;
import org.influxdb.dto.QueryResult;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * 为尽可能保证数据一致性，所有写请求 /write 使用 influxdbRelay分发，其他请求直接分发到各个influxdb实例
 */
public class InfluxDBClient {
    private InfluxDB influxDBRelay;
    private List<InfluxDB> influxDBCluster = new ArrayList<>();

    public void setInfluxDBRelay(String url, int actions,  int flushDuration, TimeUnit flushDurationTimeUnit) {
        influxDBRelay = InfluxDBFactory.connect(url);
        influxDBRelay.enableBatch(actions,  flushDuration, flushDurationTimeUnit);
    }

    public void setInfluxDBCluster (List<String> urls, int actions,  int flushDuration, TimeUnit flushDurationTimeUnit) {
        for (String url : urls) {
            InfluxDB influxDB = InfluxDBFactory.connect(url);
            influxDB.enableBatch(actions,  flushDuration, flushDurationTimeUnit);
            influxDBCluster.add(influxDB);
        }
    }

    public void write(List<Map<String, Object>> list, String database, String measurement, String[] tagKeys) {
        // createDatabase 的http请求路径是 /query，无法被influxRelay分发，这里直接分别发送创建请求到3台机器的influxdb(端口8086)
        for (InfluxDB influxDB : influxDBCluster) {
             influxDB.createDatabase(database);
        }
        BatchPoints batchPoints = BatchPoints.database(database).build();
        for (Map<String, Object> map : list) {
            Map<String, String> tags = new HashMap<>();
            for (String tagKey : tagKeys) {
                Object tagValue = map.remove(tagKey);
                if (tagValue != null)
                    tags.put(tagKey, tagValue.toString());
            }
            Map<String, Object> values = new HashMap<>(map);
            batchPoints.point(buildPoint(measurement, tags, values));
            influxDBRelay.write(database, "", buildPoint(measurement, tags, values));
        }
    }

    public void write(BatchPoints batchPoints){
        influxDBRelay.write(batchPoints);
    }

    public Point buildPoint(String measurement, Map<String, String> tags, Map<String, Object> fields){
        Point.Builder point = Point.measurement(measurement).tag(tags);
        for (Map.Entry<String, Object> entry: fields.entrySet()) {
            // String / num / boolean
            if(entry.getValue() instanceof String || entry.getValue() instanceof Number || entry.getValue() instanceof  Boolean)
                point.field(entry.getKey(), entry.getValue());
        }
        return point.build();
    }

    /**
     * 在数据保证数据一致性的前提下，任何一个influxdb示例返回的查询结果一致
     */
    public QueryResult query(String command, String database){
        return influxDBCluster.get(0).query(new Query(command, database));
    }

    public void createRetentionPolicy(String database){
        String command = String.format("CREATE RETENTION POLICY \"%s\" ON \"%s\" DURATION %s REPLICATION %s DEFAULT",
                "defalut", database, "30d", 1);
        query(command, database);
    }


}
