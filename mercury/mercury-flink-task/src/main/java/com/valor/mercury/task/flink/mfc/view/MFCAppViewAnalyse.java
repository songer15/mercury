package com.valor.mercury.task.flink.mfc.view;

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
 * 2019/9/26 13:24
 */
public class MFCAppViewAnalyse implements MetricAnalyse {

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
                new FlinkKafkaConsumer<>(mfcViewTopicName, new JSONKeyValueDeserializationSchema(false), properties);
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
        DataStream<PageActionModel> playActionModel = advDataStream
                .flatMap((ObjectNode value, Collector<PageActionModel> out) -> {
                    JsonNode jsonNode = value.get("value");
                    try {
                        PageActionModel model = new PageActionModel();
                        model.setLoginType(jsonNode.get("loginType").asText());
                        model.setUserId(model.getLoginType().equals("ACCT") ?
                                jsonNode.get("email").asText() : jsonNode.get("did").asText());
                        model.setDevice(jsonNode.get("device").asText());
                        model.setActionTime(jsonNode.get("actionTime").asLong());
                        model.setVendorId(jsonNode.has("vendorId") ? jsonNode.get("vendorId").asLong() : 0L);
                        model.setAppVersion(jsonNode.has("appVersion") ? jsonNode.get("appVersion").asText() : "unKnown");
                        model.setLan(jsonNode.has("language") ? jsonNode.get("language").asText() : "unKnown");
                        model.setCountryCode(jsonNode.has("CountryCode") ? jsonNode.get("CountryCode").asText() : "unKnown");
                        model.setPageNumber(jsonNode.get("actionDetail").asText());
                        if (jsonNode.has("did")) {
                            String mac = jsonNode.get("did").asText();
                            if (mac.startsWith("ec2c5") || mac.startsWith("ec2ce") || mac.startsWith("ec2c9") || mac.startsWith("ec2ca"))
                                model.setMacSub(mac.substring(0, 5));
                            else
                                model.setMacSub("default");
                        } else
                            model.setMacSub("default");
                        out.collect(model);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }).returns(TypeInformation.of(PageActionModel.class));

        //注册表
        tableEnv.registerDataStream(mfcViewTableName, playActionModel,
                Arrays.stream(PageActionModel.class.getDeclaredFields())
                        .collect(StringBuilder::new, (sb, v) -> sb.append(v.getName()).append(","), StringBuilder::append)
                        .append("proctime.proctime").toString());

        //查询数据表得到（每120分钟出一份数据）
        //浏览页面总人次
        //按照地区，设备类型（手机，盒子），页面ID，语言区分
        String sql_t = "SELECT count(1),min(actionTime),countryCode,pageNumber,device,lan,macSub " +
                "FROM " + mfcViewTableName +
                " GROUP BY TUMBLE(proctime,INTERVAL '2' HOUR),countryCode,device,pageNumber,lan,macSub";
        Table tableResult_t = tableEnv.sqlQuery(sql_t);
        DataStream<Row> tableResultRows_t = tableEnv.toAppendStream(tableResult_t, Row.class);
        //写入ElasticSearch
        List<HttpHost> httpHosts = Arrays.stream(esHost.split(","))
                .map(v -> new HttpHost(v, 9200, "http"))
                .collect(ArrayList::new, List::add, List::addAll);
        ElasticsearchSink.Builder<Row> esSinkBuilder_t = new ElasticsearchSink.Builder<>(httpHosts, new PageElasticSearchSink_T(mfcViewEsIndexName));
        esSinkBuilder_t.setBulkFlushMaxActions(50);
        esSinkBuilder_t.setBulkFlushInterval(60_000);
        tableResultRows_t.addSink(esSinkBuilder_t.build());


        //查询数据表得到（每天出一份数据）
        //浏览总人数
        //按照地区，设备类型（手机，盒子），视频类型（电视剧，电影，预告片），语言区分
        String sql_p = "SELECT count(DISTINCT userId),min(actionTime),countryCode,pageNumber,device,lan,macSub " +
                "FROM " + mfcViewTableName +
                " GROUP BY TUMBLE(proctime,INTERVAL '1' DAY),countryCode,device,pageNumber,lan,macSub";
        Table tableResult_p = tableEnv.sqlQuery(sql_p);
        DataStream<Row> tableResultRows_p = tableEnv.toAppendStream(tableResult_p, Row.class);
        //写入ElasticSearch
        ElasticsearchSink.Builder<Row> esSinkBuilder_p = new ElasticsearchSink.Builder<>(httpHosts, new PageElasticSearchSink_P(mfcViewEsIndexName));
        esSinkBuilder_p.setBulkFlushMaxActions(10);
        esSinkBuilder_p.setBulkFlushInterval(60_000);
        tableResultRows_p.addSink(esSinkBuilder_p.build());

        env.execute("MFCAppViewAnalyse");
    }
}
