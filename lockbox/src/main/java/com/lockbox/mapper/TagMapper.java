package com.lockbox.mapper;

import com.lockbox.domain.model.Tag;
import com.lockbox.dto.TagCreationDto;
import com.lockbox.dto.TagDto;

import org.springframework.stereotype.Component;

@Component
public class TagMapper {
    
    public TagDto toDto(Tag tag) {
        if (tag == null) {
            return null;
        }
        
        TagDto dto = new TagDto();
        dto.setId(tag.getId());
        dto.setName(tag.getName());
        dto.setCreatedAt(tag.getCreatedAt());
        dto.setUpdatedAt(tag.getUpdatedAt());
        
        return dto;
    }
    
    public Tag toEntity(TagCreationDto dto) {
        if (dto == null) {
            return null;
        }
        
        Tag tag = new Tag();
        tag.setName(dto.getName());
        
        return tag;
    }
    
    public void updateEntityFromDto(TagCreationDto dto, Tag tag) {
        if (dto == null || tag == null) {
            return;
        }
        
        if (dto.getName() != null) {
            tag.setName(dto.getName());
        }
    }
} 