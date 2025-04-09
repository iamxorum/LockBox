package com.lockbox.controller;

import com.lockbox.dto.ApiResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.Marker;
import org.slf4j.MarkerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/test")
public class ApiTestController extends BaseController {

    private static final Logger log = LoggerFactory.getLogger(ApiTestController.class);
    private static final Marker AUDIT = MarkerFactory.getMarker("AUDIT");
    private static final Marker API_SUCCESS = MarkerFactory.getMarker("API_SUCCESS");

    @GetMapping("/csrf")
    public ResponseEntity<ApiResponse<Map<String, String>>> getCsrfToken(HttpServletRequest request) {
        CsrfToken token = (CsrfToken) request.getAttribute(CsrfToken.class.getName());
        Map<String, String> tokenInfo = new HashMap<>();
        
        if (token != null) {
            tokenInfo.put("headerName", token.getHeaderName());
            tokenInfo.put("parameterName", token.getParameterName());
            tokenInfo.put("token", token.getToken());
            
            log.info(AUDIT, "CSRF token requested by user");
            log.info(API_SUCCESS, "CSRF token provided to client");
            
            return successResponse(tokenInfo, "CSRF token retrieved successfully");
        } else {
            log.warn(AUDIT, "CSRF token requested but not available");
            return errorResponse(org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR, "CSRF token not available");
        }
    }
    
    @PostMapping("/csrf")
    public ResponseEntity<ApiResponse<String>> testCsrfToken() {
        log.info(AUDIT, "POST request with CSRF token successful");
        log.info(API_SUCCESS, "CSRF test POST successful");
        return successResponse("CSRF token validation successful", "POST request processed successfully");
    }
} 