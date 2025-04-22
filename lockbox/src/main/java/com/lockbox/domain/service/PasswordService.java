package com.lockbox.domain.service;

import java.util.List;
import java.util.Optional;

import com.lockbox.domain.model.Password;

public interface PasswordService extends CrudService<Password, Long> {
    
    List<Password> findByUserId(Long userId);
    
    List<Password> findByUserIdAndCategoryId(Long userId, Long categoryId);
    
    Optional<Password> findByIdWithCategory(Long id);
    
    Optional<Password> findByIdWithTags(Long id);
} 