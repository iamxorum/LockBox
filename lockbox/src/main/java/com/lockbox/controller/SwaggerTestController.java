package com.lockbox.controller;

import com.lockbox.dto.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/swagger-test")
@Slf4j
@Tag(name = "Swagger Test", description = "Simple endpoints to test Swagger/OpenAPI functionality")
public class SwaggerTestController {

    @GetMapping("/health")
    @Operation(summary = "Test endpoint", description = "Simple health check endpoint to verify Swagger is working")
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Success")
    })
    public ResponseEntity<ApiResponse<Map<String, Object>>> testEndpoint() {
        Map<String, Object> data = new HashMap<>();
        data.put("status", "OK");
        data.put("timestamp", LocalDateTime.now());
        data.put("message", "Swagger is working correctly!");
        
        return ResponseEntity.ok(ApiResponse.success("Test endpoint successful", data));
    }

    @GetMapping("/info")
    @Operation(summary = "Get API info", description = "Returns information about the API and Swagger configuration")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getApiInfo() {
        Map<String, Object> info = new HashMap<>();
        info.put("apiTitle", "LockBox API");
        info.put("apiVersion", "v1");
        info.put("swaggerUrl", "/swagger-ui.html");
        info.put("openApiDocs", "/v3/api-docs");
        info.put("description", "Password manager API with Swagger documentation");
        
        return ResponseEntity.ok(ApiResponse.success("API information", info));
    }

    @PostMapping("/echo")
    @Operation(summary = "Echo test", description = "Echo back the provided message (tests POST endpoints)")
    public ResponseEntity<ApiResponse<Map<String, String>>> echo(@RequestBody Map<String, String> payload) {
        log.info("Echo endpoint called with payload: {}", payload);
        
        Map<String, String> response = new HashMap<>();
        response.put("original", payload.getOrDefault("message", "No message provided"));
        response.put("echo", "Echo: " + payload.getOrDefault("message", "No message provided"));
        
        return ResponseEntity.ok(ApiResponse.success("Message echoed successfully", response));
    }
} 