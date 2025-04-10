package com.lockbox.controller.v1;

import com.lockbox.controller.BaseController;
import com.lockbox.domain.model.AuditLog;
import com.lockbox.domain.model.User;
import com.lockbox.domain.service.AuditLogService;
import com.lockbox.domain.service.UserService;
import com.lockbox.dto.ApiResponse;
import com.lockbox.dto.AuditLogDto;
import com.lockbox.mapper.AuditLogMapper;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.lockbox.exception.ResourceNotFoundException;

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
        
        getUserOrThrow(userId);
        
        List<AuditLog> auditLogs = auditLogService.findByUserId(userId);
        List<AuditLogDto> auditLogDtos = auditLogs.stream()
                .map(auditLogMapper::toDto)
                .collect(Collectors.toList());
        
        return ResponseEntity.ok(ApiResponse.success("Audit logs retrieved successfully", auditLogDtos));
    }

    private User getUserOrThrow(Long userId) {
        return userService.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));
    }

    @SuppressWarnings("unused")
    private static class AuditLogDtoListResponse extends ApiResponse<List<AuditLogDto>> {
        public AuditLogDtoListResponse() {
            super(true, "Audit logs retrieved successfully");
        }
    }
} 