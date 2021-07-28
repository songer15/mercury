package com.valor.mercury.task.flink.mfc.login;

import com.valor.mercury.task.flink.MetricAnalyse;
import org.apache.flink.shaded.jackson2.com.fasterxml.jackson.databind.JsonNode;
import org.apache.flink.shaded.jackson2.com.fasterxml.jackson.databind.node.ObjectNode;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.datastream.SingleOutputStreamOperator;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.windowing.time.Time;
import org.apache.flink.streaming.connectors.elasticsearch6.ElasticsearchSink;
import org.apache.flink.streaming.connectors.kafka.FlinkKafkaConsumer;
import org.apache.flink.streaming.util.serialization.JSONKeyValueDeserializationSchema;
import org.apache.flink.util.Collector;
import org.apache.http.HttpHost;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;

import static com.valor.mercury.task.flink.util.Constants.*;

/**
 * 102714
 *
 * @author Gavin
 * 2019/10/12 13:36
 */
public class MFCAppVersionAnalyse implements MetricAnalyse {

    @Override
    public void run(String[] args) throws Exception {
        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
//        env.setParallelism(1);

        // 配置kafka数据源
        Properties properties = new Properties();
        properties.setProperty("bootstrap.servers", bootstrapServer);
        properties.setProperty("group.id", groupID);
        FlinkKafkaConsumer<ObjectNode> kafkaConsumer =
                new FlinkKafkaConsumer<>(mfcLoginTopicName, new JSONKeyValueDeserializationSchema(false), properties);
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
        DataStream<ObjectNode> dataStream = env.addSource(kafkaConsumer);
        DataStream<LoginActionModel> hasCleanDataStream = dataStream
                // 清理不合规的数据并格式化
                .flatMap((ObjectNode value, Collector<LoginActionModel> out) -> {
                    JsonNode jsonNode = value.get("value");
                    try {
                        if ("login-E".equals(jsonNode.get("actionType").asText())
                                || "active".equals(jsonNode.get("actionType").asText())) {
                            LoginActionModel model = new LoginActionModel();
                            model.setLoginType(jsonNode.get("loginType").asText());
                            model.setUserId(jsonNode.get("loginType").asText().equals("device") ? jsonNode.get("did").asText() : jsonNode.get("email").asText());
                            model.setDevice(jsonNode.get("device").asText());
                            model.setActionTime(jsonNode.get("actionTime").asLong());
                            model.setVendorId(jsonNode.has("vendorId") ? jsonNode.get("vendorId").asLong() : 0L);
                            model.setAppVersion(jsonNode.has("appVer") ? Long.parseLong(jsonNode.get("appVer").asText()) : 0L);
                            model.setDid(jsonNode.get("did").asText());
                            model.setCountryCode(jsonNode.has("CountryCode") ? jsonNode.get("CountryCode").asText() : "unKnown");
                            model.setMacSub(jsonNode.get("loginType").asText().equals("device") ? jsonNode.get("did").asText().substring(0, 5) : "account");
                            out.collect(model);
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }).returns(LoginActionModel.class);

        //分析得到用户升级事件，用户最新版本登录情况
        LoginVersionProcessFunction loginVersionProcessFunction = new LoginVersionProcessFunction();
        SingleOutputStreamOperator<LoginActionModel> mainDataStream =
                //按照mac地址分区
                hasCleanDataStream.keyBy(LoginActionModel::getDid)
                        //每次计算一个小时的数据量
                        .timeWindow(Time.hours(2))
                        .process(loginVersionProcessFunction);
        //写入MYSQL
        mainDataStream.addSink(new LoginVersionMysqlSink());


        //获取旁路输出（用户升级事件）
        DataStream<UpgradeActionModel> sideOutputStream
                = mainDataStream.getSideOutput(loginVersionProcessFunction.getUpgradeTag());
        //写入ES
        List<HttpHost> httpHosts = Arrays.stream(esHost.split(","))
                .map(v -> new HttpHost(v, 9200, "http"))
                .collect(ArrayList::new, List::add, List::addAll);
        ElasticsearchSink.Builder<UpgradeActionModel> esSinkBuilder_upgrade = new ElasticsearchSink.Builder<>(httpHosts, new LoginUpgradeElasticSearchSink(mfcLoginEsIndexName));
        esSinkBuilder_upgrade.setBulkFlushMaxActions(10);
        esSinkBuilder_upgrade.setBulkFlushInterval(60_000);
        sideOutputStream.addSink(esSinkBuilder_upgrade.build());

//        //分析得到当天登录用户的登录方式，设备类型，版本，地区，具体机型分布
//        DataStream<Tuple8<Integer, Integer, Date, Long, String, String, String, String>> loginDataStream = hasCleanDataStream
//                .keyBy("loginType", "device", "appVersion", "countryCode", "macSub")
//                .timeWindow(Time.days(1))
//                .aggregate(new LoginVersionAggregateFunction());
//        //写入ES
//        ElasticsearchSink.Builder<Tuple8<Integer, Integer, Date, Long, String, String, String, String>> esSinkBuilder_login = new ElasticsearchSink.Builder<>(httpHosts, new LoginElasticSearchSink(mfcLoginEsIndexName));
//        esSinkBuilder_login.setBulkFlushMaxActions(10);
//        esSinkBuilder_login.setBulkFlushInterval(60_000);
//        loginDataStream.addSink(esSinkBuilder_login.build());

        env.execute("MFCAppVersionAnalyse");
    }
}
