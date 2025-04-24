package com.lockbox.mapper;

import com.lockbox.domain.model.Category;
import com.lockbox.domain.model.Password;
import com.lockbox.domain.model.Tag;
import com.lockbox.domain.service.CategoryService;
import com.lockbox.domain.service.TagService;
import com.lockbox.dto.PasswordCreationDto;
import com.lockbox.dto.PasswordDto;
import com.lockbox.dto.TagDto;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Component
public class PasswordMapper {
    
    private final TagMapper tagMapper;
    private final CategoryService categoryService;
    private final TagService tagService;
    
    @Autowired
    public PasswordMapper(TagMapper tagMapper, CategoryService categoryService, TagService tagService) {
        this.tagMapper = tagMapper;
        this.categoryService = categoryService;
        this.tagService = tagService;
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
                    .map(tag -> {
                        TagDto tagDto = new TagDto();
                        tagDto.setId(tag.getId());
                        tagDto.setName(tag.getName());
                        return tagDto;
                    })
                    .collect(Collectors.toSet()));
            
            // Populate tagIds list
            dto.setTagIds(password.getTags().stream()
                    .map(Tag::getId)
                    .collect(Collectors.toList()));
        }
        
        return dto;
    }
    
    public PasswordCreationDto toCreationDto(Password password) {
        if (password == null) {
            return null;
        }
        
        PasswordCreationDto dto = new PasswordCreationDto();
        dto.setId(password.getId());
        dto.setTitle(password.getTitle());
        dto.setUsername(password.getUsername());
        dto.setPassword(password.getPasswordValue());
        dto.setWebsiteUrl(password.getUrl());
        dto.setNotes(password.getNotes());
        
        if (password.getCategory() != null) {
            dto.setCategoryId(password.getCategory().getId());
        }
        
        if (password.getUser() != null) {
            dto.setUserId(password.getUser().getId());
        }
        
        if (password.getTags() != null && !password.getTags().isEmpty()) {
            // Set both the tag objects and the IDs for the form
            Set<TagDto> tagDtos = password.getTags().stream()
                .map(tag -> {
                    TagDto tagDto = new TagDto();
                    tagDto.setId(tag.getId());
                    tagDto.setName(tag.getName());
                    return tagDto;
                })
                .collect(Collectors.toSet());
            dto.setTags(tagDtos);
            
            // Also set the tag IDs for saving
            List<Long> tagIds = password.getTags().stream()
                .map(Tag::getId)
                .collect(Collectors.toList());
            dto.setTagIds(tagIds);
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
        password.setPasswordValue(dto.getPassword());
        password.setUrl(dto.getWebsiteUrl());
        password.setNotes(dto.getNotes());
        
        if (dto.getCategoryId() != null) {
            Category category = categoryService.findById(dto.getCategoryId())
                .orElseThrow(() -> new IllegalArgumentException("Category not found with ID: " + dto.getCategoryId()));
            password.setCategory(category);
        }

        if (dto.getTagIds() != null && !dto.getTagIds().isEmpty()) {
            Set<Tag> tags = dto.getTagIds().stream()
                .map(tagId -> tagService.findById(tagId)
                    .orElseThrow(() -> new IllegalArgumentException("Tag not found with ID: " + tagId)))
                .collect(Collectors.toSet());
            password.setTags(tags);
        }

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
        
        if (dto.getPassword() != null) {
            password.setPasswordValue(dto.getPassword());
        }
        
        if (dto.getWebsiteUrl() != null) {
            password.setUrl(dto.getWebsiteUrl());
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