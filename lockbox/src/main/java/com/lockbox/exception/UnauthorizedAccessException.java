package com.lockbox.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.FORBIDDEN)
public class UnauthorizedAccessException extends RuntimeException {
    
    private final String resourceName;
    private final String userId;
    
    public UnauthorizedAccessException(String resourceName, String userId) {
        super(String.format("User %s is not authorized to access %s", userId, resourceName));
        this.resourceName = resourceName;
        this.userId = userId;
    }
    
    public UnauthorizedAccessException(String message) {
        super(message);
        this.resourceName = null;
        this.userId = null;
    }
    
    public String getResourceName() {
        return resourceName;
    }
    
    public String getUserId() {
        return userId;
    }
} 