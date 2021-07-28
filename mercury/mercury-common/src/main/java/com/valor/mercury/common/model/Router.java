package com.valor.mercury.common.model;


import com.fasterxml.jackson.core.type.TypeReference;
import com.google.common.collect.Lists;
import com.valor.mercury.common.formatter.ObjectFormatter;
import com.valor.mercury.common.util.JsonUtils;
import com.valor.mercury.common.util.PostUtil;
import com.valor.mercury.common.util.ProcessFieldTool;
import org.apache.commons.lang3.StringUtils;

import java.io.IOException;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Map;


public class Router extends AbstractLMI {
    public long instanceId;
    public String type;
    public String ipField;
    public String mapping;
    public int mappingApplyLevel = 1;
    public List<ObjectFormatter> formatFilter = Lists.newArrayList(new ObjectFormatter("preset_custom_ip", "String", "preset_custom_ip", "String")); //mapping的反序列化对象
    public Dest dest;
    public String destLocation;
    public Map<String, Object> consumerConfig;


    //kafka
    public String topicName;
    public int partitions = 3;
    public int replications = 1;
    public long retentionMillis = 604800000;
    public long retentionBytes = 1073741824;
    public long messageMaxBytes = 52428700;
    public String cleanupPolicy;

    //es
    public String indexName;
    public IndexNameStrategy indexNameStrategy; //索引命名策略
    public String indexMapping = "";    //索引mapping表
    public int shardNum = 3;
    public int replicaNum = 1;
    public String idField;
    public boolean needUpdateMapping = false;
    public volatile String currentIndexName;

    //Hdfs
    public String basePath;
    public FileNameStrategy fileNameStrategy = FileNameStrategy.DAY;
    public String path;
    public String baseReplacePath;
    public String replacePath;
    public FileNameStrategy replaceFileNameStrategy = FileNameStrategy.MONTH;


    //influxdb
    public String database;
    public String measurement;
    public String tags; //作为tag的字段，以逗号分隔 e.g. "app_ver,release_id"
    private String retentionPolicy;

    public Router(long instanceId, String type, String topicName) {
        this.instanceId = instanceId;
        this.type = type;
        this.topicName = topicName;
    }

    public Router(long instanceId) {
        this.instanceId = instanceId;
    }

    public Router() {}

    public enum Dest {
        ES("ElasticSearch"),
        KAFKA("Kafka"),
        INFLUXDB("InfluxDB"),
        HDFS("HDFS");

        public String value;

        Dest(String value) { this.value = value; }

        public static Dest fromString(String text) {
            for (Dest b : Dest.values()) {
                if (b.value.equalsIgnoreCase(text)) {
                    return b;
                }
            }
            return null;
        }
    }

    public enum FileNameStrategy {
        HOUR(1),
        DAY(2),
        WEEK(3),
        MONTH(4),
        QUARTER(5),
        YEAR(6);
        public int value;

        FileNameStrategy(int value) {this.value = value;}

        public static FileNameStrategy fromInteger(int nameStrategy) {
            for (FileNameStrategy h : FileNameStrategy.values()) {
                if (h.value == nameStrategy ) {
                    return h;
                }
            }
            return getDefault();
        }

        public static FileNameStrategy getDefault(){
            return DAY;
        }

        public String getFileName() {
            Calendar calendar = Calendar.getInstance();
            int year = calendar.get(Calendar.YEAR);
            int month = calendar.get(Calendar.MONTH) + 1;
            int day = calendar.get(Calendar.DATE);
            int hour  = calendar.get(Calendar.HOUR_OF_DAY);
            switch (this) {
                case HOUR:
                    return  "-" + year + "-" + month + "-" + day + "-" + hour;
                case DAY:
                    return  "-" + year + "-" + month + "-" + day;
                case WEEK:
                    int week = (day / 7) + 1;
                    return  "-" + year + "-" + month + "-" + week;
                case MONTH:
                   return  "-" + year + "-" + month;
                case QUARTER:
                    int quarter = month / 4 + 1;
                    return "-" + year + "-" + quarter;
                case YEAR:
                    return  "-" + year + "";
                default:
                    return  "";
            }
        }
    }

    public enum IndexNameStrategy {
        NULL("Null"),
        DAY("Day"),
        WEEK("Week"),
        MONTH("Month"),
        QUARTER("Quarter"),
        YEAR("Year");
        public String value;

        IndexNameStrategy(String value) {this.value = value;}

        public static IndexNameStrategy fromString(String nameStrategy) {
            for (IndexNameStrategy h : IndexNameStrategy.values()) {
                if (h.value.equalsIgnoreCase(nameStrategy)) {
                    return h;
                }
            }
            return getDefault();
        }

        public static IndexNameStrategy getDefault(){
            return NULL;
        }

        public String getIndexNameSuffix(Date date) {
            String str;
            Calendar calendar = Calendar.getInstance();
            if (date != null) {
                calendar.setTime(date);
            }
            int year = calendar.get(Calendar.YEAR);
            int month = calendar.get(Calendar.MONTH) + 1;
            int day = calendar.get(Calendar.DATE);
            switch (this) {
                case DAY:
                    str =   "-" + year + "-" + month + "-" + day;
                    break;
                case WEEK:
                    int week = ( day - 1 ) / 7 + 1;
                    str =   "-" + year + "-" + month + "-" + week;
                    break;
                case MONTH:
                    str = "-" + year + "-" + month;
                    break;
                case QUARTER:
                    int quarter =  ( month - 1 ) / 3 + 1;
                    str =  "-" + year + "-" + quarter;
                    break;
                case YEAR:
                    str =  "-" + year + "";
                    break;
                default:
                    return  "";
            }
             return str + "-server";
        }

