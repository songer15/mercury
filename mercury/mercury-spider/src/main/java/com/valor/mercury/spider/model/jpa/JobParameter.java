package com.valor.mercury.spider.model.jpa;


import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import java.util.Date;

@Entity
@Table(name = "BATCH_JOB_EXECUTION_PARAMS")
public class JobParameter {

    private Long jobExecutionId;
    private String typeCd;
    private String keyName;
    private String stringVal;
    private Date dateVal;
    private Long longVal;
    private Double doubleVal;
    private char identifying;

    @Id
    @Column(name="JOB_EXECUTION_ID")
    public Long getJobExecutionId() {
        return jobExecutionId;
    }

    public void setJobExecutionId(Long jobExecutionId) {
        this.jobExecutionId = jobExecutionId;
    }

    @Column(name="TYPE_CD")
    public String getTypeCd() {
        return typeCd;
    }

    public void setTypeCd(String typeCd) {
        this.typeCd = typeCd;
    }

    @Column(name="KEY_NAME")
    public String getKeyName() {
        return keyName;
    }

    public void setKeyName(String keyName) {
        this.keyName = keyName;
    }

    @Column(name="STRING_VAL")
    public String getStringVal() {
        return stringVal;
    }

    public void setStringVal(String stringVal) {
        this.stringVal = stringVal;
    }

    @Column(name="DATE_VAL")
    public Date getDateVal() {
        return dateVal;
    }

    public void setDateVal(Date dateVal) {
        this.dateVal = dateVal;
    }

    @Column(name="LONG_VAL")
    public Long getLongVal() {
        return longVal;
    }

    public void setLongVal(Long longVal) {
        this.longVal = longVal;
    }

    @Column(name="DOUBLE_VAL")
    public Double getDoubleVal() {
        return doubleVal;
    }

    public void setDoubleVal(Double doubleVal) {
        this.doubleVal = doubleVal;
    }

    @Column(name="IDENTIFYING")
    public char getIdentifying() {
        return identifying;
    }

    public void setIdentifying(char identifying) {
        this.identifying = identifying;
    }

    @Override
    public String toString() {
        return "JobParameter{" +
                "jobExecutionId=" + jobExecutionId +
                ", typeCd='" + typeCd + '\'' +
                ", keyName='" + keyName + '\'' +
                ", stringVal='" + stringVal + '\'' +
                ", dateVal=" + dateVal +
                ", longVal=" + longVal +
                ", doubleVal=" + doubleVal +
                ", identifying=" + identifying +
                '}';
    }
}
