package com.lockbox.domain.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.web.bind.annotation.PathVariable;

import com.lockbox.domain.model.Password;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@Repository
public interface PasswordRepository extends JpaRepository<Password, Long> {
    
    @Query("SELECT DISTINCT p FROM Password p LEFT JOIN FETCH p.category LEFT JOIN FETCH p.user LEFT JOIN FETCH p.tags WHERE p.user.id = :userId")
    List<Password> findByUserId(@Param("userId") Long userId);
    
    @Query("SELECT DISTINCT p FROM Password p LEFT JOIN FETCH p.category LEFT JOIN FETCH p.user LEFT JOIN FETCH p.tags WHERE p.user.id = :userId AND p.category.id = :categoryId")
    List<Password> findByUserIdAndCategoryId(@Param("userId") Long userId, @Param("categoryId") Long categoryId);
    
    @Query("SELECT DISTINCT p FROM Password p LEFT JOIN FETCH p.category LEFT JOIN FETCH p.user LEFT JOIN FETCH p.tags WHERE p.user.id = :userId AND LOWER(p.title) LIKE LOWER(CONCAT('%', :title, '%'))")
    List<Password> findByUserIdAndTitleContainingIgnoreCase(@Param("userId") Long userId, @Param("title") String title);
    
    @Query("SELECT DISTINCT p FROM Password p LEFT JOIN FETCH p.category LEFT JOIN FETCH p.user LEFT JOIN FETCH p.tags WHERE p.user.id = :userId AND LOWER(p.url) LIKE LOWER(CONCAT('%', :url, '%'))")
    List<Password> findByUserIdAndUrlContainingIgnoreCase(@Param("userId") Long userId, @Param("url") String url);
    
    @Query("SELECT DISTINCT p FROM Password p LEFT JOIN FETCH p.category LEFT JOIN FETCH p.user LEFT JOIN FETCH p.tags WHERE p.id = :id")
    Optional<Password> findByIdWithCategory(@Param("id") Long id);
    
    List<Password> findByUserIdAndUsernameContainingIgnoreCase(Long userId, String username);
    
    @Query("SELECT DISTINCT p FROM Password p LEFT JOIN FETCH p.tags WHERE p.user.id = :userId")
    List<Password> findByUserIdWithTags(@PathVariable("userId") Long userId);
    
    @Query("SELECT DISTINCT p FROM Password p LEFT JOIN FETCH p.tags WHERE p.id = :id")
    Optional<Password> findByIdWithTags(@PathVariable("id") Long id);

    long countByUserId(Long userId);
    
    @Query("SELECT p.category.id as categoryId, COUNT(p) as count FROM Password p WHERE p.user.id = :userId GROUP BY p.category.id")
    List<Object[]> countPasswordsByCategory(@Param("userId") Long userId);
    
    @Query("SELECT t.id as tagId, COUNT(p) as count FROM Password p JOIN p.tags t WHERE p.user.id = :userId GROUP BY t.id")
    List<Object[]> countPasswordsByTag(@Param("userId") Long userId);
} 