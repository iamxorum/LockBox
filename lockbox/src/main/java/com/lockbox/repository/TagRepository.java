package com.lockbox.repository;

import com.lockbox.model.Tag;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TagRepository extends JpaRepository<Tag, Long> {
    
    List<Tag> findByPasswordsId(Long passwordId);
    
    List<Tag> findBySecureNotesId(Long secureNoteId);
    
    Optional<Tag> findByName(String name);
    
    boolean existsByName(String name);
} 