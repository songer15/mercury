package com.valor.mercury.manager.model.ddo;

import org.hibernate.annotations.Generated;

import javax.persistence.*;
import java.util.Date;

/**
 * @author Gavin
 * 2020/8/10 9:21
 */
@Entity
@Table(name = "etl_data_dispatch")
public class ETLDataDispatch {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "create_time")
    private Date createTime;

    @Column(name = "last_modify_time", columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP")
    @Temporal(TemporalType.TIMESTAMP)
    @Generated(org.hibernate.annotations.GenerationTime.ALWAYS)
    private Date lastModifyTime;

    @Column(name = "enable")
    private String enable;

    @Column(name = "type_name")
    private String typeName;

    @Column(name = "consume_num")
    private Long consumeNum = 0L;

    @Column(name = "send_num")
    private Long sendNum = 0L;

    @Column(name = "intercept_num")
    private Long interceptNum = 0L;

    @Lob
    @Basic(fetch = FetchType.EAGER)
    @Column(name = "mapping", columnDefinition = "TEXT")
    private String mapping;

    @Column(name = "consumer_type")
    private String consumerType;

    @Lob
    @Basic(fetch = FetchType.EAGER)
    @Column(name = "consumer_config", columnDefinition = "TEXT")
    private String consumerConfig;

    @Column(name = "status")
    private String status;

    @Column(name = "last_request_time")
    private Date lastRequestTime;

    @Column(name = "last_report_time")
    private Date lastReportTime;

    @Column(name = "mapping_apply_level")
    private Integer mappingApplyLevel;

    @Column(name = "ip_field")
    private String ipField;

    @Override
    public String toString() {
        return "ETLDataDispatch{" +
                "id=" + id +
                ", createTime=" + createTime +
                ", lastModifyTime=" + lastModifyTime +
                ", enable='" + enable + '\'' +
                ", typeName='" + typeName + '\'' +
                ", consumeNum=" + consumeNum +
                ", sendNum=" + sendNum +
                ", interceptNum=" + interceptNum +
                ", mapping='" + mapping + '\'' +
                ", consumerType='" + consumerType + '\'' +
                ", consumerConfig='" + consumerConfig + '\'' +
                ", status='" + status + '\'' +
                ", lastRequestTime=" + lastRequestTime +
                ", lastReportTime=" + lastReportTime +
                ", mappingApplyLevel=" + mappingApplyLevel +
                ", ipField='" + ipField + '\'' +
                '}';
    }

    public Long getInterceptNum() {
        return interceptNum;
    }

    public void setInterceptNum(Long interceptNum) {
        this.interceptNum = interceptNum;
    }

    public Long getSendNum() {
        return sendNum;
    }

    public void setSendNum(Long sendNum) {
        this.sendNum = sendNum;
    }

    public String getIpField() {
        return ipField;
    }

    public void setIpField(String ipField) {
        this.ipField = ipField;
    }

    public Integer getMappingApplyLevel() {
        return mappingApplyLevel;
    }

    public void setMappingApplyLevel(Integer mappingApplyLevel) {
        this.mappingApplyLevel = mappingApplyLevel;
    }

    public Date getLastRequestTime() {
        return lastRequestTime;
    }

    public void setLastRequestTime(Date lastRequestTime) {
        this.lastRequestTime = lastRequestTime;
    }

    public Date getLastReportTime() {
        return lastReportTime;
    }

    public void setLastReportTime(Date lastReportTime) {
        this.lastReportTime = lastReportTime;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Date getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }

    public Date getLastModifyTime() {
        return lastModifyTime;
    }

    public void setLastModifyTime(Date lastModifyTime) {
        this.lastModifyTime = lastModifyTime;
    }

    public String getEnable() {
        return enable;
    }

    public void setEnable(String enable) {
        this.enable = enable;
    }

    public String getTypeName() {
        return typeName;
    }

    public void setTypeName(String typeName) {
        this.typeName = typeName;
    }

    public Long getConsumeNum() {
        return consumeNum;
    }

    public void setConsumeNum(Long consumeNum) {
        this.consumeNum = consumeNum;
    }

    public String getMapping() {
        return mapping;
    }

    public void setMapping(String mapping) {
        this.mapping = mapping;
    }

    public String getConsumerType() {
        return consumerType;
    }

    public void setConsumerType(String consumerType) {
        this.consumerType = consumerType;
    }

    public String getConsumerConfig() {
        return consumerConfig;
    }

    public void setConsumerConfig(String consumerConfig) {
        this.consumerConfig = consumerConfig;
    }
}
