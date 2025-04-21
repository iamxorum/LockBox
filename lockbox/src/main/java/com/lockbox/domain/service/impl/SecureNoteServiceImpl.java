package com.lockbox.domain.service.impl;

import com.lockbox.domain.repository.SecureNoteRepository;
import com.lockbox.domain.model.SecureNote;
import com.lockbox.domain.service.SecureNoteService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@Transactional(readOnly = true)
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
    @Transactional
    public SecureNote save(SecureNote secureNote) {
        return secureNoteRepository.save(secureNote);
    }
    
    @Override
    @Transactional
    public void delete(SecureNote secureNote) {
        secureNoteRepository.delete(secureNote);
    }
    
    @Override
    @Transactional
    public void deleteById(Long id) {
        secureNoteRepository.deleteById(id);
    }
    
    @Override
    public boolean existsById(Long id) {
        return secureNoteRepository.existsById(id);
    }
    
    @Override
    public List<SecureNote> findByUserId(Long userId) {
        return secureNoteRepository.findByUserIdWithTags(userId);
    }
    
    @Override
    public Optional<SecureNote> findByIdWithTags(Long id) {
        // First try to find the note with its tags
        List<SecureNote> notes = secureNoteRepository.findByUserIdWithTags(
            secureNoteRepository.findById(id)
                .map(note -> note.getUser().getId())
                .orElse(-1L)
        );
        
        // Then find the specific note we want
        return notes.stream()
            .filter(note -> note.getId().equals(id))
            .findFirst();
    }
} 