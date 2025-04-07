package com.lockbox.service;

import com.lockbox.model.Password;

import java.util.List;

public interface PasswordService extends CrudService<Password, Long> {
    
    List<Password> findByUserId(Long userId);
    
    List<Password> findByUserIdAndCategoryId(Long userId, Long categoryId);
    
    List<Password> searchByTitle(Long userId, String title);
    
    List<Password> searchByUrl(Long userId, String url);
} 