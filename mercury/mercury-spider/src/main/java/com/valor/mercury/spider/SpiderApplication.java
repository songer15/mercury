package com.valor.mercury.spider;


import com.mfc.config.ConfigTools3;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.scheduling.annotation.EnableScheduling;


@SpringBootApplication
public class SpiderApplication {

    public static void main(String[] args) {

        ConfigTools3.load("cfg");
        SpringApplication springApplication = new SpringApplication(SpiderApplication.class);
        springApplication.run(args);
    }
}
