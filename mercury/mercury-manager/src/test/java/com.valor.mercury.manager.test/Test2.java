//package com.valor.mercury.manager.test;
//
//import com.cronutils.model.definition.CronDefinitionBuilder;
//import com.cronutils.model.time.ExecutionTime;
//import com.cronutils.parser.CronParser;
//import org.apache.hadoop.conf.Configuration;
//import org.apache.hadoop.fs.FSDataInputStream;
//import org.apache.hadoop.fs.FSDataOutputStream;
//import org.apache.hadoop.fs.FileSystem;
//import org.apache.hadoop.fs.Path;
//import org.apache.hadoop.io.IOUtils;
//import org.junit.Test;
//
//import java.net.URI;
//import java.nio.charset.Charset;
//import java.nio.charset.StandardCharsets;
//import java.util.ArrayList;
//import java.util.Comparator;
//import java.util.List;
//
//import static com.cronutils.model.CronType.QUARTZ;
//
///**
// * @author Gavin
// * 2020/8/12 17:39
// */
//public class Test2 {
//
//    @Test
//    public void test() {
//        CronParser parser = new CronParser(CronDefinitionBuilder.instanceDefinitionFor(QUARTZ));
//        ExecutionTime executionTime = ExecutionTime.forCron(parser.parse("0 0 0 * * ? "));
//        System.out.println(executionTime.toString());
//    }
//
//    @Test
//    public void testHDFSWrite() throws Exception {
//        String fileWrite = "hdfs://192.168.191.133:9000/user/kqiao/test/FileWrite";
//        Configuration conf = new Configuration();
//        FileSystem fs = FileSystem.get(URI.create(fileWrite), conf);
//        Path path = new Path(fileWrite);
//        FSDataOutputStream out = fs.create(path);   //创建文件
//        for (int i = 0; i < 100; i++)
//            out.write((i + "value").getBytes(StandardCharsets.UTF_8));
//        out.close();
//    }
//
//    @Test
//    public void testHDFSRead() throws Exception {
//        String fileWrite = "hdfs://192.168.191.133:9000/user/kqiao/test/FileWrite";
//        Configuration conf = new Configuration();
//        FileSystem fs = FileSystem.get(URI.create(fileWrite), conf);
//        Path path = new Path(fileWrite);
//        FSDataInputStream in = fs.open(path);
//        IOUtils.copyBytes(in, System.out, 4096, true);
//    }
//}
