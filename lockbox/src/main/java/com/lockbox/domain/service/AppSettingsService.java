package com.lockbox.domain.service;

import com.lockbox.domain.model.AppSettings;
import com.lockbox.domain.repository.AppSettingsRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
public class AppSettingsService {

    private final AppSettingsRepository appSettingsRepository;

    @Autowired
    public AppSettingsService(AppSettingsRepository appSettingsRepository) {
        this.appSettingsRepository = appSettingsRepository;
    }

    /**
     * Get current application settings
     * @return the AppSettings instance
     */
    public AppSettings getAppSettings() {
        AppSettings settings = appSettingsRepository.findFirstByOrderById();
        if (settings == null) {
            // Initialize with default settings if none exist
            settings = new AppSettings();
            settings = appSettingsRepository.save(settings);
        }
        return settings;
    }

    /**
     * Check if the application setup has been completed
     * @return true if setup is complete, false otherwise
     */
    public boolean isSetupCompleted() {
        return getAppSettings().isSetupCompleted();
    }

    /**
     * Mark the application setup as completed
     */
    @Transactional
    public void completeSetup() {
        AppSettings settings = getAppSettings();
        settings.setSetupCompleted(true);
        settings.setSetupCompletedAt(LocalDateTime.now());
        appSettingsRepository.save(settings);
    }

    /**
     * Update application settings
     * @param settings the updated settings
     * @return the updated AppSettings instance
     */
    @Transactional
    public AppSettings updateSettings(AppSettings settings) {
        AppSettings currentSettings = getAppSettings();
        
        // Update settings fields
        currentSettings.setAppName(settings.getAppName());
        currentSettings.setEncryptionAlgorithm(settings.getEncryptionAlgorithm());
        currentSettings.setSessionTimeoutMinutes(settings.getSessionTimeoutMinutes());
        
        return appSettingsRepository.save(currentSettings);
    }
} 