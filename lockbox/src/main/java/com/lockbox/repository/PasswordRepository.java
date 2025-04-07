package com.lockbox.repository;

import com.lockbox.model.Password;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PasswordRepository extends JpaRepository<Password, Long> {
    
    List<Password> findByUserId(Long userId);
    
    List<Password> findByUserIdAndCategoryId(Long userId, Long categoryId);
    
    List<Password> findByUserIdAndTitleContainingIgnoreCase(Long userId, String title);
    
    List<Password> findByUserIdAndUrlContainingIgnoreCase(Long userId, String url);
} 