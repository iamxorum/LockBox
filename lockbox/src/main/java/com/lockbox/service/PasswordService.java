package com.lockbox.service;

import com.lockbox.model.Password;

import java.util.List;

public interface PasswordService extends CrudService<Password, Long> {
    
    List<Password> findByUserId(Long userId);
} 