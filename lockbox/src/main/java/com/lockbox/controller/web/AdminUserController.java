package com.lockbox.controller.web;

import com.lockbox.domain.model.User;
import com.lockbox.domain.service.UserService;
import com.lockbox.dto.UserCreationDto;
import com.lockbox.mapper.UserMapper;

import org.springframework.beans.factory.annotation.Autowired;
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
import java.util.Optional;

@Controller
@RequestMapping("/users")
@PreAuthorize("hasRole('ADMIN')")
public class AdminUserController {

    private final UserService userService;
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;

    @Autowired
    public AdminUserController(
            UserService userService,
            UserMapper userMapper,
            PasswordEncoder passwordEncoder) {
        this.userService = userService;
        this.userMapper = userMapper;
        this.passwordEncoder = passwordEncoder;
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
            RedirectAttributes redirectAttributes) {
        
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
            // Create user
            User user = userMapper.toEntity(userDto);
            user.setPassword(passwordEncoder.encode(userDto.getPassword()));
            // Regular users only, not admins
            user.addRole("USER");
            userService.save(user);
            
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
            RedirectAttributes redirectAttributes) {
        
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
            // Update basic info
            existingUser.setUsername(userDto.getUsername());
            existingUser.setEmail(userDto.getEmail());
            existingUser.setFirstName(userDto.getFirstName());
            existingUser.setLastName(userDto.getLastName());
            
            // Update password if provided
            if (userDto.getPassword() != null && !userDto.getPassword().isEmpty()) {
                existingUser.setPassword(passwordEncoder.encode(userDto.getPassword()));
            }
            
            userService.save(existingUser);
            
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
            userService.deleteById(id);
            redirectAttributes.addFlashAttribute("successMessage", "User deleted successfully");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Error deleting user: " + e.getMessage());
        }
        
        return "redirect:/users";
    }
} 