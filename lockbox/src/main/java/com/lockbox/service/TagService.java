package com.lockbox.service;

import com.lockbox.model.Tag;

public interface TagService extends CrudService<Tag, Long> {
    
    boolean existsByName(String name);
} 