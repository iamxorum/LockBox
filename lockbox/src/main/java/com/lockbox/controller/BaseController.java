package com.lockbox.controller;

import com.lockbox.dto.ApiResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

public abstract class BaseController {
    
    protected <T> ResponseEntity<ApiResponse<T>> successResponse(T data, String message) {
        return ResponseEntity.ok(ApiResponse.success(message, data));
    }
    
    protected <T> ResponseEntity<ApiResponse<T>> successResponse(String message) {
        return ResponseEntity.ok(ApiResponse.success(message));
    }
    
    protected <T> ResponseEntity<ApiResponse<T>> createdResponse(T data, String message) {
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(message, data));
    }
    
    protected <T> ResponseEntity<ApiResponse<T>> notFoundResponse(String message) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ApiResponse.error(message));
    }
    
    protected <T> ResponseEntity<ApiResponse<T>> badRequestResponse(String message) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ApiResponse.error(message));
    }
    
    protected <T> ResponseEntity<ApiResponse<T>> errorResponse(HttpStatus status, String message) {
        return ResponseEntity.status(status).body(ApiResponse.error(message));
    }
} 