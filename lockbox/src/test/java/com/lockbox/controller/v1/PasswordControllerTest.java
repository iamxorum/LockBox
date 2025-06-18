package com.lockbox.controller.v1;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.lockbox.domain.model.Category;
import com.lockbox.domain.model.Password;
import com.lockbox.domain.model.User;
import com.lockbox.domain.service.CategoryService;
import com.lockbox.domain.service.PasswordService;
import com.lockbox.domain.service.UserService;
import com.lockbox.dto.PasswordCreationDto;
import com.lockbox.dto.PasswordDto;
import com.lockbox.mapper.PasswordMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureWebMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;

@SpringBootTest
@AutoConfigureWebMvc
@ActiveProfiles("test")
@Transactional
class PasswordControllerTest {

    @Autowired
    private WebApplicationContext context;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private PasswordService passwordService;

    @MockBean
    private UserService userService;

    @MockBean
    private CategoryService categoryService;

    @MockBean
    private PasswordMapper passwordMapper;

    private MockMvc mockMvc;
    private User testUser;
    private Password testPassword;
    private PasswordDto testPasswordDto;
    private PasswordCreationDto testPasswordCreationDto;
    private Category testCategory;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders
                .webAppContextSetup(context)
                .apply(springSecurity())
                .build();
        testUser = new User();
        testUser.setId(1L);
        testUser.setUsername("testuser");
        testUser.setEmail("test@example.com");

        testCategory = new Category();
        testCategory.setId(1L);
        testCategory.setName("Work");
        testCategory.setUser(testUser);

        testPassword = new Password();
        testPassword.setId(1L);
        testPassword.setTitle("Test Password");
        testPassword.setUsername("testusername");
        testPassword.setPasswordValue("encrypted_password");
        testPassword.setUrl("https://example.com");
        testPassword.setNotes("Test notes");
        testPassword.setUser(testUser);
        testPassword.setCategory(testCategory);
        testPassword.setCreatedAt(LocalDateTime.now());

        testPasswordDto = new PasswordDto();
        testPasswordDto.setId(1L);
        testPasswordDto.setTitle("Test Password");
        testPasswordDto.setUsername("testusername");
        testPasswordDto.setUrl("https://example.com");

