package com.valor.mercury.task.flink.devops;

import com.valor.mercury.task.flink.MetricAnalyse;
import com.valor.mercury.task.flink.devops.pm.MFCPMMessage;
import com.valor.mercury.task.flink.devops.pm.MFCPMTopQueryResult;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.apache.flink.api.common.typeinfo.TypeInformation;
import org.apache.flink.configuration.Configuration;
import org.apache.flink.shaded.jackson2.com.fasterxml.jackson.databind.JsonNode;
import org.apache.flink.shaded.jackson2.com.fasterxml.jackson.databind.node.ObjectNode;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.functions.sink.RichSinkFunction;
import org.apache.flink.streaming.connectors.kafka.FlinkKafkaConsumer;
import org.apache.flink.streaming.util.serialization.JSONKeyValueDeserializationSchema;
import org.apache.flink.table.api.TableEnvironment;
import org.apache.flink.table.api.java.StreamTableEnvironment;
import org.apache.flink.types.Row;
import org.apache.flink.util.Collector;
import org.apache.logging.log4j.util.Strings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileWriter;
import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.Properties;

import static com.valor.mercury.task.flink.util.Constants.*;

/**
 * 每天固定时间段输出统计结果到csv文件
 */
public class MFCTrafficAnalyse implements MetricAnalyse {

    public static LocalTime start = LocalTime.of(9, 0);
    public static LocalTime end = LocalTime.of(9, 30);
    public static DateTimeFormatter timeKeyFormatter =  DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
    public static DateTimeFormatter dirFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    public static DateTimeFormatter fileNameFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd-HH-mm");

    private static final Logger logger = LoggerFactory.getLogger(MFCTrafficAnalyse.class);

    @Override
    public void run(String[] args) throws Exception {

        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
        StreamTableEnvironment tableEnv = TableEnvironment.getTableEnvironment(env);

        logger.info("start hour {}", Integer.parseInt(args[1]));
        logger.info("end hour {}", Integer.parseInt(args[2]));

        start = LocalTime.of(Integer.parseInt(args[1]), 0);
        end  = LocalTime.of(Integer.parseInt(args[2]), 0);
        // 配置kafka数据源
        Properties properties = new Properties();
        properties.setProperty("bootstrap.servers", PMBootstrapServer);
        properties.setProperty("group.id", "Metric_traffic");
        FlinkKafkaConsumer<ObjectNode> kafkaConsumer = new FlinkKafkaConsumer<>(mfcPMTopicName, new JSONKeyValueDeserializationSchema(false), properties);
        kafkaConsumer.setStartFromLatest();  // 设置读取最新的数据

        DataStream<ObjectNode> pmStream = env.addSource(kafkaConsumer);
        //资源数据流
        DataStream<MFCPMMessage> resourceStream = pmStream
                .flatMap((ObjectNode value, Collector<MFCPMMessage> out) -> {
                    JsonNode jsonNode = value.get("value");
                    try {
                        String did = jsonNode.get("selfDid").asText();
                        int i = 0;
                        // 缓存资源
                        while (jsonNode.has("ttNum_" + i)) {
                            MFCPMMessage model = new MFCPMMessage();
                            model.setSelfDid(did);
                            model.setPlayType(jsonNode.get("playType") != null ? jsonNode.get("playType").asText() :  "default");
                            model.setPlayHash(jsonNode.get("resHash_" + i).asText());
                            model.setPlayTtNum(jsonNode.get("ttNum_" + i).asText());
                            model.setPlayNetDisk(jsonNode.get("netDisk_" + i).asText());
                            model.setCacheType(jsonNode.get("cacheType_" + i).asText());
                            model.setRecvBkFromPeerP(Double.parseDouble(jsonNode.get("recvBkFromPeerP").asText()));
                            model.setRecvBkFromPeerT(Double.parseDouble(jsonNode.get("recvBkFromPeerT").asText()));
                            model.setRecvBkFromPmP(Double.parseDouble(jsonNode.get("recvBkFromPmP").asText()));
                            model.setRecvBkFromPmT(Double.parseDouble(jsonNode.get("recvBkFromPmT").asText()));
                            model.setRecvBkFromStorageP(Double.parseDouble(jsonNode.get("recvBkFromStorageP").asText()));
                            model.setRecvBkFromStorageT(Double.parseDouble(jsonNode.get("recvBkFromStorageT").asText()));
                            model.setPeerNums(Double.parseDouble(jsonNode.get("peerNums").asText()));
                            model.setResourceType("buffer");
                            if (Strings.isNotEmpty(model.getSelfDid()))
                                out.collect(model);
                            i++;
                        }
                        // 在播资源
                        MFCPMMessage model = new MFCPMMessage();
                        model.setSelfDid(did);
                        model.setPlayType(jsonNode.get("playType") != null ? jsonNode.get("playType").asText() :  "default");
                        model.setPlayHash(jsonNode.get("playHash").asText());
                        model.setPlayNetDisk(jsonNode.get("playNetDisk").asText());
                        model.setPlayTtNum(jsonNode.get("playTtNum").asText());
                        model.setEngineVersion(jsonNode.has("engineVer") ? jsonNode.get("engineVer").asText().split(" ")[0] : "empty");
                        model.setRecvBkFromPeerP(Double.parseDouble(jsonNode.get("recvBkFromPeerP").asText()));
                        model.setRecvBkFromPeerT(Double.parseDouble(jsonNode.get("recvBkFromPeerT").asText()));
                        model.setRecvBkFromPmP(Double.parseDouble(jsonNode.get("recvBkFromPmP").asText()));
                        model.setRecvBkFromPmT(Double.parseDouble(jsonNode.get("recvBkFromPmT").asText()));
                        model.setRecvBkFromStorageP(Double.parseDouble(jsonNode.get("recvBkFromStorageP").asText()));
                        model.setRecvBkFromStorageT(Double.parseDouble(jsonNode.get("recvBkFromStorageT").asText()));
                        model.setPeerNums(Double.parseDouble(jsonNode.get("peerNums").asText()));
                        model.setPlayBufferingTimes(Integer.parseInt(jsonNode.get("playBufferingTimes").asText()));
                        model.setMac(jsonNode.has("mac") ? jsonNode.get("mac").asText() : "default");
                        if (model.getRecvBkFromPeerT() == 0 && model.getRecvBkFromPmT() == 0) {
                            model.setUseTag(0);
                            model.setShareRate(0d);
                        } else {
                            model.setUseTag(1);
                            Double shareRate = model.getRecvBkFromPeerT() / (model.getRecvBkFromPeerT() + model.getRecvBkFromPmT()) * 10000;
                            model.setShareRate(shareRate);
                        }
                        model.setResourceType("playing");

                        if (Strings.isNotEmpty(model.getSelfDid()))
                            out.collect(model);
                    } catch (Exception e) {

                    }
                }).returns(TypeInformation.of(MFCPMMessage.class));

        tableEnv.registerDataStream(mfcLocalPMTableName, resourceStream,
                Arrays.stream(MFCPMMessage.class.getDeclaredFields())
                        .collect(StringBuilder::new, (sb, v) -> sb.append(v.getName()).append(","), StringBuilder::append)
                        .append("proctime.proctime").toString());

        tableEnv.toAppendStream(tableEnv.sqlQuery(
                "SELECT playTtNum, " +
                "playHash, " +
                "resourceType, " +
                "playType, " +
                "count(1), " +
                "count(DISTINCT selfDid), " +
                "sum(recvBkFromPeerT)," +
                "sum(recvBkFromPmT)," +
                "sum(recvBkFromPeerT) / sum(recvBkFromPeerT+recvBkFromPmT)," +
                "avg(peerNums)" +
                "FROM " + mfcLocalPMTableName +
                " GROUP BY TUMBLE(proctime,INTERVAL '2' MINUTE),playTtNum,playHash, playType, resourceType"), Row.class)
                .map(value -> {
                    try {
                        MFCPMTopQueryResult result = new MFCPMTopQueryResult();
                        result.setTtId(value.getField(0).toString());
                        result.setPlayHash(value.getField(1).toString());
                        result.setResourceType(value.getField(2).toString());
                        result.setPlayType(value.getField(3).toString());
                        result.setCount(((Number) value.getField(4)).longValue());
                        result.setUniqueCount(((Number) value.getField(5)).longValue());
                        result.setRecvBkFromPeerT((Double.parseDouble(value.getField(6).toString())));
                        result.setRecvBkFromPmT((Double.parseDouble(value.getField(7).toString())));
                        result.setShareRate((Double.parseDouble(value.getField(8).toString())));
                        Double peerNums = Double.parseDouble(value.getField(9).toString());
                        result.setPeerNums(peerNums.intValue());
                        result.setTime(LocalDateTime.now().format(timeKeyFormatter));

                        return result;
                    } catch (Exception ex) {
                        logger.error("sql query error", ex);
                        return new MFCPMTopQueryResult();
                    }

                }).returns(TypeInformation.of(MFCPMTopQueryResult.class))

                .addSink(new FileSink());

        env.execute("MFCTrafficAnalyse");


    }


