package com.stoury;

import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;

@EnableBatchProcessing
@SpringBootApplication
public class StouryBatchApplication {
    public static void main(String[] args) {
        ConfigurableApplicationContext context = SpringApplication.run(StouryBatchApplication.class, args);
        int exitCode = SpringApplication.exit(context);
        System.exit(exitCode);
    }
}