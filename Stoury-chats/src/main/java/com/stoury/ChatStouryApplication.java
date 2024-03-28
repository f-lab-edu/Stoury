package com.stoury;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@EnableAsync
@SpringBootApplication
public class ChatStouryApplication {
    public static void main(String[] args) {
        SpringApplication.run(ChatStouryApplication.class, args);
    }
}