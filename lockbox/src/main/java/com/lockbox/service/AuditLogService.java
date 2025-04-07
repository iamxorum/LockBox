package com.lockbox.service;

import com.lockbox.model.AuditLog;

import java.time.LocalDateTime;
import java.util.List;

public interface AuditLogService extends CrudService<AuditLog, Long> {
    
    List<AuditLog> findByUserId(Long userId);
    
    List<AuditLog> findByUserIdAndTimeRange(Long userId, LocalDateTime start, LocalDateTime end);
    
    List<AuditLog> findByUserIdAndAction(Long userId, String action);

    AuditLog createAuditLog(Long userId, String action, String entityType, Long entityId, String details);
} 