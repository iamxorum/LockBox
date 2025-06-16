package com.lockbox.service;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@Slf4j
public class DistributedLoggingService {

    private final Counter requestCounter;
    private final Timer requestTimer;

    public DistributedLoggingService(MeterRegistry meterRegistry) {
        this.requestCounter = Counter.builder("lockbox.requests.total")
                .description("Total number of requests")
                .register(meterRegistry);
        this.requestTimer = Timer.builder("lockbox.requests.duration")
                .description("Request duration")
                .register(meterRegistry);
    }

    public void logRequest(String operation, String userId, Object request) {
        String correlationId = generateCorrelationId();
        MDC.put("correlationId", correlationId);
        MDC.put("operation", operation);
        MDC.put("userId", userId);
        MDC.put("service", "lockbox-service");

        log.info("Request started - Operation: {}, User: {}, Request: {}", 
                operation, userId, request);
        
        requestCounter.increment();
    }

    public void logResponse(String operation, String userId, Object response) {
        log.info("Request completed - Operation: {}, User: {}, Response: {}", 
                operation, userId, response);
        
        // Clear MDC after request
        MDC.clear();
    }

    public void logError(String operation, String userId, Exception error) {
        log.error("Request failed - Operation: {}, User: {}, Error: {}", 
                operation, userId, error.getMessage(), error);
        
        // Clear MDC after request
        MDC.clear();
    }

    public Timer.Sample startTimer() {
        return Timer.start();
    }

    public void stopTimer(Timer.Sample sample) {
        sample.stop(requestTimer);
    }

    private String generateCorrelationId() {
        return UUID.randomUUID().toString();
    }

    public String getCorrelationId() {
        return MDC.get("correlationId");
    }
} 