//package com.valor.mercury.dispatcher.service.hadoop;
//
//import org.apache.hadoop.io.IntWritable;
//import org.apache.hadoop.io.Text;
//import org.apache.hadoop.mapred.JobConf;
//import org.apache.hadoop.mapred.OutputCollector;
//import org.apache.hadoop.mapred.Reducer;
//import org.apache.hadoop.mapred.Reporter;
//
//import java.io.IOException;
//import java.util.Iterator;
//
//public class DeduplicateReducer implements Reducer<Text, IntWritable, Text, IntWritable> {
//    @Override
//    public void reduce(Text key, Iterator<IntWritable> values, OutputCollector<Text, IntWritable> output, Reporter reporter) throws IOException {
//
//    }
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
//}
