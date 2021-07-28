package com.valor.mercury.task.flink.mfc.play;

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
 * 2019/9/20 16:43
 */
public class MFCAppPlayAnalyse implements MetricAnalyse {

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
                new FlinkKafkaConsumer<>(mfcPlayTopicName, new JSONKeyValueDeserializationSchema(false), properties);
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
        DataStream<PlayActionModel> playActionModel = advDataStream
                .flatMap((ObjectNode value, Collector<PlayActionModel> out) -> {
                    JsonNode jsonNode = value.get("value");
                    try {
                        PlayActionModel model = new PlayActionModel();
                        model.setVideoType(jsonNode.get("videoType").asText());
                        //手机端上报的数据单位是毫秒、盒子端是秒
                        if (jsonNode.get("device").asText().equals("phone"))
                            model.setPlayDuration(jsonNode.get("playDuration").asLong() / 1000);
                        else
                            model.setPlayDuration(jsonNode.get("playDuration").asLong());
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
                        model.setActionDetail(jsonNode.get("actionDetail").asText()
                                .replace("from_", ""));
                        model.setActionType(jsonNode.get("actionType").asText());
                        model.setCountryCode(jsonNode.has("CountryCode") ? jsonNode.get("CountryCode").asText() : "unKnown");
                        if (jsonNode.has("did")) {
                            String mac = jsonNode.get("did").asText();
                            if (mac.startsWith("ec2c5") || mac.startsWith("ec2ce") || mac.startsWith("ec2c9") || mac.startsWith("ec2ca"))
                                model.setMacSub(mac.substring(0, 5));
                            else
                                model.setMacSub("default");
                        } else
                            model.setMacSub("default");
                        if (model.getPlayDuration() <= 600)
                            model.setPlayTag(0);   //播放时长小于10min
                        else if (model.getPlayDuration() <= 1800)
                            model.setPlayTag(1);  //播放时长大于10min小于30min
                        else
                            model.setPlayTag(2);  //播放时长大于30min
                        //播放时长大于12小时或者小于0都视为脏数据
                        if (model.getPlayDuration() >= 0 && model.getPlayDuration() < 43200)
                            out.collect(model);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }).returns(TypeInformation.of(PlayActionModel.class));

        //注册表
        tableEnv.registerDataStream(mfcPlayTableName, playActionModel,
                Arrays.stream(PlayActionModel.class.getDeclaredFields())
                        .collect(StringBuilder::new, (sb, v) -> sb.append(v.getName()).append(","), StringBuilder::append)
                        .append("proctime.proctime").toString());

        //查询数据表得到（每120分钟出一份数据）
        //播放总人次，平均播放时长，播放top50的剧集
        //按照地区，设备类型（手机，盒子），视频类型（电视剧，电影，预告片），具体剧集，语言，播放时长标记区分
        String sql_t = "SELECT count(1),avg(playDuration)/60,min(actionTime),countryCode,videoType,device,videoId,se,lan,playTag,actionDetail,macSub,vendorId " +
                "FROM " + mfcPlayTableName +
                " GROUP BY TUMBLE(proctime,INTERVAL '2' HOUR),countryCode,device,videoType,videoId,se,lan,playTag,actionDetail,macSub,vendorId";
        Table tableResult_t = tableEnv.sqlQuery(sql_t);
        DataStream<Row> tableResultRows_t = tableEnv.toAppendStream(tableResult_t, Row.class);
        //写入ElasticSearch
        List<HttpHost> httpHosts = Arrays.stream(esHost.split(","))
                .map(v -> new HttpHost(v, 9200, "http"))
                .collect(ArrayList::new, List::add, List::addAll);
        ElasticsearchSink.Builder<Row> esSinkBuilder_t = new ElasticsearchSink.Builder<>(httpHosts, new PlayElasticSearchSink_T(mfcPlayEsIndexName));
        esSinkBuilder_t.setBulkFlushMaxActions(100);
        esSinkBuilder_t.setBulkFlushInterval(60_000);
        tableResultRows_t.addSink(esSinkBuilder_t.build());


        //查询数据表得到（每天出一份数据）
        //播放总人数
        //按照地区，设备类型（手机，盒子），视频类型（电视剧，电影，预告片），语言区分
        String sql_p = "SELECT count(DISTINCT userId),min(actionTime),countryCode,videoType,device,lan,macSub,vendorId " +
                "FROM " + mfcPlayTableName +
                " GROUP BY TUMBLE(proctime,INTERVAL '1' DAY),countryCode,device,videoType,lan,macSub,vendorId";
        Table tableResult_p = tableEnv.sqlQuery(sql_p);
        DataStream<Row> tableResultRows_p = tableEnv.toAppendStream(tableResult_p, Row.class);
        //写入ElasticSearch
        ElasticsearchSink.Builder<Row> esSinkBuilder_p = new ElasticsearchSink.Builder<>(httpHosts, new PlayElasticSearchSink_P(mfcPlayEsIndexName));
        esSinkBuilder_p.setBulkFlushMaxActions(10);
        esSinkBuilder_p.setBulkFlushInterval(60_000);
        tableResultRows_p.addSink(esSinkBuilder_p.build());

        //查询数据表得到（每天出一份数据）
        //播放总人数
        //按照地区，设备类型（手机，盒子），语言，播放时长标记区分
        String sql_p2 = "SELECT count(DISTINCT userId),min(actionTime),countryCode,device,lan,playTag,actionDetail,macSub,vendorId " +
                "FROM " + mfcPlayTableName +
                " GROUP BY TUMBLE(proctime,INTERVAL '1' DAY),countryCode,device,lan,playTag,actionDetail,macSub,vendorId";
        Table tableResult_p2 = tableEnv.sqlQuery(sql_p2);
        DataStream<Row> tableResultRows_p2 = tableEnv.toAppendStream(tableResult_p2, Row.class);
        //写入ElasticSearch
        ElasticsearchSink.Builder<Row> esSinkBuilder_p2 = new ElasticsearchSink.Builder<>(httpHosts, new PlayElasticSearchSink_P2(mfcPlayEsIndexName));
        esSinkBuilder_p2.setBulkFlushMaxActions(10);
        esSinkBuilder_p2.setBulkFlushInterval(60_000);
        tableResultRows_p2.addSink(esSinkBuilder_p2.build());

        env.execute("MFCAppPlayAnalyse");
    }
}

