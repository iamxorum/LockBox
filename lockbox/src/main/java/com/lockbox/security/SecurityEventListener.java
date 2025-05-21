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

/**
 * This class handles security-related events and logs them for audit purposes.
 */
@Component
public class SecurityEventListener {

    private static final Logger log = LoggerFactory.getLogger(SecurityEventListener.class);
    
    /**
     * Constants for audit action types to maintain consistency across the application
     */
    public static final class AuditActions {
        // Authentication related actions
        public static final String SUCCESSFUL_AUTHENTICATION = "Successful Authentication";
        public static final String FAILED_AUTHENTICATION = "Failed Authentication";
        public static final String UNKNOWN_AUTHENTICATION_ATTEMPT = "Unknown Authentication Attempt";
        public static final String USER_SIGNED_OUT = "User Signed Out";
        public static final String ACCOUNT_LOCKED = "Account Locked";
        public static final String PASSWORD_RESET_REQUESTED = "Password Reset Requested";
        public static final String PASSWORD_CHANGED = "Password Changed";
        public static final String PROFILE_UPDATED = "Profile Updated";
        
        // Password management actions
        public static final String PASSWORD_CREATED = "Password Entry Created";
        public static final String PASSWORD_UPDATED = "Password Entry Updated";
        public static final String PASSWORD_DELETED = "Password Entry Deleted";
        public static final String PASSWORD_VIEWED = "Password Entry Viewed";
        public static final String PASSWORD_COPIED = "Password Copied";
        
        // Secure note actions
        public static final String NOTE_CREATED = "Secure Note Created";
        public static final String NOTE_UPDATED = "Secure Note Updated";
        public static final String NOTE_DELETED = "Secure Note Deleted";
        public static final String NOTE_VIEWED = "Secure Note Viewed";
        
        // Category and tag actions
        public static final String CATEGORY_CREATED = "Category Created";
        public static final String CATEGORY_UPDATED = "Category Updated";
        public static final String CATEGORY_DELETED = "Category Deleted";
        public static final String TAG_CREATED = "Tag Created";
        public static final String TAG_UPDATED = "Tag Updated";
        public static final String TAG_DELETED = "Tag Deleted";
        
        // Administrative actions
        public static final String SETTINGS_UPDATED = "Settings Updated";
        public static final String USER_CREATED = "User Created";
        public static final String USER_UPDATED = "User Updated";
        public static final String USER_DELETED = "User Deleted";
        public static final String USER_VIEWED = "User Profile Viewed";
        
        // Security actions
        public static final String SECURITY_SCAN_PERFORMED = "Security Scan Performed";
        public static final String SECURITY_ALERT_TRIGGERED = "Security Alert Triggered";
        public static final String DATA_EXPORTED = "Data Exported";
        public static final String DATA_IMPORTED = "Data Imported";
    }
    
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
                        AuditActions.SUCCESSFUL_AUTHENTICATION,
                        "SECURITY",
                        null,
                        "Authentication successful from IP: " + remoteAddress
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
                        AuditActions.FAILED_AUTHENTICATION,
                        "SECURITY",
                        null,
                        "Authentication failed from IP: " + remoteAddress + ", Reason: Invalid credentials"
                );
            } else {
                User adminUser = userRepository.findByUsername("admin")
                    .orElse(userRepository.findAll().stream().findFirst().orElse(null));
                
                if (adminUser != null) {
                    safelyStoreAuditLog(
                            adminUser.getId(),
                            AuditActions.UNKNOWN_AUTHENTICATION_ATTEMPT,
                            "SECURITY",
                            null,
                            "Authentication attempt with unknown account: " + username + ", IP: " + remoteAddress
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
                        AuditActions.USER_SIGNED_OUT,
                        "SECURITY",
                        null,
                        "User signed out from IP: " + remoteAddress
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