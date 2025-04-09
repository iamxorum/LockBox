package com.lockbox.service;

import com.lockbox.model.User;

import java.util.Optional;

public interface UserService extends CrudService<User, Long> {
    
    Optional<User> findByUsername(String username);
    
    boolean existsByUsername(String username);
    
    boolean existsByEmail(String email);
} 