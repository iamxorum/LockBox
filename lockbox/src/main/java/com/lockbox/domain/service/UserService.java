package com.lockbox.domain.service;

import java.util.Optional;

import com.lockbox.domain.model.User;
import com.lockbox.dto.UserStatsDto;

public interface UserService extends CrudService<User, Long> {
    
    Optional<User> findByUsername(String username);
    
    boolean existsByUsername(String username);
    
    boolean existsByEmail(String email);

    /**
     * Get statistics about a user's data
     * @param userId the ID of the user
     * @return UserStatsDto containing counts of user's passwords, secure notes, categories, and tags
     */
    UserStatsDto getUserStats(Long userId);
} 