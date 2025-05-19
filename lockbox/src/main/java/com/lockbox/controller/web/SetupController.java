package com.lockbox.controller.web;

import com.lockbox.domain.model.AppSettings;
import com.lockbox.domain.model.User;
import com.lockbox.domain.service.AppSettingsService;
import com.lockbox.domain.service.UserService;
import com.lockbox.dto.UserCreationDto;
import com.lockbox.mapper.UserMapper;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import jakarta.validation.Valid;

@Controller
@RequestMapping("/setup")
public class SetupController {

    private final AppSettingsService appSettingsService;
    private final UserService userService;
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;

    @Autowired
    public SetupController(
            AppSettingsService appSettingsService,
            UserService userService,
            UserMapper userMapper,
            PasswordEncoder passwordEncoder) {
        this.appSettingsService = appSettingsService;
        this.userService = userService;
        this.userMapper = userMapper;
        this.passwordEncoder = passwordEncoder;
    }

    @GetMapping
    public String setupForm(Model model) {
        // Redirect to login if setup is already completed
        if (appSettingsService.isSetupCompleted()) {
            return "redirect:/login";
        }

        // Add admin user creation form if not already in model
        if (!model.containsAttribute("adminUser")) {
            model.addAttribute("adminUser", new UserCreationDto());
        }
        
        // Add app settings form if not already in model
        if (!model.containsAttribute("appSettings")) {
            model.addAttribute("appSettings", appSettingsService.getAppSettings());
        }

        return "auth/setup";
    }

    @PostMapping
    public String processSetup(
            @Valid @ModelAttribute("adminUser") UserCreationDto adminUserDto,
            BindingResult adminUserBindingResult,
            @Valid @ModelAttribute("appSettings") AppSettings appSettings,
            BindingResult appSettingsBindingResult,
            RedirectAttributes redirectAttributes) {

        // Check if setup is already completed
        if (appSettingsService.isSetupCompleted()) {
            return "redirect:/login";
        }

        // Validate inputs
        if (adminUserBindingResult.hasErrors() || appSettingsBindingResult.hasErrors()) {
            redirectAttributes.addFlashAttribute("adminUser", adminUserDto);
            redirectAttributes.addFlashAttribute("appSettings", appSettings);
            redirectAttributes.addFlashAttribute("org.springframework.validation.BindingResult.adminUser", adminUserBindingResult);
            redirectAttributes.addFlashAttribute("org.springframework.validation.BindingResult.appSettings", appSettingsBindingResult);
            return "redirect:/setup";
        }

        // Check if username or email already exists
        if (userService.existsByUsername(adminUserDto.getUsername())) {
            adminUserBindingResult.rejectValue("username", "error.adminUser", "Username already exists");
            redirectAttributes.addFlashAttribute("adminUser", adminUserDto);
            redirectAttributes.addFlashAttribute("appSettings", appSettings);
            redirectAttributes.addFlashAttribute("org.springframework.validation.BindingResult.adminUser", adminUserBindingResult);
            return "redirect:/setup";
        }

        if (userService.existsByEmail(adminUserDto.getEmail())) {
            adminUserBindingResult.rejectValue("email", "error.adminUser", "Email already exists");
            redirectAttributes.addFlashAttribute("adminUser", adminUserDto);
            redirectAttributes.addFlashAttribute("appSettings", appSettings);
            redirectAttributes.addFlashAttribute("org.springframework.validation.BindingResult.adminUser", adminUserBindingResult);
            return "redirect:/setup";
        }

        try {
            // Create admin user
            User adminUser = userMapper.toEntity(adminUserDto);
            adminUser.setPassword(passwordEncoder.encode(adminUserDto.getPassword()));
            adminUser.addRole("ADMIN");
            userService.save(adminUser);

            // Update and save app settings
            appSettingsService.updateSettings(appSettings);
            appSettingsService.completeSetup();

            redirectAttributes.addFlashAttribute("setupSuccess", true);
            return "redirect:/login";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Error during setup: " + e.getMessage());
            redirectAttributes.addFlashAttribute("adminUser", adminUserDto);
            redirectAttributes.addFlashAttribute("appSettings", appSettings);
            return "redirect:/setup";
        }
    }
} 