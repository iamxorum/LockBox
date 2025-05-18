package com.lockbox.domain.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.lockbox.domain.model.AuditLog;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface AuditLogRepository extends JpaRepository<AuditLog, Long> {
    
    @Query("SELECT a FROM AuditLog a JOIN FETCH a.user WHERE a.user.id = :userId")
    List<AuditLog> findByUserId(@Param("userId") Long userId);
    
    @Query("SELECT a FROM AuditLog a JOIN FETCH a.user WHERE a.user.id = :userId ORDER BY a.timestamp DESC")
    List<AuditLog> findByUserIdOrderByTimestampDesc(@Param("userId") Long userId);
    
    @Query(nativeQuery = true, value = "SELECT * FROM audit_logs a WHERE a.user_id = :userId ORDER BY a.timestamp DESC LIMIT :limit")
    List<AuditLog> findTopByUserIdOrderByTimestampDesc(@Param("userId") Long userId, @Param("limit") int limit);
    
    @Query("SELECT a FROM AuditLog a JOIN FETCH a.user WHERE a.user.id = :userId AND a.timestamp BETWEEN :start AND :end")
    List<AuditLog> findByUserIdAndTimestampBetween(
            @Param("userId") Long userId, 
            @Param("start") LocalDateTime start, 
            @Param("end") LocalDateTime end);
    
    @Query("SELECT a FROM AuditLog a JOIN FETCH a.user WHERE a.user.id = :userId AND a.action = :action")
    List<AuditLog> findByUserIdAndAction(@Param("userId") Long userId, @Param("action") String action);
    
    @Query("SELECT a FROM AuditLog a JOIN FETCH a.user WHERE a.user.id = :userId AND a.entityType = :entityType AND a.entityId = :entityId")
    List<AuditLog> findByUserIdAndEntityTypeAndEntityId(
            @Param("userId") Long userId, 
            @Param("entityType") String entityType, 
            @Param("entityId") Long entityId);
} 