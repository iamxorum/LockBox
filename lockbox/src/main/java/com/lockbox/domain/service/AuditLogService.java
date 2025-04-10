package com.lockbox.domain.service;

import java.util.List;

import com.lockbox.domain.model.AuditLog;

public interface AuditLogService extends CrudService<AuditLog, Long> {
    
    List<AuditLog> findByUserId(Long userId);
    
    List<AuditLog> findRecentByUserId(Long userId, int limit);
    
    AuditLog createAuditLog(Long userId, String action, String entityType, Long entityId, String details);
} 