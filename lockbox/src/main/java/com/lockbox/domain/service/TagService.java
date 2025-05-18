package com.lockbox.domain.service;

import com.lockbox.domain.model.Tag;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public interface TagService {
    List<Tag> findAll();
    
    Optional<Tag> findById(Long id);
    
    Tag save(Tag tag);
    
    void delete(Tag tag);
    
    void deleteById(Long id);
    
    boolean existsById(Long id);
    
    boolean existsByName(String name);
    
    boolean existsByNameAndUserId(String name, Long userId);
    
    List<Tag> findByUserId(Long userId);
    
    List<Tag> findAllById(Iterable<Long> ids);
    
    Map<Long, Long> getPasswordCountsByTag(Long userId);
} 