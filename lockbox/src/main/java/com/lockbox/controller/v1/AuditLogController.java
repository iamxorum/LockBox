package com.lockbox.controller.v1;

import com.lockbox.controller.BaseController;
import com.lockbox.dto.ApiResponse;
import com.lockbox.dto.AuditLogDto;
import com.lockbox.mapper.AuditLogMapper;
import com.lockbox.service.AuditLogService;
import com.lockbox.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/audit-logs")
@Tag(name = "Audit Log", description = "Audit Log management APIs")
public class AuditLogController extends BaseController {

    private final AuditLogService auditLogService;
    private final UserService userService;
    private final AuditLogMapper auditLogMapper;

    @Autowired
    public AuditLogController(AuditLogService auditLogService, UserService userService, 
                              AuditLogMapper auditLogMapper) {
        this.auditLogService = auditLogService;
        this.userService = userService;
        this.auditLogMapper = auditLogMapper;
    }

    @GetMapping("/user/{userId}")
    @Operation(summary = "Get user audit logs", description = "Retrieves all audit logs for a specific user")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Audit logs found",
                    content = @Content(schema = @Schema(implementation = AuditLogDtoListResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "User not found")
    })
    public ResponseEntity<ApiResponse<List<AuditLogDto>>> getUserAuditLogs(
            @Parameter(description = "User ID", required = true) @PathVariable Long userId) {
        if (!userService.existsById(userId)) {
            return notFoundResponse("User not found with ID: " + userId);
        }
        
        List<AuditLogDto> auditLogs = auditLogService.findByUserId(userId).stream()
                .map(auditLogMapper::toDto)
                .collect(Collectors.toList());
        return successResponse(auditLogs, "Audit logs retrieved successfully");
    }

    @GetMapping("/user/{userId}/time-range")
    @Operation(summary = "Get user audit logs by time range", 
            description = "Retrieves audit logs for a specific user within a time range")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Audit logs found",
                    content = @Content(schema = @Schema(implementation = AuditLogDtoListResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Invalid input"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "User not found")
    })
    public ResponseEntity<ApiResponse<List<AuditLogDto>>> getUserAuditLogsByTimeRange(
            @Parameter(description = "User ID", required = true) @PathVariable Long userId,
            @Parameter(description = "Start time (ISO format)", required = true)
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startTime,
            @Parameter(description = "End time (ISO format)", required = true)
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endTime) {
        
        if (!userService.existsById(userId)) {
            return notFoundResponse("User not found with ID: " + userId);
        }
        
        if (endTime.isBefore(startTime)) {
            return badRequestResponse("End time must be after start time");
        }
        
        List<AuditLogDto> auditLogs = auditLogService.findByUserIdAndTimeRange(userId, startTime, endTime).stream()
                .map(auditLogMapper::toDto)
                .collect(Collectors.toList());
        return successResponse(auditLogs, "Audit logs retrieved successfully");
    }

    @GetMapping("/user/{userId}/action/{action}")
    @Operation(summary = "Get user audit logs by action", 
            description = "Retrieves audit logs for a specific user and action")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Audit logs found",
                    content = @Content(schema = @Schema(implementation = AuditLogDtoListResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "User not found")
    })
    public ResponseEntity<ApiResponse<List<AuditLogDto>>> getUserAuditLogsByAction(
            @Parameter(description = "User ID", required = true) @PathVariable Long userId,
            @Parameter(description = "Action", required = true) @PathVariable String action) {
        
        if (!userService.existsById(userId)) {
            return notFoundResponse("User not found with ID: " + userId);
        }
        
        List<AuditLogDto> auditLogs = auditLogService.findByUserIdAndAction(userId, action).stream()
                .map(auditLogMapper::toDto)
                .collect(Collectors.toList());
        return successResponse(auditLogs, "Audit logs retrieved successfully");
    }

    // Schema classes for Swagger documentation
    @SuppressWarnings("unused")
    private static class AuditLogDtoResponse extends ApiResponse<AuditLogDto> {
        public AuditLogDtoResponse() {
            super(true, "");
        }
    }
    
    @SuppressWarnings("unused")
    private static class AuditLogDtoListResponse extends ApiResponse<List<AuditLogDto>> {
        public AuditLogDtoListResponse() {
            super(true, "");
        }
    }
} 