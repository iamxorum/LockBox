package com.lockbox.aspect;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.After;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.Marker;
import org.slf4j.MarkerFactory;
import org.springframework.stereotype.Component;

@Aspect
@Component
@Slf4j
public class ServiceLoggingAspect {

    private static final Marker SERVICE = MarkerFactory.getMarker("SERVICE");
    
    @Pointcut("within(@org.springframework.stereotype.Service *)")
    public void servicePointcut() {
    }
    
    @Before("servicePointcut() && !execution(* com.lockbox.service.AuditLogService.*(..))")
    public void logBeforeService(JoinPoint joinPoint) {
        MethodSignature methodSignature = (MethodSignature) joinPoint.getSignature();
        String methodName = methodSignature.getName();
        String className = methodSignature.getDeclaringType().getSimpleName();
        String params = formatParams(joinPoint.getArgs());
        
        log.debug(SERVICE, "🔹 Starting service method: {}.{}({})", 
                className, methodName, params);
    }
    
    @After("servicePointcut() && !execution(* com.lockbox.service.AuditLogService.*(..))")
    public void logAfterService(JoinPoint joinPoint) {
        MethodSignature methodSignature = (MethodSignature) joinPoint.getSignature();
        String methodName = methodSignature.getName();
        String className = methodSignature.getDeclaringType().getSimpleName();
        
        log.debug(SERVICE, "🔸 Completed service method: {}.{}", 
                className, methodName);
    }
    
    private String formatParams(Object[] args) {
        if (args == null || args.length == 0) {
            return "";
        }
        
        StringBuilder params = new StringBuilder();
        
        for (int i = 0; i < args.length; i++) {
            Object arg = args[i];
            if (arg == null) {
                params.append("null");
            } else if (arg instanceof String) {
                // Truncate long strings
                String str = (String) arg;
                params.append(str.length() > 50 ? str.substring(0, 47) + "..." : str);
            } else if (arg instanceof Number || arg instanceof Boolean) {
                params.append(arg);
            } else {
                params.append(arg.getClass().getSimpleName());
            }
            
            if (i < args.length - 1) {
                params.append(", ");
            }
        }
        
        return params.toString();
    }
} 