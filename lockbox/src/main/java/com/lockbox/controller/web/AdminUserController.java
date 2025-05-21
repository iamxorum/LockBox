package com.lockbox.controller.web;

import com.lockbox.domain.model.User;
import com.lockbox.domain.service.AuditLogService;
import com.lockbox.domain.service.UserService;
import com.lockbox.dto.UserCreationDto;
import com.lockbox.mapper.UserMapper;
import com.lockbox.security.SecurityEventListener.AuditActions;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import jakarta.validation.Valid;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Controller
@RequestMapping("/users")
@PreAuthorize("hasRole('ADMIN')")
public class AdminUserController {

    private final UserService userService;
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;
    private final AuditLogService auditLogService;

    @Autowired
    public AdminUserController(
            UserService userService,
            UserMapper userMapper,
            PasswordEncoder passwordEncoder,
            AuditLogService auditLogService) {
        this.userService = userService;
        this.userMapper = userMapper;
        this.passwordEncoder = passwordEncoder;
        this.auditLogService = auditLogService;
    }

    @GetMapping
    @Transactional(readOnly = true)
    public String listUsers(Model model, Authentication authentication) {
        List<User> users = userService.findAll();
        User currentUser = userService.findByUsername(authentication.getName()).orElseThrow();
        
        model.addAttribute("users", users);
        model.addAttribute("currentUser", currentUser);
        return "users/user-list";
    }
    
    @GetMapping("/new")
    public String createUserForm(Model model) {
        model.addAttribute("user", new UserCreationDto());
        return "users/user-form";
    }
    
    @PostMapping
    @Transactional
    public String createUser(
            @Valid @ModelAttribute("user") UserCreationDto userDto,
            BindingResult bindingResult,
            RedirectAttributes redirectAttributes,
            Authentication authentication) {
        
        if (bindingResult.hasErrors()) {
            return "users/user-form";
        }
        
        // Check if username or email already exists
        if (userService.existsByUsername(userDto.getUsername())) {
            bindingResult.rejectValue("username", "error.user", "Username already exists");
            return "users/user-form";
        }
        
        if (userService.existsByEmail(userDto.getEmail())) {
            bindingResult.rejectValue("email", "error.user", "Email already exists");
            return "users/user-form";
        }
        
        try {
            // Get current admin user for audit logging
            User adminUser = userService.findByUsername(authentication.getName()).orElseThrow();
            
            // Create user
            User user = userMapper.toEntity(userDto);
            user.setPassword(passwordEncoder.encode(userDto.getPassword()));
            // Regular users only, not admins
            user.addRole("USER");
            User savedUser = userService.save(user);
            
            // Log the user creation for audit
            auditLogService.createAuditLog(
                adminUser.getId(),
                AuditActions.USER_CREATED,
                "User",
                savedUser.getId(),
                "User created: " + savedUser.getUsername()
            );
            
            redirectAttributes.addFlashAttribute("successMessage", "User created successfully");
            return "redirect:/users";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Error creating user: " + e.getMessage());
            return "redirect:/users/new";
        }
    }
    
    @GetMapping("/{id}/edit")
    @Transactional(readOnly = true)
    public String editUserForm(@PathVariable("id") Long id, Model model, Authentication authentication) {
        Optional<User> userOpt = userService.findById(id);
        
        if (userOpt.isEmpty()) {
            return "redirect:/users";
        }
        
        User user = userOpt.get();
        // Convert to DTO to avoid exposing sensitive information
        UserCreationDto userDto = userMapper.toCreationDto(user);
        // Don't expose password
        userDto.setPassword("");
        
        model.addAttribute("user", userDto);
        model.addAttribute("userId", id);
        
        return "users/user-edit";
    }
    
