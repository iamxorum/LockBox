package com.lockbox.controller.v1;

import com.lockbox.controller.BaseController;
import com.lockbox.dto.ApiResponse;
import com.lockbox.dto.PasswordCreationDto;
import com.lockbox.dto.PasswordDto;
import com.lockbox.mapper.PasswordMapper;
import com.lockbox.model.Category;
import com.lockbox.model.Password;
import com.lockbox.model.Tag;
import com.lockbox.model.User;
import com.lockbox.service.CategoryService;
import com.lockbox.service.PasswordService;
import com.lockbox.service.TagService;
import com.lockbox.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/passwords")
@io.swagger.v3.oas.annotations.tags.Tag(name = "Password", description = "Password management APIs")
public class PasswordController extends BaseController {

    private final PasswordService passwordService;
    private final UserService userService;
    private final CategoryService categoryService;
    private final TagService tagService;
    private final PasswordMapper passwordMapper;

    @Autowired
    public PasswordController(PasswordService passwordService, UserService userService,
                              CategoryService categoryService, TagService tagService,
                              PasswordMapper passwordMapper) {
        this.passwordService = passwordService;
        this.userService = userService;
        this.categoryService = categoryService;
        this.tagService = tagService;
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
        if (!userService.existsById(userId)) {
            return notFoundResponse("User not found with ID: " + userId);
        }
        
        List<PasswordDto> passwords = passwordService.findByUserId(userId).stream()
                .map(passwordMapper::toDto)
                .collect(Collectors.toList());
        return successResponse(passwords, "Passwords retrieved successfully");
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
        return passwordService.findById(id)
                .<ResponseEntity<ApiResponse<PasswordDto>>>map(password -> 
                    successResponse(passwordMapper.toDto(password), "Password retrieved successfully"))
                .orElse(notFoundResponse("Password not found with ID: " + id));
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
        
        // Check if user exists
        User user = userService.findById(userId)
                .orElse(null);
        if (user == null) {
            return notFoundResponse("User not found with ID: " + userId);
        }
        
        // Create password entity
        Password password = passwordMapper.toEntity(passwordCreationDto);
        password.setUser(user);
        
        // Set category if provided
        if (passwordCreationDto.getCategoryId() != null) {
            Category category = categoryService.findById(passwordCreationDto.getCategoryId())
                    .orElse(null);
            if (category == null) {
                return notFoundResponse("Category not found with ID: " + passwordCreationDto.getCategoryId());
            }
            password.setCategory(category);
        }
        
        // Save password
        final Password savedPassword = passwordService.save(password);
        
        // Add tags if provided
        if (passwordCreationDto.getTagIds() != null && !passwordCreationDto.getTagIds().isEmpty()) {
            for (Long tagId : passwordCreationDto.getTagIds()) {
                tagService.findById(tagId).ifPresent(tag -> {
                    passwordMapper.addTagToPassword(savedPassword, tag);
                });
            }
            // Save again with tags
            final Password updatedPassword = passwordService.save(savedPassword);
            return createdResponse(passwordMapper.toDto(updatedPassword), "Password created successfully");
        }
        
        return createdResponse(passwordMapper.toDto(savedPassword), "Password created successfully");
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update password", description = "Updates an existing password")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Password updated",
                    content = @Content(schema = @Schema(implementation = PasswordDtoResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Invalid input"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Password or category not found")
    })
    public ResponseEntity<ApiResponse<PasswordDto>> updatePassword(
            @Parameter(description = "Password ID", required = true) @PathVariable Long id,
            @Parameter(description = "Password update data", required = true)
            @Valid @RequestBody PasswordCreationDto passwordCreationDto) {
        
        return passwordService.findById(id)
                .<ResponseEntity<ApiResponse<PasswordDto>>>map(existingPassword -> {
                    // Update fields
                    passwordMapper.updateEntityFromDto(passwordCreationDto, existingPassword);
                    
                    // Update category if provided
                    if (passwordCreationDto.getCategoryId() != null) {
                        Category category = categoryService.findById(passwordCreationDto.getCategoryId())
                                .orElse(null);
                        if (category == null) {
                            return notFoundResponse("Category not found with ID: " + passwordCreationDto.getCategoryId());
                        }
                        existingPassword.setCategory(category);
                    }
                    
                    // Save password
                    Password updatedPassword = passwordService.save(existingPassword);
                    
                    return successResponse(passwordMapper.toDto(updatedPassword), "Password updated successfully");
                })
                .orElse(notFoundResponse("Password not found with ID: " + id));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete password", description = "Deletes a password by its ID")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Password deleted"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Password not found")
    })
    public ResponseEntity<ApiResponse<Void>> deletePassword(
            @Parameter(description = "Password ID", required = true) @PathVariable Long id) {
        
        return passwordService.findById(id)
                .<ResponseEntity<ApiResponse<Void>>>map(password -> {
                    passwordService.deleteById(id);
                    return successResponse("Password deleted successfully");
                })
                .orElse(notFoundResponse("Password not found with ID: " + id));
    }

    @PostMapping("/{id}/tags/{tagId}")
    @Operation(summary = "Add tag to password", description = "Adds a tag to a password")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Tag added to password",
                    content = @Content(schema = @Schema(implementation = PasswordDtoResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Password or tag not found")
    })
    public ResponseEntity<ApiResponse<PasswordDto>> addTagToPassword(
            @Parameter(description = "Password ID", required = true) @PathVariable Long id,
            @Parameter(description = "Tag ID", required = true) @PathVariable Long tagId) {
        
        // Check if password exists
        Password password = passwordService.findById(id)
                .orElse(null);
        if (password == null) {
            return notFoundResponse("Password not found with ID: " + id);
        }
        
        // Check if tag exists
        Tag tag = tagService.findById(tagId)
                .orElse(null);
        if (tag == null) {
            return notFoundResponse("Tag not found with ID: " + tagId);
        }
        
        // Add tag to password
        passwordMapper.addTagToPassword(password, tag);
        Password updatedPassword = passwordService.save(password);
        
        return successResponse(passwordMapper.toDto(updatedPassword), "Tag added to password successfully");
    }

    @DeleteMapping("/{id}/tags/{tagId}")
    @Operation(summary = "Remove tag from password", description = "Removes a tag from a password")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Tag removed from password",
                    content = @Content(schema = @Schema(implementation = PasswordDtoResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Password or tag not found")
    })
    public ResponseEntity<ApiResponse<PasswordDto>> removeTagFromPassword(
            @Parameter(description = "Password ID", required = true) @PathVariable Long id,
            @Parameter(description = "Tag ID", required = true) @PathVariable Long tagId) {
        
        // Check if password exists
        Password password = passwordService.findById(id)
                .orElse(null);
        if (password == null) {
            return notFoundResponse("Password not found with ID: " + id);
        }
        
        // Check if tag exists
        Tag tag = tagService.findById(tagId)
                .orElse(null);
        if (tag == null) {
            return notFoundResponse("Tag not found with ID: " + tagId);
        }
        
        // Remove tag from password
        passwordMapper.removeTagFromPassword(password, tag);
        Password updatedPassword = passwordService.save(password);
        
        return successResponse(passwordMapper.toDto(updatedPassword), "Tag removed from password successfully");
    }

    // Schema classes for Swagger documentation
    @SuppressWarnings("unused")
    private static class PasswordDtoResponse extends ApiResponse<PasswordDto> {
        public PasswordDtoResponse() {
            super(true, "");
        }
    }
    
    @SuppressWarnings("unused")
    private static class PasswordDtoListResponse extends ApiResponse<List<PasswordDto>> {
        public PasswordDtoListResponse() {
            super(true, "");
        }
    }
} 