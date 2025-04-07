package com.lockbox.controller.v1;

import com.lockbox.controller.BaseController;
import com.lockbox.dto.ApiResponse;
import com.lockbox.dto.UserCreationDto;
import com.lockbox.dto.UserDto;
import com.lockbox.mapper.UserMapper;
import com.lockbox.model.User;
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

    @GetMapping
    @Operation(summary = "Get all users", description = "Retrieves a list of all users")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Users found",
                    content = @Content(schema = @Schema(implementation = UserDtoListResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Forbidden")
    })
    public ResponseEntity<ApiResponse<List<UserDto>>> getAllUsers() {
        List<UserDto> users = userService.findAll().stream()
                .map(userMapper::toDto)
                .collect(Collectors.toList());
        return successResponse(users, "Users retrieved successfully");
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
        return userService.findById(id)
                .<ResponseEntity<ApiResponse<UserDto>>>map(user -> 
                    successResponse(userMapper.toDto(user), "User retrieved successfully"))
                .orElse(notFoundResponse("User not found with ID: " + id));
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
        // Check if username or email already exists
        if (userService.existsByUsername(userCreationDto.getUsername())) {
            return badRequestResponse("Username already exists");
        }
        if (userService.existsByEmail(userCreationDto.getEmail())) {
            return badRequestResponse("Email already exists");
        }

        User user = userMapper.toEntity(userCreationDto);
        User savedUser = userService.save(user);
        return createdResponse(userMapper.toDto(savedUser), "User created successfully");
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update user", description = "Updates an existing user")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "User updated",
                    content = @Content(schema = @Schema(implementation = UserDtoResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Invalid input"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "User not found"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "409", description = "Username or email already exists")
    })
    public ResponseEntity<ApiResponse<UserDto>> updateUser(
            @Parameter(description = "User ID", required = true) @PathVariable Long id,
            @Parameter(description = "User update data", required = true)
            @Valid @RequestBody UserCreationDto userCreationDto) {
        return userService.findById(id)
                .<ResponseEntity<ApiResponse<UserDto>>>map(existingUser -> {
                    // Check if username is taken by another user
                    if (!existingUser.getUsername().equals(userCreationDto.getUsername()) &&
                            userService.existsByUsername(userCreationDto.getUsername())) {
                        return badRequestResponse("Username already exists");
                    }
                    // Check if email is taken by another user
                    if (!existingUser.getEmail().equals(userCreationDto.getEmail()) &&
                            userService.existsByEmail(userCreationDto.getEmail())) {
                        return badRequestResponse("Email already exists");
                    }

                    userMapper.updateEntityFromDto(userCreationDto, existingUser);
                    User updatedUser = userService.save(existingUser);
                    return successResponse(userMapper.toDto(updatedUser), "User updated successfully");
                })
                .orElse(notFoundResponse("User not found with ID: " + id));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete user", description = "Deletes a user by their ID")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "User deleted"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "User not found")
    })
    public ResponseEntity<ApiResponse<Void>> deleteUser(
            @Parameter(description = "User ID", required = true) @PathVariable Long id) {
        return userService.findById(id)
                .<ResponseEntity<ApiResponse<Void>>>map(user -> {
                    userService.deleteById(id);
                    return successResponse("User deleted successfully");
                })
                .orElse(notFoundResponse("User not found with ID: " + id));
    }

    // Schema classes for Swagger documentation
    @SuppressWarnings("unused")
    private static class UserDtoResponse extends ApiResponse<UserDto> {
        public UserDtoResponse() {
            super(true, "");
        }
    }
    
    @SuppressWarnings("unused")
    private static class UserDtoListResponse extends ApiResponse<List<UserDto>> {
        public UserDtoListResponse() {
            super(true, "");
        }
    }
} 