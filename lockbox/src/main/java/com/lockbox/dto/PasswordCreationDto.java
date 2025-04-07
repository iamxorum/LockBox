package com.lockbox.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.HashSet;
import java.util.Set;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PasswordCreationDto {
    
    @NotBlank(message = "Title is required")
    private String title;
    
    @NotBlank(message = "Username is required")
    private String username;
    
    @NotBlank(message = "Password is required")
    private String passwordValue;
    
    private String url;
    
    private String notes;
    
    private Long categoryId;
    
    private Set<Long> tagIds = new HashSet<>();
} 