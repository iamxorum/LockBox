package com.lockbox.service;

import com.lockbox.model.SecureNote;

import java.util.List;

public interface SecureNoteService extends CrudService<SecureNote, Long> {
    
    List<SecureNote> findByUserId(Long userId);
    
    List<SecureNote> findByUserIdAndCategoryId(Long userId, Long categoryId);
    
    List<SecureNote> searchByTitle(Long userId, String title);
    
    List<SecureNote> searchByContent(Long userId, String content);
    
    SecureNote addTag(Long noteId, Long tagId);
    
    SecureNote removeTag(Long noteId, Long tagId);
} 