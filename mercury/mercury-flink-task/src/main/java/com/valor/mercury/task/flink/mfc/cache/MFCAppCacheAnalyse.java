package com.valor.mercury.task.flink.mfc.cache;

import com.valor.mercury.task.flink.MetricAnalyse;
import org.apache.flink.api.common.typeinfo.TypeInformation;
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
 * 2019/10/10 10:15
 */
public class MFCAppCacheAnalyse implements MetricAnalyse {

    @Override
    public void run(String[] args) throws Exception {
        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
//        env.setParallelism(1);
        StreamTableEnvironment tableEnv = TableEnvironment.getTableEnvironment(env);

        // 配置kafka数据源
        Properties properties = new Properties();
        properties.setProperty("bootstrap.servers", bootstrapServer);
        properties.setProperty("group.id", groupID);
        FlinkKafkaConsumer<ObjectNode> kafkaConsumer =
                new FlinkKafkaConsumer<>(mfcCacheTopicName, new JSONKeyValueDeserializationSchema(false), properties);
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
        DataStream<CacheActionModel> playActionModel = advDataStream
                .flatMap((ObjectNode value, Collector<CacheActionModel> out) -> {
                    JsonNode jsonNode = value.get("value");
                    try {
                        CacheActionModel model = new CacheActionModel();
                        model.setVideoType(jsonNode.get("videoType").asText());
                        model.setVideoId(jsonNode.get("videoId").asText());
                        model.setSe("S" + jsonNode.get("season").asInt() + "E" + jsonNode.get("episode").asInt());
                        model.setLoginType(jsonNode.get("loginType").asText());
                        model.setUserId(model.getLoginType().equals("ACCT") ?
                                jsonNode.get("email").asText() : jsonNode.get("did").asText());
                        model.setDevice(jsonNode.get("device").asText());
                        model.setActionTime(jsonNode.get("actionTime").asLong());
                        model.setVendorId(jsonNode.has("vendorId") ? jsonNode.get("vendorId").asLong() : 0L);
                        model.setAppVersion(jsonNode.has("appVersion") ? jsonNode.get("appVersion").asText() : "unKnown");
                        model.setLan(jsonNode.has("language") ? jsonNode.get("language").asText() : "unKnown");
                        model.setCountryCode(jsonNode.has("CountryCode") ? jsonNode.get("CountryCode").asText() : "unKnown");
                        out.collect(model);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }).returns(TypeInformation.of(CacheActionModel.class));

        //注册表
        tableEnv.registerDataStream(mfcCacheTableName, playActionModel,
                Arrays.stream(CacheActionModel.class.getDeclaredFields())
                        .collect(StringBuilder::new, (sb, v) -> sb.append(v.getName()).append(","), StringBuilder::append)
                        .append("proctime.proctime").toString());

        //查询数据表得到（每60分钟出一份数据）
        //缓存总次数
        //按照地区，设备类型（手机，盒子），视频类型（电视剧，电影，预告片），具体剧集，语言区分
        String sql = "SELECT count(1),min(actionTime),countryCode,videoType,device,videoId,se,lan " +
                "FROM " + mfcCacheTableName +
                " GROUP BY TUMBLE(proctime,INTERVAL '60' MINUTE),countryCode,device,videoType,videoId,se,lan";
        Table tableResult = tableEnv.sqlQuery(sql);
        DataStream<Row> tableResultRows = tableEnv.toAppendStream(tableResult, Row.class);
        //写入ElasticSearch
        List<HttpHost> httpHosts = Arrays.stream(esHost.split(","))
                .map(v -> new HttpHost(v, 9200, "http"))
                .collect(ArrayList::new, List::add, List::addAll);
        ElasticsearchSink.Builder<Row> esSinkBuilder = new ElasticsearchSink.Builder<>(httpHosts, new CacheElasticSearchSink(mfcCacheEsIndexName));
        esSinkBuilder.setBulkFlushMaxActions(100);
        esSinkBuilder.setBulkFlushInterval(60_000);
        tableResultRows.addSink(esSinkBuilder.build());

        env.execute("MFCAppCacheAnalyse");
    }
}

