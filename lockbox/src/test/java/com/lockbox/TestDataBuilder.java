package com.lockbox;

import com.lockbox.domain.model.*;
import com.lockbox.dto.*;

import java.time.LocalDateTime;

/**
 * Test Data Builder utility class
 * Provides convenient methods to create test objects with sensible defaults
 */
public class TestDataBuilder {

    public static class UserBuilder {
        private User user = new User();

        public UserBuilder() {
            user.setUsername("testuser");
            user.setEmail("test@example.com");
            user.setPassword("hashedPassword");
            user.setFirstName("Test");
            user.setLastName("User");
        }

        public UserBuilder withId(Long id) {
            user.setId(id);
            return this;
        }

        public UserBuilder withUsername(String username) {
            user.setUsername(username);
            return this;
        }

        public UserBuilder withEmail(String email) {
            user.setEmail(email);
            return this;
        }

        public UserBuilder withName(String firstName, String lastName) {
            user.setFirstName(firstName);
            user.setLastName(lastName);
            return this;
        }

        public UserBuilder withPassword(String password) {
            user.setPassword(password);
            return this;
        }

        public UserBuilder withRole(String roleName) {
            user.addRole(roleName);
            return this;
        }

        public User build() {
            return user;
        }
    }

    public static class PasswordBuilder {
        private Password password = new Password();

        public PasswordBuilder() {
            password.setTitle("Test Password");
            password.setUsername("testusername");
            password.setPasswordValue("encrypted_password");
            password.setUrl("https://example.com");
            password.setNotes("Test notes");
            password.setCreatedAt(LocalDateTime.now());
        }

        public PasswordBuilder withId(Long id) {
            password.setId(id);
            return this;
        }

        public PasswordBuilder withTitle(String title) {
            password.setTitle(title);
            return this;
        }

        public PasswordBuilder withUsername(String username) {
            password.setUsername(username);
            return this;
        }

        public PasswordBuilder withPasswordValue(String passwordValue) {
            password.setPasswordValue(passwordValue);
            return this;
        }

        public PasswordBuilder withUrl(String url) {
            password.setUrl(url);
            return this;
        }

        public PasswordBuilder withNotes(String notes) {
            password.setNotes(notes);
            return this;
        }

        public PasswordBuilder withUser(User user) {
            password.setUser(user);
            return this;
        }

        public PasswordBuilder withCategory(Category category) {
            password.setCategory(category);
            return this;
        }

        public Password build() {
            return password;
        }
    }

    public static class CategoryBuilder {
        private Category category = new Category();

        public CategoryBuilder() {
            category.setName("Test Category");
            category.setDescription("Test category description");
            category.setColor("#FF0000");
        }

        public CategoryBuilder withId(Long id) {
            category.setId(id);
            return this;
        }

        public CategoryBuilder withName(String name) {
            category.setName(name);
            return this;
        }

        public CategoryBuilder withDescription(String description) {
            category.setDescription(description);
            return this;
        }

        public CategoryBuilder withColor(String color) {
            category.setColor(color);
            return this;
        }

        public CategoryBuilder withUser(User user) {
            category.setUser(user);
            return this;
        }

        public Category build() {
            return category;
        }
    }

    public static class UserCreationDtoBuilder {
        private UserCreationDto dto = new UserCreationDto();

        public UserCreationDtoBuilder() {
            dto.setUsername("newuser");
            dto.setEmail("new@example.com");
            dto.setPassword("securePassword123");
            dto.setFirstName("New");
            dto.setLastName("User");
        }

        public UserCreationDtoBuilder withUsername(String username) {
            dto.setUsername(username);
            return this;
        }

        public UserCreationDtoBuilder withEmail(String email) {
            dto.setEmail(email);
            return this;
        }

        public UserCreationDtoBuilder withPassword(String password) {
            dto.setPassword(password);
            return this;
        }

        public UserCreationDtoBuilder withName(String firstName, String lastName) {
            dto.setFirstName(firstName);
            dto.setLastName(lastName);
            return this;
        }

        public UserCreationDto build() {
            return dto;
        }
    }

    public static class PasswordCreationDtoBuilder {
        private PasswordCreationDto dto = new PasswordCreationDto();

        public PasswordCreationDtoBuilder() {
            dto.setTitle("New Password");
            dto.setUsername("newusername");
            dto.setPassword("newpassword123");
            dto.setUrl("https://newsite.com");
            dto.setNotes("New password notes");
        }

        public PasswordCreationDtoBuilder withTitle(String title) {
            dto.setTitle(title);
            return this;
        }

        public PasswordCreationDtoBuilder withUsername(String username) {
            dto.setUsername(username);
            return this;
        }

        public PasswordCreationDtoBuilder withPassword(String password) {
            dto.setPassword(password);
            return this;
        }

        public PasswordCreationDtoBuilder withUrl(String url) {
            dto.setUrl(url);
            return this;
        }

        public PasswordCreationDtoBuilder withNotes(String notes) {
            dto.setNotes(notes);
            return this;
        }

        public PasswordCreationDtoBuilder withCategoryId(Long categoryId) {
            dto.setCategoryId(categoryId);
            return this;
        }

        public PasswordCreationDto build() {
            return dto;
        }
    }

    // Static factory methods for convenience
    public static UserBuilder aUser() {
        return new UserBuilder();
    }

    public static PasswordBuilder aPassword() {
        return new PasswordBuilder();
    }

    public static CategoryBuilder aCategory() {
        return new CategoryBuilder();
    }

    public static UserCreationDtoBuilder aUserCreationDto() {
        return new UserCreationDtoBuilder();
    }

    public static PasswordCreationDtoBuilder aPasswordCreationDto() {
        return new PasswordCreationDtoBuilder();
    }

    // Common test data
    public static User defaultUser() {
        return aUser().build();
    }

    public static User adminUser() {
        return aUser()
                .withUsername("admin")
                .withEmail("admin@example.com")
                .withRole("ADMIN")
                .build();
    }

    public static Password defaultPassword() {
        return aPassword()
                .withUser(defaultUser())
                .build();
    }

    public static Category defaultCategory() {
        return aCategory()
                .withUser(defaultUser())
                .build();
    }
} 