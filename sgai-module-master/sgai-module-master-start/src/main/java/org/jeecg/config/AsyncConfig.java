package org.jeecg.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.ThreadPoolExecutor;

/**
 * 集成推送专用线程池（实时增量 @Async 监听器用）。
 * 启动类已有 @EnableAsync，此处仅提供 bean。
 */
@Configuration
public class AsyncConfig {

    @Bean("integrationTaskExecutor")
    public ThreadPoolTaskExecutor integrationTaskExecutor() {
        ThreadPoolTaskExecutor exec = new ThreadPoolTaskExecutor();
        exec.setCorePoolSize(2);
        exec.setMaxPoolSize(8);
        exec.setQueueCapacity(500);
        exec.setKeepAliveSeconds(60);
        exec.setThreadNamePrefix("integration-push-");
        exec.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
        exec.initialize();
        return exec;
    }
}
