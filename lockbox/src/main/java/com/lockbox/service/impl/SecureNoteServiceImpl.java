package com.lockbox.service.impl;

import com.lockbox.model.SecureNote;
import com.lockbox.model.Tag;
import com.lockbox.repository.SecureNoteRepository;
import com.lockbox.repository.TagRepository;
import com.lockbox.service.SecureNoteService;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class SecureNoteServiceImpl implements SecureNoteService {

    private final SecureNoteRepository secureNoteRepository;
    private final TagRepository tagRepository;
    
    @Autowired
    public SecureNoteServiceImpl(SecureNoteRepository secureNoteRepository, TagRepository tagRepository) {
        this.secureNoteRepository = secureNoteRepository;
        this.tagRepository = tagRepository;
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
    
    @Override
    public List<SecureNote> findByUserIdAndCategoryId(Long userId, Long categoryId) {
        return secureNoteRepository.findByUserIdAndCategoryId(userId, categoryId);
    }
    
    @Override
    public List<SecureNote> searchByTitle(Long userId, String title) {
        return secureNoteRepository.findByUserIdAndTitleContainingIgnoreCase(userId, title);
    }
    
    @Override
    public List<SecureNote> searchByContent(Long userId, String content) {
        return secureNoteRepository.findByUserIdAndContentContainingIgnoreCase(userId, content);
    }
    
    @Override
    @Transactional
    public SecureNote addTag(Long noteId, Long tagId) {
        SecureNote secureNote = secureNoteRepository.findById(noteId)
                .orElseThrow(() -> new EntityNotFoundException("Secure note not found with ID: " + noteId));
        
        Tag tag = tagRepository.findById(tagId)
                .orElseThrow(() -> new EntityNotFoundException("Tag not found with ID: " + tagId));
        
        secureNote.getTags().add(tag);
        return secureNoteRepository.save(secureNote);
    }
    
    @Override
    @Transactional
    public SecureNote removeTag(Long noteId, Long tagId) {
        SecureNote secureNote = secureNoteRepository.findById(noteId)
                .orElseThrow(() -> new EntityNotFoundException("Secure note not found with ID: " + noteId));
        
        Tag tag = tagRepository.findById(tagId)
                .orElseThrow(() -> new EntityNotFoundException("Tag not found with ID: " + tagId));
        
        secureNote.getTags().remove(tag);
        return secureNoteRepository.save(secureNote);
    }
} 