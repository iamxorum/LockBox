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
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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
        if (!userService.existsById(userId)) {
            return notFoundResponse("User not found with ID: " + userId);
        }
        
        List<CategoryDto> categories = categoryService.findByUserId(userId).stream()
                .map(categoryMapper::toDto)
                .collect(Collectors.toList());
        return successResponse(categories, "Categories retrieved successfully");
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
        return categoryService.findById(id)
                .map(category -> successResponse(categoryMapper.toDto(category), "Category retrieved successfully"))
                .orElse(notFoundResponse("Category not found with ID: " + id));
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
        
        // Check if user exists
        User user = userService.findById(userId)
                .orElse(null);
        if (user == null) {
            return notFoundResponse("User not found with ID: " + userId);
        }
        
        // Check if category name already exists for this user
        if (categoryService.existsByUserIdAndName(userId, categoryCreationDto.getName())) {
            return badRequestResponse("Category with name '" + categoryCreationDto.getName() 
                    + "' already exists for this user");
        }
        
        // Create category
        Category category = categoryMapper.toEntity(categoryCreationDto);
        category.setUser(user);
        
        // Save category
        Category savedCategory = categoryService.save(category);
        
        return createdResponse(categoryMapper.toDto(savedCategory), "Category created successfully");
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update category", description = "Updates an existing category")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Category updated",
                    content = @Content(schema = @Schema(implementation = CategoryDtoResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Invalid input"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Category not found"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "409", description = "Category name already exists for this user")
    })
    public ResponseEntity<ApiResponse<CategoryDto>> updateCategory(
            @Parameter(description = "Category ID", required = true) @PathVariable Long id,
            @Parameter(description = "Category update data", required = true)
            @Valid @RequestBody CategoryCreationDto categoryCreationDto) {
        
        return categoryService.findById(id)
                .<ResponseEntity<ApiResponse<CategoryDto>>>map(existingCategory -> {
                    // Check if name is changed and already exists for this user
                    if (!existingCategory.getName().equals(categoryCreationDto.getName()) &&
                            categoryService.existsByUserIdAndName(
                                    existingCategory.getUser().getId(), categoryCreationDto.getName())) {
                        return badRequestResponse("Category with name '" + categoryCreationDto.getName() 
                                + "' already exists for this user");
                    }
                    
                    // Update fields
                    categoryMapper.updateEntityFromDto(categoryCreationDto, existingCategory);
                    
                    // Save category
                    Category updatedCategory = categoryService.save(existingCategory);
                    
                    return successResponse(categoryMapper.toDto(updatedCategory), "Category updated successfully");
                })
                .orElse(notFoundResponse("Category not found with ID: " + id));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete category", description = "Deletes a category by its ID")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Category deleted"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Category not found")
    })
    public ResponseEntity<ApiResponse<Void>> deleteCategory(
            @Parameter(description = "Category ID", required = true) @PathVariable Long id) {
        
        return categoryService.findById(id)
                .<ResponseEntity<ApiResponse<Void>>>map(category -> {
                    categoryService.deleteById(id);
                    return successResponse("Category deleted successfully");
                })
                .orElse(notFoundResponse("Category not found with ID: " + id));
    }

    // Schema classes for Swagger documentation
    @SuppressWarnings("unused")
    private static class CategoryDtoResponse extends ApiResponse<CategoryDto> {
        public CategoryDtoResponse() {
            super(true, "");
        }
    }
    
    @SuppressWarnings("unused")
    private static class CategoryDtoListResponse extends ApiResponse<List<CategoryDto>> {
        public CategoryDtoListResponse() {
            super(true, "");
        }
    }
}