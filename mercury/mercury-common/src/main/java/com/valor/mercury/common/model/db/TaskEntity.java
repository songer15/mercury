package com.valor.mercury.common.model.db;


import com.valor.mercury.common.model.AbstractLMI;
import org.hibernate.annotations.GenericGenerator;

import javax.persistence.*;
import java.util.Date;

@Entity
@Table(name = "work_item")
public class TaskEntity extends AbstractLMI implements Comparable {

    @Id
    @Column(name = "id")
    @GenericGenerator(name = "idGenerator", strategy = "identity")
    @GeneratedValue(generator = "idGenerator")
    private Long id;


    @Column(name = "task_name")
    private String taskName;  //任务名

    @Column(name = "enable")
    private boolean enable;    //该任务实例是否可用：true表示可用，即可以被调度。false表示不可用，已完成或发生错误。

    @Column(name = "ter")
    private String ter;    //增量标记上限

    @Column(name = "pre")
    private String pre;   //增量标记下限

    @Column(name = "priority")
    private Integer priority;    //任务优先级

    @Column(name = "retry_flag")
    private Boolean retryFlag;      //是否是重启的任务

    @Column(name = "instance_id")
    private Long instanceId;      //spring_batch执行实例ID

    @Column(name = "server_id")
    private long serverId;     //所属服务Id

    @Column(name = "result")
    private String result;              //运行结果

    @Column(name = "config", length = 2048)
    private String config;    //运行参数

    @Column(name = "start_time")
    private Date startTime;

    @Column(name = "end_time")
    private Date endTime;

    @Column(name = "error_msg", length = 2048)
    private String errorMsg;

    @Column(name = "error_type")
    private String errorType;

    @Column(name = "success_count")
    private Integer successCount;  //成功的数据量

    @Column(name = "fail_count")
    private Integer failCount;  //失败的数据量

    @Column(name = "total_count")
    private Integer totalCount;   //总共的数据量

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

    @Transient
    private Object temVariable;     //预留


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


    public long getServerId() {
        return serverId;
    }

    public void setServerId(long serverId) {
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
        TaskEntity item = (TaskEntity) o;
        if (this.priority > item.priority) return 1;
        else if (this.priority < item.priority) return -1;
        else return 0;
    }

}
