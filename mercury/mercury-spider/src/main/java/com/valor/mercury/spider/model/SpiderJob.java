package com.valor.mercury.spider.model;

import com.valor.mercury.common.model.AbstractLMI;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import static com.valor.mercury.common.constant.MercuryConstants.EXECUTOR_INSTANCE_STATUS_LINING;

public class SpiderJob extends AbstractLMI {
    public String commandType;
    public Long instanceID;
    public String taskName;
    public String outPutName;
    public String reader;
    public String processor;
    public String writer = "HttpItemWriter";
    public String listener = "IncJobListener";
    public String incTag;
    public String incValue;
    public String config;
    public Map<String, Object> configMap = new HashMap<>();

    public int readNumber;
    public int processNumber;
    public int sendNumber;
    public long startTime  = System.currentTimeMillis();
    public long endTime = System.currentTimeMillis();
    //任务实时的增量标记
    public String errorMessage = null;
    //任务当前运行状态, 初始化 LINING
    public String instanceStatus = EXECUTOR_INSTANCE_STATUS_LINING;

    public SpiderJob() {}

    public SpiderJob(String commandType, Long instanceID) {
        this.commandType = commandType;
        this.instanceID = instanceID;
    }

    public void addTaskConfig(SpiderJob another) {
        this.taskName = another.taskName;
        this.outPutName = another.outPutName;
        this.reader = another.reader;
        this.processor = another.processor;
        this.writer = another.writer;
        this.listener = another.listener;
        this.incTag = another.incTag;
        this.incValue = another.incValue;
        this.config = another.config;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SpiderJob that = (SpiderJob) o;
        return instanceID.equals(that.instanceID);
    }

    @Override
    public int hashCode() {
        return Objects.hash(instanceID);
    }
}
