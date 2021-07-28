package com.valor.mercury.manager.model.ddo;

import org.hibernate.annotations.Generated;

import javax.persistence.*;
import java.util.Date;

/**
 * @author Gavin
 * 2020/7/31 11:05
 */
@Entity
@Table(name = "etl_data_receive")
public class ETLDataReceive {
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

    @Column(name = "receive_name")
    private String receiveName;

    @Column(name = "receive_num")
    private Long receiveNum = 0L;

    @Column(name = "forward_num")
    private Long forwardNum = 0L;

    @Column(name = "enable")
    private String enable;

    @Column(name = "last_request_time")
    private Date lastRequestTime;

    @Column(name = "last_report_time")
    private Date lastReportTime;

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

    public String getReceiveName() {
        return receiveName;
    }

    public void setReceiveName(String receiveName) {
        this.receiveName = receiveName;
    }

    public Long getReceiveNum() {
        return receiveNum;
    }

    public void setReceiveNum(Long receiveNum) {
        this.receiveNum = receiveNum;
    }

    public Long getForwardNum() {
        return forwardNum;
    }

    public void setForwardNum(Long forwardNum) {
        this.forwardNum = forwardNum;
    }

    public String getEnable() {
        return enable;
    }

    public void setEnable(String enable) {
        this.enable = enable;
    }

    @Override
    public String toString() {
        return "ETLDataReceive{" +
                "id=" + id +
                ", createTime=" + createTime +
                ", lastModifyTime=" + lastModifyTime +
                ", receiveName='" + receiveName + '\'' +
                ", receiveNum=" + receiveNum +
                ", forwardNum=" + forwardNum +
                ", enable='" + enable + '\'' +
                '}';
    }
}
