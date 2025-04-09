package com.lockbox.controller.web;

import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.Marker;
import org.slf4j.MarkerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import com.lockbox.aspect.Audited;

@Controller
public class AuthController {

    private static final Logger log = LoggerFactory.getLogger(AuthController.class);
    private static final Marker AUDIT = MarkerFactory.getMarker("AUDIT");

    @GetMapping("/login")
    @Audited(action = "LOGIN_PAGE_ACCESS", description = "User accessed login page")
    public String login(HttpServletRequest request,
                        @RequestParam(required = false) String error, 
                        @RequestParam(required = false) String logout) {
        
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
        
        return "login";
    }
} 