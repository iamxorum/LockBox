package com.lockbox.aspect;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.Marker;
import org.slf4j.MarkerFactory;
import org.springframework.stereotype.Component;
import org.springframework.util.StopWatch;

@Aspect
@Component
@Slf4j
public class ExecutionTimeAspect {

    private static final Marker PERFORMANCE = MarkerFactory.getMarker("PERFORMANCE");
    private static final long SLOW_EXECUTION_THRESHOLD_MS = 1000;

    @Pointcut("within(@org.springframework.stereotype.Repository *)" +
            " || within(@org.springframework.stereotype.Service *)" +
            " || within(@org.springframework.web.bind.annotation.RestController *)")
    public void springBeanPointcut() {
    }

    @Pointcut("within(com.lockbox.repository..*)" +
            " || within(com.lockbox.service..*)" +
            " || within(com.lockbox.controller..*)")
    public void applicationPackagePointcut() {
    }

    @Around("@annotation(com.lockbox.aspect.LogExecutionTime)")
    public Object logAnnotatedExecutionTime(ProceedingJoinPoint joinPoint) throws Throwable {
        return logMethodExecutionTime(joinPoint, true);
    }

    @Around("applicationPackagePointcut() && springBeanPointcut() && !@annotation(com.lockbox.aspect.LogExecutionTime)")
    public Object logAllExecutionTime(ProceedingJoinPoint joinPoint) throws Throwable {
        return logMethodExecutionTime(joinPoint, false);
    }
    
    private Object logMethodExecutionTime(ProceedingJoinPoint joinPoint, boolean isAnnotated) throws Throwable {
        MethodSignature methodSignature = (MethodSignature) joinPoint.getSignature();
        String className = methodSignature.getDeclaringType().getSimpleName();
        String methodName = methodSignature.getName();
        
        StopWatch stopWatch = new StopWatch();
        stopWatch.start();
        
        Object result = joinPoint.proceed();
        
        stopWatch.stop();
        
        long executionTime = stopWatch.getTotalTimeMillis();
        String timeColor = getTimeColorEmoji(executionTime);
        
        // Always log explicitly annotated methods
        // For non-annotated methods, only log if they exceed the threshold
        if (isAnnotated || executionTime >= SLOW_EXECUTION_THRESHOLD_MS) {
            log.info(PERFORMANCE, "{} Method {}.{}() executed in {} ms", 
                    timeColor, 
                    className, 
                    methodName, 
                    executionTime);
        }
        
        return result;
    }
    
    private String getTimeColorEmoji(long executionTime) {
        if (executionTime < 100) {
            return "🟢"; 
        } else if (executionTime < SLOW_EXECUTION_THRESHOLD_MS) {
            return "🟡"; 
        } else {
            return "🔴"; 
        }
    }
} 