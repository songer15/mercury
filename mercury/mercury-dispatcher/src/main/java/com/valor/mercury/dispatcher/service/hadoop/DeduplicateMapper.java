//package com.valor.mercury.dispatcher.service.hadoop;
//
//import com.fasterxml.jackson.core.type.TypeReference;
//import com.valor.mercury.common.model.MetricMessage;
//import com.valor.mercury.common.model.WrapperEntity;
//import com.valor.mercury.common.util.JsonUtil;
//import com.valor.mercury.common.util.JsonUtils;
//import org.apache.hadoop.io.IntWritable;
//import org.apache.hadoop.io.LongWritable;
//import org.apache.hadoop.io.Text;
//import org.apache.hadoop.mapred.JobConf;
//import org.apache.hadoop.mapred.Mapper;
//import org.apache.hadoop.mapred.OutputCollector;
//import org.apache.hadoop.mapred.Reporter;
//import org.codehaus.jackson.annotate.JsonUnwrapped;
//
//import java.io.IOException;
//import java.util.List;
//import java.util.Map;
//
//public class DeduplicateMapper implements Mapper<LongWritable, Text, Text, Text> {
//
//    private String incrementalField;
//    private String idField;
//
//    @Override
//    public void close() throws IOException {
//
//    }
//
//    @Override
//    public void configure(JobConf job) {
//
//    }
//
//    @Override
//    public void map(LongWritable key, Text value, OutputCollector<Text, Text> output, Reporter reporter) throws IOException {
//        Map<String, Object> row = JsonUtil.jsonToMap(value.toString());
//        Object obj = row.get(incrementalField);
//        String id = row.get(idField).toString();
//        //数据的id 作为key, 数据本身作为 value
//        output.collect(new Text(id), value);
//    }
//}
