package com.lockbox.controller.web;

import com.lockbox.domain.service.AppSettingsService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.Marker;
import org.slf4j.MarkerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.servlet.ModelAndView;

@Controller
public class WelcomeController {

    private static final Logger log = LoggerFactory.getLogger(WelcomeController.class);
    private static final Marker AUDIT = MarkerFactory.getMarker("AUDIT");
    
    private final AppSettingsService appSettingsService;
    
    @Autowired
    public WelcomeController(AppSettingsService appSettingsService) {
        this.appSettingsService = appSettingsService;
    }
    
    @GetMapping("/")
    public ModelAndView welcome() {
        // If setup is not completed, redirect to setup page
        if (!appSettingsService.isSetupCompleted()) {
            log.info("Application setup not completed, redirecting to setup page");
            return new ModelAndView("redirect:/setup");
        }
        
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        
        if (auth != null && auth.isAuthenticated() && !"anonymousUser".equals(auth.getName())) {
            log.info(AUDIT, "👤 Authenticated user {} accessed home page", auth.getName());
            return new ModelAndView("redirect:/dashboard");
        } else {
            log.info(AUDIT, "👤 Unauthenticated user accessed home page");
            return new ModelAndView("redirect:/login");
        }
    }
} 