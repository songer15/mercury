package com.vms.metric.analyse.service;

import com.vms.metric.analyse.model.WorkItem;

import java.text.SimpleDateFormat;


public interface BaseDataAnalyse {

    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    WorkItem execute(WorkItem workItem);
}
