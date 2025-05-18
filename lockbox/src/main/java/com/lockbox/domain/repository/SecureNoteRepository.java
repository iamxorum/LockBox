package com.lockbox.domain.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.web.bind.annotation.PathVariable;

import com.lockbox.domain.model.SecureNote;

import java.util.List;
import java.util.Optional;

@Repository
public interface SecureNoteRepository extends JpaRepository<SecureNote, Long> {
    
    List<SecureNote> findByUserId(Long userId);
    
    List<SecureNote> findByUserIdAndCategoryId(Long userId, Long categoryId);
    
    List<SecureNote> findByUserIdAndTitleContainingIgnoreCase(Long userId, String title);
    
    List<SecureNote> findByUserIdAndContentContainingIgnoreCase(Long userId, String content);
    
    @Query("SELECT DISTINCT n FROM SecureNote n LEFT JOIN FETCH n.category c LEFT JOIN FETCH n.tags t LEFT JOIN FETCH t.user WHERE n.user.id = :userId")
    List<SecureNote> findByUserIdWithTags(@PathVariable("userId") Long userId);
    
    @Query("SELECT DISTINCT n FROM SecureNote n LEFT JOIN FETCH n.category c LEFT JOIN FETCH n.tags t LEFT JOIN FETCH t.user WHERE n.id = :id")
    Optional<SecureNote> findByIdWithTags(@PathVariable("id") Long id);

    long countByUserId(Long userId);
} 