package com.lockbox.service;

import com.lockbox.model.LoginAttempt;

import java.time.LocalDateTime;
import java.util.List;

public interface LoginAttemptService extends CrudService<LoginAttempt, Long> {
    
    List<LoginAttempt> findByUsername(String username);

    List<LoginAttempt> findByUsernameAndSuccessful(String username, boolean successful);
    
    List<LoginAttempt> findByUsernameAndTimeRange(String username, LocalDateTime start, LocalDateTime end);
    
    int countFailedAttempts(String username, int minutes);
    
    List<LoginAttempt> findByIpAddress(String ipAddress);
    
    LoginAttempt recordLoginAttempt(String username, boolean successful, String ipAddress, String userAgent);
    
    boolean isUserLockedOut(String username, int maxAttempts, int lockoutMinutes);
} 