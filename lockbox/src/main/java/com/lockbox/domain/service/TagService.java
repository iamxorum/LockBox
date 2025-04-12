package com.lockbox.domain.service;

import com.lockbox.domain.model.Tag;

import java.util.List;

public interface TagService extends CrudService<Tag, Long> {
    
    boolean existsByName(String name);
    
    boolean existsByNameAndUserId(String name, Long userId);
    
    List<Tag> findByUserId(Long userId);
} 