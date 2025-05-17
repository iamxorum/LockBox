package com.lockbox.domain.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.lockbox.domain.model.Tag;

import java.util.List;
import java.util.Optional;

@Repository
public interface TagRepository extends JpaRepository<Tag, Long> {
    
    List<Tag> findByPasswordsId(Long passwordId);
    
    List<Tag> findBySecureNotesId(Long secureNoteId);
    
    Optional<Tag> findByName(String name);
    
    boolean existsByName(String name);
    
    List<Tag> findByUserId(Long userId);
    
    boolean existsByNameAndUserId(String name, Long userId);
    
    Optional<Tag> findByNameAndUserId(String name, Long userId);

    long countByUserId(Long userId);
} 