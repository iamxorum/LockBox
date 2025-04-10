package com.lockbox.aspect;

import com.lockbox.domain.model.User;
import com.lockbox.domain.repository.UserRepository;
import com.lockbox.domain.service.AuditLogService;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.Marker;
import org.slf4j.MarkerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallbackWithoutResult;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.lang.reflect.Method;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@Aspect
@Component
@Slf4j
public class AuditLogAspect {

    private static final Marker AUDIT = MarkerFactory.getMarker("AUDIT");
    
    // Cache to prevent duplicate logs for the same request
    private final ConcurrentHashMap<String, Long> recentRequests = new ConcurrentHashMap<>();
    private static final long REQUEST_CACHE_THRESHOLD_MS = 1000; // 1 second
    
    private final AuditLogService auditLogService;
    private final UserRepository userRepository;
    private final TransactionTemplate transactionTemplate;
    
    @Autowired
    public AuditLogAspect(AuditLogService auditLogService, 
                          UserRepository userRepository,
                          PlatformTransactionManager transactionManager) {
        this.auditLogService = auditLogService;
        this.userRepository = userRepository;
        this.transactionTemplate = new TransactionTemplate(transactionManager);
        this.transactionTemplate.setTimeout(5);
    }

    @Pointcut("@annotation(com.lockbox.aspect.Audited)")
    public void auditedMethodPointcut() {
    }
    
    @Pointcut("@within(com.lockbox.aspect.Audited)")
    public void auditedClassPointcut() {
    }
    
    @Pointcut("execution(* com.lockbox.service.AuditLogService.*(..))")
    public void auditServicePointcut() {
    }
    
    @Pointcut("execution(* org.springframework.security.authentication.*.authenticate(..))")
    public void authenticationPointcut() {
    }

    private void safelyStoreAuditLog(final Long userId, final String action, final String entityType, 
                                    final Long entityId, final String details) {
        if (userId == null) {
            return;
        }
        
        try {
            transactionTemplate.execute(new TransactionCallbackWithoutResult() {
                @Override
                protected void doInTransactionWithoutResult(TransactionStatus status) {
                    try {
                        auditLogService.createAuditLog(userId, action, entityType, entityId, details);
                    } catch (Exception e) {
                        log.error(AUDIT, "Failed to store audit log: {}", e.getMessage(), e);
                        status.setRollbackOnly();
                    }
                }
            });
        } catch (Exception e) {
            log.error(AUDIT, "Transaction error storing audit log: {}", e.getMessage(), e);
        }
    }

    @Around("(auditedMethodPointcut() || auditedClassPointcut()) && !auditServicePointcut()")
    public Object logAndStoreExplicitlyAuditedOperation(ProceedingJoinPoint joinPoint) throws Throwable {
        // Check if this is a duplicate/resource request we should ignore
        if (shouldSkipLogging(joinPoint)) {
            return joinPoint.proceed();
        }
        
        MethodSignature methodSignature = (MethodSignature) joinPoint.getSignature();
        Method method = methodSignature.getMethod();
        Class<?> declaringType = methodSignature.getDeclaringType();
        
        String methodName = methodSignature.getName();
        String className = declaringType.getSimpleName();
        
        Audited methodAudited = method.getAnnotation(Audited.class);
        Audited classAudited = declaringType.getAnnotation(Audited.class);
        
        Audited audited = methodAudited != null ? methodAudited : classAudited;
        
        String entityType = audited != null && !audited.entityType().isEmpty() ? 
                audited.entityType() : extractEntityTypeFromClassName(className);
                
        String action = audited != null && !audited.action().isEmpty() ? 
                audited.action() : determineAction(methodName);
                
        String description = audited != null && !audited.description().isEmpty() ?
                audited.description() : "Method: " + methodName + ", Class: " + className;
        
        boolean sensitive = audited != null && audited.sensitive();
        
        Optional<User> currentUser = getCurrentUser();
        Long userId = currentUser.map(User::getId).orElse(null);
        String username = currentUser.map(User::getUsername).orElse("anonymous");
        
        String details = sensitive ? 
                "SENSITIVE OPERATION - " + description : 
                prepareDetails(description, joinPoint.getArgs());
        
        if (log.isDebugEnabled()) {
            log.debug(AUDIT, "🔍 User '{}' initiated audited operation: {}.{} - [{}]", 
                    username, 
                    className, 
                    methodName,
                    action);
        }
        
        try {
            Object result = joinPoint.proceed();
            
            Long entityId = extractEntityId(result);
            
            if (log.isDebugEnabled()) {
                log.debug(AUDIT, "✅ User '{}' completed audited operation: {}.{} - [{}]", 
                        username, 
                        className, 
                        methodName,
                        action);
            }
            
            safelyStoreAuditLog(userId, action, entityType, entityId, details);
            
            return result;
        } catch (Throwable e) {
            log.error(AUDIT, "❌ User '{}' encountered error in audited operation: {}.{} - Error: {}", 
                    username, 
                    className, 
                    methodName,
                    e.getMessage());
            
            safelyStoreAuditLog(userId, "ERROR_" + action, entityType, null, 
                               "Error in " + details + ": " + e.getMessage());
            
            throw e;
        }
    }

