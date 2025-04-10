package com.lockbox.controller.web;

import com.lockbox.domain.model.Category;
import com.lockbox.domain.model.Password;
import com.lockbox.domain.model.User;
import com.lockbox.domain.service.AuditLogService;
import com.lockbox.domain.service.CategoryService;
import com.lockbox.domain.service.PasswordService;
import com.lockbox.domain.service.UserService;

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

@Controller
@RequestMapping("/passwords")
public class PasswordViewController {

    private static final Logger logger = LoggerFactory.getLogger(PasswordViewController.class);
    private final PasswordService passwordService;
    private final UserService userService;
    private final AuditLogService auditLogService;
    private final CategoryService categoryService;

    @Autowired
    public PasswordViewController(PasswordService passwordService, UserService userService, AuditLogService auditLogService, CategoryService categoryService) {
        this.passwordService = passwordService;
        this.userService = userService;
        this.auditLogService = auditLogService;
        this.categoryService = categoryService;
    }

    @GetMapping("/new")
    public String showCreateForm(Model model, Authentication authentication) {
        Password password = new Password();
        var user = userService.findByUsername(authentication.getName()).orElseThrow();
        
        // Set the user directly to ensure proper binding
        password.setUser(user);
        
        // Initialize timestamps to prevent binding errors
        LocalDateTime now = LocalDateTime.now();
        password.setCreatedAt(now);
        password.setUpdatedAt(now);
        
        // Add categories to the form
        List<Category> categories = categoryService.findByUserId(user.getId());
        model.addAttribute("categories", categories);
        
        model.addAttribute("password", password);
        model.addAttribute("currentUserId", user.getId());
        return "passwords/password-form";
    }

    @PostMapping("/save")
    @Transactional
    public String savePassword(@Valid @ModelAttribute Password password, BindingResult bindingResult, 
                               Authentication authentication, Model model, RedirectAttributes redirectAttributes) {
        // Get the authenticated user
        User user = userService.findByUsername(authentication.getName()).orElseThrow();
        
        model.addAttribute("currentUserId", user.getId());
        
        // Return to form with validation errors
        if (bindingResult.hasErrors()) {
            logger.warn("Validation errors occurred: {}", bindingResult.getAllErrors());
            return "passwords/password-form";
        }
        
        try {
            if (password.getUser() == null) {
                password.setUser(user);
            }
            
            if (!user.getId().equals(password.getUser().getId())) {
                throw new SecurityException("Not authorized to save this password");
            }
            
            boolean isNew = password.getId() == null;
            if (isNew || passwordWasChanged(password)) {
                validatePasswordStrength(password.getPasswordValue(), bindingResult);
                if (bindingResult.hasErrors()) {
                    return "passwords/password-form";
                }
            }
            
            LocalDateTime now = LocalDateTime.now();
            
            if (isNew) {
                password.setCreatedAt(now);
            } else if (password.getCreatedAt() == null) {
                Password existingPassword = passwordService.findById(password.getId())
                    .orElseThrow(() -> new IllegalArgumentException("Password not found with ID: " + password.getId()));
                password.setCreatedAt(existingPassword.getCreatedAt());
            }
            
            password.setUpdatedAt(now);
            
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
    
    private void validatePasswordStrength(String password, BindingResult bindingResult) {
        // Check minimum length
        if (password.length() < 8) {
            bindingResult.rejectValue("passwordValue", "password.tooWeak", 
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
            bindingResult.rejectValue("passwordValue", "password.tooWeak", 
                "Password must contain at least 3 of the following: lowercase letters, uppercase letters, numbers, and special characters");
        }
    }

    @GetMapping("/edit/{id}")
    @Transactional(readOnly = true)
    public String showEditForm(@PathVariable Long id, Model model, Authentication authentication, RedirectAttributes redirectAttributes) {
        // Get the authenticated user
        var authenticatedUser = userService.findByUsername(authentication.getName()).orElseThrow();
        
        try {
            // Get the password
            Password password = passwordService.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Invalid password Id:" + id));
                
            // Security check - ensure user owns this password by comparing IDs (no lazy loading issue)
            if (!password.getUser().getId().equals(authenticatedUser.getId())) {
                throw new SecurityException("Not authorized to edit this password");
            }
            
            // Add categories to the form
            List<Category> categories = categoryService.findByUserId(authenticatedUser.getId());
            model.addAttribute("categories", categories);
            
            model.addAttribute("password", password);
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