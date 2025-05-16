package com.lockbox.security;

import com.lockbox.domain.model.User;
import com.lockbox.domain.repository.UserRepository;
import com.lockbox.domain.service.AuditLogService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.security.authentication.event.AbstractAuthenticationEvent;
import org.springframework.security.authentication.event.AuthenticationFailureBadCredentialsEvent;
import org.springframework.security.authentication.event.AuthenticationSuccessEvent;
import org.springframework.security.authentication.event.LogoutSuccessEvent;
import org.springframework.security.web.authentication.WebAuthenticationDetails;
import org.springframework.stereotype.Component;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallbackWithoutResult;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.Optional;

@Component
public class SecurityEventListener {

    private static final Logger log = LoggerFactory.getLogger(SecurityEventListener.class);
    
    private final AuditLogService auditLogService;
    private final UserRepository userRepository;
    private final TransactionTemplate transactionTemplate;
    
    @Autowired
    public SecurityEventListener(AuditLogService auditLogService, 
                                UserRepository userRepository,
                                PlatformTransactionManager transactionManager) {
        this.auditLogService = auditLogService;
        this.userRepository = userRepository;
        this.transactionTemplate = new TransactionTemplate(transactionManager);
        this.transactionTemplate.setTimeout(5); // 5 second timeout
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
                        log.error("Failed to store audit log: {}", e.getMessage(), e);
                        status.setRollbackOnly();
                    }
                }
            });
        } catch (Exception e) {
            log.error("Transaction error storing audit log: {}", e.getMessage(), e);
        }
    }
    
    @EventListener
    public void onSuccess(AuthenticationSuccessEvent success) {
        String remoteAddress = extractRemoteAddress(success);
        String username = success.getAuthentication().getName();
        
        log.info("🔓 Login success - User: {}, IP: {}, Time: {}", 
                username, 
                remoteAddress,
                System.currentTimeMillis());
        
        try {
            Optional<User> user = userRepository.findByUsername(username);
            if (user.isPresent()) {
                safelyStoreAuditLog(
                        user.get().getId(),
                        "LOGIN_SUCCESS",
                        "SECURITY",
                        null,
                        "Login success from IP: " + remoteAddress
                );
            }
        } catch (Exception e) {
            log.error("Failed to store login success audit log: {}", e.getMessage(), e);
        }
    }
    
    @EventListener
    public void onFailure(AuthenticationFailureBadCredentialsEvent failures) {
        String remoteAddress = extractRemoteAddress(failures);
        String username = failures.getAuthentication().getName();
        
        log.warn("🔒 Login failure - User: {}, IP: {}, Time: {}, Reason: Bad credentials", 
                username, 
                remoteAddress,
                System.currentTimeMillis());
        
        try {
            Optional<User> user = userRepository.findByUsername(username);
            if (user.isPresent()) {
                safelyStoreAuditLog(
                        user.get().getId(),
                        "LOGIN_FAILURE",
                        "SECURITY",
                        null,
                        "Login failure from IP: " + remoteAddress + ", Reason: Bad credentials"
                );
            } else {
                User adminUser = userRepository.findByUsername("admin")
                    .orElse(userRepository.findAll().stream().findFirst().orElse(null));
                
                if (adminUser != null) {
                    safelyStoreAuditLog(
                            adminUser.getId(),
                            "UNKNOWN_LOGIN_ATTEMPT",
                            "SECURITY",
                            null,
                            "Unknown username login attempt: " + username + ", IP: " + remoteAddress
                    );
                }
            }
        } catch (Exception e) {
            log.error("Failed to store login failure audit log: {}", e.getMessage(), e);
        }
    }
    
    @EventListener
    public void onLogout(LogoutSuccessEvent event) {
        String remoteAddress = extractRemoteAddress(event);
        String username = event.getAuthentication().getName();
        
        log.info("👋 Logout - User: {}, IP: {}, Time: {}", 
                username, 
                remoteAddress, 
                System.currentTimeMillis());
        
        try {
            Optional<User> user = userRepository.findByUsername(username);
            if (user.isPresent()) {
                safelyStoreAuditLog(
                        user.get().getId(),
                        "LOGOUT",
                        "SECURITY",
                        null,
                        "Logout from IP: " + remoteAddress
                );
            }
        } catch (Exception e) {
            log.error("Failed to store logout audit log: {}", e.getMessage(), e);
        }
    }
    
    private String extractRemoteAddress(AbstractAuthenticationEvent event) {
        if (event.getAuthentication().getDetails() instanceof WebAuthenticationDetails) {
            WebAuthenticationDetails details = (WebAuthenticationDetails) event.getAuthentication().getDetails();
            return details.getRemoteAddress();
        }
        return "unknown";
    }
} 