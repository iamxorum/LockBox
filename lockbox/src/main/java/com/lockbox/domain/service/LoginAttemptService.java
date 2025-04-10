package com.lockbox.domain.service;

import java.time.LocalDateTime;
import java.util.List;

import com.lockbox.domain.model.LoginAttempt;

public interface LoginAttemptService extends CrudService<LoginAttempt, Long> {
    
    List<LoginAttempt> findByUsername(String username);

    List<LoginAttempt> findByUsernameAndSuccessful(String username, boolean successful);
    
    List<LoginAttempt> findByUsernameAndTimeRange(String username, LocalDateTime start, LocalDateTime end);
    
    int countFailedAttempts(String username, int minutes);
    
    List<LoginAttempt> findByIpAddress(String ipAddress);
    
    LoginAttempt recordLoginAttempt(String username, boolean successful, String ipAddress, String userAgent);
    
    boolean isUserLockedOut(String username, int maxAttempts, int lockoutMinutes);
} 