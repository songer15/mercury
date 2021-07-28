package com.valor.mercury.manager.model.ddo;

import org.hibernate.annotations.Generated;

import javax.persistence.*;
import java.util.Date;

/**
 * @Author: Gavin
 * @Date: 2020/2/24 14:05
 */
@Entity
@Table(name = "executor")
public class Executor {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "client_name")
    private String clientName;

    @Column(name = "description")
    private String description;

    @Column(name = "client_password")
    private String clientPsd;

    @Column(name = "client_ip")
    private String clientIP;

    @Column(name = "create_time")
    private Date createTime;

    @Column(name = "last_action_time")
    private Date lastActionTime;

    @Column(name = "last_modify_time", columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP")
    @Temporal(TemporalType.TIMESTAMP)
    @Generated(org.hibernate.annotations.GenerationTime.ALWAYS)
    private Date lastModifyTime;

    @Column(name = "enable")
    private String enable;

    @Column(name = "status")
    private String status;

    @Column(name = "executor_type")
    private String executorType;

    public String getExecutorType() {
        return executorType;
    }

    public void setExecutorType(String executorType) {
        this.executorType = executorType;
    }

    public Date getLastModifyTime() {
        return lastModifyTime;
    }

    public void setLastModifyTime(Date lastModifyTime) {
        this.lastModifyTime = lastModifyTime;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getClientName() {
        return clientName;
    }

    public void setClientName(String clientName) {
        this.clientName = clientName;
    }

    public String getClientPsd() {
        return clientPsd;
    }

    public void setClientPsd(String clientPsd) {
        this.clientPsd = clientPsd;
    }

    public String getClientIP() {
        return clientIP;
    }

    public void setClientIP(String clientIP) {
        this.clientIP = clientIP;
    }

    public Date getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }

    public Date getLastActionTime() {
        return lastActionTime;
    }

    public void setLastActionTime(Date lastActionTime) {
        this.lastActionTime = lastActionTime;
    }

    public String getEnable() {
        return enable;
    }

    public void setEnable(String enable) {
        this.enable = enable;
    }

    @Override
    public String toString() {
        return "Executor{" +
                "id=" + id +
                ", clientName='" + clientName + '\'' +
                ", clientPsd='" + clientPsd + '\'' +
                ", clientIP='" + clientIP + '\'' +
                ", createTime=" + createTime +
                ", lastActionTime=" + lastActionTime +
                ", enable=" + enable +
                '}';
    }
}
