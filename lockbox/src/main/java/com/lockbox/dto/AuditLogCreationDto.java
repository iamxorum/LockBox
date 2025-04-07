package com.lockbox.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AuditLogCreationDto {
    
    @NotBlank(message = "Action is required")
    private String action;
    
    @NotBlank(message = "Entity type is required")
    private String entityType;
    
    private Long entityId;
    
    private String details;
    
    @NotNull(message = "User ID is required")
    private Long userId;
} 