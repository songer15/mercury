package com.valor.mercury.manager.model.ddo;

import org.hibernate.annotations.Generated;

import javax.persistence.*;
import java.util.Date;

/**
 * @author Gavin
 * 2019/11/25 17:29
 */
@Entity
@Table(name = "offline_meta_task_instance")
public class OffLineMetaTaskInstance {

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

    @Column(name = "type")
    private String type;

    @Lob
    @Basic(fetch = FetchType.EAGER)
    @Column(name = "primary_value")
    private String primaryValue;

    @Column(name = "executor")
    private String executor;

    @Column(name = "last_action_time")
    private Date lastActionTime;

    @Column(name = "execute_time")
    private Date executeTime;

    @Column(name = "start_time")
    private Date startTime;

    @Column(name = "end_time")
    private Date endTime;

    @Column(name = "task_instance_id")
    private Long taskInstanceId;

    @Column(name = "meta_task_id")
    private Long metaTaskId;

    @Column(name = "result")
    private String result;

    @Column(name = "dep_node")
    private Long depNode;

    @Column(name = "next_node")
    private Long nextNode;

    @Column(name = "running_batch")
    private Integer runningBatch;

    @Lob
    @Basic(fetch = FetchType.EAGER)
    @Column(name = "metrics_value", columnDefinition = "TEXT")
    private String metrics;

    @Lob
    @Basic(fetch = FetchType.EAGER)
    @Column(name = "error_msg", columnDefinition = "TEXT")
    private String errorMsg;

    @Transient
    private String extValue;

    public String getExtValue() {
        return extValue;
    }

    public void setExtValue(String extValue) {
        this.extValue = extValue;
    }

    public String getErrorMsg() {
        return errorMsg;
    }

    public void setErrorMsg(String errorMsg) {
        this.errorMsg = errorMsg;
    }

    public Date getLastActionTime() {
        return lastActionTime;
    }

    public void setLastActionTime(Date lastActionTime) {
        this.lastActionTime = lastActionTime;
    }

    public String getMetrics() {
        return metrics;
    }

    public void setMetrics(String metrics) {
        this.metrics = metrics;
    }

    public Long getNextNode() {
        return nextNode;
    }

    public void setNextNode(Long nextNode) {
        this.nextNode = nextNode;
    }

    public Date getExecuteTime() {
        return executeTime;
    }

    public void setExecuteTime(Date executeTime) {
        this.executeTime = executeTime;
    }

    public Integer getRunningBatch() {
        return runningBatch;
    }

    public void setRunningBatch(Integer runningBatch) {
        this.runningBatch = runningBatch;
    }

    public Long getDepNode() {
        return depNode;
    }

    public void setDepNode(Long depNode) {
        this.depNode = depNode;
    }

    public String getResult() {
        return result;
    }

    public void setResult(String result) {
        this.result = result;
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

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getPrimaryValue() {
        return primaryValue;
    }

    public void setPrimaryValue(String primaryValue) {
        this.primaryValue = primaryValue;
    }

    public String getExecutor() {
        return executor;
    }

    public void setExecutor(String executor) {
        this.executor = executor;
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

    public Long getTaskInstanceId() {
        return taskInstanceId;
    }

    public void setTaskInstanceId(Long taskInstanceId) {
        this.taskInstanceId = taskInstanceId;
    }

    public Long getMetaTaskId() {
        return metaTaskId;
    }

    public void setMetaTaskId(Long metaTaskId) {
        this.metaTaskId = metaTaskId;
    }
}
