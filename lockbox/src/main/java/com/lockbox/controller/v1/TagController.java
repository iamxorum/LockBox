package com.lockbox.controller.v1;

import com.lockbox.controller.BaseController;
import com.lockbox.domain.model.Tag;
import com.lockbox.domain.model.User;
import com.lockbox.domain.service.TagService;
import com.lockbox.domain.service.UserService;
import com.lockbox.dto.ApiResponse;
import com.lockbox.dto.TagCreationDto;
import com.lockbox.dto.TagDto;
import com.lockbox.exception.ResourceNotFoundException;
import com.lockbox.mapper.TagMapper;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import com.lockbox.exception.ResourceAlreadyExistsException;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/tags")
@io.swagger.v3.oas.annotations.tags.Tag(name = "Tag", description = "Tag management APIs")
public class TagController extends BaseController {

    private final TagService tagService;
    private final UserService userService;
    private final TagMapper tagMapper;

    @Autowired
    public TagController(TagService tagService, UserService userService, TagMapper tagMapper) {
        this.tagService = tagService;
        this.userService = userService;
        this.tagMapper = tagMapper;
    }

    @GetMapping
    @Operation(summary = "Get all tags for current user", description = "Retrieves a list of all tags for the authenticated user")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Tags found",
                    content = @Content(schema = @Schema(implementation = TagDtoListResponse.class)))
    })
    public ResponseEntity<ApiResponse<List<TagDto>>> getCurrentUserTags() {
        User currentUser = getCurrentUser();
        List<Tag> tags = tagService.findByUserId(currentUser.getId());
        List<TagDto> tagDtos = tags.stream()
                .map(tagMapper::toDto)
                .collect(Collectors.toList());
        
        return ResponseEntity.ok(ApiResponse.success("Tags retrieved successfully", tagDtos));
    }

    @PostMapping
    @Operation(summary = "Create tag", description = "Creates a new tag for the authenticated user")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "Tag created",
                    content = @Content(schema = @Schema(implementation = TagDtoResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Invalid input"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "409", description = "Tag already exists")
    })
    public ResponseEntity<ApiResponse<TagDto>> createTag(
            @Parameter(description = "Tag creation data", required = true)
            @Valid @RequestBody TagCreationDto tagCreationDto) {
        
        User currentUser = getCurrentUser();
        tagCreationDto.setUserId(currentUser.getId());
        
        // Check if tag name already exists for this user
        if (tagService.existsByNameAndUserId(tagCreationDto.getName(), currentUser.getId())) {
            throw new ResourceAlreadyExistsException("Tag", "name", tagCreationDto.getName());
        }
        
        // Create tag
        Tag tag = tagMapper.toEntity(tagCreationDto);
        
        // Save tag
        Tag savedTag = tagService.save(tag);
        TagDto savedTagDto = tagMapper.toDto(savedTag);
        
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Tag created successfully", savedTagDto));
    }
    
    /**
     * Helper method to get the current authenticated user
     */
    private User getCurrentUser() {
        UserDetails userDetails = (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        return userService.findByUsername(userDetails.getUsername())
                .orElseThrow(() -> new ResourceNotFoundException("User", "username", userDetails.getUsername()));
    }

    @SuppressWarnings("unused")
    private static class TagDtoResponse extends ApiResponse<TagDto> {
        public TagDtoResponse() {
            super(true, "Tag retrieved successfully");
        }
    }

    @SuppressWarnings("unused")
    private static class TagDtoListResponse extends ApiResponse<List<TagDto>> {
        public TagDtoListResponse() {
            super(true, "Tags retrieved successfully");
        }
    }
} 