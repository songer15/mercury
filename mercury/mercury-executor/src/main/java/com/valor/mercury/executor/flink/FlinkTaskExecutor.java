package com.valor.mercury.executor.flink;

import com.alibaba.fastjson.JSON;
import com.mfc.config.ConfigTools3;
import com.nextbreakpoint.flinkclient.api.FlinkApi;
import com.nextbreakpoint.flinkclient.model.JobDetailsInfo;
import com.valor.mercury.common.model.ExecutorCommand;
import com.valor.mercury.common.model.ExecutorReport;
import com.valor.mercury.common.util.PostUtil;
import com.valor.mercury.executor.TaskExecutor;

import java.util.*;
import java.util.concurrent.TimeUnit;

import static com.valor.mercury.common.constant.MercuryConstants.*;

public class FlinkTaskExecutor extends TaskExecutor {
    private FlinkApi flinkApi;
    private Map<Long, FlinkTask> jobIdMap;

    public FlinkTaskExecutor() {
        flinkApi = new FlinkApi();
        flinkApi.getApiClient().setBasePath(ConfigTools3.getConfigAsString("flink.jobmanager.addr"));
        flinkApi.getApiClient().getHttpClient().setConnectTimeout(20000, TimeUnit.MILLISECONDS);
        flinkApi.getApiClient().getHttpClient().setWriteTimeout(30000, TimeUnit.MILLISECONDS);
        flinkApi.getApiClient().getHttpClient().setReadTimeout(30000, TimeUnit.MILLISECONDS);
        flinkApi.getApiClient().setDebugging(true);
        jobIdMap = new HashMap<>();
        executorStatus = EXECUTOR_STATUS_WORKING;
        continuouslyReport = true;
        concurrentlyRun = false;
    }

    @Override
    public void runTask(ExecutorCommand command, ExecutorReport report) throws Exception{
        FlinkTask flinkTask = JSON.parseObject(JSON.toJSONString(command.getTaskConfig()), FlinkTask.class);
        flinkTask.setClientVariables(clientName, clientPsd);
        flinkTask.setExecutorVariables(command, report);
        flinkTask.flinkApi = flinkApi;
        flinkTask.init();
        flinkTask.exec();
        //持续汇报成功的任务进度
        if (flinkTask.isStartCommand() && EXECUTOR_INSTANCE_STATUS_SUCCESS.equals(report.getInstanceStatus()))
            jobIdMap.put(flinkTask.instanceId, flinkTask);
    }

    @Override
    public void continuouslyReport() throws Exception {
        Iterator<Map.Entry<Long, FlinkTask>> iterator =  jobIdMap.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<Long, FlinkTask> entry = iterator.next();
            FlinkTask flinkTask = entry.getValue();
            JobDetailsInfo jobDetailsInfo = flinkApi.getJobDetails(flinkTask.jobId);
            if (jobDetailsInfo != null)  {
                JobDetailsInfo.StateEnum jobStatusFromApi = jobDetailsInfo.getState();
                String jobStatus;
                switch (jobStatusFromApi) {
                    case CANCELED:
                    case CANCELLING:
                        jobStatus = EXECUTOR_INSTANCE_STATUS_CANCELED;
                        iterator.remove();
                        break;
                    case FAILED:
                    case FAILING:
                        jobStatus = EXECUTOR_INSTANCE_STATUS_FAIL;
                        iterator.remove();
                        break;
                    default:
                        jobStatus = EXECUTOR_INSTANCE_STATUS_RUNNING;
                }
                flinkTask.executorReport.setInstanceStatus(jobStatus);
                PostUtil.reportToMercury(flinkTask.executorReport);
            }
        }
    }
}
