package com.valor.mercury.manager.model.ddo;

import org.hibernate.annotations.Generated;

import javax.persistence.*;
import java.util.Date;

/**
 * @Author: Gavin
 * @Date: 2020/2/19 14:34
 */
@Entity
@Table(name = "realtime_task")
public class RealTimeTask {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "name")
    private String name;

    @Column(name = "create_time")
    private Date createTime;

    @Column(name = "last_modify_time", columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP")
    @Temporal(TemporalType.TIMESTAMP)
    @Generated(org.hibernate.annotations.GenerationTime.ALWAYS)
    private Date lastModifyTime;

    @Column(name = "description")
    private String desc;

    @Column(name = "status")
    private String status;

    @Column(name = "last_action_time")
    private Date lastActionTime;

    @Column(name = "last_publish_time")
    private Date lastPublishTime;

    @Column(name = "last_publish_instance_id")
    private Long lastPublishInstanceId;

    @Column(name = "executor")
    private String executor;

    @Column(name = "job_id")
    private String jobId;

    @Column(name = "entry_class")
    private String entryClass;

    @Column(name = "file_id")
    private Long fileId;

    @Column(name = "program_arguments")
    private String programArguments;

    @Column(name = "parallelism")
    private int parallelism;

    public String getProgramArguments() {
        return programArguments;
    }

    public void setProgramArguments(String programArguments) {
        this.programArguments = programArguments;
    }

    public Date getLastActionTime() {
        return lastActionTime;
    }

    public void setLastActionTime(Date lastActionTime) {
        this.lastActionTime = lastActionTime;
    }

    public Long getLastPublishInstanceId() {
        return lastPublishInstanceId;
    }

    public void setLastPublishInstanceId(Long lastPublishInstanceId) {
        this.lastPublishInstanceId = lastPublishInstanceId;
    }

    public Long getFileId() {
        return fileId;
    }

    public void setFileId(Long fileId) {
        this.fileId = fileId;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
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

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Date getLastPublishTime() {
        return lastPublishTime;
    }

    public void setLastPublishTime(Date lastPublishTime) {
        this.lastPublishTime = lastPublishTime;
    }

    public String getExecutor() {
        return executor;
    }

    public void setExecutor(String executor) {
        this.executor = executor;
    }

    public String getJobId() {
        return jobId;
    }

    public void setJobId(String jobId) {
        this.jobId = jobId;
    }

    public String getEntryClass() {
        return entryClass;
    }

    public void setEntryClass(String entryClass) {
        this.entryClass = entryClass;
    }

    public int getParallelism() {
        return parallelism;
    }

    public void setParallelism(int parallelism) {
        this.parallelism = parallelism;
    }

    @Override
    public String toString() {
        return "RealTimeTask{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", createTime=" + createTime +
                ", lastModifyTime=" + lastModifyTime +
                ", desc='" + desc + '\'' +
                ", status='" + status + '\'' +
                ", lastActionTime=" + lastActionTime +
                ", lastPublishTime=" + lastPublishTime +
                ", lastPublishInstanceId=" + lastPublishInstanceId +
                ", executor='" + executor + '\'' +
                ", jobId='" + jobId + '\'' +
                ", entryClass='" + entryClass + '\'' +
                ", fileId=" + fileId +
                ", programArguments='" + programArguments + '\'' +
                ", parallelism=" + parallelism +
                '}';
    }
}
