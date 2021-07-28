package com.valor.mercury.manager.model.system;

import java.util.Date;

/**
 * @author Gavin
 * 2020/10/29 18:14
 */
public class HDFSMetric extends MonitorMetric {

    private int liveNodes=-1;
    private int totalBlocks=-1;
    private int used=-1;
    private int free=-1;
    private int threads=-1;
    private int total=-1;
    private int missingBlocks=-1;

    public int getMissingBlocks() {
        return missingBlocks;
    }

    public void setMissingBlocks(int missingBlocks) {
        this.missingBlocks = missingBlocks;
    }

    public int getThreads() {
        return threads;
    }

    public void setThreads(int threads) {
        this.threads = threads;
    }

    public int getTotal() {
        return total;
    }

    public void setTotal(int total) {
        this.total = total;
    }

    public int getUsed() {
        return used;
    }

    public void setUsed(int used) {
        this.used = used;
    }

    public int getFree() {
        return free;
    }

    public void setFree(int free) {
        this.free = free;
    }

    public int getLiveNodes() {
        return liveNodes;
    }

    public void setLiveNodes(int liveNodes) {
        this.liveNodes = liveNodes;
    }

    public int getTotalBlocks() {
        return totalBlocks;
    }

    public void setTotalBlocks(int totalBlocks) {
        this.totalBlocks = totalBlocks;
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