    @PostMapping("/{id}")
    @Transactional
    public String updateUser(
            @PathVariable("id") Long id,
            @Valid @ModelAttribute("user") UserCreationDto userDto,
            BindingResult bindingResult,
            RedirectAttributes redirectAttributes,
            Authentication authentication) {
        
        if (bindingResult.hasErrors()) {
            return "users/user-edit";
        }
        
        Optional<User> userOpt = userService.findById(id);
        if (userOpt.isEmpty()) {
            redirectAttributes.addFlashAttribute("errorMessage", "User not found");
            return "redirect:/users";
        }
        
        User existingUser = userOpt.get();
        
        // Check if username already exists (but ignore if it's the same user)
        if (!existingUser.getUsername().equals(userDto.getUsername()) && 
            userService.existsByUsername(userDto.getUsername())) {
            bindingResult.rejectValue("username", "error.user", "Username already exists");
            return "users/user-edit";
        }
        
        // Check if email already exists (but ignore if it's the same user)
        if (!existingUser.getEmail().equals(userDto.getEmail()) && 
            userService.existsByEmail(userDto.getEmail())) {
            bindingResult.rejectValue("email", "error.user", "Email already exists");
            return "users/user-edit";
        }
        
        try {
            // Get current admin user for audit logging
            User adminUser = userService.findByUsername(authentication.getName()).orElseThrow();
            
            // Store original username for audit
            String originalUsername = existingUser.getUsername();
            
            // Update basic info
            existingUser.setUsername(userDto.getUsername());
            existingUser.setEmail(userDto.getEmail());
            existingUser.setFirstName(userDto.getFirstName());
            existingUser.setLastName(userDto.getLastName());
            
            // Update password if provided
            boolean passwordChanged = false;
            if (userDto.getPassword() != null && !userDto.getPassword().isEmpty()) {
                existingUser.setPassword(passwordEncoder.encode(userDto.getPassword()));
                passwordChanged = true;
            }
            
            userService.save(existingUser);
            
            // Log the user update for audit
            String details = "User updated: " + originalUsername;
            if (passwordChanged) {
                details += " (password was changed)";
            }
            auditLogService.createAuditLog(
                adminUser.getId(),
                AuditActions.USER_UPDATED,
                "User",
                existingUser.getId(),
                details
            );
            
            redirectAttributes.addFlashAttribute("successMessage", "User updated successfully");
            return "redirect:/users";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Error updating user: " + e.getMessage());
            return "redirect:/users/" + id + "/edit";
        }
    }
    
    @PostMapping("/{id}/delete")
    @Transactional
    public String deleteUser(
            @PathVariable("id") Long id,
            Authentication authentication,
            RedirectAttributes redirectAttributes) {
        
        // Get current user
        User currentUser = userService.findByUsername(authentication.getName()).orElseThrow();
        
        // Prevent admins from deleting themselves
        if (currentUser.getId().equals(id)) {
            redirectAttributes.addFlashAttribute("errorMessage", "You cannot delete your own account");
            return "redirect:/users";
        }
        
        try {
            // Get user information before deletion
            User userToDelete = userService.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
            String deletedUsername = userToDelete.getUsername();
            
            // Delete the user
            userService.deleteById(id);
            
            // Log the user deletion for audit
            auditLogService.createAuditLog(
                currentUser.getId(),
                AuditActions.USER_DELETED,
                "User",
                id,
                "User deleted: " + deletedUsername
            );
            
            redirectAttributes.addFlashAttribute("successMessage", "User deleted successfully");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Error deleting user: " + e.getMessage());
        }
        
        return "redirect:/users";
    }

    @PostMapping("/{id}/current-password")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> getCurrentPassword(
            @PathVariable("id") Long id,
            @RequestBody Map<String, String> request,
            Authentication authentication) {
        
        // Get current admin user
        User currentUser = userService.findByUsername(authentication.getName()).orElseThrow();
        
        // Verify the provided password matches the admin's password
        if (!passwordEncoder.matches(request.get("password"), currentUser.getPassword())) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(Map.of(
                    "success", false,
                    "message", "Invalid administrator password"
                ));
        }
        
        // Get target user
        Optional<User> targetUserOpt = userService.findById(id);
        if (targetUserOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(Map.of(
                    "success", false,
                    "message", "User not found"
                ));
        }
        
        // Log the password view for audit
        auditLogService.createAuditLog(
            currentUser.getId(),
            AuditActions.PASSWORD_VIEWED,
            "User",
            id,
            "Administrator viewed user password hash: " + targetUserOpt.get().getUsername()
        );
        
        return ResponseEntity.ok(Map.of(
            "success", true,
            "password", targetUserOpt.get().getPassword()
        ));
    }
} 