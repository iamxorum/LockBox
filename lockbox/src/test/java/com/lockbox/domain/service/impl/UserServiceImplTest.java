package com.lockbox.domain.service.impl;

import com.lockbox.domain.model.User;
import com.lockbox.domain.repository.*;
import com.lockbox.dto.UserStatsDto;
import com.lockbox.exception.ResourceNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceImplTest {

    @Mock
    private UserRepository userRepository;
    
    @Mock
    private PasswordRepository passwordRepository;
    
    @Mock
    private SecureNoteRepository secureNoteRepository;
    
    @Mock
    private CategoryRepository categoryRepository;
    
    @Mock
    private TagRepository tagRepository;

    @InjectMocks
    private UserServiceImpl userService;

    private User testUser;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setId(1L);
        testUser.setUsername("testuser");
        testUser.setEmail("test@example.com");
        testUser.setFirstName("Test");
        testUser.setLastName("User");
    }

    @Test
    void findAll_ShouldReturnAllUsers() {
        // Given
        List<User> users = Arrays.asList(testUser, createUser(2L, "user2", "user2@example.com"));
        when(userRepository.findAll()).thenReturn(users);

        // When
        List<User> result = userService.findAll();

        // Then
        assertEquals(2, result.size());
        assertEquals(testUser.getUsername(), result.get(0).getUsername());
        verify(userRepository).findAll();
    }

    @Test
    void findById_WithValidId_ShouldReturnUser() {
        // Given
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));

        // When
        Optional<User> result = userService.findById(1L);

        // Then
        assertTrue(result.isPresent());
        assertEquals(testUser.getUsername(), result.get().getUsername());
        verify(userRepository).findById(1L);
    }

    @Test
    void findById_WithInvalidId_ShouldReturnEmpty() {
        // Given
        when(userRepository.findById(999L)).thenReturn(Optional.empty());

        // When
        Optional<User> result = userService.findById(999L);

        // Then
        assertFalse(result.isPresent());
        verify(userRepository).findById(999L);
    }

    @Test
    void save_ShouldReturnSavedUser() {
        // Given
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        // When
        User result = userService.save(testUser);

        // Then
        assertEquals(testUser.getUsername(), result.getUsername());
        verify(userRepository).save(testUser);
    }

    @Test
    void delete_ShouldCallRepositoryDelete() {
        // When
        userService.delete(testUser);

        // Then
        verify(userRepository).delete(testUser);
    }

    @Test
    void deleteById_ShouldCallRepositoryDeleteById() {
        // When
        userService.deleteById(1L);

        // Then
        verify(userRepository).deleteById(1L);
    }

    @Test
    void existsById_WithValidId_ShouldReturnTrue() {
        // Given
        when(userRepository.existsById(1L)).thenReturn(true);

        // When
        boolean result = userService.existsById(1L);

        // Then
        assertTrue(result);
        verify(userRepository).existsById(1L);
    }

    @Test
    void existsById_WithInvalidId_ShouldReturnFalse() {
        // Given
        when(userRepository.existsById(999L)).thenReturn(false);

        // When
        boolean result = userService.existsById(999L);

        // Then
        assertFalse(result);
        verify(userRepository).existsById(999L);
    }

    @Test
    void findByUsername_WithValidUsername_ShouldReturnUser() {
        // Given
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));

        // When
        Optional<User> result = userService.findByUsername("testuser");

        // Then
        assertTrue(result.isPresent());
        assertEquals(testUser.getEmail(), result.get().getEmail());
        verify(userRepository).findByUsername("testuser");
    }

    @Test
    void existsByUsername_WithExistingUsername_ShouldReturnTrue() {
        // Given
        when(userRepository.existsByUsername("testuser")).thenReturn(true);

        // When
        boolean result = userService.existsByUsername("testuser");

        // Then
        assertTrue(result);
        verify(userRepository).existsByUsername("testuser");
    }

    @Test
    void existsByEmail_WithExistingEmail_ShouldReturnTrue() {
        // Given
        when(userRepository.existsByEmail("test@example.com")).thenReturn(true);

        // When
        boolean result = userService.existsByEmail("test@example.com");

        // Then
        assertTrue(result);
        verify(userRepository).existsByEmail("test@example.com");
    }

    @Test
    void getUserStats_WithValidUserId_ShouldReturnStats() {
        // Given
        Long userId = 1L;
        when(userRepository.existsById(userId)).thenReturn(true);
        when(passwordRepository.countByUserId(userId)).thenReturn(5L);
        when(secureNoteRepository.countByUserId(userId)).thenReturn(3L);
        when(categoryRepository.countByUserId(userId)).thenReturn(2L);
        when(tagRepository.countByUserId(userId)).thenReturn(4L);

        // When
        UserStatsDto result = userService.getUserStats(userId);

        // Then
        assertNotNull(result);
        assertEquals(5L, result.getPasswords());
        assertEquals(3L, result.getSecureNotes());
        assertEquals(2L, result.getCategories());
        assertEquals(4L, result.getTags());
        verify(userRepository).existsById(userId);
        verify(passwordRepository).countByUserId(userId);
        verify(secureNoteRepository).countByUserId(userId);
        verify(categoryRepository).countByUserId(userId);
        verify(tagRepository).countByUserId(userId);
    }

    @Test
    void getUserStats_WithInvalidUserId_ShouldThrowException() {
        // Given
        Long userId = 999L;
        when(userRepository.existsById(userId)).thenReturn(false);

        // When & Then
        ResourceNotFoundException exception = assertThrows(
            ResourceNotFoundException.class,
            () -> userService.getUserStats(userId)
        );
        
        assertTrue(exception.getMessage().contains("User"));
        assertTrue(exception.getMessage().contains("id"));
        assertTrue(exception.getMessage().contains("999"));
        verify(userRepository).existsById(userId);
        verifyNoInteractions(passwordRepository, secureNoteRepository, categoryRepository, tagRepository);
    }

    private User createUser(Long id, String username, String email) {
        User user = new User();
        user.setId(id);
        user.setUsername(username);
        user.setEmail(email);
        return user;
    }
} 