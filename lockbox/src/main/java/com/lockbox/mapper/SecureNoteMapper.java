package com.lockbox.mapper;

import com.lockbox.domain.model.Category;
import com.lockbox.domain.model.SecureNote;
import com.lockbox.domain.model.Tag;
import com.lockbox.dto.SecureNoteCreationDto;
import com.lockbox.dto.SecureNoteDto;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.stream.Collectors;

@Component
public class SecureNoteMapper {
    
    private final TagMapper tagMapper;
    
    @Autowired
    public SecureNoteMapper(TagMapper tagMapper) {
        this.tagMapper = tagMapper;
    }
    
    public SecureNoteDto toDto(SecureNote secureNote) {
        if (secureNote == null) {
            return null;
        }
        
        SecureNoteDto dto = new SecureNoteDto();
        dto.setId(secureNote.getId());
        dto.setTitle(secureNote.getTitle());
        dto.setContent(secureNote.getContent());
        dto.setCreatedAt(secureNote.getCreatedAt());
        dto.setUpdatedAt(secureNote.getUpdatedAt());
        
        if (secureNote.getCategory() != null) {
            dto.setCategoryId(secureNote.getCategory().getId());
            dto.setCategoryName(secureNote.getCategory().getName());
        }
        
        if (secureNote.getTags() != null) {
            dto.setTags(secureNote.getTags().stream()
                    .map(tagMapper::toDto)
                    .collect(Collectors.toSet()));
        }
        
        return dto;
    }
    
    public SecureNote toEntity(SecureNoteCreationDto dto) {
        if (dto == null) {
            return null;
        }
        
        SecureNote secureNote = new SecureNote();
        secureNote.setTitle(dto.getTitle());
        secureNote.setContent(dto.getContent());
        
        return secureNote;
    }
    
    public void updateEntityFromDto(SecureNoteCreationDto dto, SecureNote secureNote) {
        if (dto == null || secureNote == null) {
            return;
        }
        
        if (dto.getTitle() != null) {
            secureNote.setTitle(dto.getTitle());
        }
        
        if (dto.getContent() != null) {
            secureNote.setContent(dto.getContent());
        }
    }
    
    public void setCategoryInSecureNote(SecureNote secureNote, Category category) {
        if (secureNote != null && category != null) {
            secureNote.setCategory(category);
        }
    }
    
    public void addTagToSecureNote(SecureNote secureNote, Tag tag) {
        if (secureNote != null && tag != null) {
            secureNote.getTags().add(tag);
        }
    }
    
    public void removeTagFromSecureNote(SecureNote secureNote, Tag tag) {
        if (secureNote != null && tag != null) {
            secureNote.getTags().remove(tag);
        }
    }
} 