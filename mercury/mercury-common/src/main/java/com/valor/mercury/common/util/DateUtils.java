package com.valor.mercury.common.util;

import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.Map;

public class DateUtils {
    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private static final ThreadLocal<SimpleDateFormat[]> THREAD_LOCAL = new ThreadLocal<SimpleDateFormat[]>() {
        @Override
        protected SimpleDateFormat[] initialValue() {
            return new SimpleDateFormat[] {
                    new SimpleDateFormat("yyyy-MM-dd HH:mm:ss"),
                    new SimpleDateFormat("yyyy-MM-dd HH:mm:ss:SSS"),
                    new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSXXX"),
                    new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ")
            };
        }
    };

    public static String getCurrentDateStr() {
        return formatter.format(LocalDate.now());
    }

    /**
     * 插入本地时间相关数据
     */
    public static void processCreateTime(Map<String, Object> json) {
        json.put("LocalCreateTime", new Date());
        json.put("LocalCreateTimestamps", System.currentTimeMillis());
    }

    public static Date tryParse(String source) {
        for (SimpleDateFormat sdf : THREAD_LOCAL.get()) {
            try {
                return sdf.parse(source);
            } catch (Exception ex) {

            }
        }
        return null;
    }

    public static String tryFormat(Date date) {
        for (SimpleDateFormat sdf : THREAD_LOCAL.get()) {
            try {
                return sdf.format(date);
            } catch (Exception ex) {

            }
        }
        return null;
    }


}
