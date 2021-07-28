package com.valor.mercury.manager.model.system;

import java.util.Date;

/**
 * @author Gavin
 * 2020/10/29 18:14
 */
public class FlinkMetric extends MonitorMetric {
    private int runningJobs = -1;
    private int taskSlotsAvailable = -1;
    private int nodeNumber = -1;
    private int memoryUsed = -1;
    private int taskSlotsTotal = -1;
    private int memoryMax = -1;

    public int getMemoryUsed() {
        return memoryUsed;
    }

    public void setMemoryUsed(int memoryUsed) {
        this.memoryUsed = memoryUsed;
    }

    public int getTaskSlotsTotal() {
        return taskSlotsTotal;
    }

    public void setTaskSlotsTotal(int taskSlotsTotal) {
        this.taskSlotsTotal = taskSlotsTotal;
    }

    public int getMemoryMax() {
        return memoryMax;
    }

    public void setMemoryMax(int memoryMax) {
        this.memoryMax = memoryMax;
    }

    public int getNodeNumber() {
        return nodeNumber;
    }

    public void setNodeNumber(int nodeNumber) {
        this.nodeNumber = nodeNumber;
    }

    public int getRunningJobs() {
        return runningJobs;
    }

    public void setRunningJobs(int runningJobs) {
        this.runningJobs = runningJobs;
    }

    public int getTaskSlotsAvailable() {
        return taskSlotsAvailable;
    }

    public void setTaskSlotsAvailable(int taskSlotsAvailable) {
        this.taskSlotsAvailable = taskSlotsAvailable;
    }

    @Override
    public void setStatus(String status) {
        this.status = status;
    }

    @Override
    public void setActionTime(Date actionTime) {
        this.actionTime = actionTime;
    }

    @Override
    public void setEnable(String enable) {
        this.enable = enable;
    }

    @Override
    public String getStatus() {
        return status;
    }

    @Override
    public Date getActionTime() {
        return actionTime;
    }

    @Override
    public String getEnable() {
        return enable;
    }
}
