package com.lockbox.mapper;

import com.lockbox.domain.model.Tag;
import com.lockbox.domain.model.User;
import com.lockbox.domain.service.UserService;
import com.lockbox.dto.TagCreationDto;
import com.lockbox.dto.TagDto;
import com.lockbox.exception.ResourceNotFoundException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class TagMapper {
    
    private final UserService userService;
    
    @Autowired
    public TagMapper(UserService userService) {
        this.userService = userService;
    }
    
    public TagDto toDto(Tag tag) {
        if (tag == null) {
            return null;
        }
        
        TagDto dto = new TagDto();
        dto.setId(tag.getId());
        dto.setName(tag.getName());
        dto.setUserId(tag.getUser().getId());
        dto.setCreatedAt(tag.getCreatedAt());
        dto.setUpdatedAt(tag.getUpdatedAt());
        
        return dto;
    }
    
    public Tag toEntity(TagCreationDto dto) {
        if (dto == null) {
            return null;
        }
        
        User user = userService.findById(dto.getUserId())
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", dto.getUserId()));
        
        Tag tag = new Tag();
        tag.setName(dto.getName());
        tag.setUser(user);
        
        return tag;
    }
    
    public void updateEntityFromDto(TagCreationDto dto, Tag tag) {
        if (dto == null || tag == null) {
            return;
        }
        
        if (dto.getName() != null) {
            tag.setName(dto.getName());
        }
        
        if (dto.getUserId() != null) {
            User user = userService.findById(dto.getUserId())
                    .orElseThrow(() -> new ResourceNotFoundException("User", "id", dto.getUserId()));
            tag.setUser(user);
        }
    }
} 