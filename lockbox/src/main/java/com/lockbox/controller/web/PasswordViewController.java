package com.lockbox.controller.web;

import com.lockbox.domain.model.Category;
import com.lockbox.domain.model.Password;
import com.lockbox.domain.model.Tag;
import com.lockbox.domain.model.User;
import com.lockbox.domain.service.AuditLogService;
import com.lockbox.domain.service.CategoryService;
import com.lockbox.domain.service.PasswordService;
import com.lockbox.domain.service.TagService;
import com.lockbox.domain.service.UserService;
import com.lockbox.dto.PasswordCreationDto;
import com.lockbox.mapper.PasswordMapper;

import jakarta.validation.Valid;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/passwords")
public class PasswordViewController {

    private static final Logger logger = LoggerFactory.getLogger(PasswordViewController.class);
    private final PasswordService passwordService;
    private final UserService userService;
    private final AuditLogService auditLogService;
    private final CategoryService categoryService;
    private final TagService tagService;
    private final PasswordMapper passwordMapper;

    @Autowired
    public PasswordViewController(PasswordService passwordService, UserService userService, 
                                AuditLogService auditLogService, CategoryService categoryService,
                                TagService tagService, PasswordMapper passwordMapper) {
        this.passwordService = passwordService;
        this.userService = userService;
        this.auditLogService = auditLogService;
        this.categoryService = categoryService;
        this.tagService = tagService;
        this.passwordMapper = passwordMapper;
    }

    @GetMapping("/new")
    public String showCreateForm(Model model, Authentication authentication) {
        var user = userService.findByUsername(authentication.getName()).orElseThrow();
        
        // Create new DTO
        PasswordCreationDto passwordDto = new PasswordCreationDto();
        passwordDto.setUserId(user.getId());
        
        // Add categories to the form
        List<Category> categories = categoryService.findByUserId(user.getId());
        model.addAttribute("categories", categories);
        
        model.addAttribute("password", passwordDto);
        model.addAttribute("currentUserId", user.getId());
        return "passwords/password-form";
    }

    @PostMapping("/save")
    @Transactional
    public String savePassword(@Valid @ModelAttribute("password") PasswordCreationDto passwordDto, 
                             BindingResult bindingResult, 
                             Authentication authentication, 
                             Model model, 
                             RedirectAttributes redirectAttributes) {
        // Get the authenticated user
        User user = userService.findByUsername(authentication.getName()).orElseThrow();
        
        model.addAttribute("currentUserId", user.getId());
        
        // Return to form with validation errors
        if (bindingResult.hasErrors()) {
            logger.warn("Validation errors occurred: {}", bindingResult.getAllErrors());
            // Add categories to the form for redisplay
            List<Category> categories = categoryService.findByUserId(user.getId());
            model.addAttribute("categories", categories);
            return "passwords/password-form";
        }
        
        try {
            // Set the user ID
            passwordDto.setUserId(user.getId());
            
            boolean isNew = passwordDto.getId() == null;
            Password password;
            
            if (isNew) {
                // Create new password
                password = passwordMapper.toEntity(passwordDto);
                password.setUser(user);
                password.setCreatedAt(LocalDateTime.now());
            } else {
                // Edit existing password
                password = passwordService.findByIdWithTags(passwordDto.getId())
                    .orElseThrow(() -> new IllegalArgumentException("Password not found with ID: " + passwordDto.getId()));
                
                // Security check - ensure user owns this password
                if (!password.getUser().getId().equals(user.getId())) {
                    throw new SecurityException("Not authorized to edit this password");
                }
                
                // Update fields from DTO
                password.setTitle(passwordDto.getTitle());
                password.setUsername(passwordDto.getUsername());
                password.setUrl(passwordDto.getWebsiteUrl());
                password.setNotes(passwordDto.getNotes());
                
                // Set category if provided
                if (passwordDto.getCategoryId() != null) {
                    Category category = categoryService.findById(passwordDto.getCategoryId())
                        .orElseThrow(() -> new IllegalArgumentException("Category not found with ID: " + passwordDto.getCategoryId()));
                    password.setCategory(category);
                } else {
                    password.setCategory(null);
                }
                
                // Update password if provided
                if (passwordDto.getPassword() != null && !passwordDto.getPassword().isEmpty()) {
                    password.setPasswordValue(passwordDto.getPassword());
                    
                    // Check password strength but don't block submission
                    String strengthWarning = checkPasswordStrength(passwordDto.getPassword());
                    if (strengthWarning != null) {
                        redirectAttributes.addFlashAttribute("passwordWarning", strengthWarning);
                    }
                }
                
                // Update tags
                password.getTags().clear();
                if (passwordDto.getTagIds() != null && !passwordDto.getTagIds().isEmpty()) {
                    Set<Tag> tags = passwordDto.getTagIds().stream()
                        .map(tagId -> tagService.findById(tagId)
                            .orElseThrow(() -> new IllegalArgumentException("Tag not found with ID: " + tagId)))
                        .collect(Collectors.toSet());
                    password.setTags(tags);
                }
            }
            
            // Always update the modified timestamp
            password.setUpdatedAt(LocalDateTime.now());
            
            Password savedPassword = passwordService.save(password);
            
            // Log the activity
            String action = isNew ? "PASSWORD_CREATED" : "PASSWORD_UPDATED";
            auditLogService.createAuditLog(
                user.getId(),
                action,
                "Password",
                savedPassword.getId(),
                "Title: " + savedPassword.getTitle()
            );
            
            redirectAttributes.addFlashAttribute("success", 
                isNew ? "Password created successfully." : "Password updated successfully.");
            
            return "redirect:/passwords";
        } catch (Exception e) {
            logger.error("Error saving password", e);
            model.addAttribute("error", "An error occurred: " + e.getMessage());
            // Add categories to the form for redisplay
            List<Category> categories = categoryService.findByUserId(user.getId());
            model.addAttribute("categories", categories);
            return "passwords/password-form";
        }
    }
    
