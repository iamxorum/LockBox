package com.lockbox.domain.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class PasswordTest {

    private Password password;
    private User user;

    @BeforeEach
    void setUp() {
        user = new User();
        user.setId(1L);
        user.setUsername("testuser");
        user.setEmail("test@example.com");

        password = new Password();
        password.setId(1L);
        password.setTitle("Test Password");
        password.setUsername("testusername");
        password.setPasswordValue("secretpassword");
        password.setUrl("https://example.com");
        password.setNotes("Test notes");
        password.setUser(user);
    }

    @Test
    void setMetadata_ShouldStoreMetadata() {
        // When
        password.setMetadata("strength", "strong");
        password.setMetadata("lastUsed", "2023-01-01");

        // Then
        assertEquals("strong", password.getMetadata("strength"));
        assertEquals("2023-01-01", password.getMetadata("lastUsed"));
    }

    @Test
    void getMetadata_WithNonExistentKey_ShouldReturnNull() {
        // When & Then
        assertNull(password.getMetadata("nonexistent"));
    }

    @Test
    void setMetadata_WithNullMetadataMap_ShouldCreateMap() {
        // Given
        password.setMetadata(null); // Reset metadata map

        // When
        password.setMetadata("key", "value");

        // Then
        assertEquals("value", password.getMetadata("key"));
    }

    @Test
    void onCreate_ShouldSetCreatedAt() {
        // Given
        Password newPassword = new Password();
        LocalDateTime beforeCreate = LocalDateTime.now().minusSeconds(1);

        // When
        newPassword.onCreate(); // Simulate @PrePersist

        // Then
        assertNotNull(newPassword.getCreatedAt());
        assertTrue(newPassword.getCreatedAt().isAfter(beforeCreate));
    }

    @Test
    void onUpdate_ShouldSetUpdatedAt() {
        // Given
        LocalDateTime beforeUpdate = LocalDateTime.now().minusSeconds(1);

        // When
        password.onUpdate(); // Simulate @PreUpdate

        // Then
        assertNotNull(password.getUpdatedAt());
        assertTrue(password.getUpdatedAt().isAfter(beforeUpdate));
    }

    @Test
    void equals_WithSamePassword_ShouldReturnTrue() {
        // Given
        Password anotherPassword = new Password();
        anotherPassword.setId(1L);
        anotherPassword.setTitle("Test Password");
        anotherPassword.setUsername("testusername");

        // When & Then
        assertEquals(password, anotherPassword);
    }

    @Test
    void equals_WithDifferentPassword_ShouldReturnFalse() {
        // Given
        Password anotherPassword = new Password();
        anotherPassword.setId(2L);
        anotherPassword.setTitle("Different Password");
        anotherPassword.setUsername("differentusername");

        // When & Then
        assertNotEquals(password, anotherPassword);
    }

    @Test
    void hashCode_ShouldBeConsistent() {
        // Given
        Password anotherPassword = new Password();
        anotherPassword.setId(1L);
        anotherPassword.setTitle("Test Password");
        anotherPassword.setUsername("testusername");

        // When & Then
        assertEquals(password.hashCode(), anotherPassword.hashCode());
    }

    @Test
    void toString_ShouldNotIncludeUserDetails() {
        // When
        String result = password.toString();

        // Then
        assertTrue(result.contains("Test Password"));
        assertTrue(result.contains("testusername"));
        // Should exclude user, category, and tags as per @ToString annotation
    }

    @Test
    void constructor_NoArgs_ShouldCreatePassword() {
        // When
        Password newPassword = new Password();

        // Then
        assertNotNull(newPassword);
        assertNotNull(newPassword.getTags());
        assertNotNull(newPassword.getMetadata());
    }

    @Test
    void allArgsConstructor_ShouldSetAllFields() {
        // When
        Password newPassword = new Password(
            2L, "New Password", "newuser", "newpassword", 
            "https://newsite.com", "New notes", 
            LocalDateTime.now(), null, user, null, null, null
        );

        // Then
        assertEquals(2L, newPassword.getId());
        assertEquals("New Password", newPassword.getTitle());
        assertEquals("newuser", newPassword.getUsername());
        assertEquals("newpassword", newPassword.getPasswordValue());
        assertEquals("https://newsite.com", newPassword.getUrl());
        assertEquals("New notes", newPassword.getNotes());
        assertEquals(user, newPassword.getUser());
    }

    @Test
    void tags_ShouldBeInitializedAsEmptySet() {
        // Given
        Password newPassword = new Password();

        // When & Then
        assertNotNull(newPassword.getTags());
        assertTrue(newPassword.getTags().isEmpty());
    }
} 