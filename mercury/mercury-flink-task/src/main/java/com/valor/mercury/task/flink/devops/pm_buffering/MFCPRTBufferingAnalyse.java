package com.valor.mercury.task.flink.devops.pm_buffering;

import com.valor.mercury.task.flink.MetricAnalyse;
import com.valor.mercury.task.flink.devops.pm.ConnectedStreamData;
import com.valor.mercury.task.flink.devops.pm.MFCPMBufferingMessage;
import com.valor.mercury.task.flink.devops.pm.MFCPMMessage;
import com.valor.mercury.task.flink.util.InfluxDBInterface;
import org.apache.flink.api.common.typeinfo.TypeHint;
import org.apache.flink.api.common.typeinfo.TypeInformation;
import org.apache.flink.api.java.tuple.Tuple2;
import org.apache.flink.shaded.jackson2.com.fasterxml.jackson.databind.JsonNode;
import org.apache.flink.shaded.jackson2.com.fasterxml.jackson.databind.node.ObjectNode;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.datastream.SingleOutputStreamOperator;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.functions.co.CoMapFunction;
import org.apache.flink.streaming.connectors.kafka.FlinkKafkaConsumer;
import org.apache.flink.streaming.util.serialization.JSONKeyValueDeserializationSchema;
import org.apache.flink.table.api.TableEnvironment;
import org.apache.flink.table.api.java.StreamTableEnvironment;
import org.apache.flink.types.Row;
import org.apache.flink.util.Collector;
import org.apache.logging.log4j.util.Strings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import static com.valor.mercury.task.flink.util.Constants.*;


public class MFCPRTBufferingAnalyse implements MetricAnalyse {

    private static Logger logger = LoggerFactory.getLogger(MFCPRTBufferingAnalyse.class);
    private static Map<String, String> engineVerMap= new HashMap<String, String>(){{ put("VER-2.2T21", "2010231");put("VER-2.2T22", "2010232"); }
    };


