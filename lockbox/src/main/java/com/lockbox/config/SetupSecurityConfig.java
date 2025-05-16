package com.lockbox.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

import com.lockbox.domain.service.AppSettingsService;

@Configuration
@EnableWebSecurity
public class SetupSecurityConfig {

    private final AppSettingsService appSettingsService;

    @Autowired
    public SetupSecurityConfig(AppSettingsService appSettingsService) {
        this.appSettingsService = appSettingsService;
    }

    @Bean
    @Order(1) // Makes this security filter chain take precedence over the others
    public SecurityFilterChain setupSecurityFilterChain(HttpSecurity http) throws Exception {
        // Only apply this to setup-related paths
        http.securityMatcher(new AntPathRequestMatcher("/setup/**"));
        
        // Disable CSRF for the setup form to make it easier
        http.csrf(csrf -> csrf.disable());
        
        // Setup access logic depends on whether setup is completed
        http.authorizeHttpRequests(auth -> {
            // If setup has not been completed, allow access
            // If setup has already been completed, redirect to login
            if (!appSettingsService.isSetupCompleted()) {
                auth.anyRequest().permitAll();
            } else {
                auth.anyRequest().authenticated();
            }
        });
        
        return http.build();
    }
} 