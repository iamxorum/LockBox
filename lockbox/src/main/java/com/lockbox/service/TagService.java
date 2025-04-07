package com.lockbox.service;

import com.lockbox.model.Tag;

import java.util.List;
import java.util.Optional;

public interface TagService extends CrudService<Tag, Long> {
    
    List<Tag> findByPasswordId(Long passwordId);
    
    List<Tag> findBySecureNoteId(Long secureNoteId);
    
    Optional<Tag> findByName(String name);

    boolean existsByName(String name);
} 