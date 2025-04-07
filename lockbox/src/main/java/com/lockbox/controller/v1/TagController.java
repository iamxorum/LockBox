package com.lockbox.controller.v1;

import com.lockbox.controller.BaseController;
import com.lockbox.dto.ApiResponse;
import com.lockbox.dto.TagCreationDto;
import com.lockbox.dto.TagDto;
import com.lockbox.mapper.TagMapper;
import com.lockbox.model.Tag;
import com.lockbox.service.TagService;
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
@RequestMapping("/api/v1/tags")
@io.swagger.v3.oas.annotations.tags.Tag(name = "Tag", description = "Tag management APIs")
public class TagController extends BaseController {

    private final TagService tagService;
    private final TagMapper tagMapper;

    @Autowired
    public TagController(TagService tagService, TagMapper tagMapper) {
        this.tagService = tagService;
        this.tagMapper = tagMapper;
    }

    @GetMapping
    @Operation(summary = "Get all tags", description = "Retrieves a list of all tags")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Tags found",
                    content = @Content(schema = @Schema(implementation = TagDtoListResponse.class)))
    })
    public ResponseEntity<ApiResponse<List<TagDto>>> getAllTags() {
        List<TagDto> tags = tagService.findAll().stream()
                .map(tagMapper::toDto)
                .collect(Collectors.toList());
        return successResponse(tags, "Tags retrieved successfully");
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get tag by ID", description = "Retrieves a tag by its ID")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Tag found",
                    content = @Content(schema = @Schema(implementation = TagDtoResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Tag not found")
    })
    public ResponseEntity<ApiResponse<TagDto>> getTagById(
            @Parameter(description = "Tag ID", required = true) @PathVariable Long id) {
        return tagService.findById(id)
                .map(tag -> successResponse(tagMapper.toDto(tag), "Tag retrieved successfully"))
                .orElse(notFoundResponse("Tag not found with ID: " + id));
    }

    @PostMapping
    @Operation(summary = "Create tag", description = "Creates a new tag")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "Tag created",
                    content = @Content(schema = @Schema(implementation = TagDtoResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Invalid input"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "409", description = "Tag already exists")
    })
    public ResponseEntity<ApiResponse<TagDto>> createTag(
            @Parameter(description = "Tag creation data", required = true)
            @Valid @RequestBody TagCreationDto tagCreationDto) {
        
        // Check if tag name already exists
        if (tagService.existsByName(tagCreationDto.getName())) {
            return badRequestResponse("Tag with name '" + tagCreationDto.getName() + "' already exists");
        }
        
        // Create tag
        Tag tag = tagMapper.toEntity(tagCreationDto);
        
        // Save tag
        Tag savedTag = tagService.save(tag);
        
        return createdResponse(tagMapper.toDto(savedTag), "Tag created successfully");
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update tag", description = "Updates an existing tag")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Tag updated",
                    content = @Content(schema = @Schema(implementation = TagDtoResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Invalid input"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Tag not found"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "409", description = "Tag name already exists")
    })
    public ResponseEntity<ApiResponse<TagDto>> updateTag(
            @Parameter(description = "Tag ID", required = true) @PathVariable Long id,
            @Parameter(description = "Tag update data", required = true)
            @Valid @RequestBody TagCreationDto tagCreationDto) {
        
        return tagService.findById(id)
                .<ResponseEntity<ApiResponse<TagDto>>>map(existingTag -> {
                    // Check if name is changed and already exists
                    if (!existingTag.getName().equals(tagCreationDto.getName()) &&
                            tagService.existsByName(tagCreationDto.getName())) {
                        return badRequestResponse("Tag with name '" + tagCreationDto.getName() + "' already exists");
                    }
                    
                    // Update fields
                    tagMapper.updateEntityFromDto(tagCreationDto, existingTag);
                    
                    // Save tag
                    Tag updatedTag = tagService.save(existingTag);
                    
                    return successResponse(tagMapper.toDto(updatedTag), "Tag updated successfully");
                })
                .orElse(notFoundResponse("Tag not found with ID: " + id));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete tag", description = "Deletes a tag by its ID")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Tag deleted"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Tag not found")
    })
    public ResponseEntity<ApiResponse<Void>> deleteTag(
            @Parameter(description = "Tag ID", required = true) @PathVariable Long id) {
        
        return tagService.findById(id)
                .<ResponseEntity<ApiResponse<Void>>>map(tag -> {
                    tagService.deleteById(id);
                    return successResponse("Tag deleted successfully");
                })
                .orElse(notFoundResponse("Tag not found with ID: " + id));
    }

    @GetMapping("/password/{passwordId}")
    @Operation(summary = "Get tags by password", description = "Retrieves all tags for a specific password")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Tags found",
                    content = @Content(schema = @Schema(implementation = TagDtoListResponse.class)))
    })
    public ResponseEntity<ApiResponse<List<TagDto>>> getTagsByPasswordId(
            @Parameter(description = "Password ID", required = true) @PathVariable Long passwordId) {
        List<TagDto> tags = tagService.findByPasswordId(passwordId).stream()
                .map(tagMapper::toDto)
                .collect(Collectors.toList());
        return successResponse(tags, "Tags retrieved successfully");
    }

    @GetMapping("/secure-note/{secureNoteId}")
    @Operation(summary = "Get tags by secure note", description = "Retrieves all tags for a specific secure note")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Tags found",
                    content = @Content(schema = @Schema(implementation = TagDtoListResponse.class)))
    })
    public ResponseEntity<ApiResponse<List<TagDto>>> getTagsBySecureNoteId(
            @Parameter(description = "Secure Note ID", required = true) @PathVariable Long secureNoteId) {
        List<TagDto> tags = tagService.findBySecureNoteId(secureNoteId).stream()
                .map(tagMapper::toDto)
                .collect(Collectors.toList());
        return successResponse(tags, "Tags retrieved successfully");
    }

    // Schema classes for Swagger documentation
    @SuppressWarnings("unused")
    private static class TagDtoResponse extends ApiResponse<TagDto> {
        public TagDtoResponse() {
            super(true, "");
        }
    }
    
    @SuppressWarnings("unused")
    private static class TagDtoListResponse extends ApiResponse<List<TagDto>> {
        public TagDtoListResponse() {
            super(true, "");
        }
    }
} 