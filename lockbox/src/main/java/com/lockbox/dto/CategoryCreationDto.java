package com.lockbox.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CategoryCreationDto {
    
    @NotBlank(message = "Category name is required")
    @Size(max = 50, message = "Category name must be less than 50 characters")
    @Pattern(regexp = "^[A-Za-z0-9\\s\\-_]{1,50}$", message = "Category name can only contain letters, numbers, spaces, hyphens, and underscores")
    private String name;
    
    private String description;
    
    private String color;
    
    private Long userId;
} 