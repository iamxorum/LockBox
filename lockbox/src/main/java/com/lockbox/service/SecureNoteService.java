package com.lockbox.service;

import com.lockbox.model.SecureNote;

import java.util.List;

public interface SecureNoteService extends CrudService<SecureNote, Long> {
    
    List<SecureNote> findByUserId(Long userId);
} 