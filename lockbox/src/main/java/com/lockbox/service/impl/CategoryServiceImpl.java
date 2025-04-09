package com.lockbox.service.impl;

import com.lockbox.model.Category;
import com.lockbox.repository.CategoryRepository;
import com.lockbox.service.CategoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class CategoryServiceImpl implements CategoryService {

    private final CategoryRepository categoryRepository;
    
    @Autowired
    public CategoryServiceImpl(CategoryRepository categoryRepository) {
        this.categoryRepository = categoryRepository;
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
} 