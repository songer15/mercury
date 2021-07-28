package com.vms.metric.analyse;

import com.mfc.config.ConfigTools3;
import com.vms.metric.analyse.tool.SpringUtil;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableScheduling
@SpringBootApplication
@ComponentScan("com.vms.metric.analyse.*")
public class AnalyseApplication {

	public static void main(String[] args) {
		ConfigTools3.load("cfg");
		SpringApplication.run(AnalyseApplication.class, args);
		SpringUtil.listAllBeans();
	}
}
