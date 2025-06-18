package com.lockbox.controller;

import com.lockbox.client.UserServiceClient;
import com.lockbox.dto.ApiResponse;
import com.lockbox.dto.UserDto;
import com.lockbox.service.DistributedLoggingService;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.ratelimiter.annotation.RateLimiter;
import io.github.resilience4j.retry.annotation.Retry;
import io.micrometer.core.annotation.Timed;
import io.micrometer.core.instrument.Timer;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/microservices")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Microservices", description = "Microservices integration endpoints")
@ConditionalOnProperty(name = "eureka.client.enabled", havingValue = "true")
public class MicroservicesController {

    private final UserServiceClient userServiceClient;
    private final DistributedLoggingService loggingService;
    private final DiscoveryClient discoveryClient;

    @GetMapping("/health")
    @Operation(summary = "Check microservices health", description = "Returns the health status of microservices integration")
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Microservices are healthy"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "503", description = "Some microservices are unavailable")
    })
    public ResponseEntity<ApiResponse<Map<String, Object>>> checkHealth() {
        Map<String, Object> healthData = new HashMap<>();
        healthData.put("discoveryClient", "enabled");
        healthData.put("feignClients", "configured");
        healthData.put("circuitBreaker", "active");
        
        return ResponseEntity.ok(ApiResponse.success(
            "Microservices integration is healthy", 
            healthData
        ));
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
    @Operation(summary = "Get user via microservice", description = "Retrieves user data through microservices with circuit breaker protection")
    @CircuitBreaker(name = "user-service", fallbackMethod = "getUserFallback")
    @Retry(name = "user-service")
    public ResponseEntity<ApiResponse<UserDto>> getUserViaMicroservice(@PathVariable Long id) {
        log.info("Attempting to get user {} via microservice", id);
        
        try {
            UserDto user = userServiceClient.getUser(id);
            return ResponseEntity.ok(ApiResponse.success(
                "User retrieved successfully via microservice", 
                user
            ));
        } catch (Exception e) {
            log.error("Error getting user via microservice: {}", e.getMessage());
            throw e;
        }
    }

    @GetMapping("/users")
    @Operation(summary = "Get all users via microservice", description = "Retrieves all users through microservices with circuit breaker protection")
    @CircuitBreaker(name = "user-service", fallbackMethod = "getAllUsersFallback")
    @Retry(name = "user-service")
    public ResponseEntity<ApiResponse<List<UserDto>>> getAllUsersViaMicroservice() {
        log.info("Attempting to get all users via microservice");
        
        try {
            List<UserDto> users = userServiceClient.getAllUsers();
            return ResponseEntity.ok(ApiResponse.success(
                "Users retrieved successfully via microservice", 
                users
            ));
        } catch (Exception e) {
            log.error("Error getting users via microservice: {}", e.getMessage());
            throw e;
        }
    }

    @PostMapping("/users")
    @Operation(summary = "Create user via microservice", description = "Creates a new user through microservices with circuit breaker protection")
    @CircuitBreaker(name = "user-service", fallbackMethod = "createUserFallback")
    @Retry(name = "user-service")
    public ResponseEntity<ApiResponse<UserDto>> createUserViaMicroservice(@RequestBody UserDto userDto) {
        log.info("Attempting to create user via microservice: {}", userDto.getUsername());
        
        try {
            UserDto createdUser = userServiceClient.createUser(userDto);
            return ResponseEntity.ok(ApiResponse.success(
                "User created successfully via microservice", 
                createdUser
            ));
        } catch (Exception e) {
            log.error("Error creating user via microservice: {}", e.getMessage());
            throw e;
        }
    }

    @GetMapping("/circuit-breaker/status")
    @Operation(summary = "Get circuit breaker status", description = "Returns the current status of circuit breakers")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getCircuitBreakerStatus() {
        Map<String, Object> status = new HashMap<>();
        status.put("userService", "Circuit breaker monitoring available via actuator endpoints");
        status.put("endpoint", "/actuator/circuitbreakers");
        status.put("metrics", "/actuator/metrics/resilience4j.circuitbreaker.calls");
        
        return ResponseEntity.ok(ApiResponse.success(
            "Circuit breaker status information", 
            status
        ));
    }

    // Fallback methods for circuit breaker
    public ResponseEntity<ApiResponse<UserDto>> getUserFallback(Long id, Exception ex) {
        log.warn("Circuit breaker activated for getUser({}): {}", id, ex.getMessage());
        
        return ResponseEntity.ok(ApiResponse.error(
            "User service is currently unavailable. Please try again later."
        ));
    }

    public ResponseEntity<ApiResponse<List<UserDto>>> getAllUsersFallback(Exception ex) {
        log.warn("Circuit breaker activated for getAllUsers: {}", ex.getMessage());
        
        return ResponseEntity.ok(ApiResponse.error(
            "User service is currently unavailable. Please try again later."
        ));
    }

    public ResponseEntity<ApiResponse<UserDto>> createUserFallback(UserDto userDto, Exception ex) {
        log.warn("Circuit breaker activated for createUser({}): {}", userDto.getUsername(), ex.getMessage());
        
        return ResponseEntity.ok(ApiResponse.error(
            "User service is currently unavailable. User creation failed."
        ));
    }
} 