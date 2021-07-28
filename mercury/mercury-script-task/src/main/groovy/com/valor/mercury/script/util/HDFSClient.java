package com.valor.mercury.script.util;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.*;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Gavin
 * 2020/9/20 14:36
 */
public class HDFSClient {

    private FileSystem fileSystem;

    public HDFSClient() throws Exception {
        System.setProperty("HADOOP_USER_NAME", "valor");
        Configuration conf = new Configuration();
        conf.set("dfs.support.append", "true");
        conf.set("dfs.client.block.write.replace-datanode-on-failure.policy", "NEVER");//配置权限
        conf.set("dfs.client.block.write.replace-datanode-on-failure.enable", "true");//配置权限
        conf.addResource(new Path("hadoop/hdfs-site.xml"));
        conf.addResource(new Path("hadoop/core-site.xml"));
        fileSystem = FileSystem.get(URI.create("hdfs://mycluster"), conf);
    }

    public boolean writeData(String filePath, String value) throws Exception {
        Path path = new Path(filePath);
        if (!fileSystem.exists(path)) {
            fileSystem.createNewFile(path);
        }
        FSDataOutputStream outpustream = fileSystem.append(path);
        outpustream.write(value.getBytes(StandardCharsets.UTF_8));//追加内容
        outpustream.close();
        return true;
    }

    public boolean writeData(String filePath, List<String> values) throws Exception {
        Path path = new Path(filePath);
        if (!fileSystem.exists(path)) {
            fileSystem.createNewFile(path);
        }
        FSDataOutputStream outputStream = fileSystem.append(path);
        for (String value : values)
            outputStream.write(value.getBytes(StandardCharsets.UTF_8));//追加内容
        outputStream.close();
        return true;
    }

    public void readValue(String filePath, HDFSReader HDFSReader) throws Exception {
        System.out.println(filePath);
        Path path = new Path(filePath);
        List<Path> paths = new ArrayList<>();
        if (fileSystem.exists(path)) {
            FileStatus[] fileStatus = fileSystem.listStatus(path);
            for (FileStatus status : fileStatus) {
                if (status.isFile()) {
                    Path oneFilePath = status.getPath();
                    if (oneFilePath != null
                            && !oneFilePath.getName().contains("staging")) {
                        System.out.println(oneFilePath.toString());
                        paths.add(oneFilePath);
                    }
                }
            }
        }
        for (Path subFilePath : paths) {
            FSDataInputStream in = fileSystem.open(subFilePath);
            BufferedReader reader = new BufferedReader(new InputStreamReader(in));
            String line;
            while ((line = reader.readLine()) != null) {
                HDFSReader.read(line);
            }
            HDFSReader.finish(line);
            in.close();
        }
    }

    public void close() throws Exception {
        fileSystem.close();
    }
}

