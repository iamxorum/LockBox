package com.lockbox.controller.v1;

import com.lockbox.controller.BaseController;
import com.lockbox.dto.ApiResponse;
import com.lockbox.dto.SecureNoteCreationDto;
import com.lockbox.dto.SecureNoteDto;
import com.lockbox.mapper.SecureNoteMapper;
import com.lockbox.model.Category;
import com.lockbox.model.SecureNote;
import com.lockbox.model.User;
import com.lockbox.service.CategoryService;
import com.lockbox.service.SecureNoteService;
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
@RequestMapping("/api/v1/secure-notes")
@io.swagger.v3.oas.annotations.tags.Tag(name = "Secure Note", description = "Secure Note management APIs")
public class SecureNoteController extends BaseController {

    private final SecureNoteService secureNoteService;
    private final UserService userService;
    private final CategoryService categoryService;
    private final SecureNoteMapper secureNoteMapper;

    @Autowired
    public SecureNoteController(SecureNoteService secureNoteService, UserService userService,
                                CategoryService categoryService, SecureNoteMapper secureNoteMapper) {
        this.secureNoteService = secureNoteService;
        this.userService = userService;
        this.categoryService = categoryService;
        this.secureNoteMapper = secureNoteMapper;
    }

    @GetMapping("/user/{userId}")
    @Operation(summary = "Get user secure notes", description = "Retrieves all secure notes for a specific user")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Secure notes found",
                    content = @Content(schema = @Schema(implementation = SecureNoteDtoListResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "User not found")
    })
    public ResponseEntity<ApiResponse<List<SecureNoteDto>>> getUserSecureNotes(
            @Parameter(description = "User ID", required = true) @PathVariable Long userId) {
        
        getUserOrThrow(userId);
        
        List<SecureNote> secureNotes = secureNoteService.findByUserId(userId);
        List<SecureNoteDto> secureNoteDtos = secureNotes.stream()
                .map(secureNoteMapper::toDto)
                .collect(Collectors.toList());
        
        return ResponseEntity.ok(ApiResponse.success("Secure notes retrieved successfully", secureNoteDtos));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get secure note by ID", description = "Retrieves a secure note by its ID")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Secure note found",
                    content = @Content(schema = @Schema(implementation = SecureNoteDtoResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Secure note not found")
    })
    public ResponseEntity<ApiResponse<SecureNoteDto>> getSecureNoteById(
            @Parameter(description = "Secure Note ID", required = true) @PathVariable Long id) {
        
        SecureNote secureNote = getSecureNoteOrThrow(id);
        SecureNoteDto secureNoteDto = secureNoteMapper.toDto(secureNote);
        
        return ResponseEntity.ok(ApiResponse.success("Secure note retrieved successfully", secureNoteDto));
    }

    @PostMapping("/user/{userId}")
    @Operation(summary = "Create secure note", description = "Creates a new secure note for a specific user")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "Secure note created",
                    content = @Content(schema = @Schema(implementation = SecureNoteDtoResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Invalid input"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "User or category not found")
    })
    public ResponseEntity<ApiResponse<SecureNoteDto>> createSecureNote(
            @Parameter(description = "User ID", required = true) @PathVariable Long userId,
            @Parameter(description = "Secure Note creation data", required = true)
            @Valid @RequestBody SecureNoteCreationDto secureNoteCreationDto) {
        
        User user = getUserOrThrow(userId);
        
        // Create secure note entity
        SecureNote secureNote = secureNoteMapper.toEntity(secureNoteCreationDto);
        secureNote.setUser(user);
        
        // Set category if provided
        if (secureNoteCreationDto.getCategoryId() != null) {
            Category category = categoryService.findById(secureNoteCreationDto.getCategoryId())
                    .orElseThrow(() -> new ResourceNotFoundException("Category", "id", secureNoteCreationDto.getCategoryId()));
            
            // Verify that the category belongs to the user
            if (!category.getUser().getId().equals(userId)) {
                throw new ResourceNotFoundException("Category", "id", secureNoteCreationDto.getCategoryId());
            }
            
            secureNoteMapper.setCategoryInSecureNote(secureNote, category);
        }
        
        // Save secure note
        SecureNote savedSecureNote = secureNoteService.save(secureNote);
        SecureNoteDto savedSecureNoteDto = secureNoteMapper.toDto(savedSecureNote);
        
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Secure note created successfully", savedSecureNoteDto));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete secure note", description = "Deletes a secure note by its ID")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Secure note deleted"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Secure note not found")
    })
    public ResponseEntity<ApiResponse<Void>> deleteSecureNote(
            @Parameter(description = "Secure Note ID", required = true) @PathVariable Long id) {
        
        SecureNote secureNote = getSecureNoteOrThrow(id);
        secureNoteService.delete(secureNote);
        
        return ResponseEntity.ok(ApiResponse.success("Secure note deleted successfully"));
    }

    private User getUserOrThrow(Long userId) {
        return userService.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));
    }

    private SecureNote getSecureNoteOrThrow(Long id) {
        return secureNoteService.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("SecureNote", "id", id));
    }

    @SuppressWarnings("unused")
    private static class SecureNoteDtoResponse extends ApiResponse<SecureNoteDto> {
        public SecureNoteDtoResponse() {
            super(true, "Secure note retrieved successfully");
        }
    }

    @SuppressWarnings("unused")
    private static class SecureNoteDtoListResponse extends ApiResponse<List<SecureNoteDto>> {
        public SecureNoteDtoListResponse() {
            super(true, "Secure notes retrieved successfully");
        }
    }
} 