package com.lockbox.controller.v1;

import com.lockbox.controller.BaseController;
import com.lockbox.dto.ApiResponse;
import com.lockbox.dto.SecureNoteCreationDto;
import com.lockbox.dto.SecureNoteDto;
import com.lockbox.mapper.SecureNoteMapper;
import com.lockbox.model.Category;
import com.lockbox.model.SecureNote;
import com.lockbox.model.Tag;
import com.lockbox.model.User;
import com.lockbox.service.CategoryService;
import com.lockbox.service.SecureNoteService;
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
@RequestMapping("/api/v1/secure-notes")
@io.swagger.v3.oas.annotations.tags.Tag(name = "Secure Note", description = "Secure Note management APIs")
public class SecureNoteController extends BaseController {

    private final SecureNoteService secureNoteService;
    private final UserService userService;
    private final CategoryService categoryService;
    private final TagService tagService;
    private final SecureNoteMapper secureNoteMapper;

    @Autowired
    public SecureNoteController(SecureNoteService secureNoteService, UserService userService,
                                CategoryService categoryService, TagService tagService,
                                SecureNoteMapper secureNoteMapper) {
        this.secureNoteService = secureNoteService;
        this.userService = userService;
        this.categoryService = categoryService;
        this.tagService = tagService;
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
        if (!userService.existsById(userId)) {
            return notFoundResponse("User not found with ID: " + userId);
        }
        
        List<SecureNoteDto> secureNotes = secureNoteService.findByUserId(userId).stream()
                .map(secureNoteMapper::toDto)
                .collect(Collectors.toList());
        return successResponse(secureNotes, "Secure notes retrieved successfully");
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
        return secureNoteService.findById(id)
                .map(secureNote -> successResponse(secureNoteMapper.toDto(secureNote), "Secure note retrieved successfully"))
                .orElse(notFoundResponse("Secure note not found with ID: " + id));
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
        
        // Check if user exists
        User user = userService.findById(userId)
                .orElse(null);
        if (user == null) {
            return notFoundResponse("User not found with ID: " + userId);
        }
        
        // Create secure note entity
        SecureNote secureNote = secureNoteMapper.toEntity(secureNoteCreationDto);
        secureNote.setUser(user);
        
        // Set category if provided
        if (secureNoteCreationDto.getCategoryId() != null) {
            Category category = categoryService.findById(secureNoteCreationDto.getCategoryId())
                    .orElse(null);
            if (category == null) {
                return notFoundResponse("Category not found with ID: " + secureNoteCreationDto.getCategoryId());
            }
            secureNote.setCategory(category);
        }
        
        // Save secure note
        final SecureNote savedSecureNote = secureNoteService.save(secureNote);
        
        // Add tags if provided
        if (secureNoteCreationDto.getTagIds() != null && !secureNoteCreationDto.getTagIds().isEmpty()) {
            for (Long tagId : secureNoteCreationDto.getTagIds()) {
                tagService.findById(tagId).ifPresent(tag -> {
                    secureNoteMapper.addTagToSecureNote(savedSecureNote, tag);
                });
            }
            // Save again with tags
            final SecureNote updatedSecureNote = secureNoteService.save(savedSecureNote);
            return createdResponse(secureNoteMapper.toDto(updatedSecureNote), "Secure note created successfully");
        }
        
        return createdResponse(secureNoteMapper.toDto(savedSecureNote), "Secure note created successfully");
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update secure note", description = "Updates an existing secure note")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Secure note updated",
                    content = @Content(schema = @Schema(implementation = SecureNoteDtoResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Invalid input"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Secure note or category not found")
    })
    public ResponseEntity<ApiResponse<SecureNoteDto>> updateSecureNote(
            @Parameter(description = "Secure Note ID", required = true) @PathVariable Long id,
            @Parameter(description = "Secure Note update data", required = true)
            @Valid @RequestBody SecureNoteCreationDto secureNoteCreationDto) {
        
        return secureNoteService.findById(id)
                .<ResponseEntity<ApiResponse<SecureNoteDto>>>map(existingSecureNote -> {
                    // Update fields
                    secureNoteMapper.updateEntityFromDto(secureNoteCreationDto, existingSecureNote);
                    
                    // Update category if provided
                    if (secureNoteCreationDto.getCategoryId() != null) {
                        Category category = categoryService.findById(secureNoteCreationDto.getCategoryId())
                                .orElse(null);
                        if (category == null) {
                            return notFoundResponse("Category not found with ID: " + secureNoteCreationDto.getCategoryId());
                        }
                        existingSecureNote.setCategory(category);
                    }
                    
                    // Save secure note
                    SecureNote updatedSecureNote = secureNoteService.save(existingSecureNote);
                    
                    return successResponse(secureNoteMapper.toDto(updatedSecureNote), "Secure note updated successfully");
                })
                .orElse(notFoundResponse("Secure note not found with ID: " + id));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete secure note", description = "Deletes a secure note by its ID")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Secure note deleted"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Secure note not found")
    })
    public ResponseEntity<ApiResponse<Void>> deleteSecureNote(
            @Parameter(description = "Secure Note ID", required = true) @PathVariable Long id) {
        
        return secureNoteService.findById(id)
                .<ResponseEntity<ApiResponse<Void>>>map(secureNote -> {
                    secureNoteService.deleteById(id);
                    return successResponse("Secure note deleted successfully");
                })
                .orElse(notFoundResponse("Secure note not found with ID: " + id));
    }

    @PostMapping("/{id}/tags/{tagId}")
    @Operation(summary = "Add tag to secure note", description = "Adds a tag to a secure note")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Tag added to secure note",
                    content = @Content(schema = @Schema(implementation = SecureNoteDtoResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Secure note or tag not found")
    })
    public ResponseEntity<ApiResponse<SecureNoteDto>> addTagToSecureNote(
            @Parameter(description = "Secure Note ID", required = true) @PathVariable Long id,
            @Parameter(description = "Tag ID", required = true) @PathVariable Long tagId) {
        
        // Check if secure note exists
        SecureNote secureNote = secureNoteService.findById(id)
                .orElse(null);
        if (secureNote == null) {
            return notFoundResponse("Secure note not found with ID: " + id);
        }
        
        // Check if tag exists
        Tag tag = tagService.findById(tagId)
                .orElse(null);
        if (tag == null) {
            return notFoundResponse("Tag not found with ID: " + tagId);
        }
        
        // Add tag to secure note
        secureNoteMapper.addTagToSecureNote(secureNote, tag);
        SecureNote updatedSecureNote = secureNoteService.save(secureNote);
        
        return successResponse(secureNoteMapper.toDto(updatedSecureNote), "Tag added to secure note successfully");
    }

    @DeleteMapping("/{id}/tags/{tagId}")
    @Operation(summary = "Remove tag from secure note", description = "Removes a tag from a secure note")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Tag removed from secure note",
                    content = @Content(schema = @Schema(implementation = SecureNoteDtoResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Secure note or tag not found")
    })
    public ResponseEntity<ApiResponse<SecureNoteDto>> removeTagFromSecureNote(
            @Parameter(description = "Secure Note ID", required = true) @PathVariable Long id,
            @Parameter(description = "Tag ID", required = true) @PathVariable Long tagId) {
        
        // Check if secure note exists
        SecureNote secureNote = secureNoteService.findById(id)
                .orElse(null);
        if (secureNote == null) {
            return notFoundResponse("Secure note not found with ID: " + id);
        }
        
        // Check if tag exists
        Tag tag = tagService.findById(tagId)
                .orElse(null);
        if (tag == null) {
            return notFoundResponse("Tag not found with ID: " + tagId);
        }
        
        // Remove tag from secure note
        secureNoteMapper.removeTagFromSecureNote(secureNote, tag);
        SecureNote updatedSecureNote = secureNoteService.save(secureNote);
        
        return successResponse(secureNoteMapper.toDto(updatedSecureNote), "Tag removed from secure note successfully");
    }

    // Schema classes for Swagger documentation
    @SuppressWarnings("unused")
    private static class SecureNoteDtoResponse extends ApiResponse<SecureNoteDto> {
        public SecureNoteDtoResponse() {
            super(true, "");
        }
    }
    
    @SuppressWarnings("unused")
    private static class SecureNoteDtoListResponse extends ApiResponse<List<SecureNoteDto>> {
        public SecureNoteDtoListResponse() {
            super(true, "");
        }
    }
} 