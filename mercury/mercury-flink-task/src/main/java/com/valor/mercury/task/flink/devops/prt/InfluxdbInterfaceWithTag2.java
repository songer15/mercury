package com.valor.mercury.task.flink.devops.prt;

import org.apache.flink.configuration.Configuration;
import org.apache.flink.streaming.api.functions.sink.RichSinkFunction;
import org.influxdb.InfluxDB;
import org.influxdb.InfluxDBFactory;
import org.influxdb.dto.Point;

import java.util.HashMap;
import java.util.Map;


public class InfluxdbInterfaceWithTag2 extends RichSinkFunction<PrtActionModel> {


    private InfluxDB influxDB;
    private String tableName;
    private String dataBaseName;
    private String retentionPolicy;
    private String url;


    public InfluxdbInterfaceWithTag2(String tableName, String dataBaseName, String retentionPolicy, String url) {
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
    public void invoke(PrtActionModel model, Context context) throws Exception {
        Map<String, String> key = new HashMap<>();
        Map<String, Object> value = new HashMap<>();
        key.put("channelID", model.getChannelId());
        key.put("seedId", model.getSeedId());
        value.put("actionTime", model.getActionTime());
        value.put("minSendTs", model.getMinSendTs());
        value.put("minReceivePeerTs", model.getMinReceivePeerTs());
        value.put("minReceivePrtTs", model.getMinReceivePrtTs());
        value.put("minReceiveRetryPiece", model.getMinReceiveRetryPiece());
        value.put("minReceivePiece", model.getMinReceivePiece());
        value.put("connCount", model.getConnCount());
        value.put("minSendFlow", model.getMinSendFlow());
        influxDB.write(dataBaseName, "".equals(retentionPolicy) ? null : retentionPolicy, Point.measurement(tableName).fields(value).tag(key).build());
    }


    @Override
    public void close() throws Exception {
        super.close();
    }

}
