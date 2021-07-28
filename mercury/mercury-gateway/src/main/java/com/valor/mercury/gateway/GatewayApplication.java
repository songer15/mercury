package com.valor.mercury.gateway;


import com.mfc.config.ConfigTools3;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class GatewayApplication {
    private final static Logger logger = LoggerFactory.getLogger(GatewayApplication.class);


    public static void main(String[] args) {
        ConfigTools3.load("cfg");
        SpringApplication.run(GatewayApplication.class, args);
        logger.info("*************************************** GatewayApplication Started******************************************************************");

    }
}
