package com.lockbox.repository;

import com.lockbox.model.AuditLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface AuditLogRepository extends JpaRepository<AuditLog, Long> {
    
    List<AuditLog> findByUserId(Long userId);
    
    List<AuditLog> findByUserIdAndTimestampBetween(Long userId, LocalDateTime start, LocalDateTime end);
    
    List<AuditLog> findByUserIdAndAction(Long userId, String action);
    
    List<AuditLog> findByUserIdAndEntityTypeAndEntityId(Long userId, String entityType, Long entityId);
} 