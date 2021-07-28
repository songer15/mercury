package com.valor.mercury.manager;

import com.mfc.config.ConfigTools3;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class MercuryManagerApplication {

    private final static Logger logger = LoggerFactory.getLogger(MercuryManagerApplication.class);

    public static void main(String[] args) {
        ConfigTools3.load("cfg");
        SpringApplication.run(MercuryManagerApplication.class, args);
        logger.info("Mercury Manager Application Started");
    }
}