        public String getIndexNameSuffix(Date date, int offset) {
            String str;
            Calendar calendar = Calendar.getInstance();
            if (date != null) {
                calendar.setTime(date);
            }
            int year;
            int month;
            int day;
            switch (this) {
                case DAY:
                    calendar.add(Calendar.DATE, offset);
                    year = calendar.get(Calendar.YEAR);
                    month = calendar.get(Calendar.MONTH)  + 1;
                    day = calendar.get(Calendar.DATE);
                    str =   "-" + year + "-" + month + "-" + day;
                    break;
                case WEEK:
                    calendar.add(Calendar.WEEK_OF_MONTH, offset);
                    year = calendar.get(Calendar.YEAR);
                    month = calendar.get(Calendar.MONTH)  + 1;
                    day = calendar.get(Calendar.DATE);
                    int week = ( day - 1 ) / 7 + 1;
                    str =   "-" + year + "-" + month + "-" + week;
                    break;
                case MONTH:
                    calendar.add(Calendar.MONTH, offset);
                    year = calendar.get(Calendar.YEAR);
                    month = calendar.get(Calendar.MONTH)  + 1;
                    str = "-" + year + "-" + month;
                    break;
                case QUARTER:
                    calendar.add(Calendar.MONTH, offset * 3);
                    year = calendar.get(Calendar.YEAR);
                    month = calendar.get(Calendar.MONTH)  + 1;
                    int quarter =  ( month - 1 ) / 3 + 1;
                    str =  "-" + year + "-" + quarter;
                    break;
                case YEAR:
                    calendar.add(Calendar.YEAR, offset);
                    year = calendar.get(Calendar.YEAR);
                    str =  "-" + year + "";
                    break;
                default:
                    return "";
            }
            return str + "-server";
        }
    }

    public void loadConsumerConfig() {
        switch (dest) {
            case ES: {
                indexName = (String) consumerConfig.get("indexName");
                if(consumerConfig.get("indexNameStrategy") != null) {
                    indexNameStrategy = IndexNameStrategy.fromString((String)consumerConfig.get("indexNameStrategy"));
                }
                idField = (String) consumerConfig.get("idField");
                shardNum = (int)consumerConfig.get("shardNum");
                replicaNum = (int)consumerConfig.get("replicaNum");
                currentIndexName = indexName + indexNameStrategy.getIndexNameSuffix(new Date());
                destLocation = currentIndexName;
                indexMapping = (String)consumerConfig.get("indexMapping");
                break;
            }
            case KAFKA:{
                topicName = (String) consumerConfig.get("topicName");
                partitions = (int) consumerConfig.get("partitions");
                replications = (int) consumerConfig.get("replications");
                retentionMillis = Long.parseLong(consumerConfig.get("retentionMillis").toString());
                retentionBytes = Long.parseLong(consumerConfig.get("retentionBytes").toString());
                messageMaxBytes = Long.parseLong(consumerConfig.get("messageMaxBytes").toString());
                cleanupPolicy = (String) consumerConfig.get("cleanupPolicy");
                destLocation = topicName;
                break;
            }
            case INFLUXDB: {
                database = (String) consumerConfig.get("database");
                measurement = (String) consumerConfig.get("measurement");
                tags = (String) consumerConfig.get("tags");
                destLocation = database + "/" + measurement;
                break;
            }
            case HDFS: {
                basePath = (String) consumerConfig.get("path");
                baseReplacePath = (String) consumerConfig.get("replacePath");
                if (consumerConfig.get("namedStrategy") != null) {
                    FileNameStrategy strategy = FileNameStrategy.fromInteger((int)consumerConfig.get("namedStrategy"));
                    if (fileNameStrategy != null)
                        fileNameStrategy = strategy;
                }
                path = basePath + "/" + PostUtil.getClientName() + "_"+ fileNameStrategy.getFileName();
                if (!StringUtils.isEmpty(baseReplacePath))
                    replacePath = baseReplacePath + "/" + PostUtil.getClientName() + "_"+ replaceFileNameStrategy.getFileName();
                destLocation = path;
                break;
            }
            default: throw new IllegalArgumentException(String.format("No such consumerType: %s", dest));
        }
    }


    public void loadDest(String consumerType) {
        dest = Dest.fromString(consumerType);
    }

    public void loadFilter() throws IOException {
        if (!StringUtils.isEmpty(mapping))
            if (formatFilter != null) formatFilter.addAll(ProcessFieldTool.removeDuplicateFormatter(JsonUtils.objectMapper.readValue(mapping, new TypeReference<List<ObjectFormatter>>() {})));
            else formatFilter = (ProcessFieldTool.removeDuplicateFormatter(JsonUtils.objectMapper.readValue(mapping, new TypeReference<List<ObjectFormatter>>() {})));
    }
}
