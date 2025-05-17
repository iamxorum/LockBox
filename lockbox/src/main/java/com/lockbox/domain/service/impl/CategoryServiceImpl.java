package com.lockbox.domain.service.impl;

import com.lockbox.domain.repository.CategoryRepository;
import com.lockbox.domain.repository.PasswordRepository;
import com.lockbox.domain.model.Category;
import com.lockbox.domain.service.CategoryService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class CategoryServiceImpl implements CategoryService {

    private final CategoryRepository categoryRepository;
    private final PasswordRepository passwordRepository;
    
    @Autowired
    public CategoryServiceImpl(CategoryRepository categoryRepository, PasswordRepository passwordRepository) {
        this.categoryRepository = categoryRepository;
        this.passwordRepository = passwordRepository;
    }
    
    @Override
    public List<Category> findAll() {
        return categoryRepository.findAll();
    }
    
    @Override
    public Optional<Category> findById(Long id) {
        return categoryRepository.findById(id);
    }
    
    @Override
    public Category save(Category category) {
        return categoryRepository.save(category);
    }
    
    @Override
    public void delete(Category category) {
        categoryRepository.delete(category);
    }
    
    @Override
    public void deleteById(Long id) {
        categoryRepository.deleteById(id);
    }
    
    @Override
    public boolean existsById(Long id) {
        return categoryRepository.existsById(id);
    }
    
    @Override
    public List<Category> findByUserId(Long userId) {
        return categoryRepository.findByUserId(userId);
    }
    
    @Override
    public boolean existsByUserIdAndName(Long userId, String name) {
        return categoryRepository.existsByUserIdAndName(userId, name);
    }
    
    @Override
    public Map<Long, Long> getPasswordCountsByCategory(Long userId) {
        return passwordRepository.countPasswordsByCategory(userId)
                .stream()
                .collect(Collectors.toMap(
                    row -> (Long) row[0],  // categoryId
                    row -> (Long) row[1]   // count
                ));
    }
} 