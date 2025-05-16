package com.lockbox.domain.repository;

import com.lockbox.domain.model.AppSettings;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AppSettingsRepository extends JpaRepository<AppSettings, Long> {
    // Find the first app settings entry (should be only one)
    AppSettings findFirstByOrderById();
} 