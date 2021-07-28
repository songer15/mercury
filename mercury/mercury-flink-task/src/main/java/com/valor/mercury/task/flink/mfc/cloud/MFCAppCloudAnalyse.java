package com.valor.mercury.task.flink.mfc.cloud;

import com.valor.mercury.task.flink.MetricAnalyse;
import org.apache.flink.api.common.typeinfo.Types;
import org.apache.flink.api.java.tuple.Tuple5;
import org.apache.flink.shaded.jackson2.com.fasterxml.jackson.databind.JsonNode;
import org.apache.flink.shaded.jackson2.com.fasterxml.jackson.databind.node.ObjectNode;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.connectors.elasticsearch6.ElasticsearchSink;
import org.apache.flink.streaming.connectors.kafka.FlinkKafkaConsumer;
import org.apache.flink.streaming.util.serialization.JSONKeyValueDeserializationSchema;
import org.apache.flink.table.api.Table;
import org.apache.flink.table.api.TableEnvironment;
import org.apache.flink.table.api.java.StreamTableEnvironment;
import org.apache.flink.types.Row;
import org.apache.flink.util.Collector;
import org.apache.http.HttpHost;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;

import static com.valor.mercury.task.flink.util.Constants.*;


/**
 * @author Gavin
 * 2019/10/22 22:35
 * 统计添加网盘信息
 */
public class MFCAppCloudAnalyse implements MetricAnalyse {


    @Override
    public void run(String[] args) throws Exception {
        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
        StreamTableEnvironment tableEnv = TableEnvironment.getTableEnvironment(env);

        // 配置kafka数据源
        Properties properties = new Properties();
        properties.setProperty("bootstrap.servers", bootstrapServer);
        properties.setProperty("group.id", groupID);
        FlinkKafkaConsumer<ObjectNode> kafkaConsumer =
                new FlinkKafkaConsumer<>(mfcCloudTopicName, new JSONKeyValueDeserializationSchema(false), properties);
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
        DataStream<Tuple5<Integer, Long, Long, Long, String>> hasCleanDataStream = advDataStream
                .flatMap((ObjectNode value, Collector<Tuple5<Integer, Long, Long, Long, String>> out) -> {
                    JsonNode jsonNode = value.get("value");
                    try {
                        Tuple5<Integer, Long, Long, Long, String> tuple = new Tuple5<>();
                        tuple.f0 = jsonNode.get("account_type").asInt();
                        tuple.f1 = jsonNode.get("cid").asLong();
                        tuple.f2 = jsonNode.get("account_id").asLong();
                        tuple.f3 = jsonNode.get("create_time").asLong();
                        if (jsonNode.has("alias") && jsonNode.get("alias") != null)
                            tuple.f4 = jsonNode.get("alias").asText();
                        else
                            tuple.f4 = "private";
                        out.collect(tuple);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }).returns(Types.TUPLE(Types.INT, Types.LONG, Types.LONG, Types.LONG, Types.STRING));

        //注册表
        tableEnv.registerDataStream(mfcCloudTableName, hasCleanDataStream, "account_type,cid,account_id,create_time,alias,proctime.proctime");

        //查询数据表得到（每60分钟出一份数据）
        //添加网盘总次数
        //按照网盘类型，账号类型（邮箱，设备）区分
        String sql = "SELECT count(1),min(create_time),account_type,alias " +
                "FROM " + mfcCloudTableName +
                " GROUP BY TUMBLE(proctime,INTERVAL '60' MINUTE),account_type,alias";
        Table tableResult = tableEnv.sqlQuery(sql);
        DataStream<Row> tableResultRows = tableEnv.toAppendStream(tableResult, Row.class);
        //写入ElasticSearch
        List<HttpHost> httpHosts = Arrays.stream(esHost.split(","))
                .map(v -> new HttpHost(v, 9200, "http"))
                .collect(ArrayList::new, List::add, List::addAll);
        ElasticsearchSink.Builder<Row> esSinkBuilder = new ElasticsearchSink.Builder<>(httpHosts, new CloudElasticSearchSink(mfcCloudEsIndexName));
        esSinkBuilder.setBulkFlushMaxActions(100);
        esSinkBuilder.setBulkFlushInterval(60_000);
        tableResultRows.addSink(esSinkBuilder.build());

        env.execute("MFCAppCloudAnalyse");
    }
}
