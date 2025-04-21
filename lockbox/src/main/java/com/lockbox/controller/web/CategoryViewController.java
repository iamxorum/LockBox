package com.lockbox.controller.web;

import com.lockbox.domain.model.Category;
import com.lockbox.domain.model.User;
import com.lockbox.domain.service.CategoryService;
import com.lockbox.domain.service.UserService;
import com.lockbox.dto.CategoryCreationDto;
import com.lockbox.mapper.CategoryMapper;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/categories")
public class CategoryViewController {
    private static final Logger logger = LoggerFactory.getLogger(CategoryViewController.class);
    
    private final CategoryService categoryService;
    private final UserService userService;
    private final CategoryMapper categoryMapper;

    public CategoryViewController(CategoryService categoryService,
                                UserService userService,
                                CategoryMapper categoryMapper) {
        this.categoryService = categoryService;
        this.userService = userService;
        this.categoryMapper = categoryMapper;
    }

    @PostMapping
    @ResponseBody
    public ResponseEntity<?> createCategory(@RequestBody CategoryCreationDto categoryCreationDto,
                                          Authentication authentication) {
        try {
            User user = userService.findByUsername(authentication.getName())
                    .orElseThrow(() -> new SecurityException("User not found"));

            // Check if category with same name exists
            if (categoryService.existsByUserIdAndName(user.getId(), categoryCreationDto.getName())) {
                return ResponseEntity.badRequest()
                    .body("A category with this name already exists");
            }

            // Create and save the category
            Category category = categoryMapper.toEntity(categoryCreationDto);
            category.setUser(user);
            category = categoryService.save(category);

            // Return the created category
            return ResponseEntity.ok(categoryMapper.toDto(category));
        } catch (Exception e) {
            logger.error("Error creating category", e);
            return ResponseEntity.internalServerError()
                .body("Error creating category: " + e.getMessage());
        }
    }
} 