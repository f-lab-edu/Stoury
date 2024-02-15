package com.stoury.config;

import com.google.maps.GeoApiContext;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnThreading;
import org.springframework.boot.autoconfigure.thread.Threading;
import org.springframework.boot.task.SimpleAsyncTaskExecutorBuilder;
import org.springframework.boot.task.ThreadPoolTaskExecutorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.core.task.SimpleAsyncTaskExecutor;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.concurrent.TimeUnit;

@Configuration
public class ContextConfiguration {
    @Value("${google.geocoding.api-key}")
    private String apiKey;

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public GeoApiContext geoApiContext() {
        return new GeoApiContext.Builder()
                .apiKey(apiKey)
                .connectTimeout(2, TimeUnit.SECONDS)
                .readTimeout(3, TimeUnit.SECONDS)
                .maxRetries(3)
                .build();
    }

    @Bean(
            name = {"applicationTaskExecutor", "taskExecutor"}
    )
    @ConditionalOnThreading(Threading.VIRTUAL)
    SimpleAsyncTaskExecutor applicationTaskExecutorVirtualThreads(SimpleAsyncTaskExecutorBuilder builder) {
        return builder.build();
    }

    @Lazy
    @Bean(
            name = {"applicationTaskExecutor", "taskExecutor"}
    )
    @ConditionalOnThreading(Threading.PLATFORM)
    ThreadPoolTaskExecutor applicationTaskExecutor(ThreadPoolTaskExecutorBuilder taskExecutorBuilder, ObjectProvider<ThreadPoolTaskExecutorBuilder> threadPoolTaskExecutorBuilderProvider) {
        ThreadPoolTaskExecutorBuilder threadPoolTaskExecutorBuilder = threadPoolTaskExecutorBuilderProvider.getIfUnique();
        return threadPoolTaskExecutorBuilder != null ? threadPoolTaskExecutorBuilder.build() : taskExecutorBuilder.build();
    }
}
