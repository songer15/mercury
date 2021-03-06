package com.valor.mercury.spider.service;

import com.valor.mercury.common.util.PropertyUtil;
import com.valor.mercury.spider.model.SpiderJob;
import com.valor.mercury.spider.springbatch.listener.BaseJobListener;
import com.valor.mercury.spider.springbatch.listener.IncJobListener;
import com.valor.mercury.spider.springbatch.processor.BaseProcessor;
import com.valor.mercury.spider.springbatch.processor.DefaultItemProcessor;
import com.valor.mercury.spider.springbatch.processor.IDIncItemProcessor;
import com.valor.mercury.spider.springbatch.processor.TimeIncItemProcessor;
import com.valor.mercury.spider.springbatch.reader.BaseReader;
import com.valor.mercury.spider.springbatch.reader.NumberIncPageItemReader;
import com.valor.mercury.spider.springbatch.reader.TimeIncPageItemReader;
import com.valor.mercury.spider.springbatch.writer.BaseWriter;
import com.valor.mercury.spider.springbatch.writer.HttpItemWriter;
import org.apache.commons.lang3.StringUtils;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.batch.core.launch.JobOperator;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.factory.FaultTolerantStepFactoryBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.util.Assert;

import java.util.Date;
import java.util.HashMap;

import static com.valor.mercury.common.constant.MercuryConstants.EXECUTOR_INSTANCE_STATUS_CANCELED;

@Service
public class SpringBatchService {
    @Autowired
    private JobLauncher jobLauncher;
    @Autowired
    private JobOperator jobOperator;
    @Autowired
    @Qualifier("jobRepository")
    private JobRepository jobRepository;
    @Autowired
    @Qualifier("jobBuilderFactory")
    private JobBuilderFactory jobBuilderFactory;
    @Autowired
    @Qualifier("dispatchTx")
    private PlatformTransactionManager dispatchTransactionManager;

    public void run(SpiderJob spiderJob) throws Exception {
        org.springframework.batch.core.Job job = buildJob(spiderJob);
        //??????????????????
        JobParameters jobParams = new JobParametersBuilder().addDate("executeDate", new Date()).addLong("jobId", spiderJob.instanceID).toJobParameters();
        //?????????????????????
        jobLauncher.run(job, jobParams);
    }

    public void stop(SpiderJob spiderJob) throws Exception{
        jobOperator.stop(jobOperator.getRunningExecutions(spiderJob.instanceID.toString()).iterator().next());
        spiderJob.instanceStatus = EXECUTOR_INSTANCE_STATUS_CANCELED;
    }

    private Job buildJob(SpiderJob spiderJob) throws Exception {
        //????????????????????????
        if (!StringUtils.isEmpty(spiderJob.config))
            spiderJob.configMap = PropertyUtil.initConfig(spiderJob.config);
        //??????reader
        Assert.notNull(spiderJob.reader, "readerName is null");
        BaseReader reader;
        if ("NumberIncPageItemReader".equalsIgnoreCase(spiderJob.reader)) {
            reader = new NumberIncPageItemReader();
        } else if ("TimeIncPageItemReader".equalsIgnoreCase(spiderJob.reader)) {
            reader = new TimeIncPageItemReader();
        } else {
            throw new IllegalArgumentException(String.format("reader %s not defined", spiderJob.reader));
        }
        reader.init(spiderJob);
        //??????processor
        Assert.notNull(spiderJob.processor, "processorName is null");
        BaseProcessor processor;
        if ("TimeIncItemProcessor".equalsIgnoreCase(spiderJob.processor)) {
            processor = new TimeIncItemProcessor();
        } else if ("IDIncItemProcessor".equalsIgnoreCase(spiderJob.processor)) {
            processor = new IDIncItemProcessor();
        } else if ("DefaultItemProcessor".equalsIgnoreCase(spiderJob.processor)){
            processor = new DefaultItemProcessor();
        } else {
            throw new IllegalArgumentException(String.format("processor %s not defined", spiderJob.processor));
        }
        processor.init(spiderJob);

        BaseWriter writer = new HttpItemWriter();
        writer.init(spiderJob);

        BaseJobListener jobListener = new IncJobListener();
        jobListener.init(spiderJob);

        //????????????????????????
        FaultTolerantStepFactoryBean<HashMap<String, Object>, HashMap<String, Object>> faultTolerantStepFactoryBean = new FaultTolerantStepFactoryBean<>();
        faultTolerantStepFactoryBean.setCommitInterval(5000);//?????????????????????item??????
        faultTolerantStepFactoryBean.setItemReader(reader);
        faultTolerantStepFactoryBean.setItemProcessor(processor);
        faultTolerantStepFactoryBean.setItemWriter(writer);
        faultTolerantStepFactoryBean.setTransactionManager(dispatchTransactionManager);
        faultTolerantStepFactoryBean.setJobRepository(jobRepository);
//        Map<Class<? extends Throwable>, Boolean> retryErrorMap = new HashMap<>();
//        retryErrorMap.put(IOException.class, true);
//        retryErrorMap.put(HttpTransmitException.class, true);
        //faultTolerantStepFactoryBean.setRetryableExceptionClasses(retryErrorMap);//?????????????????????????????????
//        faultTolerantStepFactoryBean.setRetryLimit(5);//??????????????????
//        Map<Class<? extends Throwable>, Boolean> skipErrorMap = new HashMap<>();
        //skipErrorMap.put(DataProcessException.class, true);
        //faultTolerantStepFactoryBean.setSkippableExceptionClasses(skipErrorMap);//?????????????????????????????????
//        faultTolerantStepFactoryBean.setSkipLimit(10);
        faultTolerantStepFactoryBean.setBeanName("customStep");
        Step step = faultTolerantStepFactoryBean.getObject();

        //????????????
        return jobBuilderFactory.get(spiderJob.instanceID.toString())
                .incrementer(new RunIdIncrementer())
                .listener(jobListener)
                .flow(step)
                .end()
                .build();

    }
}
