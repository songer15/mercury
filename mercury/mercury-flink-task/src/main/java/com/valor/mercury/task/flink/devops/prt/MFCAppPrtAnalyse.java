package com.valor.mercury.task.flink.devops.prt;

import com.valor.mercury.task.flink.MetricAnalyse;
import org.apache.flink.api.common.typeinfo.TypeInformation;
import org.apache.flink.shaded.jackson2.com.fasterxml.jackson.databind.JsonNode;
import org.apache.flink.shaded.jackson2.com.fasterxml.jackson.databind.node.ObjectNode;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.connectors.kafka.FlinkKafkaConsumer;
import org.apache.flink.streaming.util.serialization.JSONKeyValueDeserializationSchema;
import org.apache.flink.table.api.TableEnvironment;
import org.apache.flink.table.api.java.StreamTableEnvironment;
import org.apache.flink.util.Collector;

import java.util.Properties;

import static com.valor.mercury.task.flink.util.Constants.*;

/**
 * @author Gavin
 * 2020/3/9 14:06
 * prt网络监控分析
 */
public class MFCAppPrtAnalyse implements MetricAnalyse {

    @Override
    public void run(String[] args) throws Exception {
        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
        StreamTableEnvironment tableEnv = TableEnvironment.getTableEnvironment(env);

        // 配置kafka数据源
        Properties properties = new Properties();
        properties.setProperty("bootstrap.servers", bootstrapServer);
        properties.setProperty("group.id", groupID);
        FlinkKafkaConsumer<ObjectNode> kafkaConsumer =
                new FlinkKafkaConsumer<>(mfcPrtTopicName, new JSONKeyValueDeserializationSchema(false), properties);
        if (args.length > 1 && args[1] != null) {
            try {
                //从指定的时间戳开始
                kafkaConsumer.setStartFromTimestamp(Long.parseLong(args[1]));
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else
            kafkaConsumer.setStartFromLatest();  // 设置读取最新的数据


        // 拿到kafka的数据
        DataStream<ObjectNode> advDataStream = env.addSource(kafkaConsumer);
        // 清理不合规的数据并格式化
        DataStream<PrtActionModel> prtActionModelDataStream = advDataStream
                .flatMap((ObjectNode value, Collector<PrtActionModel> out) -> {
                    JsonNode jsonNode = value.get("value");
                    try {
                        PrtActionModel model = new PrtActionModel();
                        model.setMinReceivePeerTs(jsonNode.get("minReceivePeerTs").asDouble());
                        model.setConnCount(jsonNode.get("connCount").asDouble());
                        model.setMinSendTs(jsonNode.get("minSendTs").asDouble());
                        model.setMinReceivePrtTs(jsonNode.get("minReceivePrtTs").asDouble());
                        model.setSeedId(jsonNode.get("seedId").asText());
                        model.setMinReceivePiece(jsonNode.get("minReceivePiece").asDouble());
                        model.setActionTime(jsonNode.get("time").asLong());
                        model.setMinSendTime(jsonNode.get("minSendTime").asDouble());
                        model.setMinReceiveRetryPiece(jsonNode.get("minReceiveRetryPiece").asDouble());
                        model.setClientIp(jsonNode.get("preset_custom_ip").asText());
                        model.setProductType(jsonNode.get("productType").asDouble());
                        model.setChannelId(jsonNode.get("channelId").asText());
                        model.setMinSendFlow(jsonNode.has("minSendFlow") ?
                                jsonNode.get("minSendFlow").asDouble() : 0.0);
//                        System.out.println(model.toString());
                        out.collect(model);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }).returns(TypeInformation.of(PrtActionModel.class));
        prtActionModelDataStream.
                addSink(new InfluxdbInterfaceWithTag2(mfcPrtEsIndexName, "prt_all", "", influxDbUrl));
//
//        tableEnv.registerDataStream(mfcPrtTableName, prtActionModelDataStream,
//                Arrays.stream(PrtActionModel.class.getDeclaredFields())
//                        .collect(StringBuilder::new, (sb, v) -> sb.append(v.getName()).append(","), StringBuilder::append)
//                        .append("proctime.proctime").toString());
//        String sql_channel = "SELECT channelId,seedId,min(actionTime)," +
//                " CAST(max(minSendTs/(minReceivePeerTs+minReceivePrtTs)) AS DECIMAL (18, 2))," +
//                " CAST(max(minReceiveRetryPiece/(minReceiveRetryPiece+minReceivePiece)) AS DECIMAL (18, 2))," +
//                " CAST(sum(minSendTs)/sum(minReceivePeerTs + minReceivePrtTs) AS DECIMAL (18, 2))," +
//                " CAST(sum(minReceiveRetryPiece)/sum(minReceiveRetryPiece + minReceivePiece) AS DECIMAL (18, 2))," +
//                " max(connCount)" +
//                " FROM " + mfcPrtTableName +
//                " GROUP BY TUMBLE(proctime,INTERVAL '1' MINUTE),channelId,seedId";
//        DataStream<Row> tableResultRows = tableEnv.toAppendStream(tableEnv.sqlQuery(sql_channel), Row.class);
//        //写入ElasticSearch
//        List<HttpHost> httpHosts = Arrays.stream(esHost.split(","))
//                .map(v -> new HttpHost(v, 9200, "http"))
//                .collect(ArrayList::new, List::add, List::addAll);
//        ElasticsearchSink.Builder<Row> esSinkBuilder = new ElasticsearchSink.Builder<>(httpHosts, new PrtElasticSearchSink(mfcPrtEsIndexName));
//        esSinkBuilder.setBulkFlushMaxActions(20);
//        esSinkBuilder.setBulkFlushInterval(60_000);
//        InfluxdbInterfaceWithTag influxdbInterfaceWithTag =
//                new InfluxdbInterfaceWithTag(mfcPrtEsIndexName, "prt_all", "", influxDbUrl);
//        tableResultRows.addSink(influxdbInterfaceWithTag);
//
//        String sql_seed = "SELECT seedId,min(actionTime),CAST(sum(minSendTs)/sum(minReceivePeerTs + minReceivePrtTs) AS DECIMAL (18, 2))," +
//                " CAST(sum(minReceiveRetryPiece) / sum(minReceiveRetryPiece + minReceivePiece) AS DECIMAL (18, 2)),'seedId'" +
//                " ,max(connCount)" +
//                " FROM " + mfcPrtTableName +
//                " GROUP BY TUMBLE(proctime,INTERVAL '1' MINUTE), seedId";
//        DataStream<Row> tableResultRows2 = tableEnv.toAppendStream(tableEnv.sqlQuery(sql_seed), Row.class);
//        tableResultRows2.addSink(influxdbInterfaceWithTag);

        env.execute("MFCAppPrtAnalyse");
    }
}
