package com.lockbox.client;

import com.lockbox.dto.UserDto;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

@FeignClient(
    name = "user-service",
    fallback = UserServiceClientFallback.class
)
public interface UserServiceClient {

    @GetMapping("/api/users/{id}")
    @CircuitBreaker(name = "user-service")
    @Retry(name = "user-service")
    UserDto getUser(@PathVariable("id") Long id);

    @GetMapping("/api/users")
    @CircuitBreaker(name = "user-service")
    @Retry(name = "user-service")
    List<UserDto> getAllUsers();

    @PostMapping("/api/users")
    @CircuitBreaker(name = "user-service")
    @Retry(name = "user-service")
    UserDto createUser(@RequestBody UserDto userDto);
} 