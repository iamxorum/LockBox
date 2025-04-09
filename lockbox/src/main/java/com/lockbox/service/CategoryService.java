package com.lockbox.service;

import com.lockbox.model.Category;

import java.util.List;

public interface CategoryService extends CrudService<Category, Long> {
    
    List<Category> findByUserId(Long userId);
    
    boolean existsByUserIdAndName(Long userId, String name);
} 