package com.valor.mercury.common.client;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FSDataOutputStream;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

public class HDFSClient {

    private static final Logger logger = LoggerFactory.getLogger(HDFSClient.class);

    private FileSystem fileSystem;

    public HDFSClient(String HDFSUser, String nameservices, String coreSiteConf, String hdfsSiteConf) throws IOException{
        System.setProperty("HADOOP_USER_NAME", HDFSUser);
        Configuration conf = new Configuration();
        conf.addResource(new Path(coreSiteConf));
        conf.addResource(new Path(hdfsSiteConf));
        fileSystem =FileSystem.get(URI.create(nameservices), conf);
    }

    public void write(String content, String filePath) throws IOException {
        Path path = new Path(filePath);
        FSDataOutputStream outputStream;
        if (!fileSystem.exists(path)) {
            logger.info(fileSystem.getStatus(path).toString());
            fileSystem.createNewFile(path);
        }
        outputStream = fileSystem.append(path);
        outputStream.write(content.getBytes(StandardCharsets.UTF_8));//追加内容
        outputStream.close();
    }

    public void copyToLocalFile(String src, String dest) throws Exception {
        fileSystem.copyToLocalFile(false, new Path(src), new Path(dest), true);
    }

    public void mkdir(String path) throws IOException {
        fileSystem.mkdirs(new Path(path));
    }
    public void removeFile(String path) throws IOException{
        fileSystem.delete(new Path(path), true);
    }
}
