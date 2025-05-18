package com.lockbox.domain.repository;

import com.lockbox.domain.model.Tag;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

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

    boolean existsByUserIdAndName(Long userId, String name);
    
    @Query("SELECT t.id, COUNT(p) FROM Tag t LEFT JOIN t.passwords p WHERE t.user.id = :userId GROUP BY t.id")
    List<Object[]> getPasswordCountsByUserId(@Param("userId") Long userId);
} 