package com.lockbox.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TagDto {
    
    private Long id;
    private String name;
    private String color;
    private Long userId;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
} 