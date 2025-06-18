package com.lockbox.dto;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class UserStatsDtoTest {

    @Test
    void noArgsConstructor_ShouldCreateEmptyDto() {
        // When
        UserStatsDto dto = new UserStatsDto();

        // Then
        assertNotNull(dto);
        assertEquals(0L, dto.getPasswords());
        assertEquals(0L, dto.getSecureNotes());
        assertEquals(0L, dto.getCategories());
        assertEquals(0L, dto.getTags());
    }

    @Test
    void allArgsConstructor_ShouldSetAllFields() {
        // When
        UserStatsDto dto = new UserStatsDto(10L, 5L, 3L, 8L);

        // Then
        assertEquals(10L, dto.getPasswords());
        assertEquals(5L, dto.getSecureNotes());
        assertEquals(3L, dto.getCategories());
        assertEquals(8L, dto.getTags());
    }

    @Test
    void setters_ShouldUpdateValues() {
        // Given
        UserStatsDto dto = new UserStatsDto();

        // When
        dto.setPasswords(15L);
        dto.setSecureNotes(7L);
        dto.setCategories(4L);
        dto.setTags(12L);

        // Then
        assertEquals(15L, dto.getPasswords());
        assertEquals(7L, dto.getSecureNotes());
        assertEquals(4L, dto.getCategories());
        assertEquals(12L, dto.getTags());
    }
} 