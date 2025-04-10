package com.lockbox.controller.v1;

import com.lockbox.controller.BaseController;
import com.lockbox.dto.ApiResponse;
import com.lockbox.dto.CategoryCreationDto;
import com.lockbox.dto.CategoryDto;
import com.lockbox.mapper.CategoryMapper;
import com.lockbox.model.Category;
import com.lockbox.model.User;
import com.lockbox.service.CategoryService;
import com.lockbox.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.lockbox.exception.ResourceNotFoundException;
import com.lockbox.exception.ResourceAlreadyExistsException;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/categories")
@Tag(name = "Category", description = "Category management APIs")
public class CategoryController extends BaseController {

    private final CategoryService categoryService;
    private final UserService userService;
    private final CategoryMapper categoryMapper;

    @Autowired
    public CategoryController(CategoryService categoryService, UserService userService, CategoryMapper categoryMapper) {
        this.categoryService = categoryService;
        this.userService = userService;
        this.categoryMapper = categoryMapper;
    }

    @GetMapping("/user/{userId}")
    @Operation(summary = "Get user categories", description = "Retrieves all categories for a specific user")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Categories found",
                    content = @Content(schema = @Schema(implementation = CategoryDtoListResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "User not found")
    })
    public ResponseEntity<ApiResponse<List<CategoryDto>>> getUserCategories(
            @Parameter(description = "User ID", required = true) @PathVariable Long userId) {
        
        getUserOrThrow(userId);
        
        List<Category> categories = categoryService.findByUserId(userId);
        List<CategoryDto> categoryDtos = categories.stream()
                .map(categoryMapper::toDto)
                .collect(Collectors.toList());
        
        return ResponseEntity.ok(ApiResponse.success("Categories retrieved successfully", categoryDtos));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get category by ID", description = "Retrieves a category by its ID")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Category found",
                    content = @Content(schema = @Schema(implementation = CategoryDtoResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Category not found")
    })
    public ResponseEntity<ApiResponse<CategoryDto>> getCategoryById(
            @Parameter(description = "Category ID", required = true) @PathVariable Long id) {
        
        Category category = getCategoryOrThrow(id);
        CategoryDto categoryDto = categoryMapper.toDto(category);
        
        return ResponseEntity.ok(ApiResponse.success("Category retrieved successfully", categoryDto));
    }

    @PostMapping("/user/{userId}")
    @Operation(summary = "Create category", description = "Creates a new category for a specific user")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "Category created",
                    content = @Content(schema = @Schema(implementation = CategoryDtoResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Invalid input"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "User not found"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "409", description = "Category name already exists for this user")
    })
    public ResponseEntity<ApiResponse<CategoryDto>> createCategory(
            @Parameter(description = "User ID", required = true) @PathVariable Long userId,
            @Parameter(description = "Category creation data", required = true)
            @Valid @RequestBody CategoryCreationDto categoryCreationDto) {
        
        User user = getUserOrThrow(userId);
        
        if (categoryService.existsByUserIdAndName(userId, categoryCreationDto.getName())) {
            throw new ResourceAlreadyExistsException("Category", "name", categoryCreationDto.getName());
        }
        
        Category category = categoryMapper.toEntity(categoryCreationDto);
        category.setUser(user);
        
        Category savedCategory = categoryService.save(category);
        CategoryDto savedCategoryDto = categoryMapper.toDto(savedCategory);
        
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Category created successfully", savedCategoryDto));
    }

    private User getUserOrThrow(Long userId) {
        return userService.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));
    }

    private Category getCategoryOrThrow(Long id) {
        return categoryService.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Category", "id", id));
    }

    @SuppressWarnings("unused")
    private static class CategoryDtoResponse extends ApiResponse<CategoryDto> {
        public CategoryDtoResponse() {
            super(true, "Category retrieved successfully");
        }
    }

    @SuppressWarnings("unused")
    private static class CategoryDtoListResponse extends ApiResponse<List<CategoryDto>> {
        public CategoryDtoListResponse() {
            super(true, "Categories retrieved successfully");
        }
    }
}