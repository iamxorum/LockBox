package com.lockbox.domain.service;

import java.util.Optional;

import com.lockbox.domain.model.User;

public interface UserService extends CrudService<User, Long> {
    
    Optional<User> findByUsername(String username);
    
    boolean existsByUsername(String username);
    
    boolean existsByEmail(String email);
} 