package com.valor.mercury.common.formatter;

import com.valor.mercury.common.model.AbstractPrintable;
import com.valor.mercury.common.model.exception.FieldFormatException;
import com.valor.mercury.common.util.ProcessFieldTool;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Method;
import java.util.*;

public class ObjectFormatter extends AbstractPrintable {
    private static final Logger logger = LoggerFactory.getLogger(ObjectFormatter.class);

    private String acceptName ;//接受的字段名，与数据中实际字段名不一样则拒绝该字段。

    private String acceptType;//接受的字段的类型，与数据中实际字段类型不一样则拒绝该字段。

    private String outputName;//将要修改成的字段名

    private String outputType;//将要转化成的字段类型

    private List<ObjectFormatter> nested_formatter;//嵌套的 ObjectFormatter

    private static Map<String, Class<?>> classMap = new HashMap<>();

    static {
        classMap.put( "Integer", Integer.class);
        classMap.put( "Date", Date.class);
        classMap.put( "Long", Long.class);
        classMap.put( "String", String.class);
        classMap.put( "Double",Double.class);
        classMap.put( "Boolean",Boolean.class);
        classMap.put( "Map", Object.class);
        classMap.put( "Number", Number.class);
    }

    public ObjectFormatter() {
    }

    public ObjectFormatter(String acceptName, String acceptType, String outputName, String outputType) {
        this.acceptName = acceptName;
        this.acceptType = acceptType;
        this.outputName = outputName;
        this.outputType = outputType;
    }

    /**
     *
     * @param newFields  包含新字段的对象
     * @param entry   需要格式化的旧字段
     * @param instance  ObjectFormatterTool.instance
     */
    @SuppressWarnings("unchecked")
    public void format(Map<String, Object> newFields, Map.Entry<String, Object> entry,Object instance) throws Exception {
        String oldKey = entry.getKey();
        Object oldValue = entry.getValue();

        //旧的值为null直接返回
        if (oldValue == null) {
            return;
        }

        //格式化key的名称
        if (acceptName.isEmpty()) {
            throw new FieldFormatException("accept_name is null");
        }
        if (outputName.isEmpty() ) {
            throw new FieldFormatException("output_name is null");
        }
        if (!oldKey.equals(acceptName) ) {
            throw new FieldFormatException(String.format("accept_name mismatches: %s != %s", acceptName, oldKey));
        }
        String newKey;
        if (acceptName.equals(outputName)) {
            newKey = oldKey;
        } else {
            newKey = outputName;
        }

        //java类型首字母大写
        acceptType = StringUtils.capitalize(acceptType);
        outputType = StringUtils.capitalize(outputType);

        //转换value的类型

        if (acceptType.isEmpty() || classMap.get(acceptType)== null) {
            throw new FieldFormatException("accept_type is null or invalid");
        }
        if (outputType.isEmpty() || classMap.get(outputType) == null) {
            throw new FieldFormatException("output_type is null or invalid");
        }

        //accpet_type和实际类型不符
        if (!classMap.get(acceptType).isInstance(oldValue)) {
            //int -> long & float -> double 兼容
            if (classMap.get(acceptType) == Long.class && oldValue.getClass() == Integer.class) {
                oldValue = new Long((Integer)oldValue);
            } else if (classMap.get(acceptType) == Double.class && oldValue.getClass() == Float.class) {
                oldValue = new Double((Float)oldValue);
            } else {
                throw new FieldFormatException(String.format("accept type = %s but actual_type = %s", acceptType, oldValue.getClass()) );
            }
        }

        //处理value类型是Map的情况
        if (nested_formatter != null && oldValue instanceof Map) {
            Map<String, Object> oldNestedFields = (Map<String, Object>) oldValue;
            Map<String, Object> newNestedFields = new HashMap<>();
            for (Map.Entry<String, Object> nestedEntry : oldNestedFields.entrySet()) {
                Optional<ObjectFormatter> opt = ProcessFieldTool.getProperFormatter(nestedEntry.getKey(), nested_formatter);
                if(opt.isPresent()){
                    opt.get().format(newNestedFields, nestedEntry, instance );
                }
            }
            newFields.put(newKey, newNestedFields);
            return;
        }

        //处理value是一般java类型的情况
        Object newValue;
        if (acceptType.equalsIgnoreCase(outputType)) {
            //不需要转换
            newValue = oldValue;
        } else {
            String methodName = StringUtils.uncapitalize(acceptType) + "To" + StringUtils.capitalize(outputType);
            Method method = ProcessFieldTool.methodMap.get(methodName);
            //转换方法不存在
            if (method == null) {
                logger.error("methodName: {} doesnt exist！" , methodName);
                throw new FieldFormatException(String.format("undefined type converter: %s", methodName));
            }
            newValue = method.invoke(instance, oldValue);
        }
        newFields.put(newKey, newValue);
    }

    public String getAcceptName() {
        return acceptName;
    }

    public void setAcceptName(String acceptName) {
        this.acceptName = acceptName;
    }

    public String getAcceptType() {
        return acceptType;
    }

    public void setAcceptType(String acceptType) {
        this.acceptType = acceptType;
    }

    public String getOutputName() {
        return outputName;
    }

    public void setOutputName(String outputName) {
        this.outputName = outputName;
    }

    public String getOutputType() {
        return outputType;
    }

    public void setOutputType(String outputType) {
        this.outputType = outputType;
    }

    public List<ObjectFormatter> getNested_formatter() { return nested_formatter; }

    public void setNested_formatter(List<ObjectFormatter> nested_formatter) { this.nested_formatter = nested_formatter; }
}
