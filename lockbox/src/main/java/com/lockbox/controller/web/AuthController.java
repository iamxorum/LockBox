package com.lockbox.controller.web;

import com.lockbox.domain.service.AppSettingsService;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.Marker;
import org.slf4j.MarkerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class AuthController {

    private static final Logger log = LoggerFactory.getLogger(AuthController.class);
    private static final Marker AUDIT = MarkerFactory.getMarker("AUDIT");
    
    private final AppSettingsService appSettingsService;
    
    @Autowired
    public AuthController(AppSettingsService appSettingsService) {
        this.appSettingsService = appSettingsService;
    }

    @GetMapping("/login")
    public String login(HttpServletRequest request,
                        @RequestParam(required = false) String error, 
                        @RequestParam(required = false) String logout) {
        
        // If setup is not completed, redirect to setup page
        if (!appSettingsService.isSetupCompleted()) {
            log.info("Application setup not completed, redirecting to setup page");
            return "redirect:/setup";
        }
        
        // Don't log for requests that might be for resources or AJAX calls
        String requestedWith = request.getHeader("X-Requested-With");
        boolean isAjax = "XMLHttpRequest".equals(requestedWith);
        String accept = request.getHeader("Accept");
        boolean isHtmlRequest = accept != null && accept.contains("text/html");
        
        // Only log for main page requests, not for potential resource requests
        if (!isAjax && isHtmlRequest) {
            if (error != null) {
                log.warn(AUDIT, "🔒 Login page accessed with error parameter");
            } else if (logout != null) {
                log.info(AUDIT, "👋 Login page accessed after logout");
            }
        }
        
        return "auth/login";
    }
} 