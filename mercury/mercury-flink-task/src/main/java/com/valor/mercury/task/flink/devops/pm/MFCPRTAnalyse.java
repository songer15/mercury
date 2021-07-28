package com.valor.mercury.task.flink.devops.pm;

import com.valor.mercury.task.flink.MetricAnalyse;
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

import java.lang.reflect.Field;
import java.util.*;

import static com.valor.mercury.task.flink.util.Constants.*;

/**
 * @author Gavin
 * 2020/4/9 13:51
 */
public class MFCPRTAnalyse implements MetricAnalyse {

    private static final Logger logger = LoggerFactory.getLogger(MFCPRTAnalyse.class);

    @Override
    public void run(String[] args) throws Exception {
        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
        StreamTableEnvironment tableEnv = TableEnvironment.getTableEnvironment(env);

        // 配置kafka数据源
        Properties properties = new Properties();
        properties.setProperty("bootstrap.servers", PMBootstrapServer);
        properties.setProperty("group.id", groupID);
        FlinkKafkaConsumer<ObjectNode> kafkaConsumer =
                new FlinkKafkaConsumer<>(mfcPMTopicName, new JSONKeyValueDeserializationSchema(false), properties);
        if (args.length > 1 && args[1] != null) {
            try {
                //从指定的时间戳开始
                kafkaConsumer.setStartFromTimestamp(Long.parseLong(args[1]));
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else
            kafkaConsumer.setStartFromLatest();  // 设置读取最新的数据
        FlinkKafkaConsumer<ObjectNode> bufKafkaConsumer =
                new FlinkKafkaConsumer<>(mfcPMBufferingTopicName, new JSONKeyValueDeserializationSchema(false), properties);
        if (args.length > 1 && args[1] != null) {
            try {
                //从指定的时间戳开始
                bufKafkaConsumer.setStartFromTimestamp(Long.parseLong(args[1]));
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else
            bufKafkaConsumer.setStartFromLatest();  // 设置读取最新的数据

        // 拿到kafka的数据
        DataStream<ObjectNode> dataStream = env.addSource(kafkaConsumer);
        DataStream<ObjectNode> bufDataStream = env.addSource(bufKafkaConsumer);

        //获得卡顿资源数据流
        DataStream<MFCPMBufferingMessage> bufferingDataStream = bufDataStream
                .flatMap((ObjectNode value, Collector<MFCPMBufferingMessage> out) -> {
                    JsonNode jsonNode = value.get("value");
                    try {
                        if (jsonNode.has("mac")) {
                            MFCPMBufferingMessage message = new MFCPMBufferingMessage();
                            message.setTtID(jsonNode.get("ttid").asText());
                            message.setDid(jsonNode.get("mac").asText());
                            message.setPlayProtocol(jsonNode.get("playProtocol").asText());
                            message.setResourceSite(jsonNode.get("ResourceSite").asText());
                            message.setBufferingStartTime(Long.parseLong(jsonNode.get("bufferingStartTime").asText()));
                            message.setBufferingTime(Integer.parseInt(jsonNode.get("bufferingTime").asText()));
                            message.setAppVer(jsonNode.has("app_ver_code") ? jsonNode.get("app_ver_code").asText() : "default");
                            message.setAppName(jsonNode.has("app_ver_name") ? jsonNode.get("app_ver_name").asText() : "default");
                            message.setBrand(jsonNode.has("hw_brand") ? jsonNode.get("hw_brand").asText() : "default");
                            message.setBrand(jsonNode.has("hw_model") ? jsonNode.get("hw_model").asText() : "default");
                            message.setMac(jsonNode.get("mac").asText());
                            message.setAndroid(jsonNode.has("hw_android") ? jsonNode.get("hw_android").asText() : "default");
                            message.setAppId(jsonNode.has("app_id") ? jsonNode.get("app_id").asText() : "mfc_stb");

                            long prepareFirstBufferingTime = Long.parseLong(jsonNode.get("prepareFirstBufferingTime").asText());
                            if (prepareFirstBufferingTime > 5000)
                                //起播阶段的卡顿prepareFirstBufferingTime通常小于 5000ms， 过滤掉起播阶段的卡顿
                                message.setIsPrepare("true");
                            else
                                message.setIsPrepare("false");
                            out.collect(message);
                        }
                    } catch (Exception e) {
                        logger.error("MFCPMBufferingMessage error", e);
                    }
                }).returns(TypeInformation.of(MFCPMBufferingMessage.class));

        //获得在播资源数据流
        DataStream<MFCPMMessage> playingDataStream = dataStream
                .flatMap((ObjectNode value, Collector<MFCPMMessage> out) -> {
                    JsonNode jsonNode = value.get("value");
                    try {
                        MFCPMMessage model = new MFCPMMessage();
                        model.setPlayHash(jsonNode.get("playHash").asText());
                        model.setPlayNetDisk(jsonNode.get("playNetDisk").asText());
                        model.setPlayTtNum(jsonNode.get("playTtNum").asText());
                        model.setPlayType(jsonNode.get("playType").asText());
                        model.setEngineVersion(jsonNode.has("engineVer") ? jsonNode.get("engineVer").asText().split(" ")[0] : "empty");
                        model.setRecvBkFromPeerP(Double.parseDouble(jsonNode.get("recvBkFromPeerP").asText()));
                        model.setRecvBkFromPeerT(Double.parseDouble(jsonNode.get("recvBkFromPeerT").asText()));
                        model.setRecvBkFromPmP(Double.parseDouble(jsonNode.get("recvBkFromPmP").asText()));
                        model.setRecvBkFromPmT(Double.parseDouble(jsonNode.get("recvBkFromPmT").asText()));
                        model.setRecvBkFromStorageP(Double.parseDouble(jsonNode.get("recvBkFromStorageP").asText()));
                        model.setRecvBkFromStorageT(Double.parseDouble(jsonNode.get("recvBkFromStorageT").asText()));
                        model.setSelfDid(jsonNode.get("selfDid").asText());
                        model.setPeerNums(Double.parseDouble(jsonNode.get("peerNums").asText()));
                        model.setPlayBufferingTimes(Integer.parseInt(jsonNode.get("playBufferingTimes").asText()));
                        model.setAppVer(jsonNode.has("app_ver_code") ? jsonNode.get("app_ver_code").asText() : "default");
                        model.setAppName(jsonNode.has("app_ver_name") ? jsonNode.get("app_ver_name").asText() : "default");
                        model.setBrand(jsonNode.has("hw_brand") ? jsonNode.get("hw_brand").asText() : "default");
                        model.setBrand(jsonNode.has("hw_model") ? jsonNode.get("hw_model").asText() : "default");
                        model.setMac(jsonNode.has("mac") ? jsonNode.get("mac").asText() : "default");
                        model.setAndroid(jsonNode.has("hw_android") ? jsonNode.get("hw_android").asText() : "default");
                        model.setAppId(jsonNode.has("app_id") ? jsonNode.get("app_id").asText() : "mfc_stb");
                        if (model.getRecvBkFromPeerT() == 0 && model.getRecvBkFromPmT() == 0) {
                            model.setUseTag(0);
                            model.setShareRate(0d);
                        } else {
                            model.setUseTag(1);
                            Double shareRate = model.getRecvBkFromPeerT() / (model.getRecvBkFromPeerT() + model.getRecvBkFromPmT()) * 10000;
                            model.setShareRate(shareRate);
                        }
                        if (!model.verify()) ;
//                            System.out.println(value.toString());
                        if (Strings.isNotEmpty(model.getSelfDid()))
                            out.collect(model);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }).returns(TypeInformation.of(MFCPMMessage.class));

        //注册表
        tableEnv.registerDataStream("mfc_buffering_pm_action_table", bufferingDataStream,
                Arrays.stream(MFCPMBufferingMessage.class.getDeclaredFields())
                        .collect(StringBuilder::new, (sb, v) -> sb.append(v.getName()).append(","), StringBuilder::append)
                        .append("proctime.proctime").toString());
        tableEnv.registerDataStream(mfcPlayingPMTableName, playingDataStream,
                Arrays.stream(MFCPMMessage.class.getDeclaredFields())
                        .collect(StringBuilder::new, (sb, v) -> sb.append(v.getName()).append(","), StringBuilder::append)
                        .append("proctime.proctime").toString());

        //连接在播流和卡顿流，用于后续计算比例
        SingleOutputStreamOperator<ConnectedStreamData> connectedStream = playingDataStream.connect(bufferingDataStream).map(new CoMapFunction<MFCPMMessage, MFCPMBufferingMessage, ConnectedStreamData>() {
            @Override
            public ConnectedStreamData map1(MFCPMMessage value) throws Exception {
                ConnectedStreamData data = new ConnectedStreamData();
                data.setSelfDid(value.getSelfDid());
                data.setType("play");
                data.setAppId(value.getAppId());
                data.setPlayType(value.getPlayType());
                data.setIsPrepare("true");
                return data;
            }

            @Override
            public ConnectedStreamData map2(MFCPMBufferingMessage value) throws Exception {
                ConnectedStreamData data = new ConnectedStreamData();
                data.setDid(value.getMac());
                data.setType("buf");
                data.setAppId(value.getAppId());
                data.setPlayType(value.getPlayProtocol());
                data.setIsPrepare(value.getIsPrepare());
                return data;
            }
        });
        tableEnv.registerDataStream("mfc_pm_buf_rate_table", connectedStream,
                Arrays.stream(ConnectedStreamData.class.getDeclaredFields())
                        .collect(StringBuilder::new, (sb, v) -> sb.append(v.getName()).append(","), StringBuilder::append)
                        .append("proctime.proctime").toString());



        // 统计卡顿的用户数，次数，时长。按不同维度，分别写入3张表
        tableEnv.toAppendStream(tableEnv.sqlQuery("SELECT count(DISTINCT mac),count(1),avg(bufferingTime),appId, playProtocol " +
                "FROM " + "mfc_buffering_pm_action_table" +
                " GROUP BY TUMBLE(proctime,INTERVAL '2' MINUTE),appId,playProtocol"), Row.class)
                .map(v -> {
                    Map<String, Object> value = new HashMap<>();
                    value.put("userNum", Long.parseLong(v.getField(0).toString()));
                    value.put("count", Long.parseLong(v.getField(1).toString()));
                    value.put("bufferingTime", Long.parseLong(v.getField(2).toString()));
                    Map<String, String> key = new HashMap<>();
                    key.put("appId", v.getField(3).toString());
                    key.put("playType", v.getField(4).toString());
                    return new Tuple2<>(value, key);
                })
                .returns(new TypeHint<Tuple2<Map<String, Object>, Map<String, String>>>() {
                    @Override
                    public TypeInformation<Tuple2<Map<String, Object>, Map<String, String>>> getTypeInfo() {
                        return super.getTypeInfo();
                    }
                }).addSink(new InfluxDBInterface("mfc_buffering_appid_playtype", "mfc_prt_metric", "", PMInfluxDbUrl));

        tableEnv.toAppendStream(tableEnv.sqlQuery("SELECT count(DISTINCT mac),count(1),avg(bufferingTime),appId, playProtocol " +
                "FROM " + "mfc_buffering_pm_action_table where isPrepare = 'true' " +
                " GROUP BY TUMBLE(proctime,INTERVAL '2' MINUTE),appId,playProtocol"), Row.class)
                .map(v -> {
                    Map<String, Object> value = new HashMap<>();
                    value.put("userNum", Long.parseLong(v.getField(0).toString()));
                    value.put("count", Long.parseLong(v.getField(1).toString()));
                    value.put("bufferingTime", Long.parseLong(v.getField(2).toString()));
                    Map<String, String> key = new HashMap<>();
                    key.put("appId", v.getField(3).toString());
                    key.put("playType", v.getField(4).toString());

                    return new Tuple2<>(value, key);
                })
                .returns(new TypeHint<Tuple2<Map<String, Object>, Map<String, String>>>() {
                    @Override
                    public TypeInformation<Tuple2<Map<String, Object>, Map<String, String>>> getTypeInfo() {
                        return super.getTypeInfo();
                    }
                }).addSink(new InfluxDBInterface("mfc_buffering_without_prepare", "mfc_prt_metric", "", PMInfluxDbUrl));


        tableEnv.toAppendStream(tableEnv.sqlQuery("SELECT count(DISTINCT mac),count(1),avg(bufferingTime) " +
                "FROM " + "mfc_buffering_pm_action_table" +
                " GROUP BY TUMBLE(proctime,INTERVAL '2' MINUTE)"), Row.class)
                .map(v -> {
                    Map<String, Object> value = new HashMap<>();
                    value.put("userNum", Long.parseLong(v.getField(0).toString()));
                    value.put("count", Long.parseLong(v.getField(1).toString()));
                    value.put("bufferingTime", Long.parseLong(v.getField(2).toString()));
                    Map<String, String> key = new HashMap<>();
                    key.put("appId", "all");
                    key.put("playType", "all");
                    return new Tuple2<>(value, key);
                })
                .returns(new TypeHint<Tuple2<Map<String, Object>, Map<String, String>>>() {
                    @Override
                    public TypeInformation<Tuple2<Map<String, Object>, Map<String, String>>> getTypeInfo() {
                        return super.getTypeInfo();
                    }
                }).addSink(new InfluxDBInterface("mfc_buffering_all", "mfc_prt_metric", "", PMInfluxDbUrl));




        // 统计卡顿率，按appId, playType维度，分别写入3张表
        tableEnv.toAppendStream(tableEnv.sqlQuery("select count(DISTINCT selfDid),count(DISTINCT did),appId, playType from mfc_pm_buf_rate_table " +
                "GROUP BY TUMBLE(proctime,INTERVAL '2' MINUTE),appId,playType"), Row.class)
                .map(v -> {
                    Map<String, Object> value = new HashMap<>();
                    long usercount = Long.parseLong(v.getField(0).toString());
                    long bufcount = Long.parseLong(v.getField(1).toString());
                    String appId = v.getField(2).toString();
                    String playType = v.getField(3).toString();
                    value.put("usercount", usercount);
                    value.put("bufcount", bufcount);
                    value.put("bufRate", 100 * (double) bufcount / (double) usercount);
                    Map<String, String> key = new HashMap<>();
                    key.put("appId", appId);
                    key.put("playType", playType);
                    return new Tuple2<>(value, key);
                }).returns(new TypeHint<Tuple2<Map<String, Object>, Map<String, String>>>() {
            @Override
            public TypeInformation<Tuple2<Map<String, Object>, Map<String, String>>> getTypeInfo() {
                return super.getTypeInfo();
            }
        }).addSink(new InfluxDBInterface("mfc_buffering_rate_appid_playtype", "mfc_prt_metric", "", PMInfluxDbUrl));


        tableEnv.toAppendStream(tableEnv.sqlQuery("select count(DISTINCT selfDid),count(DISTINCT did),appId, playType from mfc_pm_buf_rate_table where isPrepare = 'true'" +
                "GROUP BY TUMBLE(proctime,INTERVAL '2' MINUTE),appId,playType"), Row.class)
                .map(v -> {
                    Map<String, Object> value = new HashMap<>();
                    long usercount = Long.parseLong(v.getField(0).toString());
                    long bufcount = Long.parseLong(v.getField(1).toString());
                    String appId = v.getField(2).toString();
                    String playType = v.getField(3).toString();
                    value.put("usercount", usercount);
                    value.put("bufcount", bufcount);
                    value.put("bufRate", 100 * (double) bufcount / (double) usercount);
                    Map<String, String> key = new HashMap<>();
                    key.put("appId", appId);
                    key.put("playType", playType);
                    return new Tuple2<>(value, key);
                }).returns(new TypeHint<Tuple2<Map<String, Object>, Map<String, String>>>() {
            @Override
            public TypeInformation<Tuple2<Map<String, Object>, Map<String, String>>> getTypeInfo() {
                return super.getTypeInfo();
            }
        }).addSink(new InfluxDBInterface("mfc_buffering_rate_without_prepare", "mfc_prt_metric", "", PMInfluxDbUrl));


        tableEnv.toAppendStream(tableEnv.sqlQuery("select count(DISTINCT selfDid),count(DISTINCT did) from mfc_pm_buf_rate_table " +
                "GROUP BY TUMBLE(proctime,INTERVAL '2' MINUTE)"), Row.class)
                .map(v -> {
                    Map<String, Object> value = new HashMap<>();
                    long usercount = Long.parseLong(v.getField(0).toString());
                    long bufcount = Long.parseLong(v.getField(1).toString());
                    value.put("usercount", usercount);
                    value.put("bufcount", bufcount);
                    value.put("bufRate", 100 * (double) bufcount / (double) usercount);
                    Map<String, String> key = new HashMap<>();
                    key.put("appId", "all");
                    key.put("playType", "all");
                    return new Tuple2<>(value, key);
                }).returns(new TypeHint<Tuple2<Map<String, Object>, Map<String, String>>>() {
            @Override
            public TypeInformation<Tuple2<Map<String, Object>, Map<String, String>>> getTypeInfo() {
                return super.getTypeInfo();
            }
        }).addSink(new InfluxDBInterface("mfc_buffering_rate_all", "mfc_prt_metric", "", PMInfluxDbUrl));





        //查询数据表得到（每2分钟出一份数据）
        tableEnv.toAppendStream(tableEnv.sqlQuery("SELECT count(DISTINCT selfDid),count(DISTINCT playTtNum),avg(peerNums),sum(playBufferingTimes)," +
                "sum(recvBkFromPeerT),sum(recvBkFromPeerT+recvBkFromPmT)," +
                "sum(shareRate),sum(useTag),avg(shareRate),appId, playType " +
                "FROM " + mfcPlayingPMTableName +
                " GROUP BY TUMBLE(proctime,INTERVAL '2' MINUTE),appId, playType"), Row.class)
                .map(v -> {
                    Map<String, Object> value = new HashMap<>();
                    value.put("userNum", Long.parseLong(v.getField(0).toString()));
                    value.put("movieNum", Long.parseLong(v.getField(1).toString()));
                    Double peerNums = Double.parseDouble(v.getField(2).toString()) * 100;
                    value.put("peerNums", peerNums.intValue());
                    value.put("avg1", Double.parseDouble(v.getField(4).toString()));
                    value.put("avg2", Double.parseDouble(v.getField(5).toString()));
                    value.put("shareRates", Double.parseDouble(v.getField(6).toString()));
                    value.put("sumShareRate", Double.parseDouble(v.getField(7).toString()));
                    Double shareRate = Double.parseDouble(v.getField(8).toString());
                    value.put("shareRate", shareRate.intValue());
                    Map<String, String> key = new HashMap<>();
                    key.put("appId", v.getField(9).toString());
                    key.put("playType", v.getField(10).toString());
                    return new Tuple2<>(value, key);
                })
                .returns(new TypeHint<Tuple2<Map<String, Object>, Map<String, String>>>() {
                    @Override
                    public TypeInformation<Tuple2<Map<String, Object>, Map<String, String>>> getTypeInfo() {
                        return super.getTypeInfo();
                    }
                }).addSink(new InfluxDBInterface("mfc_playing_pm_table", "mfc_prt_metric", "", PMInfluxDbUrl));

        tableEnv.toAppendStream(tableEnv.sqlQuery("SELECT count(DISTINCT selfDid),count(DISTINCT playTtNum),avg(peerNums),sum(playBufferingTimes)," +
                "sum(recvBkFromPeerT),sum(recvBkFromPeerT+recvBkFromPmT)," +
                "sum(shareRate),sum(useTag),avg(shareRate)" +
                "FROM " + mfcPlayingPMTableName +
                " GROUP BY TUMBLE(proctime,INTERVAL '2' MINUTE)"), Row.class)
                .map(v -> {
                    Map<String, Object> value = new HashMap<>();
                    value.put("userNum", Long.parseLong(v.getField(0).toString()));
                    value.put("movieNum", Long.parseLong(v.getField(1).toString()));
                    Double peerNums = Double.parseDouble(v.getField(2).toString()) * 100;
                    value.put("peerNums", peerNums.intValue());
                    value.put("avg1", Double.parseDouble(v.getField(4).toString()));
                    value.put("avg2", Double.parseDouble(v.getField(5).toString()));
                    value.put("shareRates", Double.parseDouble(v.getField(6).toString()));
                    value.put("sumShareRate", Double.parseDouble(v.getField(7).toString()));
                    Double shareRate = Double.parseDouble(v.getField(8).toString());
                    value.put("shareRate", shareRate.intValue());
                    Map<String, String> key = new HashMap<>();
                    key.put("appId", "all");
                    key.put("playType", "all");
                    return new Tuple2<>(value, key);
                })
                .returns(new TypeHint<Tuple2<Map<String, Object>, Map<String, String>>>() {
                    @Override
                    public TypeInformation<Tuple2<Map<String, Object>, Map<String, String>>> getTypeInfo() {
                        return super.getTypeInfo();
                    }
                }).addSink(new InfluxDBInterface("mfc_playing_pm_table", "mfc_prt_metric", "", PMInfluxDbUrl));




        //在播资源的用户数，影片数，分享率，平均Peer数,卡顿次数（总）
        //查询数据表得到（每2分钟出一份数据）
        String playEngineSQL = "SELECT count(DISTINCT selfDid),count(DISTINCT playTtNum),engineVersion,appId " +
                "FROM " + mfcPlayingPMTableName +
                " GROUP BY TUMBLE(proctime,INTERVAL '2' MINUTE),engineVersion,appId";
        tableEnv.toAppendStream(tableEnv.sqlQuery(playEngineSQL), Row.class)
                .map(v -> {
                    Map<String, Object> value = new HashMap<>();
                    value.put("userNum", Long.parseLong(v.getField(0).toString()));
                    value.put("movieNum", Long.parseLong(v.getField(1).toString()));
                    Map<String, String> key = new HashMap<>();
                    key.put("engineVersion", v.getField(2).toString());
                    key.put("appId", v.getField(3).toString());
                    return new Tuple2<>(value, key);
                })
                .returns(new TypeHint<Tuple2<Map<String, Object>, Map<String, String>>>() {
                    @Override
                    public TypeInformation<Tuple2<Map<String, Object>, Map<String, String>>> getTypeInfo() {
                        return super.getTypeInfo();
                    }
                })
                .addSink(new InfluxDBInterface("mfc_playing_pm_table_engine", "mfc_prt_metric", "", PMInfluxDbUrl));


        //在播资源Top100
        //查询数据表得到（每小时出一份数据）
        String sqlTop = "SELECT playTtNum,count(1),playHash,playNetDisk,count(DISTINCT selfDid),avg(shareRate),avg(peerNums),sum(playBufferingTimes)," +
                "sum(recvBkFromPeerT),sum(recvBkFromPeerT+recvBkFromPmT)," +
                "sum(shareRate),sum(useTag),appId " +
                "FROM " + mfcPlayingPMTableName +
                " GROUP BY TUMBLE(proctime,INTERVAL '1' HOUR),playTtNum,playHash,playNetDisk,appId";
        tableEnv.toAppendStream(tableEnv.sqlQuery(sqlTop), Row.class)
                .map(value -> {
                    MFCPMTopQueryResult result = new MFCPMTopQueryResult();
                    result.setTtId(value.getField(0).toString());
                    result.setCount(((Number) value.getField(1)).longValue());
                    result.setPlayHash(value.getField(2).toString());
                    result.setPlayNetDisk(value.getField(3).toString());
                    result.setUniqueCount(((Number) value.getField(4)).longValue());
                    result.setShareRate(Double.parseDouble(value.getField(5).toString()));
                    Double peerNums = Double.parseDouble(value.getField(6).toString()) * 100;
                    result.setPeerNums(peerNums.intValue());
                    result.setPlayBufferingTimes(((Number) value.getField(7)).intValue());
                    result.setAvg1(Double.parseDouble(value.getField(8).toString()));
                    result.setAvg2(Double.parseDouble(value.getField(9).toString()));
                    result.setAvg3(Double.parseDouble(value.getField(10).toString()));
                    result.setAvg4(Double.parseDouble(value.getField(11).toString()));
                    result.setAppId(value.getField(12).toString());
                    return result;
                }).returns(TypeInformation.of(MFCPMTopQueryResult.class))
                .keyBy(MFCPMTopQueryResult::getKey)
                .process(new TopNItemsProcess(100, mfcPlayingPMTableName))
                .returns(TypeInformation.of(MFCPMTopQueryResult.class))
                .map(v -> {
                    Map<String, String> key = new HashMap<>();
                    key.put("Media", v.getTtId());
                    key.put("CloudDisk", v.getPlayNetDisk());
                    key.put("PlayHash", v.getPlayHash());
                    key.put("appId", v.getAppId());
                    return new Tuple2<>(objectToMap(v), key);
                })
                .returns(new TypeHint<Tuple2<Map<String, Object>, Map<String, String>>>() {
                    @Override
                    public TypeInformation<Tuple2<Map<String, Object>, Map<String, String>>> getTypeInfo() {
                        return super.getTypeInfo();
                    }
                })
                .addSink(new InfluxDBInterface("mfc_playing_pm_top_table", "mfc_prt_metric", "", PMInfluxDbUrl));

        //本地资源数据流
        DataStream<MFCPMMessage> localDataStream = dataStream
                .flatMap((ObjectNode value, Collector<MFCPMMessage> out) -> {
                    JsonNode jsonNode = value.get("value");
                    try {
                        String did = jsonNode.get("selfDid").asText();
                        String appId = jsonNode.has("app_id") ? jsonNode.get("app_id").asText() : "mfc_stb";
                        int i = 0;
                        while (jsonNode.has("ttNum_" + i)) {
                            MFCPMMessage model = new MFCPMMessage();
                            model.setSelfDid(did);
                            model.setPlayHash(jsonNode.get("resHash_" + i).asText());
                            model.setPlayTtNum(jsonNode.get("ttNum_" + i).asText());
                            model.setPlayNetDisk(jsonNode.get("netDisk_" + i).asText());
                            model.setCacheType(jsonNode.get("cacheType_" + i).asText());
                            model.setAppId(appId);
                            if (!model.verify()) ;
//                                System.out.println(value.toString());
                            if (Strings.isNotEmpty(model.getSelfDid()))
                                out.collect(model);
                            i++;
                        }
                    } catch (Exception e) {
//                        e.printStackTrace();
                    }
                }).returns(TypeInformation.of(MFCPMMessage.class));
        //注册表
        tableEnv.registerDataStream(mfcLocalPMTableName, localDataStream,
                Arrays.stream(MFCPMMessage.class.getDeclaredFields())
                        .collect(StringBuilder::new, (sb, v) -> sb.append(v.getName()).append(","), StringBuilder::append)
                        .append("proctime.proctime").toString());

        //缓存资源的用户数，影片数
        //查询数据表得到（每2分钟出一份数据）
        String localSql = "SELECT count(DISTINCT selfDid),count(DISTINCT playTtNum),cacheType,appId " +
                "FROM " + mfcLocalPMTableName +
                " GROUP BY TUMBLE(proctime,INTERVAL '2' MINUTE),cacheType,appId";
        tableEnv.toAppendStream(tableEnv.sqlQuery(localSql), Row.class)
                .map(v -> {
                    Map<String, Object> value = new HashMap<>();
                    value.put("userNum", Long.parseLong(v.getField(0).toString()));
                    value.put("movieNum", Long.parseLong(v.getField(1).toString()));
                    Map<String, String> key = new HashMap<>();
                    key.put("cacheType", v.getField(2).toString());
                    key.put("appId", v.getField(3).toString());
                    return new Tuple2<>(value, key);
                })
                .returns(new TypeHint<Tuple2<Map<String, Object>, Map<String, String>>>() {
                    @Override
                    public TypeInformation<Tuple2<Map<String, Object>, Map<String, String>>> getTypeInfo() {
                        return super.getTypeInfo();
                    }
                })
                .addSink(new InfluxDBInterface("mfc_local_pm_table", "mfc_prt_metric", "", PMInfluxDbUrl));


        //缓存资源的Top100
        //查询数据表得到（每小时出一份数据）
        String localSqlTop = "SELECT playTtNum,count(1),playHash,playNetDisk,count(DISTINCT selfDid),cacheType,appId " +
                "FROM " + mfcLocalPMTableName +
                " GROUP BY TUMBLE(proctime,INTERVAL '1' HOUR),playTtNum,playHash,playNetDisk,cacheType,appId";
        tableEnv.toAppendStream(tableEnv.sqlQuery(localSqlTop), Row.class)
                .map(value -> {
                    MFCPMTopQueryResult result = new MFCPMTopQueryResult();
                    result.setTtId(value.getField(0).toString());
                    result.setCount(((Number) value.getField(1)).longValue());
                    result.setPlayHash(value.getField(2).toString());
                    result.setPlayNetDisk(value.getField(3).toString());
                    result.setUniqueCount(((Number) value.getField(4)).longValue());
                    result.setCacheType(value.getField(5).toString());
                    result.setAppId(value.getField(6).toString());
                    return result;
                }).returns(TypeInformation.of(MFCPMTopQueryResult.class))
                .keyBy(MFCPMTopQueryResult::getKey)
                .process(new TopNItemsProcess(100, mfcLocalPMTableName))
                .returns(TypeInformation.of(MFCPMTopQueryResult.class))
                .map(v -> {
                    Map<String, String> key = new HashMap<>();
                    key.put("Media", v.getTtId());
                    key.put("CloudDisk", v.getPlayNetDisk());
                    key.put("PlayHash", v.getPlayHash());
                    key.put("CacheType", v.getCacheType());
                    key.put("appId", v.getAppId());
                    return new Tuple2<>(objectToMap(v), key);
                })
                .returns(new TypeHint<Tuple2<Map<String, Object>, Map<String, String>>>() {
                    @Override
                    public TypeInformation<Tuple2<Map<String, Object>, Map<String, String>>> getTypeInfo() {
                        return super.getTypeInfo();
                    }
                })
                .addSink(new InfluxDBInterface("mfc_local_pm_top_table", "mfc_prt_metric", "", PMInfluxDbUrl));

        env.execute("MFCPRTAnalyse");
    }

    private static Map<String, Object> objectToMap(Object object) {
        Map<String, Object> result = new HashMap<>();
        Field[] fields = object.getClass().getDeclaredFields();
        try {
            for (Field field : fields) {
                field.setAccessible(true);
                String name = field.getName();
                result.put(name, field.get(object));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }
}
