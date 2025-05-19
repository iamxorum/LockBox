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
        if (appName != null && appName.contains("LockBox")) {
            // Extract organization name from app name
            return appName.replace("LockBox", "").trim();
        }
        return null;
    }
} 