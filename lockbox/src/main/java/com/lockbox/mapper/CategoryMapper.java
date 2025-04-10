package com.lockbox.mapper;

import com.lockbox.domain.model.Category;
import com.lockbox.dto.CategoryCreationDto;
import com.lockbox.dto.CategoryDto;

import org.springframework.stereotype.Component;

@Component
public class CategoryMapper {
    
    public CategoryDto toDto(Category category) {
        if (category == null) {
            return null;
        }
        
        CategoryDto dto = new CategoryDto();
        dto.setId(category.getId());
        dto.setName(category.getName());
        dto.setDescription(category.getDescription());
        dto.setCreatedAt(category.getCreatedAt());
        dto.setUpdatedAt(category.getUpdatedAt());
        
        // Map color if it exists
        if (category.getColor() != null) {
            dto.setColor(category.getColor());
        }
        
        return dto;
    }
    
    public Category toEntity(CategoryCreationDto dto) {
        if (dto == null) {
            return null;
        }
        
        Category category = new Category();
        category.setName(dto.getName());
        category.setDescription(dto.getDescription());
        category.setColor(dto.getColor());
        
        return category;
    }
    
    public void updateEntityFromDto(CategoryCreationDto dto, Category category) {
        if (dto == null || category == null) {
            return;
        }
        
        if (dto.getName() != null) {
            category.setName(dto.getName());
        }
        
        if (dto.getDescription() != null) {
            category.setDescription(dto.getDescription());
        }
        
        if (dto.getColor() != null) {
            category.setColor(dto.getColor());
        }
    }
} 