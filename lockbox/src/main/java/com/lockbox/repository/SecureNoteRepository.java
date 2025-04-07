package com.lockbox.repository;

import com.lockbox.model.SecureNote;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SecureNoteRepository extends JpaRepository<SecureNote, Long> {
    
    List<SecureNote> findByUserId(Long userId);
    
    List<SecureNote> findByUserIdAndCategoryId(Long userId, Long categoryId);
    
    List<SecureNote> findByUserIdAndTitleContainingIgnoreCase(Long userId, String title);
    
    List<SecureNote> findByUserIdAndContentContainingIgnoreCase(Long userId, String content);
} 