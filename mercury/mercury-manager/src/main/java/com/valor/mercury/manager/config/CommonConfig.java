package com.valor.mercury.manager.config;


import org.apache.commons.dbcp.BasicDataSource;
import org.hibernate.SessionFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.web.servlet.MultipartConfigFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.orm.hibernate5.LocalSessionFactoryBean;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import javax.servlet.MultipartConfigElement;
import java.util.Properties;

import static java.sql.Connection.TRANSACTION_READ_COMMITTED;

/**
 * @author Gavin
 * 2019/11/25 17:56
 */
@Configuration
@EnableTransactionManagement
public class CommonConfig {

    private static final Logger logger = LoggerFactory.getLogger(CommonConfig.class);

    @Bean(destroyMethod = "close", name = "dataSource")
    public BasicDataSource dispatchDataSource() {
        BasicDataSource basicDataSource = new BasicDataSource();
        basicDataSource.setDriverClassName(ConstantConfig.localDBDriver());
        basicDataSource.setUrl(ConstantConfig.localDBUrl());
        basicDataSource.setUsername(ConstantConfig.localDBName());
        basicDataSource.setPassword(ConstantConfig.localDBPsw());
        basicDataSource.setInitialSize(5);
        basicDataSource.setMaxActive(10);//最大活跃连接
        basicDataSource.setMinIdle(3); //最小空闲连接
        basicDataSource.setMaxIdle(10); //最大空闲连接
        basicDataSource.setMaxWait(30_000); //当没有可用连接时,连接池等待连接被归还的最大时间
        basicDataSource.setNumTestsPerEvictionRun(5);//空闲连接回收器线程运行时检查的连接数量
        basicDataSource.setTimeBetweenEvictionRunsMillis(3 * 60_000); //每3分钟回收一次空闲连接
        basicDataSource.setMinEvictableIdleTimeMillis(3 * 60_000);//连接在池中保持空闲而不被空闲连接回收器线程回收的最小时间值
        basicDataSource.setDefaultTransactionIsolation(TRANSACTION_READ_COMMITTED);
        basicDataSource.setLogAbandoned(true);
        logger.info("Connect dispatch database:{}", basicDataSource.getUrl());
        return basicDataSource;
    }

    @Bean(destroyMethod = "destroy", name = "dispatchSessionFactoryBean")
    public LocalSessionFactoryBean dispatchSessionFactoryBean() {
        LocalSessionFactoryBean sessionFactoryBean = new LocalSessionFactoryBean();
        sessionFactoryBean.setDataSource(dispatchDataSource());
        sessionFactoryBean.setPackagesToScan("com.valor.mercury.manager.model.*");

        Properties props = new Properties();
        props.put("hibernate.dialect", "org.hibernate.dialect.MySQL5Dialect");
        props.put("hibernate.show_sql", false);
        props.put("hibernate.generate_statistics", false);
        props.put("hibernate.hbm2ddl.auto", "update");        //update:加载hibernate时更新表结构，缺少相应字段则加入，对多余字段不处理。
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
        DataSourceTransactionManager manager = new DataSourceTransactionManager(dispatchDataSource());
        manager.setDefaultTimeout(10);
        return manager;
    }

    @Bean
    public MultipartConfigElement multipartConfigElement() {
        MultipartConfigFactory factory = new MultipartConfigFactory();
        factory.setMaxFileSize("300MB");
        factory.setMaxRequestSize("300MB");
        return factory.createMultipartConfig();
    }
}
