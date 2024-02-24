package com.stoury;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;

@SpringBootApplication
public class StouryBatchApplication {
    public static void main(String[] args) {
        SpringApplication springApplication = new SpringApplication(StouryBatchApplication.class);
        springApplication.setWebApplicationType(WebApplicationType.NONE);
        ConfigurableApplicationContext context = springApplication.run(args);
        int exitCode = SpringApplication.exit(context);
        System.exit(exitCode);
    }
}