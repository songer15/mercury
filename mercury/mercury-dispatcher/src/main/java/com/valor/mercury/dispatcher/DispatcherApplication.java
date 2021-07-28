package com.valor.mercury.dispatcher;

import com.mfc.config.ConfigTools3;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;


@SpringBootApplication
public class DispatcherApplication {
    private final static Logger logger = LoggerFactory.getLogger(DispatcherApplication.class);

    public static void main(String[] args) {
        ConfigTools3.load("cfg");
        SpringApplication.run(DispatcherApplication.class, args);
        logger.info("*************************************** MetricDispatcherApplication Started******************************************************************");
    }
}
