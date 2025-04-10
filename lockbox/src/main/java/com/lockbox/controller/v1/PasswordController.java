package com.lockbox.controller.v1;

import com.lockbox.controller.BaseController;
import com.lockbox.dto.ApiResponse;
import com.lockbox.dto.PasswordCreationDto;
import com.lockbox.dto.PasswordDto;
import com.lockbox.mapper.PasswordMapper;
import com.lockbox.model.Category;
import com.lockbox.model.Password;
import com.lockbox.model.User;
import com.lockbox.service.CategoryService;
import com.lockbox.service.PasswordService;
import com.lockbox.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.lockbox.exception.ResourceNotFoundException;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/passwords")
@io.swagger.v3.oas.annotations.tags.Tag(name = "Password", description = "Password management APIs")
public class PasswordController extends BaseController {

    private final PasswordService passwordService;
    private final UserService userService;
    private final CategoryService categoryService;
    private final PasswordMapper passwordMapper;

    @Autowired
    public PasswordController(PasswordService passwordService, UserService userService,
                              CategoryService categoryService, PasswordMapper passwordMapper) {
        this.passwordService = passwordService;
        this.userService = userService;
        this.categoryService = categoryService;
        this.passwordMapper = passwordMapper;
    }

    @GetMapping("/user/{userId}")
    @Operation(summary = "Get user passwords", description = "Retrieves all passwords for a specific user")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Passwords found",
                    content = @Content(schema = @Schema(implementation = PasswordDtoListResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "User not found")
    })
    public ResponseEntity<ApiResponse<List<PasswordDto>>> getUserPasswords(
            @Parameter(description = "User ID", required = true) @PathVariable Long userId) {
        
        getUserOrThrow(userId);
        
        List<Password> passwords = passwordService.findByUserId(userId);
        List<PasswordDto> passwordDtos = passwords.stream()
                .map(passwordMapper::toDto)
                .collect(Collectors.toList());
        
        return ResponseEntity.ok(ApiResponse.success("Passwords retrieved successfully", passwordDtos));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get password by ID", description = "Retrieves a password by its ID")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Password found",
                    content = @Content(schema = @Schema(implementation = PasswordDtoResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Password not found")
    })
    public ResponseEntity<ApiResponse<PasswordDto>> getPasswordById(
            @Parameter(description = "Password ID", required = true) @PathVariable Long id) {
        
        Password password = getPasswordOrThrow(id);
        PasswordDto passwordDto = passwordMapper.toDto(password);
        
        return ResponseEntity.ok(ApiResponse.success("Password retrieved successfully", passwordDto));
    }

    @PostMapping("/user/{userId}")
    @Operation(summary = "Create password", description = "Creates a new password for a specific user")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "Password created",
                    content = @Content(schema = @Schema(implementation = PasswordDtoResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Invalid input"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "User or category not found")
    })
    public ResponseEntity<ApiResponse<PasswordDto>> createPassword(
            @Parameter(description = "User ID", required = true) @PathVariable Long userId,
            @Parameter(description = "Password creation data", required = true)
            @Valid @RequestBody PasswordCreationDto passwordCreationDto) {
        
        User user = getUserOrThrow(userId);
        
        // Create password
        Password password = passwordMapper.toEntity(passwordCreationDto);
        password.setUser(user);
        
        // Set category if provided
        if (passwordCreationDto.getCategoryId() != null) {
            Category category = categoryService.findById(passwordCreationDto.getCategoryId())
                    .orElseThrow(() -> new ResourceNotFoundException("Category", "id", passwordCreationDto.getCategoryId()));
            
            // Verify that the category belongs to the user
            if (!category.getUser().getId().equals(userId)) {
                throw new ResourceNotFoundException("Category", "id", passwordCreationDto.getCategoryId());
            }
            
            passwordMapper.setCategoryInPassword(password, category);
        }
        
        // Save password
        Password savedPassword = passwordService.save(password);
        PasswordDto savedPasswordDto = passwordMapper.toDto(savedPassword);
        
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Password created successfully", savedPasswordDto));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete password", description = "Deletes a password by its ID")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Password deleted"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Password not found")
    })
    public ResponseEntity<ApiResponse<Void>> deletePassword(
            @Parameter(description = "Password ID", required = true) @PathVariable Long id) {
        
        Password password = getPasswordOrThrow(id);
        passwordService.delete(password);
        
        return ResponseEntity.ok(ApiResponse.success("Password deleted successfully"));
    }

    private User getUserOrThrow(Long userId) {
        return userService.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));
    }

    private Password getPasswordOrThrow(Long id) {
        return passwordService.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Password", "id", id));
    }

    @SuppressWarnings("unused")
    private static class PasswordDtoResponse extends ApiResponse<PasswordDto> {
        public PasswordDtoResponse() {
            super(true, "Password retrieved successfully");
        }
    }

    @SuppressWarnings("unused")
    private static class PasswordDtoListResponse extends ApiResponse<List<PasswordDto>> {
        public PasswordDtoListResponse() {
            super(true, "Passwords retrieved successfully");
        }
    }
} 