    public static class FileSink extends RichSinkFunction<MFCPMTopQueryResult> implements Serializable {
        private volatile String fileName;
        private volatile CSVPrinter printer;

        public FileSink() {

        }

        @Override
        public void open(Configuration configuration) throws Exception {
            super.open(configuration);
        }

        @Override
        public void invoke(MFCPMTopQueryResult value, Context context) throws Exception {
            try {
                LocalTime now = LocalTime.now();
                //输出每天固定时间段的数据
                if (now.isAfter(start) && now.isBefore(end)) {
                    //每次输出结果时新生成csv文件
                    String dirName = LocalDate.now().format(dirFormatter);
                    File dir = new File(dirName);
                    if (!dir.exists())
                        dir.mkdir();
                    String currentFileName =  dir + "/" + LocalDateTime.now().format(fileNameFormatter) + ".csv";
                    if (!currentFileName.equals(fileName)) {
                        fileName = currentFileName;
                        printer = new CSVPrinter(new FileWriter(fileName), CSVFormat.EXCEL);
                        printer.printRecord("time", "playHash", "playType", "ttId", "resourceType", "shareRate", "recvBkFromPmT", "recvBkFromPeerT", "users", "count", "peersNum");
                    }

                    printer.printRecord(
                            value.getTime(),
                            value.getPlayHash(),
                            value.getPlayType(),
                            value.getTtId(),
                            value.getResourceType(),
                            value.getShareRate(),
                            value.getRecvBkFromPmT(),
                            value.getRecvBkFromPeerT(),
                            value.getUniqueCount(),
                            value.getCount(),
                            value.getPeerNums());

                }
            } catch (Exception ex) {
                logger.error("write to csv error", ex);
            }
        }

        @Override
        public void close() throws Exception {
            super.close();
        }
    }
}
