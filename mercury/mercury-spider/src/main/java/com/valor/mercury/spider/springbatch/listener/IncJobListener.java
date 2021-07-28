package com.valor.mercury.spider.springbatch.listener;

import com.valor.mercury.common.constant.MercuryConstants;
import com.valor.mercury.common.util.StringTools;
import com.valor.mercury.spider.model.SpiderJob;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.BatchStatus;
import org.springframework.batch.core.JobExecution;

public class IncJobListener extends BaseJobListener {

    private static Logger logger = LoggerFactory.getLogger(BaseJobListener.class);

    @Override
    public void beforeJob(JobExecution jobExecution) {
        spiderJob.instanceStatus = MercuryConstants.EXECUTOR_INSTANCE_STATUS_RUNNING;
        spiderJob.startTime = System.currentTimeMillis();   //设置任务开始时间
        logger.info("job listener beforeJob:{}", spiderJob.taskName);
    }

    @Override
    public void afterJob(JobExecution jobExecution) {
        logger.info("job listener after jobResult:{}", jobExecution.getStatus());
        if (jobExecution.getStatus() == BatchStatus.COMPLETED) //job成功
            spiderJob.instanceStatus = MercuryConstants.EXECUTOR_INSTANCE_STATUS_SUCCESS;
        else { //job失败
            spiderJob.instanceStatus = MercuryConstants.EXECUTOR_INSTANCE_STATUS_FAIL;
            StringBuilder errorMsg = new StringBuilder();
            for (Throwable throwable : jobExecution.getAllFailureExceptions()) {
                errorMsg.append(StringTools.buildErrorMessage(throwable));
                errorMsg.append("\n");
            }
            spiderJob.errorMessage = errorMsg.toString();
        }
        spiderJob.endTime = System.currentTimeMillis();  //设置任务结束时间
    }

    @Override
    public void init(SpiderJob spiderJob) throws Exception {
        this.spiderJob = spiderJob;
    }
}
