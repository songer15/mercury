package com.vms.metric.analyse.model;

import java.io.Serializable;
import java.util.Date;

public class Response implements Serializable {

    private String key;
    private String password;
    private Long serverId;
    private Date startTime;    //任务开始时间
    private Date endTime;     //任务结束时间
    private Long id;  //workItem ID
    private Boolean status;  //任务状态（成功/失败）
    private String errorMsg = "";    //错误报告
    private String errorType;   //错误类型
    private String pre;     //增量标记
    private String ter;     //增量标记
    private Integer successCount = 0;  //成功的数据量
    private Integer failCount = 0;  //失败的数据量
    private Integer totalCount = 0;  //总的数据量
    private Long instanceId; //spring-batch 运行实例ID

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public Long getServerId() {
        return serverId;
    }

    public void setServerId(Long serverId) {
        this.serverId = serverId;
    }

    public Date getStartTime() {
        return startTime;
    }

    public void setStartTime(Date startTime) {
        this.startTime = startTime;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Boolean getStatus() {
        return status;
    }

    public void setStatus(Boolean status) {
        this.status = status;
    }

    public String getErrorMsg() {
        return errorMsg;
    }

    public void setErrorMsg(String errorMsg) {
        this.errorMsg = errorMsg;
    }

    public String getPre() {
        return pre;
    }

    public void setPre(String pre) {
        this.pre = pre;
    }

    public String getTer() {
        return ter;
    }

    public void setTer(String ter) {
        this.ter = ter;
    }

    public Long getInstanceId() {
        return instanceId;
    }

    public void setInstanceId(Long instanceId) {
        this.instanceId = instanceId;
    }

    public Date getEndTime() {
        return endTime;
    }

    public void setEndTime(Date endTime) {
        this.endTime = endTime;
    }

    public String getErrorType() {
        return errorType;
    }

    public void setErrorType(String errorType) {
        this.errorType = errorType;
    }

    public Integer getSuccessCount() {
        return successCount;
    }

    public void setSuccessCount(Integer successCount) {
        this.successCount = successCount;
    }

    public Integer getFailCount() {
        return failCount;
    }

    public void setFailCount(Integer failCount) {
        this.failCount = failCount;
    }

    public Integer getTotalCount() {
        return totalCount;
    }

    public void setTotalCount(Integer totalCount) {
        this.totalCount = totalCount;
    }

    @Override
    public String toString() {
        return "Response{" +
                "key='" + key + '\'' +
                ", password='" + password + '\'' +
                ", startTime=" + startTime +
                ", endTime=" + endTime +
                ", id=" + id +
                ", status=" + status +
                ", errorMsg='" + errorMsg + '\'' +
                ", errorType='" + errorType + '\'' +
                ", pre='" + pre + '\'' +
                ", ter='" + ter + '\'' +
                ", successCount=" + successCount +
                ", failCount=" + failCount +
                ", totalCount=" + totalCount +
                ", instanceId=" + instanceId +
                '}';
    }
}
