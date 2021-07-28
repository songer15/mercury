package com.vms.metric.analyse.tool;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.elasticsearch.common.collect.Tuple;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.*;


/**
 * 通用工具方法类
 */
public class CustomTool {

    public static ObjectMapper objectMapper = new ObjectMapper();

    private static SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSXXX");


    public static Date transStrToDate(String str) {
        if (str != null && str.length() > 0) {
            LocalDateTime dateTime = LocalDateTime.parse(str.substring(0, str.length() - 1));
            return Date.from(dateTime.toInstant(ZoneOffset.UTC));
        } else
            return null;
    }

    public static Date parseStr(String str) {
        try {
            if (str != null && str.length() > 0)
                return sdf.parse(str);
            else
                return null;
        } catch (Exception e) {
            return null;
        }
    }

    public static Date getDateFromObject (Object obj) {
        if (obj instanceof Long) {
            return new Date((Long) obj);
        } else if (obj instanceof String) {
            return transStrToDate((String) obj);
        } else return null;
    }

    public static Date extractDateFromEs(Object time) {
        if (time instanceof Long) {
            return new Date((Long) time);
        }
        if (time instanceof String) {
            return transStrToDate((String) time);
        }
        return null;
    }


    public static Date str2Date(SimpleDateFormat format, String str) {
        if (str != null && str.length() > 0) {
            try {
                return format.parse(str);
            } catch (ParseException e) {
                throw new RuntimeException("date cant be parsed");
            }
        } else
            return null;
    }

    public static Map<String, Object> objectToMap(Object object) {
        Map<String, Object> result = new HashMap<>();
        Field[] fields = object.getClass().getDeclaredFields();
        try {
            for (Field field : fields) {
                field.setAccessible(true);
                String name = new String(field.getName());
                result.put(name, field.get(object));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }


    public static <T> T mapToObject(Map<String, Object> map, Class<T> clazz) {
        if (map == null) {
            return null;
        }
        T obj = null;
        try {
            obj = clazz.newInstance();
            Method[] methods = clazz.getMethods();
            for (Method method : methods) {
                if (method.getName().startsWith("set")) {
                    String field = method.getName();
                    field = field.substring(field.indexOf("set") + 3);
                    field = field.toLowerCase().charAt(0) + field.substring(1);
                    if (map.containsKey(field)) {
                        method.invoke(obj, map.get(field));
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return obj;
    }

    public static <T> List<List<T>> splitList(List<T> list, int len) {

        if (list == null || list.isEmpty() || len < 1) {
            return Collections.emptyList();
        }

        List<List<T>> result = new ArrayList<>();

        int size = list.size();
        int count = (size + len - 1) / len;

        for (int i = 0; i < count; i++) {
            List<T> subList = list.subList(i * len, ((i + 1) * len > size ? size : len * (i + 1)));
            result.add(subList);
        }

        return result;
    }

    public static String getDateStr(GET_DATE_STR s) {
        String str;
        Calendar calendar = Calendar.getInstance();
        int year = calendar.get(calendar.YEAR);
        int month = calendar.get(calendar.MONTH) + 1;
        int day = calendar.get(calendar.DATE);
        switch (s) {
            case DAY:
                str = "-" + year + "-" + month + "-" + day;
                break;
            case WEEK:
                int week = (day / 7) + 1;
                str = "-" + year + "-" + month + "-" + week;
                break;
            case MONTH:
                str = "-" + year + "-" + month;
                break;
            case SEASON:
                int season = month / 4 + 1;
                str = "-" + year + "-" + season;
                break;
            case YEAR:
                str = "-" + year + "";
                break;
            default:
                str = "-" + year + "-" + month + "-" + day;
                break;
        }
        return str + "-server";
    }

    public static Long parseStrToLong(String reg_date) {
        try {
            if (reg_date != null && reg_date.length() > 0)
                return sdf.parse(reg_date).getTime();
            else
                return null;
        } catch (Exception e) {
            return null;
        }
    }

    public enum GET_DATE_STR {

        DAY(0), WEEK(1), MONTH(2), SEASON(3), YEAR(4);

        Integer value;

        private GET_DATE_STR(int value) {
            this.value = value;
        }

        public static GET_DATE_STR valueOf(Integer value) {
            switch (value) {
                case 0:
                    return DAY;
                case 1:
                    return WEEK;
                case 2:
                    return MONTH;
                case 3:
                    return SEASON;
                case 4:
                    return YEAR;
                default:
                    return SEASON;
            }
        }
    }

    public static long getTimeStamp(Object o) {
        if (o instanceof String) {
            return CustomTool.transStrToDate((String) o).getTime();
        } else if (o instanceof Long) {
            return (long) o;
        } return 0L;
    }

    public static int getCardValue(long millis) {
        int days = (int)(millis / (1000*60*60*24));
        if (days >= 364) return 365;
        else if (days >= 30 && days <= 50) return 30;
        else return days;
    }

    /**
     * 获得该时区的0点的固定日期间隔的2个Date,
     */
    public static Tuple<Date, Date> getCustomDateRange(int dayOffset, Date pivot, boolean isForward) {
        long oneDayMillis =  24  * 3600_000L;
        long startDayMillis;
        long endDayMillis;
        if (isForward) {
            startDayMillis = pivot.getTime() ;
            endDayMillis = startDayMillis + oneDayMillis * dayOffset;
        } else {
            endDayMillis = pivot.getTime() ;
            startDayMillis = endDayMillis - oneDayMillis * dayOffset;
        }
         return new Tuple<>(new Date(startDayMillis), new Date(endDayMillis));
    }
}
