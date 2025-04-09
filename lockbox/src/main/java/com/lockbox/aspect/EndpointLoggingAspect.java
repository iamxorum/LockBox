package com.lockbox.aspect;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.slf4j.Marker;
import org.slf4j.MarkerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.Enumeration;

@Aspect
@Component
@Slf4j
public class EndpointLoggingAspect {

    private static final Marker API = MarkerFactory.getMarker("API");
    private static final Marker API_SUCCESS = MarkerFactory.getMarker("API_SUCCESS");
    private static final Marker API_FAILED = MarkerFactory.getMarker("API_FAILED");

    @Pointcut("within(@org.springframework.web.bind.annotation.RestController *)")
    public void controllerPointcut() {
    }
    
    @Pointcut("execution(* com.lockbox.controller.AuthController.*(..))")
    public void authControllerPointcut() {
    }
    
    @Before("controllerPointcut()")
    public void logBeforeEndpoint(JoinPoint joinPoint) {
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attributes != null) {
            HttpServletRequest request = attributes.getRequest();
            String headers = getHeadersAsString(request);
            String args = formatArgs(joinPoint.getArgs());
            
            log.info(API, "📥 HTTP Request: {} {} from {}\n   Headers: {}\n   Args: {}",
                    request.getMethod(),
                    request.getRequestURI(),
                    request.getRemoteAddr(),
                    headers,
                    args);
        }
    }
    
    @AfterReturning(pointcut = "controllerPointcut()", returning = "result")
    public void logAfterEndpoint(JoinPoint joinPoint, Object result) {
        String methodName = joinPoint.getSignature().getName();
        String className = joinPoint.getSignature().getDeclaringType().getSimpleName();
        
        boolean isSuccessful = isSuccessfulResponse(result);
        Marker marker = isSuccessful ? API_SUCCESS : API_FAILED;
        
        String resultStr = formatResult(result);
        
        if (isSuccessful) {
            log.info(marker, "📤 HTTP Success: {}.{} → {}", 
                    className, 
                    methodName,
                    resultStr);
        } else {
            log.warn(marker, "📤 HTTP Failed: {}.{} → {}", 
                    className, 
                    methodName,
                    resultStr);
        }
    }
    
    private boolean isSuccessfulResponse(Object result) {
        if (result instanceof ResponseEntity) {
            ResponseEntity<?> response = (ResponseEntity<?>) result;
            return response.getStatusCode().is2xxSuccessful();
        }
        
        if (result != null) {
            String resultString = result.toString().toLowerCase();
            return !resultString.contains("error") && !resultString.contains("failed") && !resultString.contains("exception");
        }
        
        return true;
    }
    
    private String getHeadersAsString(HttpServletRequest request) {
        StringBuilder headerString = new StringBuilder();
        Enumeration<String> headerNames = request.getHeaderNames();
        
        while (headerNames.hasMoreElements()) {
            String headerName = headerNames.nextElement();
            // Skip sensitive headers
            if (headerName.equalsIgnoreCase("authorization") || 
                headerName.equalsIgnoreCase("cookie")) {
                headerString.append(headerName).append(":[PROTECTED]; ");
            } else {
                headerString.append(headerName).append(':')
                           .append(request.getHeader(headerName)).append("; ");
            }
        }
        
        return headerString.toString();
    }
    
    private String formatArgs(Object[] args) {
        if (args == null || args.length == 0) {
            return "[]";
        }
        
        StringBuilder formattedArgs = new StringBuilder("[");
        
        for (int i = 0; i < args.length; i++) {
            Object arg = args[i];
            if (arg == null) {
                formattedArgs.append("null");
            } else if (arg instanceof String) {
                // Truncate long strings
                String str = (String) arg;
                formattedArgs.append(str.length() > 100 ? str.substring(0, 97) + "..." : str);
            } else {
                formattedArgs.append(arg.getClass().getSimpleName());
            }
            
            if (i < args.length - 1) {
                formattedArgs.append(", ");
            }
        }
        
        formattedArgs.append("]");
        return formattedArgs.toString();
    }
    
    private String formatResult(Object result) {
        if (result == null) {
            return "null";
        }
        
        if (result instanceof ResponseEntity) {
            ResponseEntity<?> response = (ResponseEntity<?>) result;
            return String.format("Status: %s, Body: %s", 
                    response.getStatusCode(), 
                    response.getBody() != null ? 
                        response.getBody().getClass().getSimpleName() : "null");
        }
        
        return result.getClass().getSimpleName();
    }
} 