        testPasswordCreationDto = new PasswordCreationDto();
        testPasswordCreationDto.setTitle("New Password");
        testPasswordCreationDto.setUsername("newusername");
        testPasswordCreationDto.setPassword("newpassword123");
        testPasswordCreationDto.setUrl("https://newsite.com");
        testPasswordCreationDto.setNotes("New password notes");
        testPasswordCreationDto.setCategoryId(1L);
    }

    @Test
    @WithMockUser
    void getUserPasswords_WithValidUserId_ShouldReturnPasswords() throws Exception {
        // Given
        List<Password> passwords = Arrays.asList(testPassword);
        List<PasswordDto> passwordDtos = Arrays.asList(testPasswordDto);
        
        when(userService.findById(1L)).thenReturn(Optional.of(testUser));
        when(passwordService.findByUserId(1L)).thenReturn(passwords);
        when(passwordMapper.toDto(testPassword)).thenReturn(testPasswordDto);

        // When & Then
        mockMvc.perform(get("/api/v1/passwords/user/1"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Passwords retrieved successfully"))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data[0].id").value(1))
                .andExpect(jsonPath("$.data[0].title").value("Test Password"))
                .andExpect(jsonPath("$.data[0].username").value("testusername"));

        verify(userService).findById(1L);
        verify(passwordService).findByUserId(1L);
        verify(passwordMapper).toDto(testPassword);
    }

    @Test
    @WithMockUser
    void getUserPasswords_WithInvalidUserId_ShouldReturnNotFound() throws Exception {
        // Given
        when(userService.findById(999L)).thenReturn(Optional.empty());

        // When & Then
        mockMvc.perform(get("/api/v1/passwords/user/999"))
                .andExpect(status().isNotFound());

        verify(userService).findById(999L);
        verifyNoInteractions(passwordService);
    }

    @Test
    @WithMockUser
    void getPasswordById_WithValidId_ShouldReturnPassword() throws Exception {
        // Given
        when(passwordService.findById(1L)).thenReturn(Optional.of(testPassword));
        when(passwordMapper.toDto(testPassword)).thenReturn(testPasswordDto);

        // When & Then
        mockMvc.perform(get("/api/v1/passwords/1"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Password retrieved successfully"))
                .andExpect(jsonPath("$.data.id").value(1))
                .andExpect(jsonPath("$.data.title").value("Test Password"));

        verify(passwordService).findById(1L);
        verify(passwordMapper).toDto(testPassword);
    }

    @Test
    @WithMockUser
    void getPasswordById_WithInvalidId_ShouldReturnNotFound() throws Exception {
        // Given
        when(passwordService.findById(999L)).thenReturn(Optional.empty());

        // When & Then
        mockMvc.perform(get("/api/v1/passwords/999"))
                .andExpect(status().isNotFound());

        verify(passwordService).findById(999L);
        verifyNoInteractions(passwordMapper);
    }

    @Test
    @WithMockUser
    void createPassword_WithValidData_ShouldCreatePassword() throws Exception {
        // Given
        when(userService.findById(1L)).thenReturn(Optional.of(testUser));
        when(categoryService.findById(1L)).thenReturn(Optional.of(testCategory));
        when(passwordMapper.toEntity(testPasswordCreationDto)).thenReturn(testPassword);
        when(passwordService.save(testPassword)).thenReturn(testPassword);
        when(passwordMapper.toDto(testPassword)).thenReturn(testPasswordDto);

        // When & Then
        mockMvc.perform(post("/api/v1/passwords/user/1")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(testPasswordCreationDto)))
                .andExpect(status().isCreated())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Password created successfully"))
                .andExpect(jsonPath("$.data.title").value("Test Password"));

        verify(userService).findById(1L);
        verify(categoryService).findById(1L);
        verify(passwordService).save(testPassword);
    }

    @Test
    @WithMockUser
    void createPassword_WithInvalidUserId_ShouldReturnNotFound() throws Exception {
        // Given
        when(userService.findById(999L)).thenReturn(Optional.empty());

        // When & Then
        mockMvc.perform(post("/api/v1/passwords/user/999")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(testPasswordCreationDto)))
                .andExpect(status().isNotFound());

        verify(userService).findById(999L);
        verifyNoInteractions(passwordService);
    }

    @Test
    @WithMockUser
    void createPassword_WithInvalidCategoryId_ShouldReturnInternalServerError() throws Exception {
        // Given
        testPasswordCreationDto.setCategoryId(999L);
        when(userService.findById(1L)).thenReturn(Optional.of(testUser));
        when(categoryService.findById(999L)).thenReturn(Optional.empty());

        // When & Then
        mockMvc.perform(post("/api/v1/passwords/user/1")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(testPasswordCreationDto)))
                .andExpect(status().isInternalServerError());

        verify(userService).findById(1L);
        verifyNoInteractions(passwordService);
    }

    @Test
    @WithMockUser
    void createPassword_WithCategoryFromDifferentUser_ShouldReturnInternalServerError() throws Exception {
        // Given
        User anotherUser = new User();
        anotherUser.setId(2L);
        testCategory.setUser(anotherUser);
        
        when(userService.findById(1L)).thenReturn(Optional.of(testUser));
        when(categoryService.findById(1L)).thenReturn(Optional.of(testCategory));

        // When & Then
        mockMvc.perform(post("/api/v1/passwords/user/1")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(testPasswordCreationDto)))
                .andExpect(status().isInternalServerError());

        verify(userService).findById(1L);
        verifyNoInteractions(passwordService);
    }

    @Test
    @WithMockUser
    void createPassword_WithInvalidData_ShouldReturnBadRequest() throws Exception {
        // Given - invalid password data (empty title)
        testPasswordCreationDto.setTitle("");

        // When & Then
        mockMvc.perform(post("/api/v1/passwords/user/1")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(testPasswordCreationDto)))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(userService, passwordService);
    }

    @Test
    @WithMockUser
    void deletePassword_WithValidId_ShouldDeletePassword() throws Exception {
        // Given
        when(passwordService.findById(1L)).thenReturn(Optional.of(testPassword));

        // When & Then
        mockMvc.perform(delete("/api/v1/passwords/1")
                .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Password deleted successfully"));

        verify(passwordService).findById(1L);
        verify(passwordService).delete(testPassword);
    }

    @Test
    @WithMockUser
    void deletePassword_WithInvalidId_ShouldReturnNotFound() throws Exception {
        // Given
        when(passwordService.findById(999L)).thenReturn(Optional.empty());

        // When & Then
        mockMvc.perform(delete("/api/v1/passwords/999")
                .with(csrf()))
                .andExpect(status().isNotFound());

        verify(passwordService).findById(999L);
        verify(passwordService, never()).delete(any());
    }

    @Test
    void getPasswordById_WithoutAuthentication_ShouldRedirectToLogin() throws Exception {
        // When & Then
        mockMvc.perform(get("/api/v1/passwords/1"))
                .andExpect(status().isFound())
                .andExpect(redirectedUrl("http://localhost/login"));

        verifyNoInteractions(passwordService);
    }
} 