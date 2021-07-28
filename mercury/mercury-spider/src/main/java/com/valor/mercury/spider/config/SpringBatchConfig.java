package com.valor.mercury.spider.config;


import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.support.MapJobRegistry;
import org.springframework.batch.core.explore.JobExplorer;
import org.springframework.batch.core.explore.support.JobExplorerFactoryBean;
import org.springframework.batch.core.launch.JobOperator;
import org.springframework.batch.core.launch.support.SimpleJobLauncher;
import org.springframework.batch.core.launch.support.SimpleJobOperator;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.repository.support.JobRepositoryFactoryBean;
import org.springframework.batch.support.DatabaseType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.SimpleAsyncTaskExecutor;
import org.springframework.transaction.PlatformTransactionManager;

import javax.sql.DataSource;

@Configuration
public class SpringBatchConfig {

    private final DataSource dispatchDataSource;
    private final PlatformTransactionManager txManager;

    @Autowired
    public SpringBatchConfig(@Qualifier("dataSource") DataSource dispatchDataSource, @Qualifier("dispatchTx") PlatformTransactionManager txManager) {
        this.dispatchDataSource = dispatchDataSource;
        this.txManager = txManager;
    }

    @Bean(name = "jobRepository")
    public JobRepository jobRepository() throws Exception {
        JobRepositoryFactoryBean jobRepositoryFactoryBean = new JobRepositoryFactoryBean();
        jobRepositoryFactoryBean.setDataSource(dispatchDataSource);
        jobRepositoryFactoryBean.setTransactionManager(txManager);
        jobRepositoryFactoryBean.setDatabaseType(DatabaseType.H2.name());
        return jobRepositoryFactoryBean.getObject();
    }

    @Bean(name = "jobLauncher")
    public SimpleJobLauncher jobLauncher() throws Exception {
        SimpleJobLauncher simpleJobLauncher = new SimpleJobLauncher();
        simpleJobLauncher.setJobRepository(jobRepository());
        simpleJobLauncher.setTaskExecutor(new SimpleAsyncTaskExecutor());
        return simpleJobLauncher;
    }

    @Bean
    public JobExplorer jobExplorer() throws Exception {
        JobExplorerFactoryBean jobExplorerFactoryBean = new JobExplorerFactoryBean();
        jobExplorerFactoryBean.setDataSource(dispatchDataSource);
        jobExplorerFactoryBean.afterPropertiesSet();
        return jobExplorerFactoryBean.getObject();
    }

    @Bean(name = "jobBuilderFactory")
    public JobBuilderFactory jobBuilders() throws Exception {
        JobBuilderFactory jobBuilderFactory = new JobBuilderFactory(jobRepository());
        return jobBuilderFactory;
    }

    @Bean(name = "jobOperator")
    public JobOperator jobOperator() throws Exception {
        SimpleJobOperator jobOperator = new SimpleJobOperator();
        jobOperator.setJobExplorer(jobExplorer());
        jobOperator.setJobLauncher(jobLauncher());
        jobOperator.setJobRegistry(new MapJobRegistry());
        jobOperator.setJobRepository(jobRepository());
        return jobOperator;
    }
}
