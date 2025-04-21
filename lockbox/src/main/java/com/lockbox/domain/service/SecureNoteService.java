package com.lockbox.domain.service;

import java.util.List;
import java.util.Optional;

import com.lockbox.domain.model.SecureNote;

public interface SecureNoteService extends CrudService<SecureNote, Long> {
    
    List<SecureNote> findByUserId(Long userId);
    
    Optional<SecureNote> findByIdWithTags(Long id);
} 