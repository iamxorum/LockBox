package com.lockbox.domain.service;

import java.util.List;

import com.lockbox.domain.model.Category;

public interface CategoryService extends CrudService<Category, Long> {
    
    List<Category> findByUserId(Long userId);
    
    boolean existsByUserIdAndName(Long userId, String name);
} 