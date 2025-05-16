package com.lockbox.controller.web;

import com.lockbox.domain.model.AuditLog;
import com.lockbox.domain.model.Password;
import com.lockbox.domain.model.SecureNote;
import com.lockbox.domain.model.User;
import com.lockbox.domain.service.AuditLogService;
import com.lockbox.domain.service.BreachDetectionService;
import com.lockbox.domain.service.PasswordService;
import com.lockbox.domain.service.SecureNoteService;
import com.lockbox.domain.service.UserService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ArrayList;

@Controller
@RequestMapping("/dashboard")
public class DashboardController {

    private final UserService userService;
    private final PasswordService passwordService;
    private final SecureNoteService secureNoteService;
    private final AuditLogService auditLogService;
    private final BreachDetectionService breachDetectionService;
    private static final int RECENT_ACTIVITY_LIMIT = 5;

    @Autowired
    public DashboardController(UserService userService, 
                              PasswordService passwordService,
                              SecureNoteService secureNoteService,
                              AuditLogService auditLogService,
                              BreachDetectionService breachDetectionService) {
        this.userService = userService;
        this.passwordService = passwordService;
        this.secureNoteService = secureNoteService;
        this.auditLogService = auditLogService;
        this.breachDetectionService = breachDetectionService;
    }

    @GetMapping
    @Transactional(readOnly = true)
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
        
        List<AuditLog> recentActivity = auditLogService.findRecentByUserId(user.getId(), RECENT_ACTIVITY_LIMIT);
        model.addAttribute("recentActivity", recentActivity);
        
        // Add security overview stats
        Map<String, Object> securityStats = new HashMap<>();
        securityStats.put("weakPasswordCount", countWeakPasswords(passwords));
        securityStats.put("reusedPasswordCount", countReusedPasswords(passwords));
        
        // Check for compromised passwords
        List<Password> pwnedPasswords = new ArrayList<>();
        for (Password password : passwords) {
            if (password.getPasswordValue() != null && !password.getPasswordValue().isBlank()) {
                int occurrences = breachDetectionService.checkPasswordPwned(password.getPasswordValue());
                if (occurrences > 0) {
                    pwnedPasswords.add(password);
                }
            }
        }
        securityStats.put("pwnedPasswordCount", pwnedPasswords.size());
        
        model.addAttribute("securityStats", securityStats);
        
        return "dashboard/dashboard";
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