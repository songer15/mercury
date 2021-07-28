package com.valor.mercury.manager.config;

import com.valor.mercury.manager.service.LogAlarmService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.SchedulingConfigurer;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.scheduling.config.ScheduledTaskRegistrar;

/**
 * @author Gavin
 * 2020/9/8 14:22
 */
@Configuration
@EnableScheduling
@EnableAsync
public class ScheduleConfig implements SchedulingConfigurer {

    private static final Logger logger = LoggerFactory.getLogger(ScheduleConfig.class);
    @Autowired
    private LogAlarmService logAlarmService;

    @Override
    public void configureTasks(ScheduledTaskRegistrar scheduledTaskRegistrar) {
        scheduledTaskRegistrar.setScheduler(taskExecutor());
    }

    @Bean(destroyMethod = "shutdown")
    public ThreadPoolTaskScheduler taskExecutor() {
        ThreadPoolTaskScheduler scheduler = new ThreadPoolTaskScheduler();
        scheduler.setPoolSize(5);
        scheduler.setThreadNamePrefix("schedule-");
        scheduler.setAwaitTerminationSeconds(600);
        scheduler.setErrorHandler(throwable -> {
            logger.error("调度任务发生异常：{}", throwable);
            logAlarmService.addLog(MercuryConstants.LOG_LEVEL_ERROR, this.getClass().getName(), "scheduler", "scheduler error:" + throwable.getMessage());
        });
        scheduler.setWaitForTasksToCompleteOnShutdown(true);
        return scheduler;
    }

//    @Bean(destroyMethod = "shutdown")
//    public Executor setExecutor() {
//        return Executors.newScheduledThreadPool(5);
//    }
}
