package com.vms.metric.analyse.model;

import java.sql.Timestamp;
import java.util.Date;

public class WorkItem implements Comparable {

    private Long id;
    private String taskName;  //任务名
    private Date createTime;    //生成时间
    private boolean enable;    //是否启用
    private String ter;    //增量标记
    private String pre;   //增量标记
    private Integer priority;    //任务优先级
    private Boolean retryFlag;      //是否是重启的任务
    private Long instanceId;      //spring_batch执行实例ID
    private String serverId;     //所属服务
    private String result;              //运行结果
    private String config;    //运行参数
    private Date startTime;
    private Date endTime;
    private String errorMsg;
    private String errorType;
    private Integer successCount;  //成功的数据量
    private Integer failCount;  //失败的数据量
    private Integer totalCount;   //总共的数据量
    private String readerName;
    private String processorName;
    private String writerName;
    private String listenerName;
    private String postUrl;
    private Integer commitInterval;
    private String primaryKey;
    private Object temVariable;     //预留
    private String lastModifyUser = "auto";
    private Timestamp lastModifyTime;
    private String description;

    public String getLastModifyUser() {
        return lastModifyUser;
    }

    public void setLastModifyUser(String lastModifyUser) {
        this.lastModifyUser = lastModifyUser;
    }

    public Timestamp getLastModifyTime() {
        return lastModifyTime;
    }

    public void setLastModifyTime(Timestamp lastModifyTime) {
        this.lastModifyTime = lastModifyTime;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Object getTemVariable() {
        return temVariable;
    }

    public void setTemVariable(Object temVariable) {
        this.temVariable = temVariable;
    }

    public Long getInstanceId() {
        return instanceId;
    }

    public void setInstanceId(Long instanceId) {
        this.instanceId = instanceId;
    }

    public Integer getPriority() {
        return priority;
    }

    public void setPriority(Integer priority) {
        this.priority = priority;
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

    public boolean isEnable() {
        return enable;
    }

    public void setEnable(boolean enable) {
        this.enable = enable;
    }

    public String getTer() {
        return ter;
    }

    public void setTer(String ter) {
        this.ter = ter;
    }

    public String getPre() {
        return pre;
    }

    public void setPre(String pre) {
        this.pre = pre;
    }

    public Boolean getRetryFlag() {
        return retryFlag;
    }

    public void setRetryFlag(Boolean retryFlag) {
        this.retryFlag = retryFlag;
    }

    public String getTaskName() {
        return taskName;
    }

    public void setTaskName(String taskName) {
        this.taskName = taskName;
    }

    public String getServerId() {
        return serverId;
    }

    public void setServerId(String serverId) {
        this.serverId = serverId;
    }

    public String getConfig() {
        return config;
    }

    public void setConfig(String config) {
        this.config = config;
    }

    public String getResult() {
        return result;
    }

    public void setResult(String result) {
        this.result = result;
    }

    public Date getStartTime() {
        return startTime;
    }

    public void setStartTime(Date startTime) {
        this.startTime = startTime;
    }

    public Date getEndTime() {
        return endTime;
    }

    public void setEndTime(Date endTime) {
        this.endTime = endTime;
    }

    public String getErrorMsg() {
        return errorMsg;
    }

    public void setErrorMsg(String errorMsg) {
        this.errorMsg = errorMsg;
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

    @Override
    public int compareTo(Object o) {
        WorkItem item = (WorkItem) o;
        if (this.priority > item.priority) return 1;
        else if (this.priority < item.priority) return -1;
        else return 0;
    }

    @Override
    public String toString() {
        return "WorkItem{" +
                "id=" + id +
                ", taskName='" + taskName + '\'' +
                ", createTime=" + createTime +
                ", enable=" + enable +
                ", ter='" + ter + '\'' +
                ", pre='" + pre + '\'' +
                ", priority=" + priority +
                ", retryFlag=" + retryFlag +
                ", instanceId=" + instanceId +
                ", result='" + result + '\'' +
                ", config='" + config + '\'' +
                ", startTime=" + startTime +
                ", endTime=" + endTime +
                ", errorMsg='" + errorMsg + '\'' +
                ", errorType='" + errorType + '\'' +
                ", temVariable=" + temVariable +
                '}';
    }
}