    public void run(String[] args) throws Exception {
        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
        StreamTableEnvironment tableEnv = TableEnvironment.getTableEnvironment(env);

        // 配置kafka数据源
        Properties properties = new Properties();
        properties.setProperty("bootstrap.servers", PMBootstrapServer);
        properties.setProperty("group.id", groupID + "_2");
        FlinkKafkaConsumer<ObjectNode> kafkaConsumer =
                new FlinkKafkaConsumer<>(mfcPMBufferingTopicName, new JSONKeyValueDeserializationSchema(false), properties);
        if (args.length > 1 && args[1] != null) {
            try {
                //从指定的时间戳开始
                kafkaConsumer.setStartFromTimestamp(Long.parseLong(args[1]));
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else
            kafkaConsumer.setStartFromLatest();  // 设置读取最新的数据
        FlinkKafkaConsumer<ObjectNode> kConsumer =
                new FlinkKafkaConsumer<>(mfcPMTopicName, new JSONKeyValueDeserializationSchema(false), properties);
        if (args.length > 1 && args[1] != null) {
            try {
                //从指定的时间戳开始
                kConsumer.setStartFromTimestamp(Long.parseLong(args[1]));
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else
            kConsumer.setStartFromLatest();  // 设置读取最新的数据

        // 拿到kafka的数据
        DataStream<ObjectNode> bufdataStream = env.addSource(kConsumer);
        DataStream<ObjectNode> dataStream = env.addSource(kafkaConsumer);

        //在播资源数据流
        //在播资源数据流
//        DataStream<MFCPMMessage> playingDataStream = bufdataStream
//                .flatMap((ObjectNode value, Collector<MFCPMMessage> out) -> {
//                    JsonNode jsonNode = value.get("value");
//                    if (jsonNode.has("playType")) {
//                        try {
//                            MFCPMMessage model = new MFCPMMessage();
//                            model.setPlayHash(jsonNode.get("playHash").asText());
//                            model.setPlayNetDisk(jsonNode.get("playNetDisk").asText());
//                            model.setPlayTtNum(jsonNode.get("playTtNum").asText());
//                            model.setEngineVersion(jsonNode.has("engineVer") ? jsonNode.get("engineVer").asText().split(" ")[0] : "empty");
//                            model.setSelfDid(jsonNode.get("selfDid").asText());
//                            model.setPlayType(jsonNode.get("playType").asText());
//                            if (model.getPlayTtNum().equals("tt12361974#S1#E1"))
//                                model.setFlag("true");
//                            else
//                                model.setFlag("false");
//                            if (Strings.isNotEmpty(model.getSelfDid()))
//                                out.collect(model);
//                        } catch (Exception e) {
//                            e.printStackTrace();
//                        }
//                    }
//                }).returns(TypeInformation.of(MFCPMMessage.class));
//
//
//
//        DataStream<MFCPMBufferingMessage> bufDataStream = dataStream
//                .flatMap((ObjectNode value, Collector<MFCPMBufferingMessage> out) -> {
//                    JsonNode jsonNode = value.get("value");
//                    try {
//                        if (jsonNode.has("mac") && jsonNode.has("playProtocol") && jsonNode.has("isPrepare")) {
//                            MFCPMBufferingMessage message = new MFCPMBufferingMessage();
//                            message.setTtID(jsonNode.get("ttid").asText());
//                            message.setMac(jsonNode.get("mac").asText());
//                            message.setBufferingStartTime(Long.parseLong(jsonNode.get("bufferingStartTime").asText()));
//                            message.setBufferingTime(Integer.parseInt(jsonNode.get("bufferingTime").asText()));
//                            message.setPlayProtocol(jsonNode.get("playProtocol").asText());
//                            Long prepareFirstBufferingTime = Long.parseLong(jsonNode.get("prepareFirstBufferingTime").asText());
////                            message.setIsPrepare(jsonNode.get("isPrepare").asText());
//                            if (message.getTtID().equals("tt12361974#S1#E1"))
//                                message.setFlag("true");
//                            else
//                                message.setFlag("false");
//                            //去除起播阶段
//                            if (prepareFirstBufferingTime > 5000)
//                                message.setIsPrepare("true");
//                            else
//                                message.setIsPrepare("false");
////                            if ("true".equals(message.getIsPrepare()))
//                            out.collect(message);
//                        }
//                    } catch (Exception e) {
//                        e.printStackTrace();
//                    }
//                }).returns(TypeInformation.of(MFCPMBufferingMessage.class));
//
//        //注册表
//        tableEnv.registerDataStream("mfc_pm_buffering_analyse_tmp", bufDataStream,
//                Arrays.stream(MFCPMBufferingMessage.class.getDeclaredFields())
//                        .collect(StringBuilder::new, (sb, v) -> sb.append(v.getName()).append(","), StringBuilder::append)
//                        .append("proctime.proctime").toString());
//
//
//        SingleOutputStreamOperator<ConnectedStreamData> connectedStream = playingDataStream.connect(bufDataStream).map(new CoMapFunction<MFCPMMessage, MFCPMBufferingMessage, ConnectedStreamData>() {
//            @Override
//            public ConnectedStreamData map1(MFCPMMessage value) throws Exception {
//                ConnectedStreamData data = new ConnectedStreamData();
//                data.setSelfDid(value.getSelfDid());
//                data.setType("play");
//                data.setPlayType(value.getPlayType());
//                data.setPlayTtNum(value.getPlayTtNum());
//                data.setFlag(value.getFlag());
//                data.setIsPrepare("true");
//                return data;
//            }
//
//            @Override
//            public ConnectedStreamData map2(MFCPMBufferingMessage value) throws Exception {
//                ConnectedStreamData data = new ConnectedStreamData();
//                data.setDid(value.getMac());
//                data.setType("buf");
//                data.setPlayType(value.getPlayProtocol());
//                data.setPlayTtNum(null);
//                data.setFlag(value.getFlag());
//                data.setIsPrepare(value.getIsPrepare());
//                return data;
//            }
//        });
//
//        tableEnv.registerDataStream("mfc_pm_buf_rate_table_tmp", connectedStream,
//                Arrays.stream(ConnectedStreamData.class.getDeclaredFields())
//                        .collect(StringBuilder::new, (sb, v) -> sb.append(v.getName()).append(","), StringBuilder::append)
//                        .append("proctime.proctime").toString());
//
//        //卡顿的用户数，次数，平均卡顿时间
//        //查询数据表得到（每2分钟出一份数据）
//        String sql = "SELECT count(DISTINCT mac),count(1),avg(bufferingTime),playProtocol " +
//                "FROM mfc_pm_buffering_analyse_tmp" +
//                " GROUP BY TUMBLE(proctime,INTERVAL '2' MINUTE),playProtocol";
//        tableEnv.toAppendStream(tableEnv.sqlQuery(sql), Row.class)
//                .map(v -> {
//                    Map<String, Object> value = new HashMap<>();
//                    value.put("userNum", Long.parseLong(v.getField(0).toString()));
//                    value.put("count", Long.parseLong(v.getField(1).toString()));
//                    value.put("bufferingTime", Long.parseLong(v.getField(2).toString()));
//                    Map<String, String> key = new HashMap<>();
//                    key.put("playProtocol", v.getField(3).toString());
//                    return new Tuple2<>(value, key);
//                })
//                .returns(new TypeHint<Tuple2<Map<String, Object>, Map<String, String>>>() {
//                    @Override
//                    public TypeInformation<Tuple2<Map<String, Object>, Map<String, String>>> getTypeInfo() {
//                        return super.getTypeInfo();
//                    }
//                })
//                .addSink(new InfluxDBInterface("mfc_pm_buffering_analyse_tmp", "mfc_prt_metric", "", PMInfluxDbUrl));
//
//
//
//        //卡顿的用户数，次数，平均卡顿时间
//        //查询数据表得到（每2分钟出一份数据）
//        String sql_2 = "SELECT count(DISTINCT mac),count(1),avg(bufferingTime),playProtocol " +
//                "FROM mfc_pm_buffering_analyse_tmp where isPrepare = 'true' " +
//                " GROUP BY TUMBLE(proctime,INTERVAL '2' MINUTE),playProtocol";
//        tableEnv.toAppendStream(tableEnv.sqlQuery(sql_2), Row.class)
//                .map(v -> {
//                    Map<String, Object> value = new HashMap<>();
//                    value.put("userNum", Long.parseLong(v.getField(0).toString()));
//                    value.put("count", Long.parseLong(v.getField(1).toString()));
//                    value.put("bufferingTime", Long.parseLong(v.getField(2).toString()));
//                    Map<String, String> key = new HashMap<>();
//                    key.put("playProtocol", v.getField(3).toString());
//                    return new Tuple2<>(value, key);
//                })
//                .returns(new TypeHint<Tuple2<Map<String, Object>, Map<String, String>>>() {
//                    @Override
//                    public TypeInformation<Tuple2<Map<String, Object>, Map<String, String>>> getTypeInfo() {
//                        return super.getTypeInfo();
//                    }
//                })
//                .addSink(new InfluxDBInterface("mfc_pm_buffering_analyse_2_tmp", "mfc_prt_metric", "", PMInfluxDbUrl));

























//        String bufRateSQL = "select count(DISTINCT selfDid),count(DISTINCT did)" +
//                ",count(DISTINCT playTtNum),playType from mfc_pm_buf_rate_table_tmp where isPrepare = 'true' " +
//                "GROUP BY TUMBLE(proctime,INTERVAL '2' MINUTE),playType";
//        tableEnv.toAppendStream(tableEnv.sqlQuery(bufRateSQL), Row.class)
//                .map(v -> {
//                    Map<String, Object> value = new HashMap<>();
//                    long usercount = Long.parseLong(v.getField(0).toString());
//                    long bufcount = Long.parseLong(v.getField(1).toString());
//                    long mediacount = Long.parseLong(v.getField(2).toString());
//                    value.put("usercount", usercount);
//                    value.put("bufcount", bufcount);
//                    value.put("mediacount", mediacount);
//                    value.put("bufRate", 100 * (double) bufcount / (double) usercount);
//                    Map<String, String> key = new HashMap<>();
//                    key.put("playType", v.getField(3).toString());
//                    key.put("isPrepare", "true");
//                    return new Tuple2<>(value, key);
//                }).returns(new TypeHint<Tuple2<Map<String, Object>, Map<String, String>>>() {
//            @Override
//            public TypeInformation<Tuple2<Map<String, Object>, Map<String, String>>> getTypeInfo() {
//                return super.getTypeInfo();
//            }
//        }).addSink(new InfluxDBInterface("mfc_playing_pm_table_buf_tmp", "mfc_prt_metric", "", PMInfluxDbUrl));

//        String bufRateSQL_2 = "select count(DISTINCT selfDid),count(DISTINCT did)" +
//                ",count(DISTINCT playTtNum),playType from mfc_pm_buf_rate_table_tmp " +
//                "GROUP BY TUMBLE(proctime,INTERVAL '2' MINUTE),playType";
//        tableEnv.toAppendStream(tableEnv.sqlQuery(bufRateSQL_2), Row.class)
//                .map(v -> {
//                    Map<String, Object> value = new HashMap<>();
//                    long usercount = Long.parseLong(v.getField(0).toString());
//                    long bufcount = Long.parseLong(v.getField(1).toString());
//                    long mediacount = Long.parseLong(v.getField(2).toString());
//                    value.put("usercount", usercount);
//                    value.put("bufcount", bufcount);
//                    value.put("mediacount", mediacount);
//                    value.put("bufRate", 100 * (double) bufcount / (double) usercount);
//                    Map<String, String> key = new HashMap<>();
//                    key.put("playType", v.getField(3).toString());
//                    key.put("isPrepare", "false");
//                    return new Tuple2<>(value, key);
//                }).returns(new TypeHint<Tuple2<Map<String, Object>, Map<String, String>>>() {
//            @Override
//            public TypeInformation<Tuple2<Map<String, Object>, Map<String, String>>> getTypeInfo() {
//                return super.getTypeInfo();
//            }
//        }).addSink(new InfluxDBInterface("mfc_playing_pm_table_buf_tmp", "mfc_prt_metric", "", PMInfluxDbUrl));






//        //卡顿的用户数，次数，平均卡顿时间
//        //查询数据表得到（每2分钟出一份数据）
//        String sql_s = "SELECT count(DISTINCT mac),count(1),avg(bufferingTime),playProtocol,flag " +
//                "FROM mfc_pm_buffering_analyse_tmp" +
//                " GROUP BY TUMBLE(proctime,INTERVAL '2' MINUTE),playProtocol,flag";
//        tableEnv.toAppendStream(tableEnv.sqlQuery(sql_s), Row.class)
//                .map(v -> {
//                    Map<String, Object> value = new HashMap<>();
//                    value.put("userNum", Long.parseLong(v.getField(0).toString()));
//                    value.put("count", Long.parseLong(v.getField(1).toString()));
//                    value.put("bufferingTime", Long.parseLong(v.getField(2).toString()));
//                    Map<String, String> key = new HashMap<>();
//                    key.put("playProtocol", v.getField(3).toString());
//                    key.put("flag", v.getField(4).toString());
//                    return new Tuple2<>(value, key);
//                })
//                .returns(new TypeHint<Tuple2<Map<String, Object>, Map<String, String>>>() {
//                    @Override
//                    public TypeInformation<Tuple2<Map<String, Object>, Map<String, String>>> getTypeInfo() {
//                        return super.getTypeInfo();
//                    }
//                })
//                .addSink(new InfluxDBInterface("mfc_pm_buffering_analyse_tmp_s", "mfc_prt_metric", "", PMInfluxDbUrl));

//        //卡顿的用户数，次数，平均卡顿时间
//        //查询数据表得到（每2分钟出一份数据）
//        String sql_2_s = "SELECT count(DISTINCT mac),count(1),avg(bufferingTime),playProtocol,flag " +
//                "FROM mfc_pm_buffering_analyse_tmp where isPrepare = 'true' " +
//                " GROUP BY TUMBLE(proctime,INTERVAL '2' MINUTE),playProtocol,flag";
//        tableEnv.toAppendStream(tableEnv.sqlQuery(sql_2_s), Row.class)
//                .map(v -> {
//                    Map<String, Object> value = new HashMap<>();
//                    value.put("userNum", Long.parseLong(v.getField(0).toString()));
//                    value.put("count", Long.parseLong(v.getField(1).toString()));
//                    value.put("bufferingTime", Long.parseLong(v.getField(2).toString()));
//                    Map<String, String> key = new HashMap<>();
//                    key.put("playProtocol", v.getField(3).toString());
//                    key.put("flag", v.getField(4).toString());
//                    return new Tuple2<>(value, key);
//                })
//                .returns(new TypeHint<Tuple2<Map<String, Object>, Map<String, String>>>() {
//                    @Override
//                    public TypeInformation<Tuple2<Map<String, Object>, Map<String, String>>> getTypeInfo() {
//                        return super.getTypeInfo();
//                    }
//                })
//                .addSink(new InfluxDBInterface("mfc_pm_buffering_analyse_2_tmp_s", "mfc_prt_metric", "", PMInfluxDbUrl));


//        String bufRateSQL_s = "select count(DISTINCT selfDid),count(DISTINCT did)" +
//                ",count(DISTINCT playTtNum),playType,flag from mfc_pm_buf_rate_table_tmp where isPrepare = 'true' " +
//                "GROUP BY TUMBLE(proctime,INTERVAL '2' MINUTE),playType,flag";
//        tableEnv.toAppendStream(tableEnv.sqlQuery(bufRateSQL_s), Row.class)
//                .map(v -> {
//                    Map<String, Object> value = new HashMap<>();
//                    long usercount = Long.parseLong(v.getField(0).toString());
//                    long bufcount = Long.parseLong(v.getField(1).toString());
//                    long mediacount = Long.parseLong(v.getField(2).toString());
//                    value.put("usercount", usercount);
//                    value.put("bufcount", bufcount);
//                    value.put("mediacount", mediacount);
//                    value.put("bufRate", 100 * (double) bufcount / (double) usercount);
//                    Map<String, String> key = new HashMap<>();
//                    key.put("playType", v.getField(3).toString());
//                    key.put("flag", v.getField(4).toString());
//                    key.put("isPrepare", "true");
//                    return new Tuple2<>(value, key);
//                }).returns(new TypeHint<Tuple2<Map<String, Object>, Map<String, String>>>() {
//            @Override
//            public TypeInformation<Tuple2<Map<String, Object>, Map<String, String>>> getTypeInfo() {
//                return super.getTypeInfo();
//            }
//        }).addSink(new InfluxDBInterface("mfc_playing_pm_table_buf_tmp_s", "mfc_prt_metric", "", PMInfluxDbUrl));


//        String bufRateSQL_s_2 = "select count(DISTINCT selfDid),count(DISTINCT did)" +
//                ",count(DISTINCT playTtNum),playType,flag from mfc_pm_buf_rate_table_tmp " +
//                "GROUP BY TUMBLE(proctime,INTERVAL '2' MINUTE),playType,flag";
//        tableEnv.toAppendStream(tableEnv.sqlQuery(bufRateSQL_s_2), Row.class)
//                .map(v -> {
//                    Map<String, Object> value = new HashMap<>();
//                    long usercount = Long.parseLong(v.getField(0).toString());
//                    long bufcount = Long.parseLong(v.getField(1).toString());
//                    long mediacount = Long.parseLong(v.getField(2).toString());
//                    value.put("usercount", usercount);
//                    value.put("bufcount", bufcount);
//                    value.put("mediacount", mediacount);
//                    value.put("bufRate", 100 * (double) bufcount / (double) usercount);
//                    Map<String, String> key = new HashMap<>();
//                    key.put("playType", v.getField(3).toString());
//                    key.put("flag", v.getField(4).toString());
//                    key.put("isPrepare", "false");
//                    return new Tuple2<>(value, key);
//                }).returns(new TypeHint<Tuple2<Map<String, Object>, Map<String, String>>>() {
//            @Override
//            public TypeInformation<Tuple2<Map<String, Object>, Map<String, String>>> getTypeInfo() {
//                return super.getTypeInfo();
//            }
//        }).addSink(new InfluxDBInterface("mfc_playing_pm_table_buf_tmp_s", "mfc_prt_metric", "", PMInfluxDbUrl));



//
//
//        // appVersion ********************************************************************************************************
//        DataStream<MFCPMBufferingMessage> bufDataStreamGroupByAppver = dataStream
//                .flatMap((ObjectNode value, Collector<MFCPMBufferingMessage> out) -> {
//                    JsonNode jsonNode = value.get("value");
//                    try {
//                        if (jsonNode.has("appVersion")) {
//                            if (jsonNode.has("mac") && jsonNode.has("playProtocol") && jsonNode.has("isPrepare")) {
//                                MFCPMBufferingMessage message = new MFCPMBufferingMessage();
//                                //客户端版本号 2010233 （beta3） & 2010266 (正式版) &  2010366 都统计在 2010233下
//                                String appVersion = jsonNode.get("appVersion").asText();
//                                if ("2010266".equals(appVersion))
//                                    appVersion = "2010233";
//                                else if ("2010366".equals(appVersion))
//                                    appVersion = "2010233";
//                                message.setAppVer(appVersion);
//                                message.setTtID(jsonNode.get("ttid").asText());
//                                message.setMac(jsonNode.get("mac").asText());
//                                message.setBufferingStartTime(Long.parseLong(jsonNode.get("bufferingStartTime").asText()));
//                                message.setBufferingTime(Integer.parseInt(jsonNode.get("bufferingTime").asText()));
//                                message.setPlayProtocol(jsonNode.get("playProtocol").asText());
//                                Long prepareFirstBufferingTime = Long.parseLong(jsonNode.get("prepareFirstBufferingTime").asText());
////                            message.setIsPrepare(jsonNode.get("isPrepare").asText());
//                                if (message.getTtID().equals("tt12361974#S1#E1"))
//                                    message.setFlag("true");
//                                else
//                                    message.setFlag("false");
//                                //去除起播阶段
//                                if (prepareFirstBufferingTime > 5000)
//                                    message.setIsPrepare("true");
//                                else
//                                    message.setIsPrepare("false");
////                            if ("true".equals(message.getIsPrepare()))
//                                out.collect(message);
//                            }
//                        }
//                    } catch (Exception e) {
//                        e.printStackTrace();
//                    }
//                }).returns(TypeInformation.of(MFCPMBufferingMessage.class));

//        DataStream<MFCPMMessage> playingDataStreamGroupByAppver = bufdataStream
//                .flatMap((ObjectNode value, Collector<MFCPMMessage> out) -> {
//                    JsonNode jsonNode = value.get("value");
//                    if (jsonNode.has("playType")) {
//                        try {
//                            MFCPMMessage model = new MFCPMMessage();
//                            String engineVer = jsonNode.get("engineVer").asText();
//                            if (engineVer.contains("VER-2.2T21"))
//                                model.setAppVer("2010231");
//                            else if (engineVer.contains("VER-2.2T22"))
//                                model.setAppVer("2010233");
//                            else
//                                model.setAppVer("else");
//                            model.setPlayHash(jsonNode.get("playHash").asText());
//                            model.setPlayNetDisk(jsonNode.get("playNetDisk").asText());
//                            model.setPlayTtNum(jsonNode.get("playTtNum").asText());
//                            model.setEngineVersion(jsonNode.has("engineVer") ? jsonNode.get("engineVer").asText().split(" ")[0] : "empty");
//                            model.setSelfDid(jsonNode.get("selfDid").asText());
//                            model.setPlayType(jsonNode.get("playType").asText());
//                            if (model.getPlayTtNum().equals("tt12361974#S1#E1"))
//                                model.setFlag("true");
//                            else
//                                model.setFlag("false");
//                            if (Strings.isNotEmpty(model.getSelfDid()))
//                                out.collect(model);
//                        } catch (Exception e) {
//                            e.printStackTrace();
//                        }
//                    }
//                }).returns(TypeInformation.of(MFCPMMessage.class));

        //注册表
//        tableEnv.registerDataStream("mfc_pm_buffering_analyse_tmp_group_by_app_ver", bufDataStreamGroupByAppver,
//                Arrays.stream(MFCPMBufferingMessage.class.getDeclaredFields())
//                        .collect(StringBuilder::new, (sb, v) -> sb.append(v.getName()).append(","), StringBuilder::append)
//                        .append("proctime.proctime").toString());

//        SingleOutputStreamOperator<ConnectedStreamData> connectedStreamGroupByAppVer = playingDataStreamGroupByAppver.connect(bufDataStreamGroupByAppver).map(new CoMapFunction<MFCPMMessage, MFCPMBufferingMessage, ConnectedStreamData>() {
//            @Override
//            public ConnectedStreamData map1(MFCPMMessage value) throws Exception {
//                ConnectedStreamData data = new ConnectedStreamData();
//                data.setSelfDid(value.getSelfDid());
//                data.setAppVer(value.getAppVer());
//                data.setType("play");
//                data.setPlayType(value.getPlayType());
//                data.setPlayTtNum(value.getPlayTtNum());
//                data.setFlag(value.getFlag());
//                data.setIsPrepare("true");
//                return data;
//            }
//
//            @Override
//            public ConnectedStreamData map2(MFCPMBufferingMessage value) throws Exception {
//                ConnectedStreamData data = new ConnectedStreamData();
//                data.setAppVer(value.getAppVer());
//                data.setDid(value.getMac());
//                data.setType("buf");
//                data.setPlayType(value.getPlayProtocol());
//                data.setPlayTtNum(null);
//                data.setFlag(value.getFlag());
//                data.setIsPrepare(value.getIsPrepare());
//                return data;
//            }
//        });

//        tableEnv.registerDataStream("mfc_pm_buf_rate_table_tmp_group_by_app_ver", connectedStreamGroupByAppVer,
//                Arrays.stream(ConnectedStreamData.class.getDeclaredFields())
//                        .collect(StringBuilder::new, (sb, v) -> sb.append(v.getName()).append(","), StringBuilder::append)
//                        .append("proctime.proctime").toString());

//        tableEnv.toAppendStream(tableEnv.sqlQuery("SELECT count(DISTINCT mac),count(1),avg(bufferingTime),playProtocol, appVer " +
//                "FROM mfc_pm_buffering_analyse_tmp_group_by_app_ver WHERE isPrepare ='false'" +
//                " GROUP BY TUMBLE(proctime,INTERVAL '2' MINUTE),playProtocol, appVer"), Row.class)
//                .map(v -> {
//                    Map<String, Object> value = new HashMap<>();
//                    value.put("userNum", Long.parseLong(v.getField(0).toString()));
//                    value.put("count", Long.parseLong(v.getField(1).toString()));
//                    value.put("bufferingTime", Long.parseLong(v.getField(2).toString()));
//                    Map<String, String> key = new HashMap<>();
//                    key.put("playProtocol", v.getField(3).toString());
//                    key.put("appVer", v.getField(4).toString());
//                    key.put("isPrepare", "false");
//                    return new Tuple2<>(value, key);
//                })
//                .returns(new TypeHint<Tuple2<Map<String, Object>, Map<String, String>>>() {
//                    @Override
//                    public TypeInformation<Tuple2<Map<String, Object>, Map<String, String>>> getTypeInfo() {
//                        return super.getTypeInfo();
//                    }
//                })
//                .addSink(new InfluxDBInterface("mfc_pm_buffering_analyse_tmp_group_by_app_ver", "mfc_prt_metric", "", PMInfluxDbUrl));
//
//        tableEnv.toAppendStream(tableEnv.sqlQuery("SELECT count(DISTINCT mac),count(1),avg(bufferingTime),playProtocol, appVer " +
//                "FROM mfc_pm_buffering_analyse_tmp_group_by_app_ver WHERE isPrepare ='true' " +
//                " GROUP BY TUMBLE(proctime,INTERVAL '2' MINUTE),playProtocol, appVer"), Row.class)
//                .map(v -> {
//                    Map<String, Object> value = new HashMap<>();
//                    value.put("userNum", Long.parseLong(v.getField(0).toString()));
//                    value.put("count", Long.parseLong(v.getField(1).toString()));
//                    value.put("bufferingTime", Long.parseLong(v.getField(2).toString()));
//                    Map<String, String> key = new HashMap<>();
//                    key.put("playProtocol", v.getField(3).toString());
//                    key.put("appVer", v.getField(4).toString());
//                    key.put("isPrepare", "true");
//                    return new Tuple2<>(value, key);
//                })
//                .returns(new TypeHint<Tuple2<Map<String, Object>, Map<String, String>>>() {
//                    @Override
//                    public TypeInformation<Tuple2<Map<String, Object>, Map<String, String>>> getTypeInfo() {
//                        return super.getTypeInfo();
//                    }
//                })
//                .addSink(new InfluxDBInterface("mfc_pm_buffering_analyse_tmp_group_by_app_ver", "mfc_prt_metric", "", PMInfluxDbUrl));


//        tableEnv.toAppendStream(tableEnv.sqlQuery("select count(DISTINCT selfDid),count(DISTINCT did)" +
//                ",count(DISTINCT playTtNum),playType, appVer from mfc_pm_buf_rate_table_tmp_group_by_app_ver where isPrepare = 'true' " +
//                "GROUP BY TUMBLE(proctime,INTERVAL '2' MINUTE),playType, appVer"), Row.class)
//                .map(v -> {
//                    Map<String, Object> value = new HashMap<>();
//                    long usercount = Long.parseLong(v.getField(0).toString());
//                    long bufcount = Long.parseLong(v.getField(1).toString());
//                    long mediacount = Long.parseLong(v.getField(2).toString());
//                    value.put("usercount", usercount);
//                    value.put("bufcount", bufcount);
//                    value.put("mediacount", mediacount);
//                    value.put("bufRate", 100 * (double) bufcount / (double) usercount);
//                    Map<String, String> key = new HashMap<>();
//                    key.put("playType", v.getField(3).toString());
//                    if (v.getField(4) != null )
//                        key.put("appVer", v.getField(4).toString());
//                    key.put("isPrepare", "true");
//                    return new Tuple2<>(value, key);
//                }).returns(new TypeHint<Tuple2<Map<String, Object>, Map<String, String>>>() {
//            @Override
//            public TypeInformation<Tuple2<Map<String, Object>, Map<String, String>>> getTypeInfo() {
//                return super.getTypeInfo();
//            }
//        }).addSink(new InfluxDBInterface("mfc_playing_pm_table_buf_tmp_group_by_app_ver", "mfc_prt_metric", "", PMInfluxDbUrl));

//        tableEnv.toAppendStream(tableEnv.sqlQuery("select count(DISTINCT selfDid),count(DISTINCT did)" +
//                ",count(DISTINCT playTtNum),playType, appVer from mfc_pm_buf_rate_table_tmp_group_by_app_ver " +
//                "GROUP BY TUMBLE(proctime,INTERVAL '2' MINUTE),playType, appVer"), Row.class)
//                .map(v -> {
//                    Map<String, Object> value = new HashMap<>();
//                    long usercount = Long.parseLong(v.getField(0).toString());
//                    long bufcount = Long.parseLong(v.getField(1).toString());
//                    long mediacount = Long.parseLong(v.getField(2).toString());
//                    value.put("usercount", usercount);
//                    value.put("bufcount", bufcount);
//                    value.put("mediacount", mediacount);
//                    value.put("bufRate", 100 * (double) bufcount / (double) usercount);
//                    Map<String, String> key = new HashMap<>();
//                    key.put("playType", v.getField(3).toString());
//                    if (v.getField(4) != null )
//                        key.put("appVer", v.getField(4).toString());
//                    key.put("isPrepare", "false");
//                    return new Tuple2<>(value, key);
//                }).returns(new TypeHint<Tuple2<Map<String, Object>, Map<String, String>>>() {
//            @Override
//            public TypeInformation<Tuple2<Map<String, Object>, Map<String, String>>> getTypeInfo() {
//                return super.getTypeInfo();
//            }
//        }).addSink(new InfluxDBInterface("mfc_playing_pm_table_buf_tmp_group_by_app_ver", "mfc_prt_metric", "", PMInfluxDbUrl));


//
//        tableEnv.toAppendStream(tableEnv.sqlQuery("select count(DISTINCT selfDid),count(DISTINCT did)" +
//                ",count(DISTINCT playTtNum),playType, appVer from mfc_pm_buf_rate_table_tmp_group_by_app_ver where isPrepare = 'true' " +
//                " GROUP BY TUMBLE(proctime,INTERVAL '2' MINUTE),playType, appVer"), Row.class)
//                .map(v -> {
//                    Map<String, Object> value = new HashMap<>();
//                    long usercount = Long.parseLong(v.getField(0).toString());
//                    long bufcount = Long.parseLong(v.getField(1).toString());
//                    long mediacount = Long.parseLong(v.getField(2).toString());
//                    value.put("usercount", usercount);
//                    value.put("bufcount", bufcount);
//                    value.put("mediacount", mediacount);
//                    value.put("bufRate", 100 * (double) bufcount / (double) usercount);
//                    Map<String, String> key = new HashMap<>();
//                    key.put("playType", v.getField(3).toString());
//                    if (v.getField(4) != null )
//                        key.put("appVer", v.getField(4).toString());
//                    key.put("isPrepare", "true");
//                    return new Tuple2<>(value, key);
//                }).returns(new TypeHint<Tuple2<Map<String, Object>, Map<String, String>>>() {
//            @Override
//            public TypeInformation<Tuple2<Map<String, Object>, Map<String, String>>> getTypeInfo() {
//                return super.getTypeInfo();
//            }
//        }).addSink(new InfluxDBInterface("mfc_pm_buf_rate_table_tmp_group_by_app_ver", "mfc_prt_metric", "", PMInfluxDbUrl));

//        tableEnv.toAppendStream(tableEnv.sqlQuery("select count(DISTINCT selfDid),count(DISTINCT did)" +
//                ",count(DISTINCT playTtNum),playType, appVer from mfc_pm_buf_rate_table_tmp_group_by_app_ver where isPrepare = 'false' " +
//                "GROUP BY TUMBLE(proctime,INTERVAL '2' MINUTE),playType, appVer"), Row.class)
//                .map(v -> {
//                    Map<String, Object> value = new HashMap<>();
//                    long usercount = Long.parseLong(v.getField(0).toString());
//                    long bufcount = Long.parseLong(v.getField(1).toString());
//                    long mediacount = Long.parseLong(v.getField(2).toString());
//                    value.put("usercount", usercount);
//                    value.put("bufcount", bufcount);
//                    value.put("mediacount", mediacount);
//                    value.put("bufRate", 100 * (double) bufcount / (double) usercount);
//                    Map<String, String> key = new HashMap<>();
//                    key.put("playType", v.getField(3).toString());
//                    if (v.getField(4) != null )
//                        key.put("appVer", v.getField(4).toString());
//                    key.put("isPrepare", "false");
//                    return new Tuple2<>(value, key);
//                }).returns(new TypeHint<Tuple2<Map<String, Object>, Map<String, String>>>() {
//            @Override
//            public TypeInformation<Tuple2<Map<String, Object>, Map<String, String>>> getTypeInfo() {
//                return super.getTypeInfo();
//            }
//        }).addSink(new InfluxDBInterface("mfc_pm_buf_rate_table_tmp_group_by_app_ver", "mfc_prt_metric", "", PMInfluxDbUrl));

        env.execute("MFCPRTBufferingAnalyseTmp");
    }
}
