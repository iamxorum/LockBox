package com.lockbox.domain.service.impl;

import com.lockbox.domain.repository.*;
import com.lockbox.domain.model.User;
import com.lockbox.domain.service.UserService;
import com.lockbox.dto.UserStatsDto;
import com.lockbox.exception.ResourceNotFoundException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class UserServiceImpl implements UserService {
    
    private final UserRepository userRepository;
    private final PasswordRepository passwordRepository;
    private final SecureNoteRepository secureNoteRepository;
    private final CategoryRepository categoryRepository;
    private final TagRepository tagRepository;
    
    @Autowired
    public UserServiceImpl(
            UserRepository userRepository,
            PasswordRepository passwordRepository,
            SecureNoteRepository secureNoteRepository,
            CategoryRepository categoryRepository,
            TagRepository tagRepository) {
        this.userRepository = userRepository;
        this.passwordRepository = passwordRepository;
        this.secureNoteRepository = secureNoteRepository;
        this.categoryRepository = categoryRepository;
        this.tagRepository = tagRepository;
    }
    
    @Override
    public List<User> findAll() {
        return userRepository.findAll();
    }
    
    @Override
    @Cacheable(value = "users", key = "#id")
    public Optional<User> findById(Long id) {
        return userRepository.findById(id);
    }
    
    @Override
    public User save(User user) {
        return userRepository.save(user);
    }
    
    @Override
    public void delete(User user) {
        userRepository.delete(user);
    }
    
    @Override
    public void deleteById(Long id) {
        userRepository.deleteById(id);
    }
    
    @Override
    public boolean existsById(Long id) {
        return userRepository.existsById(id);
    }
    
    @Override
    public Optional<User> findByUsername(String username) {
        return userRepository.findByUsername(username);
    }
    
    @Override
    public boolean existsByUsername(String username) {
        return userRepository.existsByUsername(username);
    }
    
    @Override
    public boolean existsByEmail(String email) {
        return userRepository.existsByEmail(email);
    }
    
    @Override
    @Transactional(readOnly = true)
    public UserStatsDto getUserStats(Long userId) {
        // Verify user exists
        if (!userRepository.existsById(userId)) {
            throw new ResourceNotFoundException("User", "id", userId);
        }

        // Get counts for each type of data
        long passwordCount = passwordRepository.countByUserId(userId);
        long secureNoteCount = secureNoteRepository.countByUserId(userId);
        long categoryCount = categoryRepository.countByUserId(userId);
        long tagCount = tagRepository.countByUserId(userId);

        return new UserStatsDto(passwordCount, secureNoteCount, categoryCount, tagCount);
    }
} 