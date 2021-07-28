package com.valor.mercury.common.util;

import com.valor.mercury.common.client.ServiceMonitor;
import com.valor.mercury.common.constant.MercuryConstants;
import com.valor.mercury.common.formatter.ObjectFormatter;
import com.valor.mercury.common.model.Router;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Method;
import java.text.NumberFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;


public class ProcessFieldTool {
    private static final Logger logger = LoggerFactory.getLogger(ProcessFieldTool.class);
    public static Pattern linePattern;
    public static ProcessFieldTool instance;
    public static Map<String, Method> methodMap;
    public static ServiceMonitor serviceMonitor;
    public static NumberFormat numberFormat = NumberFormat.getInstance();
    public static List<String> reservedFields = new ArrayList<>();
    public static SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ");

    static  {
        linePattern = Pattern.compile("_(\\w)");
        methodMap = new HashMap<>();
        //获得所有转换方法并初始化
        List<Method> methods = new ArrayList<>(Arrays.asList(ProcessFieldTool.class.getDeclaredMethods()))
                .stream()
                .filter(method -> method.getName().contains("To"))
                .collect(Collectors.toList());
        for(Method method : methods) {
            methodMap.put(method.getName(), method);
        }
        reservedFields.add(MercuryConstants.DEFAULT_IP_FIELD);
        serviceMonitor = ServiceMonitor.getDefault();
        instance = new ProcessFieldTool();
    }

    public static Long dateToLong(Date value)  {
        return value.getTime();
    }
    public static String dateToString(Date value) {
        return value.toString();
    }
    public static Number dateToNumber(Date value) {
        return value.getTime();
    }

    public static Date longToDate(Long value) {
        return new Date(value);
    }
    public static String longToString(Long value) {
        return value.toString();
    }
    public static Double longToDouble(Long value) {
        return value.doubleValue();
    }

    public static String integerToString(Integer value){
        return  value.toString();
    }
    public static Long integerToLong(Integer value) {
        return Long.valueOf(value);
    }
    public static Double integerToDouble(Integer value) {
        return value.doubleValue();
    }

    public static Integer stringToInteger(String value) {
        return Integer.valueOf(value);
    }
    public static Long stringToLong(String value) {
        return Long.valueOf(value);
    }
    public static Date stringToDate(String value) { return new Date(Long.valueOf(value)); }
    public static Double stringToDouble(String value) { return Double.valueOf(value); }
    public static Number stringToNumber(String value) throws ParseException { return numberFormat.parse(value); }

    public static Integer doubleToInteger(Double value) { return value.intValue();}
    public static Long doubleToLong(Double value) { return value.longValue();}
    public static String doubleToString(Double value) { return value.toString();}

    public static String numberToString(Number value) {
        if (value instanceof Double) return String.valueOf(value.doubleValue());
        if (value instanceof Long) return String.valueOf(value.longValue());
        if (value instanceof Integer) return String.valueOf(value.intValue());
        if (value instanceof Float) return String.valueOf(value.floatValue());
        else throw new IllegalArgumentException(String.format("invalid number type: %s", value.getClass()));
    }
    public static String numberToDate(Number value) {
        if (value.longValue() >= 0L)
            return sdf.format(new Date(value.longValue()));
        else
            throw new IllegalArgumentException("a timestamp can not greater than 0");
    }
    public static Boolean numberToBoolean(Number value) {
        if (1 == value.intValue()) return true;
        else if (0 == value.intValue()) return false;
        else throw new IllegalArgumentException("neither 1 or 0");
    }


    public static Number booleanToNumber(Boolean value) {
        if (value) return 1;
        else return 0;
    }
    public static String booleanToString(Boolean value) {
        return value.toString();
    }

    /**
     *
     * @param key 字段名字
     * @param list 待筛选的formatterList
     * @return
     */
    public static Optional<ObjectFormatter> getProperFormatter(String key, List<ObjectFormatter> list) {
        try {
            return list
                    .stream()
                    .filter(fmt -> !fmt.getAcceptName().isEmpty() && !fmt.getOutputName().isEmpty() && (fmt.getAcceptName().equals(key)))
                    .findFirst();
        } catch (Exception ex) {
            return Optional.empty();
        }
    }

    public static List<Map<String, Object>> processFields(List<Map<String, Object>> oldList, Router router) {
        List<Map<String, Object>> newList = new ArrayList<>();
        List<ObjectFormatter> formatterList = router.formatFilter;
        //遍历每条数据
        for (Map<String, Object> oldFields : oldList) {
            Map<String, Object> newFields = new HashMap<>();
            for (Map.Entry<String, Object> oldField : oldFields.entrySet()) {
                String oldKey = oldField.getKey();
                Object oldValue = oldField.getValue();
                try {
                    Optional<ObjectFormatter> optional = ProcessFieldTool.getProperFormatter(oldKey, formatterList);
                    if (optional.isPresent()) {
                        ObjectFormatter formatter = optional.get();
                        formatter.format(newFields, oldField, ProcessFieldTool.instance);
                    } else {
                        //检查在mapping中配置的字段，不检查没有配置的字段
                        if (router.mappingApplyLevel == 1)
                            newFields.put(oldKey, oldValue);
                        //只保留在mapping中配置的字段
                        else if (router.mappingApplyLevel == 2)
                            serviceMonitor.saveException(router.instanceId, String.format("mapping is missing:  field: %s", oldKey));
                    }
                } catch (Exception e) {
                    serviceMonitor.saveException(router.instanceId, e);
                }
            }
            if (isFieldsWriteable(newFields)) {
                Object ip = newFields.get(router.ipField);
                if (ip instanceof String) {
                    GeoIpUtils.processGeoData(newFields, GeoIpUtils.getGeoIPData((String)ip));
                }
                DateUtils.processCreateTime(newFields);
                newList.add(newFields);
            } else {
                serviceMonitor.addInterceptedCount(router.instanceId, 1);
            }
        }
        return newList;
    }

    //如果数据包含任意一个保留字段之外的字段，说明数据有意义，可以写入
    public static boolean isFieldsWriteable(Map<String, Object> fields) {
        for (String key : fields.keySet()) {
            if (!reservedFields.contains(key))
                return true;
        }
        return false;
    }

    /**
     * 移除可能重复配置的元素
     */
    public static List<ObjectFormatter> removeDuplicateFormatter(List<ObjectFormatter> list) {
        Set<String> acceptNameSet = new HashSet<>();
        Iterator<ObjectFormatter> iterator = list.iterator();
        while (iterator.hasNext()) {
            ObjectFormatter formatter = iterator.next();
            if (!acceptNameSet.contains(formatter.getAcceptName())) {
                acceptNameSet.add(formatter.getAcceptName());
            } else {
                iterator.remove();
            }
        }
        return list;
    }
}