    private boolean passwordWasChanged(Password password) {
        if (password.getId() == null) {
            return true; // New password
        }
        
        Password existingPassword = passwordService.findById(password.getId()).orElse(null);
        if (existingPassword == null) {
            return true; // Can't find existing, treat as new
        }
        
        return !Objects.equals(existingPassword.getPasswordValue(), password.getPasswordValue());
    }
    
    /**
     * Checks password strength and returns a warning message if the password is weak
     * @param password the password to check
     * @return a warning message if password is weak, null if password is strong enough
     */
    private String checkPasswordStrength(String password) {
        // If password is empty (when editing and not changing password), skip validation
        if (password == null || password.isEmpty()) {
            return null;
        }
        
        // Check minimum length
        if (password.length() < 8) {
            return "Warning: Your password is less than 8 characters long. Consider using a longer password for better security.";
        }
        
        // Check for complexity (at least 3 out of 4 categories)
        int categories = 0;
        if (password.matches(".*[a-z].*")) categories++; // lowercase
        if (password.matches(".*[A-Z].*")) categories++; // uppercase
        if (password.matches(".*[0-9].*")) categories++; // digits
        if (password.matches(".*[^a-zA-Z0-9].*")) categories++; // special chars
        
        if (categories < 3) {
            return "Warning: Your password could be stronger. Consider using a mix of lowercase letters, uppercase letters, numbers, and special characters.";
        }
        
        return null; // No warning needed
    }
    
    // Keep the old validatePasswordStrength method for now for backward compatibility
    private void validatePasswordStrength(String password, BindingResult bindingResult) {
        // If password is empty (when editing and not changing password), skip validation
        if (password == null || password.isEmpty()) {
            return;
        }
        
        // Check minimum length
        if (password.length() < 8) {
            bindingResult.rejectValue("password", "password.tooWeak", 
                "Password must be at least 8 characters long");
            return;
        }
        
        // Check for complexity (at least 3 out of 4 categories)
        int categories = 0;
        if (password.matches(".*[a-z].*")) categories++; // lowercase
        if (password.matches(".*[A-Z].*")) categories++; // uppercase
        if (password.matches(".*[0-9].*")) categories++; // digits
        if (password.matches(".*[^a-zA-Z0-9].*")) categories++; // special chars
        
        if (categories < 3) {
            bindingResult.rejectValue("password", "password.tooWeak", 
                "Password must contain at least 3 of the following: lowercase letters, uppercase letters, numbers, and special characters");
        }
    }

    @GetMapping("/edit/{id}")
    @Transactional(readOnly = true)
    public String showEditForm(@PathVariable Long id, Model model, Authentication authentication, RedirectAttributes redirectAttributes) {
        // Get the authenticated user
        var authenticatedUser = userService.findByUsername(authentication.getName()).orElseThrow();
        
        try {
            // Get the password with tags
            Password password = passwordService.findByIdWithTags(id)
                .orElseThrow(() -> new IllegalArgumentException("Invalid password Id:" + id));
                
            // Security check - ensure user owns this password by comparing IDs (no lazy loading issue)
            if (!password.getUser().getId().equals(authenticatedUser.getId())) {
                throw new SecurityException("Not authorized to edit this password");
            }
            
            // Convert to DTO
            PasswordCreationDto passwordDto = passwordMapper.toCreationDto(password);
            
            // Add categories to the form
            List<Category> categories = categoryService.findByUserId(authenticatedUser.getId());
            model.addAttribute("categories", categories);
            
            model.addAttribute("password", passwordDto);
            model.addAttribute("currentUserId", authenticatedUser.getId());
            return "passwords/password-form";
        } catch (Exception e) {
            logger.error("Error retrieving password for editing", e);
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/passwords";
        }
    }

    @GetMapping
    public String listPasswords(Model model, Authentication authentication) {
        var user = userService.findByUsername(authentication.getName()).orElseThrow();
        
        // Get user's passwords
        List<Password> passwords = passwordService.findByUserId(user.getId());
        model.addAttribute("passwords", passwords);
        
        // Get categories for filtering
        List<Category> categories = categoryService.findByUserId(user.getId());
        model.addAttribute("categories", categories);
        
        return "passwords/password-list";
    }

    @ExceptionHandler(SecurityException.class)
    public String handleSecurityException(SecurityException e, RedirectAttributes redirectAttributes) {
        logger.error("Security exception", e);
        redirectAttributes.addFlashAttribute("error", e.getMessage());
        return "redirect:/passwords";
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public String handleIllegalArgumentException(IllegalArgumentException e, RedirectAttributes redirectAttributes) {
        logger.error("Illegal argument exception", e);
        redirectAttributes.addFlashAttribute("error", e.getMessage());
        return "redirect:/passwords";
    }
} 