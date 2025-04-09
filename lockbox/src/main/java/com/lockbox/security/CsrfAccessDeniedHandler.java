package com.lockbox.security;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.Marker;
import org.slf4j.MarkerFactory;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.security.web.csrf.InvalidCsrfTokenException;
import org.springframework.security.web.csrf.MissingCsrfTokenException;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class CsrfAccessDeniedHandler implements AccessDeniedHandler {

    private static final Logger log = LoggerFactory.getLogger(CsrfAccessDeniedHandler.class);
    private static final Marker SECURITY = MarkerFactory.getMarker("SECURITY");
    private static final Marker AUDIT = MarkerFactory.getMarker("AUDIT");

    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response,
                       AccessDeniedException accessDeniedException) throws IOException, ServletException {
        
        if (accessDeniedException instanceof InvalidCsrfTokenException) {
            log.warn(SECURITY, "🛡️ Invalid CSRF token detected: {} for URI: {}", 
                    accessDeniedException.getMessage(), request.getRequestURI());
            log.warn(AUDIT, "⚠️ CSRF attack attempt detected from IP: {}", request.getRemoteAddr());
        } 
        else if (accessDeniedException instanceof MissingCsrfTokenException) {
            log.warn(SECURITY, "🛡️ Missing CSRF token: {} for URI: {}", 
                    accessDeniedException.getMessage(), request.getRequestURI());
            log.warn(AUDIT, "⚠️ Possible CSRF attack attempt (missing token) from IP: {}", request.getRemoteAddr());
        } 
        else {
            log.warn(SECURITY, "🛡️ Access denied: {} for URI: {}", 
                    accessDeniedException.getMessage(), request.getRequestURI());
            log.warn(AUDIT, "⚠️ Access denied for user from IP: {}", request.getRemoteAddr());
        }
        
        // Forward to the /error endpoint to display our custom error page
        request.setAttribute("javax.servlet.error.status_code", HttpServletResponse.SC_FORBIDDEN);
        request.getRequestDispatcher("/error").forward(request, response);
    }
} 