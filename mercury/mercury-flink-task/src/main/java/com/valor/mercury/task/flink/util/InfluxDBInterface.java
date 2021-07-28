package com.valor.mercury.task.flink.util;

import org.apache.flink.api.java.tuple.Tuple2;
import org.apache.flink.configuration.Configuration;
import org.apache.flink.streaming.api.functions.sink.RichSinkFunction;
import org.influxdb.InfluxDB;
import org.influxdb.InfluxDBFactory;
import org.influxdb.dto.Point;

import java.io.Serializable;
import java.util.Map;


public class InfluxDBInterface extends RichSinkFunction<Tuple2<Map<String, Object>, Map<String, String>>> implements Serializable {

    private static final long serialVersionUID = 1L;
    private InfluxDB influxDB;
    private String tableName;
    private String dataBaseName;
    private String retentionPolicy;
    private String url;

    public InfluxDBInterface(String tableName, String dataBaseName, String retentionPolicy, String url) {
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
    public void invoke(Tuple2<Map<String, Object>, Map<String, String>> value, Context context) throws Exception {
        try {
            influxDB.write(dataBaseName, "".equals(retentionPolicy) ? null : retentionPolicy,
                    Point.measurement(tableName).fields(value.f0).tag(value.f1).build());
        } catch (Exception e) {
            System.out.println("influxDB error,current Time:" + System.currentTimeMillis());
            try {
                influxDB = InfluxDBFactory.connect(url);
            } catch (Exception e1) {
                System.out.println("influxDB retry error");
            }
        }
    }


    @Override
    public void close() throws Exception {
        super.close();
    }

}
