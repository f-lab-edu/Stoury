package com.stoury;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableAsync
public class StouryApplication {

    public static void main(String[] args) {
        SpringApplication.run(StouryApplication.class, args);
    }

}
