package com.lockbox.domain.service.impl;

import com.lockbox.domain.repository.TagRepository;
import com.lockbox.domain.repository.PasswordRepository;
import com.lockbox.domain.model.Tag;
import com.lockbox.domain.service.TagService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class TagServiceImpl implements TagService {

    private final TagRepository tagRepository;
    private final PasswordRepository passwordRepository;
    
    @Autowired
    public TagServiceImpl(TagRepository tagRepository, PasswordRepository passwordRepository) {
        this.tagRepository = tagRepository;
        this.passwordRepository = passwordRepository;
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
    public boolean existsByName(String name) {
        return tagRepository.existsByName(name);
    }
    
    @Override
    public boolean existsByNameAndUserId(String name, Long userId) {
        return tagRepository.existsByNameAndUserId(name, userId);
    }
    
    @Override
    public List<Tag> findByUserId(Long userId) {
        return tagRepository.findByUserId(userId);
    }

    @Override
    public List<Tag> findAllById(Iterable<Long> ids) {
        return tagRepository.findAllById(ids);
    }
    
    @Override
    public Map<Long, Long> getPasswordCountsByTag(Long userId) {
        return passwordRepository.countPasswordsByTag(userId)
                .stream()
                .collect(Collectors.toMap(
                    row -> (Long) row[0],  // tagId
                    row -> (Long) row[1]   // count
                ));
    }
} 