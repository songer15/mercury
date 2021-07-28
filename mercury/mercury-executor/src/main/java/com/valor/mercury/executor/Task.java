package com.valor.mercury.executor;

import com.alibaba.fastjson.JSON;
import com.valor.mercury.common.model.AbstractPrintable;
import com.valor.mercury.common.model.ExecutorCommand;
import com.valor.mercury.common.model.ExecutorReport;
import com.valor.mercury.executor.flink.FlinkTask;

public class Task extends AbstractPrintable {
    public Long instanceId;
    public String taskType;
    public String commandType;
    public boolean isArgsValid;
    public boolean isExecutable;
    public String clientName;
    public String clientPsd;
    public ExecutorReport executorReport;
    public ExecutorCommand executorCommand;

    public void setExecutorVariables(ExecutorCommand command, ExecutorReport report) {
        if (command != null) {
            executorCommand = command;
            commandType = executorCommand.getCommandType();
            instanceId = executorCommand.getInstanceID();
        } else
            throw new NullPointerException("command is null");
        if (report != null) {
            executorReport = report;
            if (executorReport.getTaskType() == null)
                report.setTaskType(taskType);
        } else
            throw new NullPointerException("report is null");
    }

    public void setClientVariables(String clientName, String clientPsd) {
        this.clientName = clientName;
        this.clientPsd = clientPsd;
    }

}
