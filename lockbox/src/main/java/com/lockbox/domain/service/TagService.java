package com.lockbox.domain.service;

import com.lockbox.domain.model.Tag;

public interface TagService extends CrudService<Tag, Long> {
    
    boolean existsByName(String name);
} 