package com.valor.mercury.manager.model.system;

import java.util.Date;

/**
 * @author Gavin
 * 2020/10/29 18:21
 */
public abstract class MonitorMetric {
    protected String status="init"; //green,yellow,red
    protected Date actionTime=new Date();
    protected String enable="init"; //true,false

    public abstract void setStatus(String status);
    public abstract void setActionTime(Date actionTime);
    public abstract void setEnable(String enable);

    public abstract String getStatus();
    public abstract Date getActionTime();
    public abstract String getEnable();
}
