package com.valor.mercury.gateway.service;

import com.csvreader.CsvWriter;
import com.valor.mercury.gateway.common.Settings;
import com.valor.mercury.gateway.util.FileUtils;
import com.valor.mercury.gateway.elasticsearch.executors.CSVResult;
import org.springframework.core.io.FileSystemResource;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 *
 */
@Service
public class FileService {

    private ExecutorService pool = Executors.newFixedThreadPool(2);

    /**
     * 下载文件
     */
    public FileSystemResource download(String filePath) {
        return FileUtils.isFileAvailable(filePath) ? new FileSystemResource(filePath) : null;
    }

    /**
     * 保存为.csv文件
     */
    public void save(CSVResult result, String filePath, AtomicBoolean isWritten) {
        File file = new File(filePath);
        pool.execute(() -> {
            try {
                //append = true 允许循环写入的方式
                CsvWriter writer = new CsvWriter(new BufferedWriter(new FileWriter(file, true)), ',');
                if(!isWritten.get()) { //第一次写入表头
                    isWritten.set(true);
                    writer.writeRecord(result.getHeaders().toArray(new String[result.getHeaders().size()]));
                }
                for (String line : result.getLines()) {
                    writer.writeRecord(new String[]{line});
                }
                writer.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    /**
     * 定时清空过期的数据
     */
    @Scheduled(cron = "0 0 0 1/1 * ?")
    public void cleanStorage(){
       File[] files = new File(System.getProperty("user.dir") + File.separator + Settings.DIRECTORY_NAME).listFiles();
       if (files != null) {
           for (File file: files) {
               if ((System.currentTimeMillis() - file.lastModified()) / 1000 * 60 * 60 * 24 > Settings.RESERVED_DAYS) file.delete();
           }
       }
    }
}
