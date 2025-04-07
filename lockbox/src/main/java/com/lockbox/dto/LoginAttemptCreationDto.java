package com.lockbox.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LoginAttemptCreationDto {
    
    @NotBlank(message = "Username is required")
    private String username;
    
    private boolean successful;
    
    @NotBlank(message = "IP address is required")
    private String ipAddress;
    
    private String userAgent;
} 