package com.lockbox.domain.service;

import java.util.List;

import com.lockbox.domain.model.AuditLog;

public interface AuditLogService extends CrudService<AuditLog, Long> {
    
    List<AuditLog> findByUserId(Long userId);
    
    AuditLog createAuditLog(Long userId, String action, String entityType, Long entityId, String details);
} 