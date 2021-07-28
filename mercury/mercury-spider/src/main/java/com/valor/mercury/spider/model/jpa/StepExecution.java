package com.valor.mercury.spider.model.jpa;


import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import java.util.Date;

@Entity
@Table(name="BATCH_STEP_EXECUTION")
public class StepExecution {

    private Long stepExecutionId;
    private Long version;
    private String stepName;
    private Long jobExecutionId;
    private Date startTime;
    private Date endTime;
    private String status;
    private Long commitCount;
    private Long readCount;
    private Long filterCount;
    private Long writeCount;
    private Long readSkipCount;
    private Long writeSkipCount;
    private Long processSkipCount;
    private Long rollBackCount;
    private String exitCode;
    private String exitMessage;
    private Date lastUpdated;

    @Id
    @Column(name="STEP_EXECUTION_ID")
    public Long getStepExecutionId() {
        return stepExecutionId;
    }

    public void setStepExecutionId(Long stepExecutionId) {
        this.stepExecutionId = stepExecutionId;
    }

    @Column(name="VERSION")
    public Long getVersion() {
        return version;
    }

    public void setVersion(Long version) {
        this.version = version;
    }

    @Column(name="STEP_NAME")
    public String getStepName() {
        return stepName;
    }

    public void setStepName(String stepName) {
        this.stepName = stepName;
    }

    @Column(name="JOB_EXECUTION_ID")
    public Long getJobExecutionId() {
        return jobExecutionId;
    }

    public void setJobExecutionId(Long jobExecutionId) {
        this.jobExecutionId = jobExecutionId;
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

    @Column(name="COMMIT_COUNT")
    public Long getCommitCount() {
        return commitCount;
    }

    public void setCommitCount(Long commitCount) {
        this.commitCount = commitCount;
    }

    @Column(name="READ_COUNT")
    public Long getReadCount() {
        return readCount;
    }

    public void setReadCount(Long readCount) {
        this.readCount = readCount;
    }

    @Column(name="FILTER_COUNT")
    public Long getFilterCount() {
        return filterCount;
    }

    public void setFilterCount(Long filterCount) {
        this.filterCount = filterCount;
    }

    @Column(name="WRITE_COUNT")
    public Long getWriteCount() {
        return writeCount;
    }

    public void setWriteCount(Long writeCount) {
        this.writeCount = writeCount;
    }

    @Column(name="READ_SKIP_COUNT")
    public Long getReadSkipCount() {
        return readSkipCount;
    }

    public void setReadSkipCount(Long readSkipCount) {
        this.readSkipCount = readSkipCount;
    }

    @Column(name="WRITE_SKIP_COUNT")
    public Long getWriteSkipCount() {
        return writeSkipCount;
    }

    public void setWriteSkipCount(Long writeSkipCount) {
        this.writeSkipCount = writeSkipCount;
    }

    @Column(name="PROCESS_SKIP_COUNT")
    public Long getProcessSkipCount() {
        return processSkipCount;
    }

    public void setProcessSkipCount(Long processSkipCount) {
        this.processSkipCount = processSkipCount;
    }

    @Column(name="ROLLBACK_COUNT")
    public Long getRollBackCount() {
        return rollBackCount;
    }

    public void setRollBackCount(Long rollBackCount) {
        this.rollBackCount = rollBackCount;
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
    public Date getLastUpdated() {
        return lastUpdated;
    }

    public void setLastUpdated(Date lastUpdated) {
        this.lastUpdated = lastUpdated;
    }
}