    @AfterReturning(pointcut = "authenticationPointcut()", returning = "result")
    public void logAuthenticationAttempt(JoinPoint joinPoint, Object result) {
        try {
            Authentication authentication = (Authentication) result;
            String username = authentication.getName();
            boolean isAuthenticated = authentication.isAuthenticated();
            
            if (isAuthenticated) {
                log.info(AUDIT, "🔓 Authentication successful for user: {}", username);
                
                Optional<User> user = userRepository.findByUsername(username);
                if (user.isPresent()) {
                    final Long userId = user.get().getId();
                    safelyStoreAuditLog(userId, "LOGIN", "AUTHENTICATION", null, "Successful login");
                }
            } else {
                log.warn(AUDIT, "🔒 Authentication failed for user: {}", username);
                
                Optional<User> user = userRepository.findByUsername(username);
                if (user.isPresent()) {
                    final Long userId = user.get().getId();
                    safelyStoreAuditLog(userId, "LOGIN_FAILED", "AUTHENTICATION", null, "Failed login attempt");
                }
            }
        } catch (Exception e) {
            log.error(AUDIT, "Error logging authentication: {}", e.getMessage(), e);
        }
    }
    
    private boolean shouldSkipLogging(ProceedingJoinPoint joinPoint) {
        try {
            // Get HTTP request if available
            ServletRequestAttributes attributes = 
                    (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            
            if (attributes == null) {
                return false;
            }
            
            HttpServletRequest request = attributes.getRequest();
            
            // Skip resource requests
            String accept = request.getHeader("Accept");
            if (accept != null && !accept.contains("text/html") && 
                (accept.contains("image/") || accept.contains("font/") || 
                 accept.contains("application/javascript") || accept.contains("text/css"))) {
                return true;
            }
            
            // Skip AJAX requests
            String requestedWith = request.getHeader("X-Requested-With");
            if ("XMLHttpRequest".equals(requestedWith)) {
                return true;
            }
            
            // Check for duplicate requests within a time threshold
            String requestKey = request.getMethod() + "-" + request.getRequestURI() + 
                               "-" + joinPoint.getSignature().toLongString();
            
            long now = System.currentTimeMillis();
            Long lastTime = recentRequests.put(requestKey, now);
            
            if (lastTime != null && (now - lastTime) < REQUEST_CACHE_THRESHOLD_MS) {
                // Duplicate request within threshold, skip logging
                return true;
            }
            
            // Clean up old entries periodically (simple cleanup)
            if (recentRequests.size() > 1000) {
                recentRequests.entrySet().removeIf(
                    entry -> (now - entry.getValue()) > REQUEST_CACHE_THRESHOLD_MS
                );
            }
        } catch (Exception e) {
            // If anything goes wrong in the check, default to not skipping
            log.debug("Error checking if logging should be skipped: {}", e.getMessage());
        }
        
        return false;
    }
    
    private Optional<User> getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated() || 
            "anonymousUser".equals(authentication.getName())) {
            return Optional.empty();
        }
        
        return userRepository.findByUsername(authentication.getName());
    }
    
    private String determineAction(String methodName) {
        if (methodName.startsWith("create") || methodName.startsWith("save") || methodName.startsWith("add")) {
            return "CREATE";
        } else if (methodName.startsWith("update") || methodName.startsWith("edit") || methodName.startsWith("modify")) {
            return "UPDATE";
        } else if (methodName.startsWith("delete") || methodName.startsWith("remove")) {
            return "DELETE";
        } else if (methodName.startsWith("find") || methodName.startsWith("get") || methodName.startsWith("retrieve")) {
            return "READ";
        } else if (methodName.startsWith("authenticate") || methodName.startsWith("login")) {
            return "LOGIN";
        } else if (methodName.startsWith("logout")) {
            return "LOGOUT";
        } else if (methodName.startsWith("verify") || methodName.startsWith("validate") || methodName.startsWith("check")) {
            return "VERIFY";
        } else {
            return methodName.toUpperCase();
        }
    }
    
    private String extractEntityTypeFromClassName(String className) {
        if (className.endsWith("ServiceImpl")) {
            return className.substring(0, className.length() - 10).toUpperCase();
        } else if (className.endsWith("Service")) {
            return className.substring(0, className.length() - 7).toUpperCase();
        } else if (className.endsWith("Controller")) {
            return className.substring(0, className.length() - 10).toUpperCase();
        } else if (className.endsWith("Impl")) {
            return className.substring(0, className.length() - 4).toUpperCase();
        } else {
            return className.toUpperCase();
        }
    }
    
    private Long extractEntityId(Object result) {
        if (result == null) {
            return null;
        }
        
        try {
            java.lang.reflect.Method getId = result.getClass().getMethod("getId");
            Object id = getId.invoke(result);
            if (id instanceof Long) {
                return (Long) id;
            }
        } catch (Exception e) {
        }
        
        return null;
    }

    private String prepareDetails(String description, Object[] args) {
        if (args == null || args.length == 0) {
            return description;
        }
        
        StringBuilder details = new StringBuilder(description);
        details.append(", Arguments: [");
        
        for (int i = 0; i < args.length; i++) {
            Object arg = args[i];
            if (arg == null) {
                details.append("null");
            } else if (arg instanceof String || arg instanceof Number || arg instanceof Boolean) {
                details.append(arg);
            } else {
                details.append(arg.getClass().getSimpleName())
                      .append("@")
                      .append(Integer.toHexString(System.identityHashCode(arg)));
            }
            
            if (i < args.length - 1) {
                details.append(", ");
            }
        }
        
        details.append("]");
        return details.toString();
    }
} 