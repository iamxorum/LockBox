package com.lockbox.exception;

import com.lockbox.dto.ApiResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.server.ResponseStatusException;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;

@ActiveProfiles("test")
class GlobalExceptionHandlerTest {

    private GlobalExceptionHandler exceptionHandler;
    private WebRequest webRequest;

    @BeforeEach
    void setUp() {
        exceptionHandler = new GlobalExceptionHandler();
        webRequest = mock(WebRequest.class);
    }

    @Test
    void handleResourceNotFoundException() {
        ResourceNotFoundException ex = new ResourceNotFoundException("User", "id", 1L);
        
        ResponseEntity<ApiResponse<Object>> response = 
            exceptionHandler.handleResourceNotFoundException(ex, webRequest);
        
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertNotNull(response.getBody());
        assertFalse(response.getBody().isSuccess());
        assertEquals("User not found with id: 1", response.getBody().getMessage());
    }

    @Test
    void handleResourceAlreadyExistsException() {
        ResourceAlreadyExistsException ex = new ResourceAlreadyExistsException("User", "username", "testuser");
        
        ResponseEntity<ApiResponse<Object>> response = 
            exceptionHandler.handleResourceAlreadyExistsException(ex, webRequest);
        
        assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
        assertNotNull(response.getBody());
        assertFalse(response.getBody().isSuccess());
        assertEquals("User already exists with username: testuser", response.getBody().getMessage());
    }

    @Test
    void handleBadRequestException() {
        BadRequestException ex = new BadRequestException("Invalid request");
        
        ResponseEntity<ApiResponse<Object>> response = 
            exceptionHandler.handleBadRequestException(ex, webRequest);
        
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertFalse(response.getBody().isSuccess());
        assertEquals("Invalid request", response.getBody().getMessage());
    }

    @Test
    void handleUnauthorizedAccessException() {
        UnauthorizedAccessException ex = new UnauthorizedAccessException("Access denied");
        
        ResponseEntity<ApiResponse<Object>> response = 
            exceptionHandler.handleUnauthorizedAccessException(ex, webRequest);
        
        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
        assertNotNull(response.getBody());
        assertFalse(response.getBody().isSuccess());
        assertEquals("Access denied", response.getBody().getMessage());
    }

    @Test
    void handleValidationException() {
        Map<String, String> errors = new HashMap<>();
        errors.put("field", "error message");
        ValidationException ex = new ValidationException("Validation failed", errors);
        
        ResponseEntity<ApiResponse<Map<String, String>>> response = 
            exceptionHandler.handleValidationException(ex);
        
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertFalse(response.getBody().isSuccess());
        assertEquals("Validation failed", response.getBody().getMessage());
        assertEquals(errors, response.getBody().getData());
    }

    @Test
    void handleResponseStatusException() {
        ResponseStatusException ex = new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid input");
        
        ResponseEntity<ApiResponse<Object>> response = 
            exceptionHandler.handleResponseStatusException(ex, webRequest);
        
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertFalse(response.getBody().isSuccess());
        assertEquals("Invalid input", response.getBody().getMessage());
    }

    @Test
    void handleGlobalException() {
        Exception ex = new Exception("Unexpected error");
        
        ResponseEntity<ApiResponse<Object>> response = 
            exceptionHandler.handleGlobalException(ex, webRequest);
        
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertNotNull(response.getBody());
        assertFalse(response.getBody().isSuccess());
        assertTrue(response.getBody().getMessage().contains("Unexpected error"));
    }
} 