package com.shadow.aicodingsystem.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;

import java.time.Duration;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

@Configuration
public class ScreenshotExecutorConfig {

    @Bean(name = "screenshotExecutor", destroyMethod = "shutdownGracefully")
    @DependsOn("cosClient")
    public ManagedVirtualThreadExecutor screenshotExecutor() {
        return new ManagedVirtualThreadExecutor();
    }

    public static class ManagedVirtualThreadExecutor implements Executor {
        private final ExecutorService delegate = Executors.newVirtualThreadPerTaskExecutor();

        @Override
        public void execute(Runnable command) {
            delegate.execute(command);
        }

        public void shutdownGracefully() {
            delegate.shutdown();
            try {
                // avoid blocking shutdown forever; screenshot/upload should be quick
                delegate.awaitTermination(Duration.ofSeconds(30).toSeconds(), TimeUnit.SECONDS);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            } finally {
                delegate.shutdownNow();
            }
        }
    }
}
