package com.lockbox.domain.service;

import java.util.List;

import com.lockbox.domain.model.SecureNote;

public interface SecureNoteService extends CrudService<SecureNote, Long> {
    
    List<SecureNote> findByUserId(Long userId);
} 