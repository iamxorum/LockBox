package com.lockbox.controller;

import com.lockbox.client.UserServiceClient;
import com.lockbox.dto.UserDto;
import com.lockbox.service.DistributedLoggingService;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.ratelimiter.annotation.RateLimiter;
import io.github.resilience4j.retry.annotation.Retry;
import io.micrometer.core.annotation.Timed;
import io.micrometer.core.instrument.Timer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/microservices")
@RequiredArgsConstructor
@Slf4j
public class MicroservicesController {

    private final UserServiceClient userServiceClient;
    private final DistributedLoggingService loggingService;
    private final DiscoveryClient discoveryClient;

    @GetMapping("/health")
    @Timed(value = "microservices.health", description = "Health check endpoint")
    public ResponseEntity<Map<String, String>> health() {
        Timer.Sample sample = loggingService.startTimer();
        try {
            loggingService.logRequest("health-check", "system", "health check request");
            
            Map<String, String> health = Map.of(
                "status", "UP",
                "service", "lockbox-service",
                "correlationId", loggingService.getCorrelationId()
            );
            
            loggingService.logResponse("health-check", "system", health);
            return ResponseEntity.ok(health);
        } finally {
            loggingService.stopTimer(sample);
        }
    }

    @GetMapping("/services")
    @Timed(value = "microservices.discovery", description = "Service discovery endpoint")
    public ResponseEntity<List<String>> getRegisteredServices() {
        Timer.Sample sample = loggingService.startTimer();
        try {
            loggingService.logRequest("service-discovery", "system", "get registered services");
            
            List<String> services = discoveryClient.getServices();
            
            loggingService.logResponse("service-discovery", "system", services);
            return ResponseEntity.ok(services);
        } finally {
            loggingService.stopTimer(sample);
        }
    }

    @GetMapping("/services/{serviceName}/instances")
    @Timed(value = "microservices.instances", description = "Service instances endpoint")
    public ResponseEntity<List<ServiceInstance>> getServiceInstances(@PathVariable String serviceName) {
        Timer.Sample sample = loggingService.startTimer();
        try {
            loggingService.logRequest("service-instances", "system", serviceName);
            
            List<ServiceInstance> instances = discoveryClient.getInstances(serviceName);
            
            loggingService.logResponse("service-instances", "system", instances);
            return ResponseEntity.ok(instances);
        } finally {
            loggingService.stopTimer(sample);
        }
    }

    @GetMapping("/users/{id}")
    @CircuitBreaker(name = "user-service", fallbackMethod = "getUserFallback")
    @Retry(name = "user-service")
    @RateLimiter(name = "user-service")
    @Timed(value = "microservices.user.get", description = "Get user via service call")
    public ResponseEntity<UserDto> getUser(@PathVariable Long id) {
        Timer.Sample sample = loggingService.startTimer();
        try {
            loggingService.logRequest("get-user", "system", id);
            
            UserDto user = userServiceClient.getUser(id);
            
            loggingService.logResponse("get-user", "system", user);
            return ResponseEntity.ok(user);
        } catch (Exception e) {
            loggingService.logError("get-user", "system", e);
            throw e;
        } finally {
            loggingService.stopTimer(sample);
        }
    }

    @GetMapping("/users")
    @CircuitBreaker(name = "user-service", fallbackMethod = "getAllUsersFallback")
    @Retry(name = "user-service")
    @RateLimiter(name = "user-service")
    @Timed(value = "microservices.users.getAll", description = "Get all users via service call")
    public ResponseEntity<List<UserDto>> getAllUsers() {
        Timer.Sample sample = loggingService.startTimer();
        try {
            loggingService.logRequest("get-all-users", "system", "get all users");
            
            List<UserDto> users = userServiceClient.getAllUsers();
            
            loggingService.logResponse("get-all-users", "system", users);
            return ResponseEntity.ok(users);
        } catch (Exception e) {
            loggingService.logError("get-all-users", "system", e);
            throw e;
        } finally {
            loggingService.stopTimer(sample);
        }
    }

    // Fallback methods
    public ResponseEntity<UserDto> getUserFallback(Long id, Exception ex) {
        log.warn("Circuit breaker activated for getUser with id: {}, error: {}", id, ex.getMessage());
        UserDto fallbackUser = new UserDto();
        fallbackUser.setId(id);
        fallbackUser.setUsername("fallback-user");
        fallbackUser.setEmail("fallback@example.com");
        return ResponseEntity.ok(fallbackUser);
    }

    public ResponseEntity<List<UserDto>> getAllUsersFallback(Exception ex) {
        log.warn("Circuit breaker activated for getAllUsers, error: {}", ex.getMessage());
        UserDto fallbackUser = new UserDto();
        fallbackUser.setId(0L);
        fallbackUser.setUsername("fallback-user");
        fallbackUser.setEmail("fallback@example.com");
        return ResponseEntity.ok(List.of(fallbackUser));
    }
} 