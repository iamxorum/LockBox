package com.lockbox.domain.service.impl;

import com.lockbox.domain.repository.PasswordRepository;
import com.lockbox.domain.model.Password;
import com.lockbox.domain.service.PasswordService;

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
        return passwordRepository.findByIdWithCategory(id);
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
} 