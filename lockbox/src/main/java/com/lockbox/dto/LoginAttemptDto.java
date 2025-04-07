package com.lockbox.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LoginAttemptDto {
    
    private Long id;
    private String username;
    private boolean successful;
    private String ipAddress;
    private String userAgent;
    private LocalDateTime timestamp;
} 