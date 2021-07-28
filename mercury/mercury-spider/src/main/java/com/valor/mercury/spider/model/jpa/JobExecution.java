package com.valor.mercury.spider.model.jpa;


import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import java.util.Date;

@Entity
@Table(name="BATCH_JOB_EXECUTION")
public class JobExecution {

    private Long jobExecutionId;
    private Long version;
    private Long jobInstanceId;
    private Date createTime;
    private Date startTime;
    private Date endTime;
    private String status;
    private String exitCode;
    private String exitMessage;
    private Date lastUpdateTime;
    private String jobConfigurationLocation;

    @Id
    @Column(name="JOB_EXECUTION_ID")
    public Long getJobExecutionId() {
        return jobExecutionId;
    }

    public void setJobExecutionId(Long jobExecutionId) {
        this.jobExecutionId = jobExecutionId;
    }

    @Column(name="VERSION")
    public Long getVersion() {
        return version;
    }

    public void setVersion(Long version) {
        this.version = version;
    }

    @Column(name="JOB_INSTANCE_ID")
    public Long getJobInstanceId() {
        return jobInstanceId;
    }

    public void setJobInstanceId(Long jobInstanceId) {
        this.jobInstanceId = jobInstanceId;
    }

    @Column(name="CREATE_TIME")
    public Date getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }

    @Column(name="START_TIME")
    public Date getStartTime() {
        return startTime;
    }

    public void setStartTime(Date startTime) {
        this.startTime = startTime;
    }

    @Column(name="END_TIME")
    public Date getEndTime() {
        return endTime;
    }

    public void setEndTime(Date endTime) {
        this.endTime = endTime;
    }

    @Column(name="STATUS")
    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    @Column(name="EXIT_CODE")
    public String getExitCode() {
        return exitCode;
    }

    public void setExitCode(String exitCode) {
        this.exitCode = exitCode;
    }

    @Column(name="EXIT_MESSAGE")
    public String getExitMessage() {
        return exitMessage;
    }

    public void setExitMessage(String exitMessage) {
        this.exitMessage = exitMessage;
    }

    @Column(name="LAST_UPDATED")
    public Date getLastUpdateTime() {
        return lastUpdateTime;
    }

    public void setLastUpdateTime(Date lastUpdateTime) {
        this.lastUpdateTime = lastUpdateTime;
    }

    @Column(name="JOB_CONFIGURATION_LOCATION")
    public String getJobConfigurationLocation() {
        return jobConfigurationLocation;
    }

    public void setJobConfigurationLocation(String jobConfigurationLocation) {
        this.jobConfigurationLocation = jobConfigurationLocation;
    }

    @Override
    public String toString() {
        return "JobExecution{" +
                "jobExecutionId=" + jobExecutionId +
                ", version=" + version +
                ", jobInstanceId=" + jobInstanceId +
                ", createTime=" + createTime +
                ", startTime=" + startTime +
                ", endTime=" + endTime +
                ", status='" + status + '\'' +
                ", exitCode='" + exitCode + '\'' +
                ", exitMessage='" + exitMessage + '\'' +
                ", lastUpdateTime=" + lastUpdateTime +
                ", jobConfigurationLocation='" + jobConfigurationLocation + '\'' +
                '}';
    }
}
