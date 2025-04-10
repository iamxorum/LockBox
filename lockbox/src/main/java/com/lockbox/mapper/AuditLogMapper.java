package com.lockbox.mapper;

import com.lockbox.domain.model.AuditLog;
import com.lockbox.domain.model.User;
import com.lockbox.dto.AuditLogCreationDto;
import com.lockbox.dto.AuditLogDto;

import org.springframework.stereotype.Component;

@Component
public class AuditLogMapper {
    
    public AuditLogDto toDto(AuditLog auditLog) {
        if (auditLog == null) {
            return null;
        }
        
        AuditLogDto dto = new AuditLogDto();
        dto.setId(auditLog.getId());
        dto.setAction(auditLog.getAction());
        dto.setEntityType(auditLog.getEntityType());
        dto.setEntityId(auditLog.getEntityId());
        dto.setDetails(auditLog.getDetails());
        dto.setTimestamp(auditLog.getTimestamp());
        
        if (auditLog.getUser() != null) {
            dto.setUserId(auditLog.getUser().getId());
            dto.setUsername(auditLog.getUser().getUsername());
        }
        
        return dto;
    }
    
    public AuditLog toEntity(AuditLogCreationDto dto) {
        if (dto == null) {
            return null;
        }
        
        AuditLog auditLog = new AuditLog();
        auditLog.setAction(dto.getAction());
        auditLog.setEntityType(dto.getEntityType());
        auditLog.setEntityId(dto.getEntityId());
        auditLog.setDetails(dto.getDetails());
        
        return auditLog;
    }
    
    public void updateEntityFromDto(AuditLogCreationDto dto, AuditLog auditLog) {
        if (dto == null || auditLog == null) {
            return;
        }
        
        if (dto.getAction() != null) {
            auditLog.setAction(dto.getAction());
        }
        
        if (dto.getEntityType() != null) {
            auditLog.setEntityType(dto.getEntityType());
        }
        
        if (dto.getEntityId() != null) {
            auditLog.setEntityId(dto.getEntityId());
        }
        
        if (dto.getDetails() != null) {
            auditLog.setDetails(dto.getDetails());
        }
    }
    
    public void setUserInAuditLog(AuditLog auditLog, User user) {
        if (auditLog != null && user != null) {
            auditLog.setUser(user);
        }
    }
} 