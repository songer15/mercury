package com.valor.mercury.receiver;

import com.mfc.config.ConfigTools3;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;


@EnableScheduling
@SpringBootApplication
public class ReceiverApplication {
    private final static Logger logger = LoggerFactory.getLogger(ReceiverApplication.class);

    public static void main(String[] args) {
        ConfigTools3.load("cfg");
        SpringApplication.run(ReceiverApplication.class, args);
        logger.info("*************************************** Metric Receiver Application Started******************************************************************");
    }
}
