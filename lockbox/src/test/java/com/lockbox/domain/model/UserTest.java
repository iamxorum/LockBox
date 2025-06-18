package com.lockbox.domain.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class UserTest {

    private User user;

    @BeforeEach
    void setUp() {
        user = new User();
        user.setId(1L);
        user.setUsername("testuser");
        user.setEmail("test@example.com");
        user.setFirstName("Test");
        user.setLastName("User");
        user.setPassword("hashedPassword");
    }

    @Test
    void addRole_ShouldAddRoleToUser() {
        // When
        user.addRole("ADMIN");

        // Then
        assertEquals(1, user.getRoles().size());
        assertTrue(user.hasRole("ADMIN"));
    }

    @Test
    void hasRole_WithExistingRole_ShouldReturnTrue() {
        // Given
        user.addRole("USER");

        // When & Then
        assertTrue(user.hasRole("USER"));
    }

    @Test
    void hasRole_WithNonExistingRole_ShouldReturnFalse() {
        // When & Then
        assertFalse(user.hasRole("ADMIN"));
    }

    @Test
    void isAdmin_WithAdminRole_ShouldReturnTrue() {
        // Given
        user.addRole("ADMIN");

        // When & Then
        assertTrue(user.isAdmin());
    }

    @Test
    void isAdmin_WithoutAdminRole_ShouldReturnFalse() {
        // Given
        user.addRole("USER");

        // When & Then
        assertFalse(user.isAdmin());
    }

    @Test
    void toString_ShouldNotIncludeSensitiveInfo() {
        // When
        String result = user.toString();

        // Then
        assertTrue(result.contains("testuser"));
        assertTrue(result.contains("test@example.com"));
        assertFalse(result.contains("hashedPassword"));
    }

    @Test
    void equals_WithSameUser_ShouldReturnTrue() {
        // Given
        User anotherUser = new User();
        anotherUser.setId(1L);
        anotherUser.setUsername("testuser");
        anotherUser.setEmail("test@example.com");

        // When & Then
        assertEquals(user, anotherUser);
    }

    @Test
    void equals_WithDifferentUser_ShouldReturnFalse() {
        // Given
        User anotherUser = new User();
        anotherUser.setId(2L);
        anotherUser.setUsername("differentuser");
        anotherUser.setEmail("different@example.com");

        // When & Then
        assertNotEquals(user, anotherUser);
    }

    @Test
    void hashCode_ShouldBeConsistent() {
        // Given
        User anotherUser = new User();
        anotherUser.setId(1L);
        anotherUser.setUsername("testuser");
        anotherUser.setEmail("test@example.com");

        // When & Then
        assertEquals(user.hashCode(), anotherUser.hashCode());
    }

    @Test
    void constructor_NoArgs_ShouldCreateUser() {
        // When
        User newUser = new User();

        // Then
        assertNotNull(newUser);
        assertNotNull(newUser.getPasswords());
        assertNotNull(newUser.getSecureNotes());
        assertNotNull(newUser.getAuditLogs());
        assertNotNull(newUser.getTags());
        assertNotNull(newUser.getRoles());
    }

    @Test
    void allArgsConstructor_ShouldSetAllFields() {
        // When
        User newUser = new User(
            2L, "newuser", "newpassword", "new@example.com", 
            "New", "User", null, null, null, null, null
        );

        // Then
        assertEquals(2L, newUser.getId());
        assertEquals("newuser", newUser.getUsername());
        assertEquals("newpassword", newUser.getPassword());
        assertEquals("new@example.com", newUser.getEmail());
        assertEquals("New", newUser.getFirstName());
        assertEquals("User", newUser.getLastName());
    }
} 