package com.valor.mercury.common.model.db;


import com.valor.mercury.common.model.AbstractLMI;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.util.Date;

@Entity
@Table(name = "task")
public class TaskDdo extends AbstractLMI implements Serializable{


    @Id
    @Column(name = "task_name")
    private String taskName;  //任务名

    @NotNull
    @Column(name = "server_id")
    private Long serverId; //所属服务id

    @Column(name = "enable",columnDefinition = "tinyint(1) default 1")
    private Boolean enable;  //是否启用

    @NotNull
    @Column(name="status") //当前运行状态   RUNNING/LINING
    private String Status;

    @Column(name="last_exe_time")
    private Date lastExeTime; //上次生成任务的时间

    @Column(name="next_exe_time")
    private Date nextExeTime;  //计划生成任务的时间

    @Column(name = "error_num")
    private Integer errorNum;       //执行错误次数

    @Column(name = "execute_regulation")
    private String executeRegulation;     //执行调度规则

    @Column(name = "last_update_obj")
    private String lastUpdateObj;    //上次执行记录的增量标记

    @Column(name="reader_name")
    private String readerName;

    @Column(name="processor_name")
    private String processorName;

    @Column(name="writer_name")
    private String writerName;

    @Column(name="listener_name")
    private String listenerName;

    @Column(name="post_url")
    private String postUrl;

    @Column(name="commit_interval")
    private Integer commitInterval;

    @Column(name="primary_key")
    private String primaryKey;

    @Column(name = "config",length = 2048)
    private String config;    //任务配置

    @Column(name="task_desc")
    private String taskDesc;


    public Long getServerId() {
        return serverId;
    }

    public void setServerId(Long serverId) {
        this.serverId = serverId;
    }

    public String getStatus() {
        return Status;
    }

    public void setStatus(String status) {
        Status = status;
    }

    public Date getLastExeTime() {
        return lastExeTime;
    }

    public void setLastExeTime(Date lastExeTime) {
        this.lastExeTime = lastExeTime;
    }

    public Date getNextExeTime() {
        return nextExeTime;
    }

    public void setNextExeTime(Date nextExeTime) {
        this.nextExeTime = nextExeTime;
    }

    public String getTaskName() {
        return taskName;
    }

    public void setTaskName(String taskName) {
        this.taskName = taskName;
    }

    public Boolean getEnable() {
        return enable;
    }

    public void setEnable(Boolean enable) {
        this.enable = enable;
    }

    public Integer getErrorNum() {
        return errorNum;
    }

    public void setErrorNum(Integer errorNum) {
        this.errorNum = errorNum;
    }

    public String getExecuteRegulation() {
        return executeRegulation;
    }

    public void setExecuteRegulation(String executeRegulation) {
        this.executeRegulation = executeRegulation;
    }

    public String getLastUpdateObj() {
        return lastUpdateObj;
    }

    public void setLastUpdateObj(String lastUpdateObj) {
        this.lastUpdateObj = lastUpdateObj;
    }

    public String getConfig() {
        return config;
    }

    public void setConfig(String config) {
        this.config = config;
    }

    public String getReaderName() {
        return readerName;
    }

    public void setReaderName(String readerName) {
        this.readerName = readerName;
    }

    public String getProcessorName() {
        return processorName;
    }

    public void setProcessorName(String processorName) {
        this.processorName = processorName;
    }

    public String getWriterName() {
        return writerName;
    }

    public void setWriterName(String writerName) {
        this.writerName = writerName;
    }

    public String getListenerName() {
        return listenerName;
    }

    public void setListenerName(String listenerName) {
        this.listenerName = listenerName;
    }

    public String getPostUrl() {
        return postUrl;
    }

    public void setPostUrl(String postUrl) {
        this.postUrl = postUrl;
    }

    public Integer getCommitInterval() {
        return commitInterval;
    }

    public void setCommitInterval(Integer commitInterval) {
        this.commitInterval = commitInterval;
    }

    public String getPrimaryKey() {
        return primaryKey;
    }

    public void setPrimaryKey(String primaryKey) {
        this.primaryKey = primaryKey;
    }

    public String getTaskDesc() {
        return taskDesc;
    }

    public void setTaskDesc(String taskDesc) {
        this.taskDesc = taskDesc;
    }

}
