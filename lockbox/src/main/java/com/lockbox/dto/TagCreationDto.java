package com.lockbox.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TagCreationDto {
    
    @NotBlank(message = "Tag name is required")
    @Size(max = 30, message = "Tag name must be less than 30 characters")
    private String name;
} 