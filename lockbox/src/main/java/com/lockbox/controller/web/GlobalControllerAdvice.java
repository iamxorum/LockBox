package com.lockbox.controller.web;

import com.lockbox.domain.service.AppSettingsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

@ControllerAdvice
public class GlobalControllerAdvice {

    private final AppSettingsService appSettingsService;

    @Autowired
    public GlobalControllerAdvice(AppSettingsService appSettingsService) {
        this.appSettingsService = appSettingsService;
    }

    @ModelAttribute("organization")
    public String addOrganization() {
        String appName = appSettingsService.getAppSettings().getAppName();
        
        // Extract organization name from app name
        if (appName != null && appName.startsWith("LockBox")) {
            // Format is expected to be "LockBox OrgName"
            String orgName = appName.substring("LockBox".length()).trim();
            return orgName.isEmpty() ? null : orgName;
        }
        
        return null;
    }
} 