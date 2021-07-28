package com.valor.mercury.manager.model.system;

/**
 * @author Gavin
 * 2020/7/28 17:22
 */
public class SpiderTask {
    private String name;
    private String description;
    private String executor;
    private String outPutName;
    private String reader;
    private String processor;
    private String listener;
    private String incTag;
    private String incValue;
    private String sql;
    private String database;
    private String pageSize;
    private String enable;

    public String getName() {
        return name;
    }

    public String getPageSize() {
        return pageSize;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getExecutor() {
        return executor;
    }

    public void setExecutor(String executor) {
        this.executor = executor;
    }

    public String getIncTag() {
        return incTag;
    }

    public void setIncTag(String incTag) {
        this.incTag = incTag;
    }

    public String getIncValue() {
        return incValue;
    }

    public void setIncValue(String incValue) {
        this.incValue = incValue;
    }

    public String getSql() {
        return sql;
    }

    public void setSql(String sql) {
        this.sql = sql;
    }

    public void setPageSize(String pageSize) {
        this.pageSize = pageSize;
    }

    public String getEnable() {
        return enable;
    }

    public void setEnable(String enable) {
        this.enable = enable;
    }

    public String getOutPutName() {
        return outPutName;
    }

    public void setOutPutName(String outPutName) {
        this.outPutName = outPutName;
    }

    public String getReader() {
        return reader;
    }

    public void setReader(String reader) {
        this.reader = reader;
    }

    public String getProcessor() {
        return processor;
    }

    public void setProcessor(String processor) {
        this.processor = processor;
    }

    public String getListener() {
        return listener;
    }

    public void setListener(String listener) {
        this.listener = listener;
    }


    public String getDatabase() {
        return database;
    }

    public void setDatabase(String database) {
        this.database = database;
    }

    @Override
    public String toString() {
        return "SpiderTask{" +
                "name='" + name + '\'' +
                ", description='" + description + '\'' +
                ", executor='" + executor + '\'' +
                ", outPutName='" + outPutName + '\'' +
                ", reader='" + reader + '\'' +
                ", processor='" + processor + '\'' +
                ", listener='" + listener + '\'' +
                ", incTag='" + incTag + '\'' +
                ", incValue='" + incValue + '\'' +
                ", sql='" + sql + '\'' +
                ", database='" + database + '\'' +
                ", pageSize='" + pageSize + '\'' +
                ", enable='" + enable + '\'' +
                '}';
    }
}
