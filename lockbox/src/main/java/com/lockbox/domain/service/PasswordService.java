package com.lockbox.domain.service;

import java.util.List;

import com.lockbox.domain.model.Password;

public interface PasswordService extends CrudService<Password, Long> {
    
    List<Password> findByUserId(Long userId);
} 