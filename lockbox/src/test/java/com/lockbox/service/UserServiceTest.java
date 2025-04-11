package com.lockbox.service;

import com.lockbox.dto.UserDto;
import com.lockbox.dto.UserCreationDto;
import com.lockbox.domain.model.User;
import com.lockbox.exception.ResourceAlreadyExistsException;
import com.lockbox.exception.ResourceNotFoundException;
import com.lockbox.domain.repository.UserRepository;
import com.lockbox.domain.service.impl.UserServiceImpl;
import com.lockbox.mapper.UserMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@ActiveProfiles("test")
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private UserMapper userMapper;

    @InjectMocks
    private UserServiceImpl userService;

    private User testUser;
    private UserCreationDto testCreationDto;
    private UserDto testUserDto;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setId(1L);
        testUser.setUsername("testuser");
        testUser.setEmail("test@example.com");
        testUser.setPassword("encodedPassword");

        testCreationDto = new UserCreationDto();
        testCreationDto.setUsername("testuser");
        testCreationDto.setEmail("test@example.com");
        testCreationDto.setPassword("password123");

        testUserDto = new UserDto();
        testUserDto.setId(1L);
        testUserDto.setUsername("testuser");
        testUserDto.setEmail("test@example.com");
    }

    @Test
    void save_Success() {
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        User result = userService.save(testUser);

        assertNotNull(result);
        assertEquals(testUser.getUsername(), result.getUsername());
        assertEquals(testUser.getEmail(), result.getEmail());
        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    void findById_Success() {
        when(userRepository.findById(anyLong())).thenReturn(Optional.of(testUser));

        Optional<User> result = userService.findById(1L);

        assertTrue(result.isPresent());
        assertEquals(testUser.getUsername(), result.get().getUsername());
        assertEquals(testUser.getEmail(), result.get().getEmail());
    }

    @Test
    void findById_NotFound() {
        when(userRepository.findById(anyLong())).thenReturn(Optional.empty());

        Optional<User> result = userService.findById(1L);

        assertTrue(result.isEmpty());
    }

    @Test
    void deleteById_Success() {
        doNothing().when(userRepository).deleteById(anyLong());

        userService.deleteById(1L);

        verify(userRepository, times(1)).deleteById(anyLong());
    }

    @Test
    void findByUsername_Success() {
        when(userRepository.findByUsername(anyString())).thenReturn(Optional.of(testUser));

        Optional<User> result = userService.findByUsername("testuser");

        assertTrue(result.isPresent());
        assertEquals(testUser.getUsername(), result.get().getUsername());
    }

    @Test
    void existsByUsername_True() {
        when(userRepository.existsByUsername(anyString())).thenReturn(true);

        boolean result = userService.existsByUsername("testuser");

        assertTrue(result);
    }

    @Test
    void existsByEmail_True() {
        when(userRepository.existsByEmail(anyString())).thenReturn(true);

        boolean result = userService.existsByEmail("test@example.com");

        assertTrue(result);
    }
} 