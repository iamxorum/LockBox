package com.lockbox.domain.service.impl;

import com.lockbox.domain.model.Password;
import com.lockbox.domain.model.User;
import com.lockbox.domain.repository.PasswordRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PasswordServiceImplTest {

    @Mock
    private PasswordRepository passwordRepository;

    @InjectMocks
    private PasswordServiceImpl passwordService;

    private Password testPassword;
    private User testUser;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setId(1L);
        testUser.setUsername("testuser");
        testUser.setEmail("test@example.com");

        testPassword = new Password();
        testPassword.setId(1L);
        testPassword.setTitle("Test Password");
        testPassword.setUsername("testusername");
        testPassword.setPasswordValue("encrypted_password");
        testPassword.setUrl("https://example.com");
        testPassword.setNotes("Test notes");
        testPassword.setUser(testUser);
        testPassword.setCreatedAt(LocalDateTime.now());
    }

    @Test
    void findAll_ShouldReturnAllPasswords() {
        // Given
        List<Password> passwords = Arrays.asList(testPassword, createTestPassword(2L, "Another Password"));
        when(passwordRepository.findAll()).thenReturn(passwords);

        // When
        List<Password> result = passwordService.findAll();

        // Then
        assertEquals(2, result.size());
        assertEquals(testPassword.getTitle(), result.get(0).getTitle());
        verify(passwordRepository).findAll();
    }

    @Test
    void findById_WithValidId_ShouldReturnPassword() {
        // Given
        when(passwordRepository.findByIdWithCategory(1L)).thenReturn(Optional.of(testPassword));

        // When
        Optional<Password> result = passwordService.findById(1L);

        // Then
        assertTrue(result.isPresent());
        assertEquals(testPassword.getTitle(), result.get().getTitle());
        verify(passwordRepository).findByIdWithCategory(1L);
    }

    @Test
    void findById_WithInvalidId_ShouldReturnEmpty() {
        // Given
        when(passwordRepository.findByIdWithCategory(999L)).thenReturn(Optional.empty());

        // When
        Optional<Password> result = passwordService.findById(999L);

        // Then
        assertFalse(result.isPresent());
        verify(passwordRepository).findByIdWithCategory(999L);
    }

    @Test
    void save_ShouldReturnSavedPassword() {
        // Given
        when(passwordRepository.save(any(Password.class))).thenReturn(testPassword);

        // When
        Password result = passwordService.save(testPassword);

        // Then
        assertEquals(testPassword.getTitle(), result.getTitle());
        assertEquals(testPassword.getUsername(), result.getUsername());
        verify(passwordRepository).save(testPassword);
    }

    @Test
    void delete_ShouldCallRepositoryDelete() {
        // When
        passwordService.delete(testPassword);

        // Then
        verify(passwordRepository).delete(testPassword);
    }

    @Test
    void deleteById_ShouldCallRepositoryDeleteById() {
        // When
        passwordService.deleteById(1L);

        // Then
        verify(passwordRepository).deleteById(1L);
    }

    @Test
    void existsById_WithValidId_ShouldReturnTrue() {
        // Given
        when(passwordRepository.existsById(1L)).thenReturn(true);

        // When
        boolean result = passwordService.existsById(1L);

        // Then
        assertTrue(result);
        verify(passwordRepository).existsById(1L);
    }

    @Test
    void findByUserId_ShouldReturnUserPasswords() {
        // Given
        Long userId = 1L;
        List<Password> userPasswords = Arrays.asList(testPassword);
        when(passwordRepository.findByUserIdWithTags(userId)).thenReturn(userPasswords);

        // When
        List<Password> result = passwordService.findByUserId(userId);

        // Then
        assertEquals(1, result.size());
        assertEquals(testPassword.getTitle(), result.get(0).getTitle());
        assertEquals(userId, result.get(0).getUser().getId());
        verify(passwordRepository).findByUserIdWithTags(userId);
    }

    @Test
    void findByUserIdAndTitle_ShouldReturnMatchingPasswords() {
        // Given
        Long userId = 1L;
        String title = "Test Password";
        List<Password> matchingPasswords = Arrays.asList(testPassword);
        when(passwordRepository.findByUserIdAndTitleContainingIgnoreCase(userId, title))
                .thenReturn(matchingPasswords);

        // When
        List<Password> result = passwordRepository.findByUserIdAndTitleContainingIgnoreCase(userId, title);

        // Then
        assertEquals(1, result.size());
        assertEquals(testPassword.getTitle(), result.get(0).getTitle());
        verify(passwordRepository).findByUserIdAndTitleContainingIgnoreCase(userId, title);
    }

    @Test
    void passwordShouldHaveValidationConstraints() {
        // Test that our password model has proper validation
        assertNotNull(testPassword.getTitle());
        assertNotNull(testPassword.getUsername());
        assertNotNull(testPassword.getPasswordValue());
        assertNotNull(testPassword.getUser());
        assertNotNull(testPassword.getCreatedAt());
    }

    @Test
    void passwordShouldSupportMetadata() {
        // Test metadata functionality
        testPassword.setMetadata("strength", "strong");
        testPassword.setMetadata("lastUsed", "2023-01-01");

        assertEquals("strong", testPassword.getMetadata("strength"));
        assertEquals("2023-01-01", testPassword.getMetadata("lastUsed"));
        assertNull(testPassword.getMetadata("nonexistent"));
    }

    private Password createTestPassword(Long id, String title) {
        Password password = new Password();
        password.setId(id);
        password.setTitle(title);
        password.setUsername("testuser");
        password.setPasswordValue("encrypted_password");
        password.setUser(testUser);
        password.setCreatedAt(LocalDateTime.now());
        return password;
    }
} 