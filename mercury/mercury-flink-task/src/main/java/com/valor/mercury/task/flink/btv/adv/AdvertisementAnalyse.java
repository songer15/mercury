package com.valor.mercury.task.flink.btv.adv;

import com.valor.mercury.task.flink.MetricAnalyse;
import org.apache.flink.api.common.functions.RuntimeContext;
import org.apache.flink.api.common.typeinfo.TypeInformation;
import org.apache.flink.shaded.jackson2.com.fasterxml.jackson.databind.JsonNode;
import org.apache.flink.shaded.jackson2.com.fasterxml.jackson.databind.node.ObjectNode;
import org.apache.flink.streaming.api.TimeCharacteristic;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.connectors.elasticsearch.ElasticsearchSinkFunction;
import org.apache.flink.streaming.connectors.elasticsearch.RequestIndexer;
import org.apache.flink.streaming.connectors.elasticsearch6.ElasticsearchSink;
import org.apache.flink.streaming.connectors.kafka.FlinkKafkaConsumer;
import org.apache.flink.streaming.util.serialization.JSONKeyValueDeserializationSchema;
import org.apache.flink.table.api.Table;
import org.apache.flink.table.api.TableEnvironment;
import org.apache.flink.table.api.java.StreamTableEnvironment;
import org.apache.flink.types.Row;
import org.apache.http.HttpHost;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.client.Requests;

import java.util.*;

import static com.valor.mercury.task.flink.util.Constants.bootstrapServer;
import static com.valor.mercury.task.flink.util.Constants.esHost;

/**
 * @author Gavin.hu
 * 2019/4/18
 * 统计广告信息
 **/

public class AdvertisementAnalyse implements MetricAnalyse {

    @Override
    public void run(String[] args) throws Exception {
        String groupID = "flink_adv_metric";
        String topicName = "Ads_Function";
        String esIndexName = "flink_adv_metric";

        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
        env.setStreamTimeCharacteristic(TimeCharacteristic.ProcessingTime);
//        env.setStateBackend(new FsStateBackend(checkPointDataUrl));
        env.setParallelism(1);
//        env.enableCheckpointing(180_000, CheckpointingMode.EXACTLY_ONCE);
        StreamTableEnvironment tableEnv = TableEnvironment.getTableEnvironment(env);

        // 配置kafka数据源
        Properties properties = new Properties();
        properties.setProperty("bootstrap.servers", bootstrapServer);
        properties.setProperty("group.id", groupID);
        FlinkKafkaConsumer<ObjectNode> kafkaConsumer =
                new FlinkKafkaConsumer<>(topicName, new JSONKeyValueDeserializationSchema(false), properties);
        kafkaConsumer.setStartFromLatest();  // 设置读取最新的数据

        // 拿到ObjectNode数据
        DataStream<ObjectNode> advDataStream = env.addSource(kafkaConsumer);

        // 格式化数据
        DataStream<AdvModel> advDataModels = advDataStream
                .map((ObjectNode objectNode) -> {
                    JsonNode jsonNode = objectNode.get("value");
                    AdvModel model = new AdvModel();
                    model.setLoginType(jsonNode.get("loginType") == null ? "null" : jsonNode.get("loginType").asText());
                    model.setDevice(jsonNode.get("device") == null ? "null" : jsonNode.get("device").asText("null"));
                    model.setUid(jsonNode.get("uid") == null ? "null" : jsonNode.get("uid").asText("null"));
                    model.setDid(jsonNode.get("did") == null ? "null" : jsonNode.get("did").asText("null"));
                    model.setClientIp(jsonNode.get("metric_client_ip") == null ? "null" : jsonNode.get("metric_client_ip").asText("null"));
                    model.setAppVersion(jsonNode.get("appVersion") == null ? "null" : jsonNode.get("appVersion").asText("null"));
                    model.setReleaseId(jsonNode.get("releaseId") == null ? "null" : jsonNode.get("releaseId").asText("null"));
                    model.setActionTime(jsonNode.get("actionDate") == null ? 0 : jsonNode.get("actionDate").asLong(0));
                    model.setUserStatus(jsonNode.get("userStatus") == null ? "null" : jsonNode.get("userStatus").asText("null"));
                    model.setAdvPlacement(jsonNode.get("placement").asInt(1));
                    model.setBrowseCount(jsonNode.get("browseCount").asDouble(1));
                    model.setClickCount(jsonNode.get("clickCount").asDouble(1));
                    model.setDuration(jsonNode.get("duration") == null ? 0 : jsonNode.get("duration").asLong(0));
                    model.setCountryCode(jsonNode.get("CountryCode") == null ? "null" : jsonNode.get("CountryCode").asText("null"));
                    return model;
                }).returns(TypeInformation.of(AdvModel.class));

        //注册表
        tableEnv.registerDataStream("adv_table", advDataModels,
                Arrays.stream(AdvModel.class.getDeclaredFields())
                        .collect(StringBuilder::new, (sb, v) -> sb.append(v.getName()).append(","), StringBuilder::append)
                        .append("proctime.proctime").toString());

        //查询数据表
        String sql = "SELECT releaseId,device,countryCode,advPlacement," +
                "sum(browseCount),sum(clickCount),avg(clickCount/browseCount),min(actionTime) " +
                "FROM adv_table " +
                "GROUP BY TUMBLE(proctime,INTERVAL '60' SECOND),releaseId,device,countryCode,advPlacement";
        Table tableResult = tableEnv.sqlQuery(sql);
        DataStream<Row> tableResultRows = tableEnv.toAppendStream(tableResult, Row.class);

        //写入elasticsearch
        List<HttpHost> httpHosts = new ArrayList<>();
        httpHosts.add(new HttpHost(esHost.split(",")[0], 9200, "http"));
        httpHosts.add(new HttpHost(esHost.split(",")[1], 9200, "http"));
        ElasticsearchSink.Builder<Row> esSinkBuilder = new ElasticsearchSink.Builder<>(httpHosts,
                new ElasticsearchSinkFunction<Row>() {
                    private IndexRequest createIndexRequest(Row element) {
                        Map<String, Object> json = new HashMap<>();
                        json.put("releaseId", element.getField(0));
                        json.put("device", element.getField(1));
                        json.put("countryCode", element.getField(2));
                        json.put("advPlacement", element.getField(3));
                        json.put("browseCount", element.getField(4));
                        json.put("clickCount", element.getField(5));
                        json.put("clickRate", ((double) element.getField(6)) * 100);
                        json.put("actionTime", new Date((long) element.getField(7)));
                        json.put("createTime", new Date());
                        return Requests.indexRequest()
                                .index(esIndexName)
                                .type("_doc")
                                .source(json);
                    }

                    @Override
                    public void process(Row element, RuntimeContext ctx, RequestIndexer indexer) {
                        indexer.add(createIndexRequest(element));
                    }
                });
        esSinkBuilder.setBulkFlushMaxActions(1);
        tableResultRows.addSink(esSinkBuilder.build());

        env.execute("AdvertisementAnalyse");
    }
}
