package com.lockbox.mapper;

import com.lockbox.domain.model.User;
import com.lockbox.dto.UserCreationDto;
import com.lockbox.dto.UserDto;

import org.springframework.stereotype.Component;

@Component
public class UserMapper {
    
    public UserDto toDto(User user) {
        if (user == null) {
            return null;
        }
        
        UserDto dto = new UserDto();
        dto.setId(user.getId());
        dto.setUsername(user.getUsername());
        dto.setEmail(user.getEmail());
        dto.setFirstName(user.getFirstName());
        dto.setLastName(user.getLastName());
        
        return dto;
    }
    
    public UserCreationDto toCreationDto(User user) {
        if (user == null) {
            return null;
        }
        
        UserCreationDto dto = new UserCreationDto();
        dto.setUsername(user.getUsername());
        // Don't set password for security reasons
        dto.setEmail(user.getEmail());
        dto.setFirstName(user.getFirstName());
        dto.setLastName(user.getLastName());
        
        return dto;
    }
    
    public User toEntity(UserCreationDto dto) {
        if (dto == null) {
            return null;
        }
        
        User user = new User();
        user.setUsername(dto.getUsername());
        user.setPassword(dto.getPassword()); // Password will be encoded later
        user.setEmail(dto.getEmail());
        user.setFirstName(dto.getFirstName());
        user.setLastName(dto.getLastName());
        
        return user;
    }
    
    public void updateEntityFromDto(UserCreationDto dto, User user) {
        if (dto == null || user == null) {
            return;
        }
        
        if (dto.getUsername() != null) {
            user.setUsername(dto.getUsername());
        }
        
        if (dto.getPassword() != null) {
            user.setPassword(dto.getPassword()); // Password will be encoded later
        }
        
        if (dto.getEmail() != null) {
            user.setEmail(dto.getEmail());
        }
        
        if (dto.getFirstName() != null) {
            user.setFirstName(dto.getFirstName());
        }
        
        if (dto.getLastName() != null) {
            user.setLastName(dto.getLastName());
        }
    }
} 