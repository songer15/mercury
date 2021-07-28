package com.valor.mercury.spider.config;


import com.valor.mercury.common.util.PropertyUtil;
import org.apache.commons.dbcp.BasicDataSource;
import org.hibernate.SessionFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.orm.hibernate4.HibernateTransactionManager;
import org.springframework.orm.hibernate4.LocalSessionFactoryBean;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import java.util.Properties;

import static java.sql.Connection.TRANSACTION_READ_COMMITTED;

@Configuration
@EnableTransactionManagement
@AutoConfigureAfter(value = {SpringBatchConfig.class})
public class DbConfig {

    private static final Logger logger = LoggerFactory.getLogger(DbConfig.class);

    @Bean(destroyMethod = "close", name = "dataSource")
    public BasicDataSource dispatchDataSource() {
        BasicDataSource basicDataSource = new BasicDataSource();
        basicDataSource.setDriverClassName(PropertyUtil.getDBDriverName("local"));
        basicDataSource.setUrl(PropertyUtil.getDBUrl("local"));
        basicDataSource.setUsername(PropertyUtil.getDBUserName("local"));
        basicDataSource.setPassword(PropertyUtil.getDBPassword("local"));
        //在返回连接前，是否进行验证。如果使用，必须使用一个SQL SELECT返回至少一行.如果没有配置，连接调用isValid()方法
        basicDataSource.setValidationQuery("SELECT 1");
        //启用poolPreparedStatements后，PreparedStatements 和CallableStatements 都会被缓存起来复用，即相同逻辑的SQL可以复用一个游标，这样可以减少创建游标的数量
        basicDataSource.setPoolPreparedStatements(true);
        // 连接池创建的时候，建立的连接数
        basicDataSource.setInitialSize(1);
        //最大连接数
        basicDataSource.setMaxActive(5);
        //最小空闲连接
        basicDataSource.setMinIdle(1);
        // 最大空闲时间。如果一个用户获取一个连接，不用多长时间会被强行收回
        basicDataSource.setMaxIdle(10);
        // 在抛出异常之前,等待连接被回收的最长时间（当没有可用连接时）。设置为-1表示无限等待
        basicDataSource.setMaxWait(500);
        //连接保持空闲而不被驱逐的最长时间
        basicDataSource.setMinEvictableIdleTimeMillis(1800000);
        //多少毫秒检查一次连接池中空闲的连接
        basicDataSource.setTimeBetweenEvictionRunsMillis(1800000);
        //执行空闲连接验证的线程数
        basicDataSource.setNumTestsPerEvictionRun(3);
        //在从池中获取连接的时候是否验证。如果验证失败，就会放弃这个连接，试图获取另外的连接
        basicDataSource.setTestOnBorrow(true);
        //空闲连接是否进行验证，如果失败，就会从连接池去除
        basicDataSource.setTestWhileIdle(true);
        //在返回连接给连接池的时候，是否进行验证
        basicDataSource.setTestOnReturn(true);
        //连接池创建的连接的默认的TransactionIsolation状态
        basicDataSource.setDefaultTransactionIsolation(TRANSACTION_READ_COMMITTED);
        logger.info("Connect dispatch database:{}", basicDataSource.getUrl());
        return basicDataSource;
    }

    @Bean(destroyMethod = "destroy", name = "dispatchSessionFactoryBean")
    public LocalSessionFactoryBean dispatchSessionFactoryBean() {
        LocalSessionFactoryBean sessionFactoryBean = new LocalSessionFactoryBean();
        sessionFactoryBean.setDataSource(dispatchDataSource());
        sessionFactoryBean.setPackagesToScan("com.valor.mercury.spider.model.*", "com.valor.mercury.common.model.*");

        Properties props = new Properties();
        props.put("hibernate.dialect", "org.hibernate.dialect.H2Dialect");
        props.put("hibernate.show_sql", false);
        props.put("hibernate.connection.isolation", TRANSACTION_READ_COMMITTED);
        props.put("hibernate.use_sql_comments", true);
        props.put("hibernate.cache.use_query_cache", false);
        props.put("hibernate.cache.use_second_level_cache", false);
        props.put("hibernate.connection.CharSet", "utf8");
        props.put("hibernate.connection.characterEncoding", "utf8");
        props.put("hibernate.connection.useUnicode", false);
        props.put("hibernate.autoReconnect", true);
        sessionFactoryBean.setHibernateProperties(props);

        return sessionFactoryBean;
    }

    @Bean(name = "localSessionFactory")
    public SessionFactory localSessionFactory() {
        return dispatchSessionFactoryBean().getObject();
    }

    @Bean(name = "dispatchTx")
    public PlatformTransactionManager txManager() {
        return new DataSourceTransactionManager(dispatchDataSource());
    }

//    @Bean(name = "hibernateTx")
//    public PlatformTransactionManager localTransactionManager() {
//        HibernateTransactionManager transactionManager = new HibernateTransactionManager(localSessionFactory());
//        transactionManager.setDefaultTimeout(30);
//        return transactionManager;
//    }

}
