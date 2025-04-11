package com.lockbox.controller;

import com.lockbox.dto.UserDto;
import com.lockbox.dto.UserCreationDto;
import com.lockbox.dto.ApiResponse;
import com.lockbox.domain.model.User;
import com.lockbox.domain.service.UserService;
import com.lockbox.controller.v1.UserController;
import com.lockbox.mapper.UserMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@ActiveProfiles("test")
class UserControllerTest {

    @Mock
    private UserService userService;
    
    @Mock
    private UserMapper userMapper;

    @InjectMocks
    private UserController userController;

    private User testUser;
    private UserDto testUserDto;
    private UserCreationDto testCreationDto;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setId(1L);
        testUser.setUsername("testuser");
        testUser.setEmail("test@example.com");
        
        testUserDto = new UserDto();
        testUserDto.setId(1L);
        testUserDto.setUsername("testuser");
        testUserDto.setEmail("test@example.com");
        
        testCreationDto = new UserCreationDto();
        testCreationDto.setUsername("testuser");
        testCreationDto.setEmail("test@example.com");
        testCreationDto.setPassword("password123");
    }

    @Test
    void createUser_Success() {
        when(userService.existsByUsername(anyString())).thenReturn(false);
        when(userService.existsByEmail(anyString())).thenReturn(false);
        when(userMapper.toEntity(any(UserCreationDto.class))).thenReturn(testUser);
        when(userService.save(any(User.class))).thenReturn(testUser);
        when(userMapper.toDto(any(User.class))).thenReturn(testUserDto);

        ResponseEntity<ApiResponse<UserDto>> response = userController.createUser(testCreationDto);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().isSuccess());
        assertEquals(testUserDto, response.getBody().getData());
    }

    @Test
    void getUserById_Success() {
        when(userService.findById(anyLong())).thenReturn(Optional.of(testUser));
        when(userMapper.toDto(any(User.class))).thenReturn(testUserDto);

        ResponseEntity<ApiResponse<UserDto>> response = userController.getUserById(1L);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().isSuccess());
        assertEquals(testUserDto, response.getBody().getData());
    }
} 