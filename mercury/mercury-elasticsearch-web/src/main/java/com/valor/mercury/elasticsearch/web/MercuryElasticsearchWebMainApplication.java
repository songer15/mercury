package com.valor.mercury.elasticsearch.web;

import com.mfc.config.ConfigTools3;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class MercuryElasticsearchWebMainApplication {
    public static void main(String[] args) {
        ConfigTools3.load("cfg");
        SpringApplication.run(MercuryElasticsearchWebMainApplication.class, args);

        System.out.println("********* Mercury Elasticsearch Web Start *********");
    }
}

