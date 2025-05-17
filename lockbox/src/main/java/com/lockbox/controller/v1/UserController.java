package com.lockbox.controller.v1;

import com.lockbox.controller.BaseController;
import com.lockbox.domain.model.User;
import com.lockbox.domain.service.UserService;
import com.lockbox.dto.ApiResponse;
import com.lockbox.dto.UserCreationDto;
import com.lockbox.dto.UserDto;
import com.lockbox.dto.UserStatsDto;
import com.lockbox.mapper.UserMapper;

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
import com.lockbox.exception.ResourceNotFoundException;

import java.util.List;

@RestController
@RequestMapping("/api/v1/users")
@io.swagger.v3.oas.annotations.tags.Tag(name = "User", description = "User management APIs")
public class UserController extends BaseController {

    private final UserService userService;
    private final UserMapper userMapper;

    @Autowired
    public UserController(UserService userService, UserMapper userMapper) {
        this.userService = userService;
        this.userMapper = userMapper;
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
        
        UserStatsDto stats = userService.getUserStats(id);
        return ResponseEntity.ok(ApiResponse.success("User statistics retrieved successfully", stats));
    }

    private User getUserOrThrow(Long id) {
        return userService.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", id));
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