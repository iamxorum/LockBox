package com.lockbox.service;

import com.lockbox.model.AuditLog;

import java.util.List;

public interface AuditLogService extends CrudService<AuditLog, Long> {
    
    List<AuditLog> findByUserId(Long userId);
    
    AuditLog createAuditLog(Long userId, String action, String entityType, Long entityId, String details);
} 