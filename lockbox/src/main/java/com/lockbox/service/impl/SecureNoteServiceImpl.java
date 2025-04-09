package com.lockbox.service.impl;

import com.lockbox.model.SecureNote;
import com.lockbox.repository.SecureNoteRepository;
import com.lockbox.service.SecureNoteService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class SecureNoteServiceImpl implements SecureNoteService {

    private final SecureNoteRepository secureNoteRepository;

    @Autowired
    public SecureNoteServiceImpl(SecureNoteRepository secureNoteRepository) {
        this.secureNoteRepository = secureNoteRepository;
    }
    
    @Override
    public List<SecureNote> findAll() {
        return secureNoteRepository.findAll();
    }
    
    @Override
    public Optional<SecureNote> findById(Long id) {
        return secureNoteRepository.findById(id);
    }
    
    @Override
    public SecureNote save(SecureNote secureNote) {
        return secureNoteRepository.save(secureNote);
    }
    
    @Override
    public void delete(SecureNote secureNote) {
        secureNoteRepository.delete(secureNote);
    }
    
    @Override
    public void deleteById(Long id) {
        secureNoteRepository.deleteById(id);
    }
    
    @Override
    public boolean existsById(Long id) {
        return secureNoteRepository.existsById(id);
    }
    
    @Override
    public List<SecureNote> findByUserId(Long userId) {
        return secureNoteRepository.findByUserId(userId);
    }
} 