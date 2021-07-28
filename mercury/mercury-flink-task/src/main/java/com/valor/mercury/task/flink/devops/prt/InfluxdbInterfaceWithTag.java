package com.valor.mercury.task.flink.devops.prt;

import org.apache.flink.configuration.Configuration;
import org.apache.flink.streaming.api.functions.sink.RichSinkFunction;
import org.apache.flink.types.Row;
import org.influxdb.InfluxDB;
import org.influxdb.InfluxDBFactory;
import org.influxdb.dto.Point;

import java.util.HashMap;
import java.util.Map;


public class InfluxdbInterfaceWithTag extends RichSinkFunction<Row> {


    private InfluxDB influxDB;
    private String tableName;
    private String dataBaseName;
    private String retentionPolicy;
    private String url;


    public InfluxdbInterfaceWithTag(String tableName, String dataBaseName, String retentionPolicy, String url) {
        super();
        this.retentionPolicy = retentionPolicy;
        this.dataBaseName = dataBaseName;
        this.tableName = tableName;
        this.url = url;

    }

    @Override
    public void open(Configuration configuration) throws Exception {
        super.open(configuration);
        influxDB = InfluxDBFactory.connect(url);
    }

    @Override
    public void invoke(Row element, Context context) throws Exception {
        Map<String, String> key = new HashMap<>();
        Map<String, Object> value = new HashMap<>();
        key.put("channelID", element.getField(0).toString());
        key.put("seedId", element.getField(1).toString());
        value.put("actionTime", element.getField(2));
        value.put("shareRatio", Double.parseDouble(element.getField(3).toString()));
        value.put("lostRatio", Double.parseDouble(element.getField(4).toString()));
        value.put("sumShareRatio", Double.parseDouble(element.getField(5).toString()));
        value.put("sumLostRatio", Double.parseDouble(element.getField(6).toString()));
        value.put("connCount", Double.parseDouble(element.getField(7).toString()));
        influxDB.write(dataBaseName, "".equals(retentionPolicy) ? null : retentionPolicy, Point.measurement(tableName).fields(value).tag(key).build());
    }


    @Override
    public void close() throws Exception {
        super.close();
    }

}
