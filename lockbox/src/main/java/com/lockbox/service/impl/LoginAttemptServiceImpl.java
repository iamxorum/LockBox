package com.lockbox.service.impl;

import com.lockbox.model.LoginAttempt;
import com.lockbox.repository.LoginAttemptRepository;
import com.lockbox.service.LoginAttemptService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class LoginAttemptServiceImpl implements LoginAttemptService {

    private final LoginAttemptRepository loginAttemptRepository;
    
    @Autowired
    public LoginAttemptServiceImpl(LoginAttemptRepository loginAttemptRepository) {
        this.loginAttemptRepository = loginAttemptRepository;
    }
    
    @Override
    public List<LoginAttempt> findAll() {
        return loginAttemptRepository.findAll();
    }
    
    @Override
    public Optional<LoginAttempt> findById(Long id) {
        return loginAttemptRepository.findById(id);
    }
    
    @Override
    public LoginAttempt save(LoginAttempt loginAttempt) {
        return loginAttemptRepository.save(loginAttempt);
    }
    
    @Override
    public void delete(LoginAttempt loginAttempt) {
        loginAttemptRepository.delete(loginAttempt);
    }
    
    @Override
    public void deleteById(Long id) {
        loginAttemptRepository.deleteById(id);
    }
    
    @Override
    public boolean existsById(Long id) {
        return loginAttemptRepository.existsById(id);
    }
    
    @Override
    public List<LoginAttempt> findByUsername(String username) {
        return loginAttemptRepository.findByUsername(username);
    }
    
    @Override
    public List<LoginAttempt> findByUsernameAndSuccessful(String username, boolean successful) {
        return loginAttemptRepository.findByUsernameAndSuccessful(username, successful);
    }
    
    @Override
    public List<LoginAttempt> findByUsernameAndTimeRange(String username, LocalDateTime start, LocalDateTime end) {
        return loginAttemptRepository.findByUsernameAndTimestampBetween(username, start, end);
    }
    
    @Override
    public int countFailedAttempts(String username, int minutes) {
        LocalDateTime since = LocalDateTime.now().minusMinutes(minutes);
        return loginAttemptRepository.countFailedAttemptsInTimeframe(username, since);
    }
    
    @Override
    public List<LoginAttempt> findByIpAddress(String ipAddress) {
        return loginAttemptRepository.findByIpAddress(ipAddress);
    }
    
    @Override
    public LoginAttempt recordLoginAttempt(String username, boolean successful, String ipAddress, String userAgent) {
        LoginAttempt loginAttempt = new LoginAttempt();
        loginAttempt.setUsername(username);
        loginAttempt.setSuccessful(successful);
        loginAttempt.setIpAddress(ipAddress);
        loginAttempt.setUserAgent(userAgent);
        loginAttempt.setTimestamp(LocalDateTime.now());
        
        return loginAttemptRepository.save(loginAttempt);
    }
    
    @Override
    public boolean isUserLockedOut(String username, int maxAttempts, int lockoutMinutes) {
        int failedAttempts = countFailedAttempts(username, lockoutMinutes);
        return failedAttempts >= maxAttempts;
    }
} 