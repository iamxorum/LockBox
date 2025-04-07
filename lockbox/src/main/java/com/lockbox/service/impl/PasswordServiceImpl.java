package com.lockbox.service.impl;

import com.lockbox.model.Password;
import com.lockbox.repository.PasswordRepository;
import com.lockbox.service.PasswordService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class PasswordServiceImpl implements PasswordService {

    private final PasswordRepository passwordRepository;
    
    @Autowired
    public PasswordServiceImpl(PasswordRepository passwordRepository) {
        this.passwordRepository = passwordRepository;
    }
    
    @Override
    public List<Password> findAll() {
        return passwordRepository.findAll();
    }
    
    @Override
    public Optional<Password> findById(Long id) {
        return passwordRepository.findById(id);
    }
    
    @Override
    public Password save(Password password) {
        return passwordRepository.save(password);
    }
    
    @Override
    public void delete(Password password) {
        passwordRepository.delete(password);
    }
    
    @Override
    public void deleteById(Long id) {
        passwordRepository.deleteById(id);
    }
    
    @Override
    public boolean existsById(Long id) {
        return passwordRepository.existsById(id);
    }
    
    @Override
    public List<Password> findByUserId(Long userId) {
        return passwordRepository.findByUserId(userId);
    }
    
    @Override
    public List<Password> findByUserIdAndCategoryId(Long userId, Long categoryId) {
        return passwordRepository.findByUserIdAndCategoryId(userId, categoryId);
    }
    
    @Override
    public List<Password> searchByTitle(Long userId, String title) {
        return passwordRepository.findByUserIdAndTitleContainingIgnoreCase(userId, title);
    }
    
    @Override
    public List<Password> searchByUrl(Long userId, String url) {
        return passwordRepository.findByUserIdAndUrlContainingIgnoreCase(userId, url);
    }
} 