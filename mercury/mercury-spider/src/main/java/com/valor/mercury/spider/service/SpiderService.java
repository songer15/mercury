package com.valor.mercury.spider.service;

import com.alibaba.fastjson.JSON;
import com.valor.mercury.common.model.ExecutorCommand;
import com.valor.mercury.common.model.ExecutorReport;
import com.valor.mercury.common.util.PostUtil;
import com.valor.mercury.common.util.StringTools;
import com.valor.mercury.spider.model.SpiderJob;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.*;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;

import static com.valor.mercury.common.constant.MercuryConstants.*;

@Service
public class SpiderService {
    private static Logger logger = LoggerFactory.getLogger(SpiderService.class);
    //任务调度间隔
    private static final int JobScheduleInterval = 30 * 1000;
    //任务执行过程中超时时间
    private static final int JobExecutionTimeout = 6 * 60 * 60 * 1000;
    //任务队列
    private BlockingQueue<SpiderJob> queue = new ArrayBlockingQueue<>(1000);
    //任务线程池
    private ThreadPoolExecutor threadPool = (ThreadPoolExecutor) Executors.newFixedThreadPool(2);
    //任务进度监控
    private Set<SpiderJob> taskReporter = new HashSet<>();
    @Autowired
    private SpringBatchService springBatchService;

    @PostConstruct
    private void init() {
        new Thread(this::seekJobs, "seekJobs").start();
        new Thread(this::runJobs, "runJobs").start();
        new Thread(this::reportJobs, "reportJobs").start();
    }

    private void seekJobs() {
        while (true) {
            try {
                //获取任务数据
                String executorStatus;
                if (taskReporter.size() == 0) {
                    //监控队列里没有任务时，认为当前服务空闲
                    executorStatus = EXECUTOR_STATUS_WAITING;
                } else {
                    executorStatus = EXECUTOR_STATUS_WORKING;
                }
                List<ExecutorCommand> commands = PostUtil.getCommandsFromMercury(executorStatus);
                if (commands.isEmpty()) {
                    logger.info("no jobs available now");
                } else {
                    for (ExecutorCommand executorCommand : commands) {
                        logger.info("get a job: [{}]", executorCommand.toString());
                        SpiderJob spiderJob = new SpiderJob(executorCommand.getCommandType(), executorCommand.getInstanceID());
                        taskReporter.add(spiderJob);
                        try {
                            SpiderJob taskConfig  = JSON.parseObject(JSON.toJSONString(executorCommand.getTaskConfig()), SpiderJob.class);
                            if (taskConfig != null) {
                                spiderJob.addTaskConfig(taskConfig);
                                if (!queue.offer(spiderJob)) { //任务队列已满
                                    spiderJob.instanceStatus = EXECUTOR_INSTANCE_STATUS_FAIL;
                                    spiderJob.errorMessage = String.format("tasks are too much: %s", queue.size());
                                }
                            } else {//任务解析出错
                                spiderJob.instanceStatus = EXECUTOR_INSTANCE_STATUS_FAIL;
                                spiderJob.errorMessage = String.format("task can not be parsed， %s", executorCommand.toString());
                            }
                        } catch (Exception e) {
                            logger.error("seek jobs error", e);
                            spiderJob.errorMessage = StringTools.buildErrorMessage(e);
                            spiderJob.instanceStatus = EXECUTOR_INSTANCE_STATUS_FAIL;
                        }
                    }
                }

                try {
                    Thread.sleep(JobScheduleInterval);
                } catch (InterruptedException e) {

                }
            } catch (Exception ex) {
                logger.error("seekJobs error, ", ex);
            }

        }
    }

    private void runJobs() {
        while (true) {
            try {
                SpiderJob spiderJob  = queue.take();
                threadPool.execute(() -> {
                    try {
                        switch (spiderJob.commandType) {
                            case EXECUTOR_COMMAND_START: {
                                logger.info("***********************running job:{}*************************", spiderJob.toString());
                                springBatchService.run(spiderJob);
                                break;
                            }
                            case EXECUTOR_COMMAND_STOP: {
                                springBatchService.stop(spiderJob);
                                break;
                            }
                            default: {
                                throw new IllegalArgumentException(String.format("invalid EXECUTOR_COMMAND: %s", spiderJob.commandType) );
                            }
                        }
                    } catch (Exception e) {             //job运行出错
                        logger.error("job execute error:", e);
                        spiderJob.instanceStatus = EXECUTOR_INSTANCE_STATUS_FAIL;
                        spiderJob.endTime = System.currentTimeMillis();
                        spiderJob.errorMessage = StringTools.buildErrorMessage(e);
                    }
                });
            } catch (Exception ex) {
                logger.error("runJobs error, ", ex);
            }
        }
    }

    private void reportJobs() {
        while (true) {
            try {
                Iterator<SpiderJob> iterator = taskReporter.iterator();
                while (iterator.hasNext()) {
                    SpiderJob spiderJob = iterator.next();
                    ExecutorReport executorReport = new ExecutorReport(PostUtil.getClientName(), PostUtil.getClientPassword(), spiderJob.instanceID);
                    executorReport.setInstanceStatus(spiderJob.instanceStatus);
                    executorReport.setErrorMessage(spiderJob.errorMessage);
                    executorReport.setMetrics(JSON.parseObject(JSON.toJSONString(spiderJob), Map.class));
                    if (EXECUTOR_INSTANCE_STATUS_FAIL.equals(executorReport.getInstanceStatus()) || EXECUTOR_INSTANCE_STATUS_SUCCESS.equals(executorReport.getInstanceStatus()) || EXECUTOR_INSTANCE_STATUS_CANCELED.equals(executorReport.getInstanceStatus())) {
                        logger.info("job {} is finished / canceled", executorReport);
                        iterator.remove();
                    }
                    else if ( (EXECUTOR_INSTANCE_STATUS_RUNNING.equals(executorReport.getInstanceStatus()) && (System.currentTimeMillis() - spiderJob.startTime) > JobExecutionTimeout )) {
                        logger.info("job {} is expired, switched status to failed", executorReport);
                        executorReport.setInstanceStatus(EXECUTOR_INSTANCE_STATUS_FAIL);
                        iterator.remove();
                    }
                    PostUtil.reportToMercury(executorReport);
                }

                try {
                    Thread.sleep(JobScheduleInterval);
                } catch (InterruptedException e) {

                }
            } catch (Exception e) {
                logger.error("reportJobs error, ", e);
            }
        }
    }
}
