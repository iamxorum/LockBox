package com.lockbox.controller.v1;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.lockbox.domain.model.User;
import com.lockbox.domain.service.AuditLogService;
import com.lockbox.domain.service.UserService;
import com.lockbox.dto.UserCreationDto;
import com.lockbox.dto.UserDto;
import com.lockbox.dto.UserStatsDto;
import com.lockbox.exception.ResourceAlreadyExistsException;
import com.lockbox.exception.ResourceNotFoundException;
import com.lockbox.mapper.UserMapper;
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
class UserControllerTest {

    @Autowired
    private WebApplicationContext context;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private UserService userService;

    @MockBean
    private UserMapper userMapper;

    @MockBean
    private AuditLogService auditLogService;

    private MockMvc mockMvc;
    private User testUser;
    private UserDto testUserDto;
    private UserCreationDto testUserCreationDto;

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
        testUser.setFirstName("Test");
        testUser.setLastName("User");

        testUserDto = new UserDto();
        testUserDto.setId(1L);
        testUserDto.setUsername("testuser");
        testUserDto.setEmail("test@example.com");

        testUserCreationDto = new UserCreationDto();
        testUserCreationDto.setUsername("newuser");
        testUserCreationDto.setEmail("new@example.com");
        testUserCreationDto.setFirstName("New");
        testUserCreationDto.setLastName("User");
        testUserCreationDto.setPassword("securePassword123");
    }

    @Test
    @WithMockUser
    void getUserById_WithValidId_ShouldReturnUser() throws Exception {
        // Given
        when(userService.findById(1L)).thenReturn(Optional.of(testUser));
        when(userMapper.toDto(testUser)).thenReturn(testUserDto);

        // When & Then
        mockMvc.perform(get("/api/v1/users/1"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("User retrieved successfully"))
                .andExpect(jsonPath("$.data.id").value(1))
                .andExpect(jsonPath("$.data.username").value("testuser"))
                .andExpect(jsonPath("$.data.email").value("test@example.com"));

        verify(userService).findById(1L);
        verify(userMapper).toDto(testUser);
    }

    @Test
    @WithMockUser
    void getUserById_WithInvalidId_ShouldReturnNotFound() throws Exception {
        // Given
        when(userService.findById(999L)).thenReturn(Optional.empty());

        // When & Then
        mockMvc.perform(get("/api/v1/users/999"))
                .andExpect(status().isNotFound());

        verify(userService).findById(999L);
        verifyNoInteractions(userMapper);
    }

    @Test
    @WithMockUser
    void createUser_WithValidData_ShouldCreateUser() throws Exception {
        // Given
        when(userService.existsByUsername(testUserCreationDto.getUsername())).thenReturn(false);
        when(userService.existsByEmail(testUserCreationDto.getEmail())).thenReturn(false);
        when(userMapper.toEntity(testUserCreationDto)).thenReturn(testUser);
        when(userService.save(testUser)).thenReturn(testUser);
        when(userMapper.toDto(testUser)).thenReturn(testUserDto);

        // When & Then
        mockMvc.perform(post("/api/v1/users")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(testUserCreationDto)))
                .andExpect(status().isCreated())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("User created successfully"))
                .andExpect(jsonPath("$.data.username").value("testuser"));

        verify(userService).existsByUsername(testUserCreationDto.getUsername());
        verify(userService).existsByEmail(testUserCreationDto.getEmail());
        verify(userService).save(testUser);
    }

    @Test
    @WithMockUser
    void createUser_WithExistingUsername_ShouldReturnConflict() throws Exception {
        // Given
        when(userService.existsByUsername(testUserCreationDto.getUsername())).thenReturn(true);

        // When & Then
        mockMvc.perform(post("/api/v1/users")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(testUserCreationDto)))
                .andExpect(status().isConflict());

        verify(userService).existsByUsername(testUserCreationDto.getUsername());
        verify(userService, never()).save(any());
    }

    @Test
    @WithMockUser
    void createUser_WithExistingEmail_ShouldReturnConflict() throws Exception {
        // Given
        when(userService.existsByUsername(testUserCreationDto.getUsername())).thenReturn(false);
        when(userService.existsByEmail(testUserCreationDto.getEmail())).thenReturn(true);

        // When & Then
        mockMvc.perform(post("/api/v1/users")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(testUserCreationDto)))
                .andExpect(status().isConflict());

        verify(userService).existsByUsername(testUserCreationDto.getUsername());
        verify(userService).existsByEmail(testUserCreationDto.getEmail());
        verify(userService, never()).save(any());
    }

    @Test
    @WithMockUser
    void createUser_WithInvalidData_ShouldReturnBadRequest() throws Exception {
        // Given - invalid user data (empty username)
        testUserCreationDto.setUsername("");

        // When & Then
        mockMvc.perform(post("/api/v1/users")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(testUserCreationDto)))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(userService);
    }

    @Test
    @WithMockUser
    void getUserStats_WithValidId_ShouldReturnStats() throws Exception {
        // Given
        UserStatsDto stats = new UserStatsDto(5L, 3L, 2L, 4L);
        when(userService.findById(1L)).thenReturn(Optional.of(testUser));
        when(userService.getUserStats(1L)).thenReturn(stats);

        // When & Then
        mockMvc.perform(get("/api/v1/users/1/stats"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("User statistics retrieved successfully"))
                .andExpect(jsonPath("$.data.passwords").value(5))
                .andExpect(jsonPath("$.data.secureNotes").value(3))
                .andExpect(jsonPath("$.data.categories").value(2))
                .andExpect(jsonPath("$.data.tags").value(4));

        verify(userService).findById(1L);
        verify(userService).getUserStats(1L);
    }

    @Test
    @WithMockUser
    void deleteUser_WithValidId_ShouldDeleteUser() throws Exception {
        // Given
        when(userService.findById(1L)).thenReturn(Optional.of(testUser));

        // When & Then
        mockMvc.perform(delete("/api/v1/users/1")
                .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("User deleted successfully"));

        verify(userService).findById(1L);
        verify(userService).deleteById(1L);
    }

    @Test
    @WithMockUser
    void updateUser_WithValidData_ShouldUpdateUser() throws Exception {
        // Given
        when(userService.findById(1L)).thenReturn(Optional.of(testUser));
        when(userService.save(testUser)).thenReturn(testUser);
        when(userMapper.toDto(testUser)).thenReturn(testUserDto);

        // When & Then
        mockMvc.perform(put("/api/v1/users/1")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(testUserCreationDto)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("User updated successfully"));

        verify(userService).findById(1L);
        verify(userService).save(testUser);
        verify(userMapper).updateEntityFromDto(testUserCreationDto, testUser);
    }

    @Test
    void getUserById_WithoutAuthentication_ShouldRedirectToLogin() throws Exception {
        // When & Then
        mockMvc.perform(get("/api/v1/users/1"))
                .andExpect(status().isFound())
                .andExpect(redirectedUrl("http://localhost/login"));

        verifyNoInteractions(userService);
    }
} 