package com.lockbox.controller.web;

import com.lockbox.domain.model.Password;
import com.lockbox.domain.model.User;
import com.lockbox.domain.service.BreachDetectionService;
import com.lockbox.domain.service.PasswordService;
import com.lockbox.domain.service.UserService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/security-report")
public class SecurityReportController {

    private final UserService userService;
    private final PasswordService passwordService;
    private final BreachDetectionService breachDetectionService;
    
    @Autowired
    public SecurityReportController(UserService userService, 
                                   PasswordService passwordService,
                                   BreachDetectionService breachDetectionService) {
        this.userService = userService;
        this.passwordService = passwordService;
        this.breachDetectionService = breachDetectionService;
    }
    
    @GetMapping
    public String securityReport(Model model, Authentication authentication) {
        // Get current user
        User user = userService.findByUsername(authentication.getName()).orElseThrow();
        model.addAttribute("user", user);
        
        // Get user's passwords
        List<Password> passwords = passwordService.findByUserId(user.getId());
        int totalPasswordCount = passwords.size();
        model.addAttribute("passwordCount", totalPasswordCount);
        
        // Find weak passwords (less than 8 chars)
        List<Password> weakPasswords = passwords.stream()
            .filter(p -> p.getPasswordValue() != null && p.getPasswordValue().length() < 8)
            .collect(Collectors.toList());
        model.addAttribute("weakPasswords", weakPasswords);
        
        // Find reused passwords
        Map<String, List<Password>> passwordGroups = passwords.stream()
            .filter(p -> p.getPasswordValue() != null && !p.getPasswordValue().isBlank())
            .collect(Collectors.groupingBy(Password::getPasswordValue));
        
        List<Map.Entry<String, List<Password>>> reusedPasswordGroups = passwordGroups.entrySet().stream()
            .filter(entry -> entry.getValue().size() > 1)
            .collect(Collectors.toList());
        
        model.addAttribute("reusedPasswordGroups", reusedPasswordGroups);
        
        // Check for compromised passwords using the free API
        List<Password> pwnedPasswords = new ArrayList<>();
        for (Password password : passwords) {
            if (password.getPasswordValue() != null && !password.getPasswordValue().isBlank()) {
                int occurrences = breachDetectionService.checkPasswordPwned(password.getPasswordValue());
                if (occurrences > 0) {
                    password.setMetadata("pwnedCount", String.valueOf(occurrences));
                    
                    // Determine risk level based on number of occurrences
                    String riskLevel;
                    if (occurrences > 1000000) {
                        riskLevel = "critical";
                    } else if (occurrences > 100000) {
                        riskLevel = "high";
                    } else if (occurrences > 10000) {
                        riskLevel = "medium";
                    } else if (occurrences > 1000) {
                        riskLevel = "low";
                    } else {
                        riskLevel = "minimal";
                    }
                    password.setMetadata("riskLevel", riskLevel);
                    
                    pwnedPasswords.add(password);
                }
            }
        }
        model.addAttribute("pwnedPasswords", pwnedPasswords);
        
        // Overall security score (simple implementation)
        int securityScore = calculateSecurityScore(
            passwords, 
            weakPasswords.size(), 
            reusedPasswordGroups.size(), 
            pwnedPasswords.size()
        );
        model.addAttribute("securityScore", securityScore);
        
        return "security/security-report";
    }
    
    private int calculateSecurityScore(List<Password> passwords, int weakPasswordCount, 
                                      int reusedPasswordGroupCount, int pwnedPasswordCount) {
        // No passwords is considered insecure
        if (passwords.isEmpty()) {
            return 0; // If no passwords, security score is 0
        }
        
        // Get total password count
        int totalCount = passwords.size();
        
        // Find all passwords with problems
        Set<String> weakPasswordValues = new HashSet<>();
        Set<String> pwnedPasswordValues = new HashSet<>();
        Set<String> reusedPasswordValues = new HashSet<>();
        
        // Find weak password values
        for (Password password : passwords) {
            if (password.getPasswordValue() != null && password.getPasswordValue().length() < 8) {
                weakPasswordValues.add(password.getPasswordValue());
            }
        }
        
        // Find pwned password values
        for (Password password : passwords) {
            if (password.getMetadata("pwnedCount") != null) {
                pwnedPasswordValues.add(password.getPasswordValue());
            }
        }
        
        // Find reused password values
        Map<String, Integer> passwordCounts = new HashMap<>();
        for (Password password : passwords) {
            if (password.getPasswordValue() != null) {
                String passwordValue = password.getPasswordValue();
                passwordCounts.put(passwordValue, passwordCounts.getOrDefault(passwordValue, 0) + 1);
            }
        }
        for (Map.Entry<String, Integer> entry : passwordCounts.entrySet()) {
            if (entry.getValue() > 1) {
                reusedPasswordValues.add(entry.getKey());
            }
        }
        
        // Create a combined set of all problem password values to avoid double counting
        Set<String> allProblemPasswordValues = new HashSet<>();
        allProblemPasswordValues.addAll(weakPasswordValues);
        allProblemPasswordValues.addAll(pwnedPasswordValues);
        allProblemPasswordValues.addAll(reusedPasswordValues);
        
        // Count good passwords (not weak, not pwned, not reused)
        int goodPasswordCount = 0;
        for (Password password : passwords) {
            if (password.getPasswordValue() != null && 
                !allProblemPasswordValues.contains(password.getPasswordValue())) {
                goodPasswordCount++;
            }
        }
        
        // Calculate the percentage of good passwords
        double goodPasswordPercentage = (double) goodPasswordCount / totalCount;
        
        // BALANCED APPROACH: Start with a score based on the percentage of good passwords
        int score = (int) (goodPasswordPercentage * 100);
        
        // Add a bonus for having more total passwords (up to 10 points)
        score += Math.min(10, totalCount);
        
        // Then apply strategic penalties for serious issues
        
        // Critical penalties for compromised passwords
        if (!pwnedPasswordValues.isEmpty()) {
            double pwnedPercentage = (double) pwnedPasswordValues.size() / totalCount;
            
            // Apply a penalty that's proportional to the percentage of pwned passwords
            int pwnedPenalty = (int) (pwnedPercentage * 50);
            score -= pwnedPenalty;
            
            // Additional fixed penalty for having ANY pwned passwords (they're serious)
            score -= 10;
        }
        
        // Penalties for password reuse
        if (!reusedPasswordValues.isEmpty()) {
            double reusedPercentage = (double) reusedPasswordValues.size() / totalCount;
            int reusedPenalty = (int) (reusedPercentage * 30);
            score -= reusedPenalty;
        }
        
        // Penalties for weak passwords
        if (!weakPasswordValues.isEmpty()) {
            double weakPercentage = (double) weakPasswordValues.size() / totalCount;
            int weakPenalty = (int) (weakPercentage * 20);
            score -= weakPenalty;
        }
        
        // Ensure the score stays within bounds
        score = Math.max(0, Math.min(100, score));
        
        // Special case: If ALL passwords are good, ensure a high score regardless of count
        if (goodPasswordCount == totalCount && totalCount > 0) {
            // Perfect score for all good passwords, with a minimum based on count
            score = Math.max(score, 90);
        }
        
        // Special case: If NO passwords are good, ensure a very low score
        if (goodPasswordCount == 0 && totalCount > 0) {
            // Cap at a very low score if no good passwords
            score = Math.min(score, 10);
        }
        
        return score;
    }
} 