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
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.lockbox.exception.ResourceAlreadyExistsException;

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
        List<Tag> tags = tagService.findAll();
        List<TagDto> tagDtos = tags.stream()
                .map(tagMapper::toDto)
                .collect(Collectors.toList());
        
        return ResponseEntity.ok(ApiResponse.success("Tags retrieved successfully", tagDtos));
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