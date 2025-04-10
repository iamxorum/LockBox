package com.lockbox.domain.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.lockbox.domain.model.LoginAttempt;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface LoginAttemptRepository extends JpaRepository<LoginAttempt, Long> {
    
    List<LoginAttempt> findByUsername(String username);
    
    List<LoginAttempt> findByUsernameAndSuccessful(String username, boolean successful);
    
    List<LoginAttempt> findByUsernameAndTimestampBetween(String username, LocalDateTime start, LocalDateTime end);
    
    @Query("SELECT COUNT(la) FROM LoginAttempt la WHERE la.username = ?1 AND la.successful = false AND la.timestamp > ?2")
    int countFailedAttemptsInTimeframe(String username, LocalDateTime since);
    
    List<LoginAttempt> findByIpAddress(String ipAddress);
} 