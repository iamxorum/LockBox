package com.lockbox.service.impl;

import com.lockbox.model.Tag;
import com.lockbox.repository.TagRepository;
import com.lockbox.service.TagService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class TagServiceImpl implements TagService {

    private final TagRepository tagRepository;
    
    @Autowired
    public TagServiceImpl(TagRepository tagRepository) {
        this.tagRepository = tagRepository;
    }
    
    @Override
    public List<Tag> findAll() {
        return tagRepository.findAll();
    }
    
    @Override
    public Optional<Tag> findById(Long id) {
        return tagRepository.findById(id);
    }
    
    @Override
    public Tag save(Tag tag) {
        return tagRepository.save(tag);
    }
    
    @Override
    public void delete(Tag tag) {
        tagRepository.delete(tag);
    }
    
    @Override
    public void deleteById(Long id) {
        tagRepository.deleteById(id);
    }
    
    @Override
    public boolean existsById(Long id) {
        return tagRepository.existsById(id);
    }
    
    @Override
    public List<Tag> findByPasswordId(Long passwordId) {
        return tagRepository.findByPasswordsId(passwordId);
    }
    
    @Override
    public List<Tag> findBySecureNoteId(Long secureNoteId) {
        return tagRepository.findBySecureNotesId(secureNoteId);
    }
    
    @Override
    public Optional<Tag> findByName(String name) {
        return tagRepository.findByName(name);
    }
    
    @Override
    public boolean existsByName(String name) {
        return tagRepository.existsByName(name);
    }
} 