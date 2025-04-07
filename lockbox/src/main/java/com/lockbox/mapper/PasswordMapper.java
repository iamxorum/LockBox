package com.lockbox.mapper;

import com.lockbox.dto.PasswordCreationDto;
import com.lockbox.dto.PasswordDto;
import com.lockbox.model.Category;
import com.lockbox.model.Password;
import com.lockbox.model.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.stream.Collectors;

@Component
public class PasswordMapper {
    
    private final TagMapper tagMapper;
    
    @Autowired
    public PasswordMapper(TagMapper tagMapper) {
        this.tagMapper = tagMapper;
    }
    
    public PasswordDto toDto(Password password) {
        if (password == null) {
            return null;
        }
        
        PasswordDto dto = new PasswordDto();
        dto.setId(password.getId());
        dto.setTitle(password.getTitle());
        dto.setUsername(password.getUsername());
        dto.setPasswordValue(password.getPasswordValue());
        dto.setUrl(password.getUrl());
        dto.setNotes(password.getNotes());
        dto.setCreatedAt(password.getCreatedAt());
        dto.setUpdatedAt(password.getUpdatedAt());
        
        if (password.getCategory() != null) {
            dto.setCategoryId(password.getCategory().getId());
            dto.setCategoryName(password.getCategory().getName());
        }
        
        if (password.getTags() != null) {
            dto.setTags(password.getTags().stream()
                    .map(tagMapper::toDto)
                    .collect(Collectors.toSet()));
        }
        
        return dto;
    }
    
    public Password toEntity(PasswordCreationDto dto) {
        if (dto == null) {
            return null;
        }
        
        Password password = new Password();
        password.setTitle(dto.getTitle());
        password.setUsername(dto.getUsername());
        password.setPasswordValue(dto.getPasswordValue());
        password.setUrl(dto.getUrl());
        password.setNotes(dto.getNotes());
        
        return password;
    }
    
    public void updateEntityFromDto(PasswordCreationDto dto, Password password) {
        if (dto == null || password == null) {
            return;
        }
        
        if (dto.getTitle() != null) {
            password.setTitle(dto.getTitle());
        }
        
        if (dto.getUsername() != null) {
            password.setUsername(dto.getUsername());
        }
        
        if (dto.getPasswordValue() != null) {
            password.setPasswordValue(dto.getPasswordValue());
        }
        
        if (dto.getUrl() != null) {
            password.setUrl(dto.getUrl());
        }
        
        if (dto.getNotes() != null) {
            password.setNotes(dto.getNotes());
        }
    }
    
    public void setCategoryInPassword(Password password, Category category) {
        if (password != null && category != null) {
            password.setCategory(category);
        }
    }
    
    public void addTagToPassword(Password password, Tag tag) {
        if (password != null && tag != null) {
            password.getTags().add(tag);
        }
    }
    
    public void removeTagFromPassword(Password password, Tag tag) {
        if (password != null && tag != null) {
            password.getTags().remove(tag);
        }
    }
} 