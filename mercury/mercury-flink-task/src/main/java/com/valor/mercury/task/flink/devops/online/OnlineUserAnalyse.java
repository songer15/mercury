package com.valor.mercury.task.flink.devops.online;

import com.valor.mercury.task.flink.MetricAnalyse;
import com.valor.mercury.task.flink.util.InfluxDBInterface;
import org.apache.flink.api.common.typeinfo.TypeHint;
import org.apache.flink.api.common.typeinfo.TypeInformation;
import org.apache.flink.api.common.typeinfo.Types;
import org.apache.flink.api.java.tuple.Tuple2;
import org.apache.flink.shaded.jackson2.com.fasterxml.jackson.databind.JsonNode;
import org.apache.flink.shaded.jackson2.com.fasterxml.jackson.databind.node.ObjectNode;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.connectors.kafka.FlinkKafkaConsumer;
import org.apache.flink.streaming.util.serialization.JSONKeyValueDeserializationSchema;
import org.apache.flink.table.api.TableEnvironment;
import org.apache.flink.table.api.java.StreamTableEnvironment;
import org.apache.flink.types.Row;
import org.apache.flink.util.Collector;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import static com.valor.mercury.task.flink.util.Constants.*;

/**
 * @author Gavin
 * 2020/06/08 17:10
 * 统计在线人数
 */
public class OnlineUserAnalyse implements MetricAnalyse {


    @Override
    public void run(String[] args) throws Exception {
        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
        StreamTableEnvironment tableEnv = TableEnvironment.getTableEnvironment(env);

        // 配置kafka数据源
        Properties properties = new Properties();
        properties.setProperty("bootstrap.servers", bootstrapServer);
        properties.setProperty("group.id", groupID);
        FlinkKafkaConsumer<ObjectNode> mfcConsumer =
                new FlinkKafkaConsumer<>("mfc_online_session", new JSONKeyValueDeserializationSchema(false), properties);
        if (args.length > 1 && args[1] != null) {
            try {
                //从指定的时间戳开始
                mfcConsumer.setStartFromTimestamp(Long.parseLong(args[1]));
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else
            mfcConsumer.setStartFromLatest();  // 设置读取最新的数据


        // 拿到kafka的数据
        // 清理不合规的数据并格式化
        DataStream<Tuple2<String, String>> mfcStream = env
                .addSource(mfcConsumer)
                .flatMap((ObjectNode value, Collector<Tuple2<String, String>> out) -> {
                    JsonNode jsonNode = value.get("value");
                    try {
                        Tuple2<String, String> tuple = new Tuple2<>();
                        tuple.f0 = jsonNode.get("clientSessionId").asText();
                        tuple.f1 = jsonNode.get("accountId").textValue() + jsonNode.get("accountType").textValue();
                        out.collect(tuple);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }).returns(Types.TUPLE(Types.STRING, Types.STRING));

        //注册表
        tableEnv.registerDataStream("mfc_online_session_table", mfcStream, "clientSessionId,accountId,proctime.proctime");

        //查询数据表得到（每10分钟出一份数据）
        tableEnv.toAppendStream(tableEnv.sqlQuery("SELECT count(1),count(DISTINCT clientSessionId),count(DISTINCT accountId) " +
                "FROM " + "mfc_online_session_table" +
                " GROUP BY TUMBLE(proctime,INTERVAL '20' MINUTE)"), Row.class)
                .map(v -> {
                    Map<String, Object> value = new HashMap<>();
                    value.put("count", Long.parseLong(v.getField(0).toString()));
                    value.put("users1", Long.parseLong(v.getField(1).toString()));
                    value.put("users2", Long.parseLong(v.getField(2).toString()));
                    Map<String, String> key = new HashMap<>();
                    return new Tuple2<>(value, key);
                })
                .returns(new TypeHint<Tuple2<Map<String, Object>, Map<String, String>>>() {
                    @Override
                    public TypeInformation<Tuple2<Map<String, Object>, Map<String, String>>> getTypeInfo() {
                        return super.getTypeInfo();
                    }
                })
                .addSink(new InfluxDBInterface("mfc_online_session_table", "mfc_prt_metric", "", PMInfluxDbUrl));




        //HOT

        FlinkKafkaConsumer<ObjectNode> hotConsumer =
                new FlinkKafkaConsumer<>("hot_online_session", new JSONKeyValueDeserializationSchema(false), properties);
        if (args.length > 1 && args[1] != null) {
            try {
                //从指定的时间戳开始
                hotConsumer.setStartFromTimestamp(Long.parseLong(args[1]));
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else
            hotConsumer.setStartFromLatest();  // 设置读取最新的数据


        // 拿到kafka的数据
        // 清理不合规的数据并格式化
        DataStream<Tuple2<String, String>> hotStream =
                env.addSource(hotConsumer)
                .flatMap((ObjectNode value, Collector<Tuple2<String, String>> out) -> {
                    JsonNode jsonNode = value.get("value");
                    try {
                        Tuple2<String, String> tuple = new Tuple2<>();
                        tuple.f0 = jsonNode.get("clientSessionId").asText();
                        tuple.f1 = jsonNode.get("accountId").textValue() + jsonNode.get("accountType").textValue();
                        out.collect(tuple);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }).returns(Types.TUPLE(Types.STRING, Types.STRING));

        //注册表
        tableEnv.registerDataStream("hot_online_session_table", hotStream, "clientSessionId,accountId,proctime.proctime");

        //查询数据表得到（每10分钟出一份数据）
        tableEnv.toAppendStream(tableEnv.sqlQuery("SELECT count(1),count(DISTINCT clientSessionId),count(DISTINCT accountId) " +
                "FROM " + "hot_online_session_table" +
                " GROUP BY TUMBLE(proctime,INTERVAL '20' MINUTE)"), Row.class)
                .map(v -> {
                    Map<String, Object> value = new HashMap<>();
                    value.put("count", Long.parseLong(v.getField(0).toString()));
                    value.put("users1", Long.parseLong(v.getField(1).toString()));
                    value.put("users2", Long.parseLong(v.getField(2).toString()));
                    Map<String, String> key = new HashMap<>();
                    return new Tuple2<>(value, key);
                })
                .returns(new TypeHint<Tuple2<Map<String, Object>, Map<String, String>>>() {
                    @Override
                    public TypeInformation<Tuple2<Map<String, Object>, Map<String, String>>> getTypeInfo() {
                        return super.getTypeInfo();
                    }
                })
                .addSink(new InfluxDBInterface("hot_online_session_table", "mfc_prt_metric", "", PMInfluxDbUrl));


        env.execute("OnlineUserAnalyse");
    }
}
