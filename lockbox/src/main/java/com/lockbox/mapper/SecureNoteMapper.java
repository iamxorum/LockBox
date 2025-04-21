package com.lockbox.mapper;

import com.lockbox.domain.model.Category;
import com.lockbox.domain.model.SecureNote;
import com.lockbox.domain.model.Tag;
import com.lockbox.dto.SecureNoteCreationDto;
import com.lockbox.dto.SecureNoteDto;
import com.lockbox.dto.TagDto;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.hibernate.Hibernate;

import java.time.LocalDateTime;
import java.util.stream.Collectors;
import java.util.HashSet;
import java.util.List;
import java.util.Collections;

@Component
public class SecureNoteMapper {
    
    private final CategoryMapper categoryMapper;
    private final TagMapper tagMapper;
    
    @Autowired
    public SecureNoteMapper(CategoryMapper categoryMapper, TagMapper tagMapper) {
        this.categoryMapper = categoryMapper;
        this.tagMapper = tagMapper;
    }
    
    public SecureNoteDto toDto(SecureNote entity) {
        if (entity == null) {
            return null;
        }

        SecureNoteDto dto = new SecureNoteDto();
        dto.setId(entity.getId());
        dto.setTitle(entity.getTitle());
        dto.setContent(entity.getContent());
        dto.setCreatedAt(entity.getCreatedAt());
        dto.setUpdatedAt(entity.getUpdatedAt());
        dto.setCategoryId(entity.getCategory() != null ? entity.getCategory().getId() : null);
        dto.setUserId(entity.getUser() != null ? entity.getUser().getId() : null);
        dto.setTagIds(entity.getTags().stream()
                .map(Tag::getId)
                .collect(Collectors.toList()));

        return dto;
    }
    
    public SecureNote toEntity(SecureNoteDto dto) {
        if (dto == null) {
            return null;
        }

        SecureNote entity = new SecureNote();
        entity.setId(dto.getId());
        entity.setTitle(dto.getTitle());
        entity.setContent(dto.getContent());
        entity.setCreatedAt(dto.getCreatedAt());
        entity.setUpdatedAt(dto.getUpdatedAt());

        return entity;
    }

    public SecureNote toEntity(SecureNoteCreationDto dto) {
        if (dto == null) {
            return null;
        }

        SecureNote entity = new SecureNote();
        entity.setTitle(dto.getTitle());
        entity.setContent(dto.getContent());
        entity.setCreatedAt(LocalDateTime.now());
        entity.setUpdatedAt(LocalDateTime.now());

        return entity;
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

        // Clear existing tags if tagIds is null or empty
        if (dto.getTagIds() == null || dto.getTagIds().isEmpty()) {
            secureNote.getTags().clear();
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
    
    public List<SecureNoteDto> toDtoList(List<SecureNote> notes) {
        if (notes == null) {
            return Collections.emptyList();
        }
        return notes.stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }
} 