package com.lockbox.domain.service.impl;

import com.lockbox.domain.repository.AuditLogRepository;
import com.lockbox.domain.repository.UserRepository;
import com.lockbox.domain.model.AuditLog;
import com.lockbox.domain.model.User;
import com.lockbox.domain.service.AuditLogService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@Transactional(readOnly = true)
public class AuditLogServiceImpl implements AuditLogService {

    private final AuditLogRepository auditLogRepository;
    private final UserRepository userRepository;
    
    @Autowired
    public AuditLogServiceImpl(AuditLogRepository auditLogRepository, UserRepository userRepository) {
        this.auditLogRepository = auditLogRepository;
        this.userRepository = userRepository;
    }
    
    @Override
    public List<AuditLog> findAll() {
        return auditLogRepository.findAll();
    }
    
    @Override
    public Optional<AuditLog> findById(Long id) {
        return auditLogRepository.findById(id);
    }
    
    @Override
    @Transactional
    public AuditLog save(AuditLog auditLog) {
        return auditLogRepository.save(auditLog);
    }
    
    @Override
    @Transactional
    public void delete(AuditLog auditLog) {
        auditLogRepository.delete(auditLog);
    }
    
    @Override
    @Transactional
    public void deleteById(Long id) {
        auditLogRepository.deleteById(id);
    }
    
    @Override
    public boolean existsById(Long id) {
        return auditLogRepository.existsById(id);
    }
    
    @Override
    public List<AuditLog> findByUserId(Long userId) {
        return auditLogRepository.findByUserId(userId);
    }
    
    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public AuditLog createAuditLog(Long userId, String action, String entityType, Long entityId, String details) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found with ID: " + userId));
        
        AuditLog auditLog = new AuditLog();
        auditLog.setUser(user);
        auditLog.setAction(action);
        auditLog.setEntityType(entityType);
        auditLog.setEntityId(entityId);
        auditLog.setDetails(details);
        auditLog.setTimestamp(LocalDateTime.now());
        
        return auditLogRepository.save(auditLog);
    }
} 