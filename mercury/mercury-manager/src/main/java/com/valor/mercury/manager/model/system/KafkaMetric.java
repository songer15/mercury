package com.valor.mercury.manager.model.system;

import java.util.Date;

/**
 * @author Gavin
 * 2020/10/29 18:15
 */
public class KafkaMetric extends MonitorMetric {

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
