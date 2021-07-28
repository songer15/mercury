package com.valor.mercury.executor;

import com.mfc.config.ConfigTools3;
import com.valor.mercury.common.model.ExecutorCommand;
import com.valor.mercury.common.model.ExecutorReport;
import com.valor.mercury.common.util.PostUtil;
import com.valor.mercury.common.util.StringTools;
import com.valor.mercury.executor.flink.FlinkTaskExecutor;
import com.valor.mercury.executor.hive.HiveTaskExecutor;
import com.valor.mercury.executor.script.groovy.ScriptTaskExecutor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Date;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;

import static com.valor.mercury.common.constant.MercuryConstants.EXECUTOR_INSTANCE_STATUS_FAIL;


public abstract class TaskExecutor {
    private static Logger logger = LoggerFactory.getLogger(TaskExecutor.class);
    private static long runInterval  = 30_000;
    private static long reportInterval = 30_000;
    private ThreadPoolExecutor threadPool = (ThreadPoolExecutor) Executors.newFixedThreadPool(3);
    protected String clientName;
    protected String clientPsd;
    protected boolean continuouslyReport;
    protected boolean concurrentlyRun;
    protected volatile String executorStatus;

    abstract public void runTask(ExecutorCommand command, ExecutorReport report) throws Exception;
    abstract public void continuouslyReport() throws Exception;

    public void start() {
        String executorType;
        if (this instanceof FlinkTaskExecutor)
           executorType = "flink";
        else if (this instanceof ScriptTaskExecutor)
           executorType = "script";
        else if (this instanceof HiveTaskExecutor)
           executorType = "hive";
        else
           throw new IllegalArgumentException("no executor");
        clientName = ConfigTools3.getConfigAsString(executorType + ".executor.name");
        clientPsd = ConfigTools3.getConfigAsString(executorType + ".executor.password");
        new Thread(() -> {
            while (true) {
                List<ExecutorCommand> commands = PostUtil.getCommandsFromMercury(clientName, clientPsd, executorStatus);
                for (ExecutorCommand command : commands) {
                    logger.info("get task: {}", command.toString());
                    ExecutorReport report = new ExecutorReport(clientName, clientPsd, command.getInstanceID());
                    if (concurrentlyRun) {
                        threadPool.execute(() -> runTaskAndReport(command, report));
                    } else {
                        runTaskAndReport(command, report);
                    }
                }
                try {
                    Thread.sleep(runInterval);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }, executorType + "-work").start();
        if (continuouslyReport)
            new Thread(() -> {
                while (true) {
                    try {
                        continuouslyReport();
                    } catch (Exception ex) {
                        logger.error("report task error: ", ex);
                    }
                    try {
                        Thread.sleep(reportInterval);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }, executorType + "-report").start();
    }

    private void runTaskAndReport(ExecutorCommand command, ExecutorReport report) {
        try {
            report.setStartTime();
            runTask(command, report);
        } catch (Exception ex) {
            logger.error("execute task error: ", ex);
            report.setInstanceStatus(EXECUTOR_INSTANCE_STATUS_FAIL);
            report.setErrorMessage(StringTools.buildErrorMessage(ex));
        } finally {
            report.setEndTime();
            report.setActionTime(new Date());
            PostUtil.reportToMercury(report);
        }
    }
}
