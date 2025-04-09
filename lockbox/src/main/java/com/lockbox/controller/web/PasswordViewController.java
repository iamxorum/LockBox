package com.lockbox.controller.web;

import com.lockbox.model.Password;
import com.lockbox.service.PasswordService;
import com.lockbox.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/passwords")
public class PasswordViewController {

    private final PasswordService passwordService;
    private final UserService userService;

    @Autowired
    public PasswordViewController(PasswordService passwordService, UserService userService) {
        this.passwordService = passwordService;
        this.userService = userService;
    }

    @GetMapping("/new")
    public String showCreateForm(Model model, Authentication authentication) {
        Password password = new Password();
        var user = userService.findByUsername(authentication.getName()).orElseThrow();
        password.setUser(user);
        model.addAttribute("password", password);
        model.addAttribute("currentUserId", user.getId());
        return "password-form";
    }

    @GetMapping("/edit/{id}")
    @Transactional(readOnly = true)
    public String showEditForm(@PathVariable Long id, Model model, Authentication authentication) {
        // Get the authenticated user
        var authenticatedUser = userService.findByUsername(authentication.getName()).orElseThrow();
        
        // Get the password
        Password password = passwordService.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("Invalid password Id:" + id));
            
        // Security check - ensure user owns this password by comparing IDs (no lazy loading issue)
        if (!password.getUser().getId().equals(authenticatedUser.getId())) {
            throw new SecurityException("Not authorized to edit this password");
        }
        
        model.addAttribute("password", password);
        model.addAttribute("currentUserId", authenticatedUser.getId());
        return "password-form";
    }

    @GetMapping
    public String listPasswords(Model model, Authentication authentication) {
        var user = userService.findByUsername(authentication.getName()).orElseThrow();
        model.addAttribute("passwords", passwordService.findByUserId(user.getId()));
        return "password-list";
    }

    @ExceptionHandler(SecurityException.class)
    public String handleSecurityException(SecurityException e, RedirectAttributes redirectAttributes) {
        redirectAttributes.addFlashAttribute("error", e.getMessage());
        return "redirect:/passwords";
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public String handleIllegalArgumentException(IllegalArgumentException e, RedirectAttributes redirectAttributes) {
        redirectAttributes.addFlashAttribute("error", e.getMessage());
        return "redirect:/passwords";
    }
} 