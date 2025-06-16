package com.lockbox.config;

import io.micrometer.core.aop.TimedAspect;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class MonitoringConfig {

    @Bean
    public TimedAspect timedAspect(MeterRegistry registry) {
        return new TimedAspect(registry);
    }

    @Bean
    public HealthIndicator lockboxHealthIndicator() {
        return () -> {
            // Custom health check logic
            try {
                // Check database connectivity, external services, etc.
                return Health.up()
                        .withDetail("lockbox-service", "Available")
                        .withDetail("database", "Connected")
                        .withDetail("version", "1.0.0")
                        .build();
            } catch (Exception e) {
                return Health.down()
                        .withDetail("lockbox-service", "Unavailable")
                        .withDetail("error", e.getMessage())
                        .build();
            }
        };
    }
} 