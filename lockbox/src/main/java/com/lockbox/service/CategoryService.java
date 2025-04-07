package com.lockbox.service;

import com.lockbox.model.Category;

import java.util.List;
import java.util.Optional;

public interface CategoryService extends CrudService<Category, Long> {
    
    List<Category> findByUserId(Long userId);
    
    Optional<Category> findByUserIdAndName(Long userId, String name);
    
    boolean existsByUserIdAndName(Long userId, String name);
} 