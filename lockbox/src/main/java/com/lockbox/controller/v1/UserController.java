package com.lockbox.controller.v1;

import com.lockbox.controller.BaseController;
import com.lockbox.domain.model.User;
import com.lockbox.domain.service.AuditLogService;
import com.lockbox.domain.service.UserService;
import com.lockbox.dto.ApiResponse;
import com.lockbox.dto.UserCreationDto;
import com.lockbox.dto.UserDto;
import com.lockbox.dto.UserStatsDto;
import com.lockbox.mapper.UserMapper;
import com.lockbox.security.SecurityEventListener.AuditActions;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import com.lockbox.exception.ResourceAlreadyExistsException;
import com.lockbox.exception.ResourceNotFoundException;

import java.util.List;

@RestController
@RequestMapping("/api/v1/users")
@io.swagger.v3.oas.annotations.tags.Tag(name = "User", description = "User management APIs")
public class UserController extends BaseController {

    private final UserService userService;
    private final UserMapper userMapper;
    private final AuditLogService auditLogService;

    @Autowired
    public UserController(UserService userService, UserMapper userMapper, AuditLogService auditLogService) {
        this.userService = userService;
        this.userMapper = userMapper;
        this.auditLogService = auditLogService;
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get user by ID", description = "Retrieves a user by their ID")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "User found",
                    content = @Content(schema = @Schema(implementation = UserDtoResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "User not found")
    })
    public ResponseEntity<ApiResponse<UserDto>> getUserById(
            @Parameter(description = "User ID", required = true) @PathVariable Long id) {
        
        User user = getUserOrThrow(id);
        UserDto userDto = userMapper.toDto(user);
        
        // Log the user view for audit
        logAction(AuditActions.USER_VIEWED, "User", id, "API: User viewed: " + user.getUsername());
        
        return ResponseEntity.ok(ApiResponse.success("User retrieved successfully", userDto));
    }

    @PostMapping
    @Operation(summary = "Create user", description = "Creates a new user")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "User created",
                    content = @Content(schema = @Schema(implementation = UserDtoResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Invalid input"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "409", description = "User already exists")
    })
    public ResponseEntity<ApiResponse<UserDto>> createUser(
            @Parameter(description = "User creation data", required = true)
            @Valid @RequestBody UserCreationDto userCreationDto) {
        
        // Check if username already exists
        if (userService.existsByUsername(userCreationDto.getUsername())) {
            throw new ResourceAlreadyExistsException("User", "username", userCreationDto.getUsername());
        }
        
        // Check if email already exists
        if (userService.existsByEmail(userCreationDto.getEmail())) {
            throw new ResourceAlreadyExistsException("User", "email", userCreationDto.getEmail());
        }
        
        // Create user
        User user = userMapper.toEntity(userCreationDto);
        
        // Save user
        User savedUser = userService.save(user);
        UserDto savedUserDto = userMapper.toDto(savedUser);
        
        // Log the user creation for audit
        logAction(AuditActions.USER_CREATED, "User", savedUser.getId(), 
                "API: User created: " + savedUser.getUsername());
        
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("User created successfully", savedUserDto));
    }

    @GetMapping("/{id}/stats")
    @Operation(summary = "Get user statistics", description = "Retrieves statistics about a user's data")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Statistics retrieved",
                    content = @Content(schema = @Schema(implementation = UserStatsDto.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "User not found")
    })
    public ResponseEntity<ApiResponse<UserStatsDto>> getUserStats(
            @Parameter(description = "User ID", required = true) @PathVariable Long id) {
        
        // Get the user to include in the audit log
        User user = getUserOrThrow(id);
        
        UserStatsDto stats = userService.getUserStats(id);
        
        // Log the stats view for audit
        logAction(AuditActions.SECURITY_SCAN_PERFORMED, "User", id,
                "API: User statistics viewed for: " + user.getUsername());
        
        return ResponseEntity.ok(ApiResponse.success("User statistics retrieved successfully", stats));
    }
    
    @DeleteMapping("/{id}")
    @Operation(summary = "Delete user", description = "Deletes a user by their ID")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "User deleted"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "User not found")
    })
    public ResponseEntity<ApiResponse<Void>> deleteUser(
            @Parameter(description = "User ID", required = true) @PathVariable Long id) {
        
        // Get the user to include in the audit log
        User user = getUserOrThrow(id);
        String username = user.getUsername();
        
        // Delete the user
        userService.deleteById(id);
        
        // Log the user deletion for audit
        logAction(AuditActions.USER_DELETED, "User", id, 
                "API: User deleted: " + username);
        
        return ResponseEntity.ok(ApiResponse.success("User deleted successfully"));
    }
    
    @PutMapping("/{id}")
    @Operation(summary = "Update user", description = "Updates a user by their ID")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "User updated"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "User not found"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "409", description = "Username or email already in use")
    })
    public ResponseEntity<ApiResponse<UserDto>> updateUser(
            @Parameter(description = "User ID", required = true) @PathVariable Long id,
            @Parameter(description = "User update data", required = true)
            @Valid @RequestBody UserCreationDto userUpdateDto) {
        
        // Get the user to update
        User existingUser = getUserOrThrow(id);
        String originalUsername = existingUser.getUsername();
        
        // Check if username already exists (but ignore if it's the same user)
        if (!existingUser.getUsername().equals(userUpdateDto.getUsername()) && 
            userService.existsByUsername(userUpdateDto.getUsername())) {
            throw new ResourceAlreadyExistsException("User", "username", userUpdateDto.getUsername());
        }
        
        // Check if email already exists (but ignore if it's the same user)
        if (!existingUser.getEmail().equals(userUpdateDto.getEmail()) && 
            userService.existsByEmail(userUpdateDto.getEmail())) {
            throw new ResourceAlreadyExistsException("User", "email", userUpdateDto.getEmail());
        }
        
        // Update user fields
        userMapper.updateEntityFromDto(userUpdateDto, existingUser);
        
        // Save updated user
        User updatedUser = userService.save(existingUser);
        UserDto updatedUserDto = userMapper.toDto(updatedUser);
        
        // Log the user update for audit
        String details = "API: User updated: " + originalUsername;
        if (!originalUsername.equals(updatedUser.getUsername())) {
            details += ", new username: " + updatedUser.getUsername();
        }
        logAction(AuditActions.USER_UPDATED, "User", id, details);
        
        return ResponseEntity.ok(ApiResponse.success("User updated successfully", updatedUserDto));
    }

    private User getUserOrThrow(Long id) {
        return userService.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", id));
    }
    
    private void logAction(String action, String entityType, Long entityId, String details) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && !authentication.getName().equals("anonymousUser")) {
            User currentUser = userService.findByUsername(authentication.getName()).orElse(null);
            if (currentUser != null) {
                auditLogService.createAuditLog(currentUser.getId(), action, entityType, entityId, details);
            }
        }
    }

    @SuppressWarnings("unused")
    private static class UserDtoResponse extends ApiResponse<UserDto> {
        public UserDtoResponse() {
            super(true, "User retrieved successfully");
        }
    }

    @SuppressWarnings("unused")
    private static class UserDtoListResponse extends ApiResponse<List<UserDto>> {
        public UserDtoListResponse() {
            super(true, "Users retrieved successfully");
        }
    }
} 