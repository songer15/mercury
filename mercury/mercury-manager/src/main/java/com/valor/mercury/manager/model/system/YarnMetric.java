package com.valor.mercury.manager.model.system;

import java.util.Date;

/**
 * @author Gavin
 * 2020/10/29 18:15
 */
public class YarnMetric extends MonitorMetric {

    private int runningJobs = -1;
    private int appsSubmitted = -1;
    private int nodeNumber = -1;
    private int activeNodes = -1;
    private int availableVirtualCores = -1;
    private int availableMB = -1;
    private int containersAllocated = -1;

    public int getAppsSubmitted() {
        return appsSubmitted;
    }

    public void setAppsSubmitted(int appsSubmitted) {
        this.appsSubmitted = appsSubmitted;
    }

    public int getActiveNodes() {
        return activeNodes;
    }

    public void setActiveNodes(int activeNodes) {
        this.activeNodes = activeNodes;
    }

    public int getAvailableVirtualCores() {
        return availableVirtualCores;
    }

    public void setAvailableVirtualCores(int availableVirtualCores) {
        this.availableVirtualCores = availableVirtualCores;
    }

    public int getAvailableMB() {
        return availableMB;
    }

    public void setAvailableMB(int availableMB) {
        this.availableMB = availableMB;
    }

    public int getContainersAllocated() {
        return containersAllocated;
    }

    public void setContainersAllocated(int containersAllocated) {
        this.containersAllocated = containersAllocated;
    }

    public int getRunningJobs() {
        return runningJobs;
    }

    public void setRunningJobs(int runningJobs) {
        this.runningJobs = runningJobs;
    }

    public int getNodeNumber() {
        return nodeNumber;
    }

    public void setNodeNumber(int nodeNumber) {
        this.nodeNumber = nodeNumber;
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
