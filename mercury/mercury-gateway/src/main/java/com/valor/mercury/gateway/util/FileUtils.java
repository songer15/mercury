package com.valor.mercury.gateway.util;

import com.valor.mercury.gateway.common.Settings;
import org.apache.commons.lang3.StringUtils;

import java.io.File;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class FileUtils {

    /**
     * 检查文件是否写入完全
     */
    public static boolean isFileAvailable(String filePath) {
        if (StringUtils.isEmpty(filePath)) return false;
        File file = new File(filePath);
        if (!file.exists()) {
            return false;
        } else {
            try {
                long preLenth = file.length();
                Thread.sleep(15_000);
                return preLenth == file.length();
            } catch (InterruptedException e) {
                e.printStackTrace();
                return false;
            }
        }
    }

    /**
     *  生成一个文件名
     */
    public static String createNewFilePath(String extension) {
        return System.getProperty("user.dir")
                + File.separator
                + Settings.DIRECTORY_NAME
                + File.separator
                + LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))
                + "-"
                + System.currentTimeMillis()
                + extension;
    }

}
