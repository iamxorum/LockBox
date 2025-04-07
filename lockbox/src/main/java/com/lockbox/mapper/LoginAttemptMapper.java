package com.lockbox.mapper;

import com.lockbox.dto.LoginAttemptCreationDto;
import com.lockbox.dto.LoginAttemptDto;
import com.lockbox.model.LoginAttempt;
import org.springframework.stereotype.Component;

@Component
public class LoginAttemptMapper {
    
    public LoginAttemptDto toDto(LoginAttempt loginAttempt) {
        if (loginAttempt == null) {
            return null;
        }
        
        LoginAttemptDto dto = new LoginAttemptDto();
        dto.setId(loginAttempt.getId());
        dto.setUsername(loginAttempt.getUsername());
        dto.setSuccessful(loginAttempt.isSuccessful());
        dto.setIpAddress(loginAttempt.getIpAddress());
        dto.setUserAgent(loginAttempt.getUserAgent());
        dto.setTimestamp(loginAttempt.getTimestamp());
        
        return dto;
    }
    
    public LoginAttempt toEntity(LoginAttemptCreationDto dto) {
        if (dto == null) {
            return null;
        }
        
        LoginAttempt loginAttempt = new LoginAttempt();
        loginAttempt.setUsername(dto.getUsername());
        loginAttempt.setSuccessful(dto.isSuccessful());
        loginAttempt.setIpAddress(dto.getIpAddress());
        loginAttempt.setUserAgent(dto.getUserAgent());
        
        return loginAttempt;
    }
    
    public void updateEntityFromDto(LoginAttemptCreationDto dto, LoginAttempt loginAttempt) {
        if (dto == null || loginAttempt == null) {
            return;
        }
        
        if (dto.getUsername() != null) {
            loginAttempt.setUsername(dto.getUsername());
        }
        
        loginAttempt.setSuccessful(dto.isSuccessful());
        
        if (dto.getIpAddress() != null) {
            loginAttempt.setIpAddress(dto.getIpAddress());
        }
        
        if (dto.getUserAgent() != null) {
            loginAttempt.setUserAgent(dto.getUserAgent());
        }
    }
} 