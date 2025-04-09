package com.lockbox.controller.web;

import com.lockbox.model.AuditLog;
import com.lockbox.model.Password;
import com.lockbox.model.SecureNote;
import com.lockbox.model.User;
import com.lockbox.service.AuditLogService;
import com.lockbox.service.PasswordService;
import com.lockbox.service.SecureNoteService;
import com.lockbox.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/dashboard")
public class DashboardController {

    private final UserService userService;
    private final PasswordService passwordService;
    private final SecureNoteService secureNoteService;
    private final AuditLogService auditLogService;

    @Autowired
    public DashboardController(UserService userService, 
                              PasswordService passwordService,
                              SecureNoteService secureNoteService,
                              AuditLogService auditLogService) {
        this.userService = userService;
        this.passwordService = passwordService;
        this.secureNoteService = secureNoteService;
        this.auditLogService = auditLogService;
    }

    @GetMapping
    public String dashboard(Model model, Authentication authentication) {
        // Get current user
        User user = userService.findByUsername(authentication.getName()).orElseThrow();
        model.addAttribute("user", user);
        
        // Get user's passwords
        List<Password> passwords = passwordService.findByUserId(user.getId());
        model.addAttribute("passwords", passwords);
        model.addAttribute("passwordCount", passwords.size());
        
        // Get user's secure notes
        List<SecureNote> secureNotes = secureNoteService.findByUserId(user.getId());
        model.addAttribute("secureNotes", secureNotes);
        model.addAttribute("secureNoteCount", secureNotes.size());
        
        // Get user's recent activity
        List<AuditLog> recentActivity = auditLogService.findByUserId(user.getId());
        model.addAttribute("recentActivity", recentActivity);
        
        // Add security overview stats
        Map<String, Object> securityStats = new HashMap<>();
        securityStats.put("weakPasswordCount", countWeakPasswords(passwords));
        securityStats.put("reusedPasswordCount", countReusedPasswords(passwords));
        model.addAttribute("securityStats", securityStats);
        
        return "dashboard";
    }
    
    private int countWeakPasswords(List<Password> passwords) {
        // Simple implementation: count passwords less than 8 chars
        return (int) passwords.stream()
            .filter(p -> p.getPasswordValue() != null && p.getPasswordValue().length() < 8)
            .count();
    }
    
    private int countReusedPasswords(List<Password> passwords) {
        // Simple implementation: count duplicated password values
        return (int) (passwords.size() - passwords.stream()
            .map(Password::getPasswordValue)
            .distinct()
            .count());
    }
} 