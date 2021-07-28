package com.valor.mercury.common.model;

import java.util.Date;
import java.util.Map;

/**
 * @author Gavin
 * 2020/8/11 11:24
 */
public class ExecutorCommand extends AbstractPrintable{
    private String commandType;    //START STOP
    private Date actionTime;
    private Long instanceID;
    private Map<String, Object> taskConfig;

    public String getCommandType() {
        return commandType;
    }

    public void setCommandType(String commandType) {
        this.commandType = commandType;
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

    public Map<String, Object> getTaskConfig() {
        return taskConfig;
    }

    public void setTaskConfig(Map<String, Object> taskConfig) {
        this.taskConfig = taskConfig;
    }

    @Override
    public String toString() {
        return "ExecutorCommand{" +
                "commandType='" + commandType + '\'' +
                ", actionTime=" + actionTime +
                ", instanceID=" + instanceID +
                ", taskConfig=" + taskConfig +
                '}';
    }
}
