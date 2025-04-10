package com.lockbox.domain.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.lockbox.domain.model.Category;

import java.util.List;
import java.util.Optional;

@Repository
public interface CategoryRepository extends JpaRepository<Category, Long> {
    
    List<Category> findByUserId(Long userId);
    
    Optional<Category> findByUserIdAndName(Long userId, String name);
    
    boolean existsByUserIdAndName(Long userId, String name);
} 