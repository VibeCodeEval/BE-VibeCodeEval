package com.yd.vibecode.global.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;

/**
 * Spring @Async 설정
 * - SSE 이벤트 리스너 비동기 처리에 사용
 */
@Configuration
@EnableAsync
public class AsyncConfig {

    @Bean(name = "sseAsyncExecutor")
    public Executor sseAsyncExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(2);
        executor.setMaxPoolSize(4);
        executor.setQueueCapacity(50);
        executor.setThreadNamePrefix("sse-async-");
        executor.initialize();
        return executor;
    }
}
