package com.lockbox.controller.web;

import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.Marker;
import org.slf4j.MarkerFactory;
import org.springframework.boot.web.servlet.error.ErrorController;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
public class CustomErrorController implements ErrorController {
    
    private static final Logger log = LoggerFactory.getLogger(CustomErrorController.class);
    private static final Marker API_FAILED = MarkerFactory.getMarker("API_FAILED");
    private static final Marker AUDIT = MarkerFactory.getMarker("AUDIT");
    
    @RequestMapping("/error")
    public String handleError(HttpServletRequest request, Model model) {
        Object status = request.getAttribute(RequestDispatcher.ERROR_STATUS_CODE);
        Object path = request.getAttribute(RequestDispatcher.ERROR_REQUEST_URI);
        Object exception = request.getAttribute(RequestDispatcher.ERROR_EXCEPTION);
        Object message = request.getAttribute(RequestDispatcher.ERROR_MESSAGE);
        
        String errorMessage = "An error occurred";
        if (message != null) {
            errorMessage = message.toString();
        } else if (exception != null) {
            errorMessage = exception.toString();
        }
        
        int statusCode = 500;
        if (status != null) {
            statusCode = Integer.parseInt(status.toString());
            model.addAttribute("status", statusCode);
        }
        
        model.addAttribute("error", errorMessage);
        
        if (exception != null) {
            model.addAttribute("exception", exception.toString());
            log.error(API_FAILED, "🔴 Exception: {}", exception);
        }
        
        log.error(API_FAILED, "🔴 Error occurred - Path: {}, Status: {}, Message: {}", 
                path != null ? path : "unknown", 
                status != null ? status : "unknown", 
                errorMessage);
        
        log.error(AUDIT, "❌ Error accessed - Path: {}, Status: {}", 
                path != null ? path : "unknown", 
                status != null ? status : "unknown");
        
        return "errors/error";
    }
} 