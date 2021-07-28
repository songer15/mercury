package com.vms.metric.analyse.model;

import java.util.List;

public class ElsIndexConfig {

    //TODO 修改配置到json
    public static final String TYPENAME = "_doc";
    private List<String> dateFields;//对象中时间类型的字段名
    private String indexName;
    private String mappingFile;
    private String ipField;
    private String idField;
    private Boolean needGeo;
    private Boolean needTime = true;
    private List<String> childSetFields;//对象中子对象的字段名

    public Boolean getNeedTime() {
        return needTime;
    }

    public void setNeedTime(Boolean needTime) {
        this.needTime = needTime;
    }

    public List<String> getDateFields() {
        return dateFields;
    }

    public void setDateFields(List<String> dateFields) {
        this.dateFields = dateFields;
    }

    public List<String> getChildSetFields() {
        return childSetFields;
    }

    public void setChildSetFields(List<String> childSetFields) {
        this.childSetFields = childSetFields;
    }

    public String getIndexName() {
        return indexName;
    }

    public void setIndexName(String indexName) {
        this.indexName = indexName;
    }

    public String getMappingFile() {
        return mappingFile;
    }

    public void setMappingFile(String mappingFile) {
        this.mappingFile = mappingFile;
    }

    public String getIpField() {
        return ipField;
    }

    public void setIpField(String ipField) {
        this.ipField = ipField;
    }

    public Boolean getNeedGeo() {
        return needGeo;
    }

    public void setNeedGeo(Boolean needGeo) {
        this.needGeo = needGeo;
    }

    public String getIdField() {
        return idField;
    }

    public void setIdField(String idField) {
        this.idField = idField;
    }

    @Override
    public String toString() {
        return "ElsIndexConfig{" +
                "dateFields=" + dateFields +
                ", indexName='" + indexName + '\'' +
                ", mappingFile='" + mappingFile + '\'' +
                ", ipField='" + ipField + '\'' +
                ", idField='" + idField + '\'' +
                ", needGeo=" + needGeo +
                ", childSetFields=" + childSetFields +
                '}';
    }

}
