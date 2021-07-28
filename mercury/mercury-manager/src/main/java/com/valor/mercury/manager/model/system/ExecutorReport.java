package com.valor.mercury.manager.model.system;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import static com.valor.mercury.manager.config.MercuryConstants.EXECUTOR_INSTANCE_STATUS_LINING;

/**
 * @author Gavin
 * 2020/8/11 11:24
 */
@JsonIgnoreProperties(value = { "hibernateLazyInitializer", "handler" })
public class ExecutorReport {
    private String clientName;
    private String clientPsd;
    //LINING RUNNING SUCCESS FAIL CANCELLED
    private String instanceStatus = EXECUTOR_INSTANCE_STATUS_LINING;
    private Date actionTime;
    private Long instanceID;
    private String taskType;
    private Map<String, Object> metrics = new HashMap<>();
    private String errorMessage;

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

    public String getTaskType() {
        return taskType;
    }

    public void setTaskType(String taskType) {
        this.taskType = taskType;
    }

    public String getInstanceStatus() { return instanceStatus; }

    public void setInstanceStatus(String instanceStatus) {
        this.instanceStatus = instanceStatus;
    }

    public Date getActionTime() {
        return actionTime;
    }

    public void setActionTime(Date actionTime) {
        this.actionTime = actionTime;
    }

    public Long getInstanceID() {
        return instanceID;
    }

    public void setInstanceID(Long instanceID) {
        this.instanceID = instanceID;
    }

    public Map<String, Object> getMetrics() {
        return metrics;
    }

    public void setMetrics(Map<String, Object> metrics) {
        this.metrics = metrics;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    public ExecutorReport(String clientName, String clientPsd, Long instanceID) {
        this.clientName = clientName;
        this.clientPsd = clientPsd;
        this.instanceID = instanceID;
    }

    public ExecutorReport(String clientName, String clientPsd, Long instanceID, String taskType) {
        this.clientName = clientName;
        this.clientPsd = clientPsd;
        this.instanceID = instanceID;
        this.taskType = taskType;
    }

    public ExecutorReport() {
    }

    @Override
    public String toString() {
        return "ExecutorReport{" +
                "clientName='" + clientName + '\'' +
                ", clientPsd='" + clientPsd + '\'' +
                ", instanceStatus='" + instanceStatus + '\'' +
                ", actionTime=" + actionTime +
                ", instanceID=" + instanceID +
                ", taskType='" + taskType + '\'' +
                ", metrics=" + metrics +
                ", errorMessage='" + errorMessage + '\'' +
                '}';
    }
}
