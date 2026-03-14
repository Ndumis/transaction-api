package com.example.transaction_api.controller;

import com.example.transaction_api.dto.UserResponse;
import com.example.transaction_api.exception.DuplicateResourceException;
import com.example.transaction_api.exception.ResourceNotFoundException;
import com.example.transaction_api.model.User;
import com.example.transaction_api.model.UserStatus;
import com.example.transaction_api.security.JwtService;
import com.example.transaction_api.service.UserService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@WithMockUser(username = "admin", roles = "ADMIN")
class UserControllerTest {
    
    @Autowired
    private MockMvc mockMvc;
    
    @MockitoBean
    private UserService userService;
    
    @MockitoBean
    private JwtService jwtService;
    
    private ObjectMapper objectMapper;
    private User validUser;
    private UserResponse userResponse;
    private String userId;
    private LocalDateTime now;
    
    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        
        now = LocalDateTime.now();
        userId = UUID.randomUUID().toString();
        
        validUser = User.builder()
                .id(userId)
                .username("kay_sime")
                .email("kay.sime@email.com")
                .password("password123")
                .firstName("Kay")
                .lastName("Sime")
                .phoneNumber("+27123456789")
                .status(UserStatus.ACTIVE)
                .createdAt(now)
                .updatedAt(now)
                .build();
        
        userResponse = UserResponse.builder()
                .id(userId)
                .username("kay_sime")
                .email("kay.sime@email.com")
                .firstName("Kay")
                .lastName("Sime")
                .phoneNumber("+27123456789")
                .status(UserStatus.ACTIVE)
                .createdAt(now)
                .updatedAt(now)
                .build();
    }
    
    @Test
    void createUser_WithValidUser_ReturnsCreated() throws Exception {
        when(userService.createUser(any(User.class))).thenReturn(validUser);
        
        mockMvc.perform(post("/api/v1/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(validUser)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(userId))
                .andExpect(jsonPath("$.username").value("kay_sime"))
                .andExpect(jsonPath("$.email").value("kay.sime@email.com"))
                .andExpect(jsonPath("$.firstName").value("Kay"))
                .andExpect(jsonPath("$.lastName").value("Sime"))
                .andExpect(jsonPath("$.status").value("ACTIVE"));
        
        verify(userService, times(1)).createUser(any(User.class));
    }
    
    @Test
    void createUser_WithBlankUsername_ReturnsBadRequest() throws Exception {
        User invalidUser = User.builder()
                .username("")
                .email("kay.sime@email.com")
                .password("password123")
                .firstName("Kay")
                .lastName("Sime")
                .build();
        
        mockMvc.perform(post("/api/v1/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidUser)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Invalid input parameters"))
                .andExpect(jsonPath("$.validationErrors").exists())
                .andExpect(jsonPath("$.validationErrors.username").exists());
        
        verify(userService, never()).createUser(any(User.class));
    }
    
    @Test
    void createUser_WithUsernameTooShort_ReturnsBadRequest() throws Exception {
        User invalidUser = User.builder()
                .username("ab") // Too short
                .email("kay.sime@email.com")
                .password("password123")
                .firstName("Kay")
                .lastName("Sime")
                .build();
        
        mockMvc.perform(post("/api/v1/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidUser)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Invalid input parameters"))
                .andExpect(jsonPath("$.validationErrors").exists())
                .andExpect(jsonPath("$.validationErrors.username").exists());
        
        verify(userService, never()).createUser(any(User.class));
    }
    
    @Test
    void createUser_WithUsernameTooLong_ReturnsBadRequest() throws Exception {
        User invalidUser = User.builder()
                .username("a".repeat(51)) // Too long (max 50)
                .email("kay.sime@email.com")
                .password("password123")
                .firstName("Kay")
                .lastName("Sime")
                .build();
        
        mockMvc.perform(post("/api/v1/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidUser)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Invalid input parameters"))
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.validationErrors").exists())
                .andExpect(jsonPath("$.validationErrors.username").exists());
        
        verify(userService, never()).createUser(any(User.class));
    }
    
    @Test
    void createUser_WithInvalidEmail_ReturnsBadRequest() throws Exception {
        User invalidUser = User.builder()
                .username("kay_sime")
                .email("invalid-email")
                .password("password123")
                .firstName("Kay")
                .lastName("Sime")
                .build();
        
        mockMvc.perform(post("/api/v1/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidUser)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Invalid input parameters"))
                .andExpect(jsonPath("$.validationErrors").exists())
                .andExpect(jsonPath("$.validationErrors.email").exists());
        
        verify(userService, never()).createUser(any(User.class));
    }
    
    @Test
    void createUser_WithBlankFirstName_ReturnsBadRequest() throws Exception {
        User invalidUser = User.builder()
                .username("kay_sime")
                .email("kay.sime@email.com")
                .password("password123")
                .firstName("")
                .lastName("Sime")
                .build();
        
        mockMvc.perform(post("/api/v1/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidUser)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Invalid input parameters"))
                .andExpect(jsonPath("$.validationErrors").exists())
                .andExpect(jsonPath("$.validationErrors.firstName").exists());
        
        verify(userService, never()).createUser(any(User.class));
    }

    @Test
    void createUser_WithBlankLastName_ReturnsBadRequest() throws Exception {
        User invalidUser = User.builder()
                .username("kay_sime")
                .email("kay.sime@email.com")
                .password("password123")
                .firstName("Kay")
                .lastName("") // Blank last name
                .build();
                
        mockMvc.perform(post("/api/v1/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidUser)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Invalid input parameters"))
                .andExpect(jsonPath("$.validationErrors").exists())
                .andExpect(jsonPath("$.validationErrors.lastName").exists());
        
        verify(userService, never()).createUser(any(User.class));
    }
    
    @Test
    void createUser_WithBlankPassword_ReturnsBadRequest() throws Exception {
        User invalidUser = User.builder()
                .username("kay_sime")
                .email("kay.sime@email.com")
                .password("") // Blank password
                .firstName("Kay")
                .lastName("Sime")
                .build();
        
        mockMvc.perform(post("/api/v1/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidUser)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Invalid input parameters"))
                .andExpect(jsonPath("$.validationErrors").exists())
                .andExpect(jsonPath("$.validationErrors.password").exists());
        
        verify(userService, never()).createUser(any(User.class));
    }
    
    @Test
    void createUser_WithInvalidPhoneNumber_ReturnsBadRequest() throws Exception {
        User invalidUser = User.builder()
                .username("kay_sime")
                .email("kay.sime@email.com")
                .password("password123")
                .firstName("Kay")
                .lastName("Sime")
                .phoneNumber("123") // Invalid phone number format
                .build();
        
        mockMvc.perform(post("/api/v1/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidUser)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").value("Invalid input parameters"))
                .andExpect(jsonPath("$.validationErrors").exists())
                .andExpect(jsonPath("$.validationErrors.phoneNumber").exists());
        
        verify(userService, never()).createUser(any(User.class));
    }
    
    @Test
    void createUser_WithMultipleValidationErrors_ReturnsBadRequest() throws Exception {
        User invalidUser = User.builder()
                .username("") // Blank
                .email("invalid") // Invalid email
                .password("") // Blank
                .firstName("") // Blank
                .lastName("") // Blank
                .build();
        
        mockMvc.perform(post("/api/v1/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidUser)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.validationErrors").exists())
                .andExpect(jsonPath("$.validationErrors.username").exists())
                .andExpect(jsonPath("$.validationErrors.email").exists())
                .andExpect(jsonPath("$.validationErrors.password").exists())
                .andExpect(jsonPath("$.validationErrors.firstName").exists())
                .andExpect(jsonPath("$.validationErrors.lastName").exists());
        
        verify(userService, never()).createUser(any(User.class));
    }
    
    @Test
    void createUser_WithDuplicateUsername_ReturnsConflict() throws Exception {
        doThrow(new DuplicateResourceException("Username already exists: " + validUser.getUsername()))
                .when(userService).createUser(any(User.class));
        
        mockMvc.perform(post("/api/v1/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(validUser)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message", containsString("Username already exists")))
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.error").value("Conflict"));
        
        verify(userService, times(1)).createUser(any(User.class));
    }
    
    @Test
    void updateUser_WithValidData_ReturnsOk() throws Exception {
        User updatedUser = User.builder()
                .id(userId) 
                .username("kay_sime") 
                .email("kay.sime@email.com")
                .password("password123")
                .firstName("Jonathan")
                .lastName("Updated")
                .phoneNumber("+27999999999")
                .status(UserStatus.ACTIVE)
                .build();
        
        UserResponse expectedResponse = UserResponse.builder()
                .id(userId)
                .username("kay_sime")
                .email("kay.sime@email.com")
                .firstName("Jonathan")
                .lastName("Updated")
                .phoneNumber("+27999999999")
                .status(UserStatus.ACTIVE)
                .createdAt(now)
                .updatedAt(now)
                .build();
        
        when(userService.updateUser(eq(userId), any(User.class))).thenReturn(expectedResponse);
        
        mockMvc.perform(put("/api/v1/users/{id}", userId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updatedUser)))  // Send full user
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.firstName").value("Jonathan"))
                .andExpect(jsonPath("$.lastName").value("Updated"))
                .andExpect(jsonPath("$.phoneNumber").value("+27999999999"));
        
        verify(userService, times(1)).updateUser(eq(userId), any(User.class));
    }
    
    @Test
    void updateUser_WithInvalidData_ReturnsBadRequest() throws Exception {
        User invalidUpdate = User.builder()
                .firstName("") // Blank first name
                .lastName("") // Blank last name
                .phoneNumber("invalid") // Invalid phone
                .build();
        
        mockMvc.perform(put("/api/v1/users/{id}", userId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidUpdate)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));
        
        verify(userService, never()).updateUser(any(), any());
    }
    
    @Test
    void getUserById_ReturnsOk() throws Exception {
        when(userService.getUserById(userId)).thenReturn(userResponse);
        
        mockMvc.perform(get("/api/v1/users/{id}", userId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(userId))
                .andExpect(jsonPath("$.username").value("kay_sime"))
                .andExpect(jsonPath("$.email").value("kay.sime@email.com"));
        
        verify(userService, times(1)).getUserById(userId);
    }
    
    @Test
    void getUserById_NotFound_Returns404() throws Exception {
        when(userService.getUserById(userId))
                .thenThrow(new ResourceNotFoundException("User not found with id: " + userId));
        
        mockMvc.perform(get("/api/v1/users/{id}", userId))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message", containsString("User not found")))
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.error").value("Not Found"));
        
        verify(userService, times(1)).getUserById(userId);
    }
    
    @Test
    void getUserByUsername_ReturnsOk() throws Exception {
        when(userService.getUserByUsername("kay_sime")).thenReturn(userResponse);
        
        mockMvc.perform(get("/api/v1/users/username/{username}", "kay_sime"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("kay_sime"))
                .andExpect(jsonPath("$.email").value("kay.sime@email.com"));
        
        verify(userService, times(1)).getUserByUsername("kay_sime");
    }
    
    @Test
    void getUserByUsername_NotFound_Returns404() throws Exception {
        when(userService.getUserByUsername("kay_sime"))
                .thenThrow(new ResourceNotFoundException("User not found with username: kay_sime"));
        
        mockMvc.perform(get("/api/v1/users/username/{username}", "kay_sime"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message", containsString("User not found")))
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.error").value("Not Found"));
        
        verify(userService, times(1)).getUserByUsername("kay_sime");
    }
    
    @Test
    void getAllUsers_ReturnsOk() throws Exception {
        Pageable pageable = PageRequest.of(0, 20);
        List<UserResponse> users = Arrays.asList(userResponse);
        Page<UserResponse> page = new PageImpl<>(users);
        
        when(userService.getAllUsers(any(Pageable.class))).thenReturn(page);
        
        mockMvc.perform(get("/api/v1/users")
                .param("page", "0")
                .param("size", "20"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].id").value(userId))
                .andExpect(jsonPath("$.content[0].username").value("kay_sime"))
                .andExpect(jsonPath("$.totalElements").value(1));
        
        verify(userService, times(1)).getAllUsers(any(Pageable.class));
    }
    
    @Test
    void getUsersByStatus_ReturnsOk() throws Exception {
        Pageable pageable = PageRequest.of(0, 20);
        List<UserResponse> users = Arrays.asList(userResponse);
        Page<UserResponse> page = new PageImpl<>(users);
        
        when(userService.getUsersByStatus(eq(UserStatus.ACTIVE), any(Pageable.class))).thenReturn(page);
        
        mockMvc.perform(get("/api/v1/users/status/{status}", "ACTIVE")
                .param("page", "0")
                .param("size", "20"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].status").value("ACTIVE"));
        
        verify(userService, times(1)).getUsersByStatus(eq(UserStatus.ACTIVE), any(Pageable.class));
    }
    
    @Test
    void searchUsers_ReturnsOk() throws Exception {
        UserResponse searchUser = UserResponse.builder()
                .id(UUID.randomUUID().toString())
                .username("john_doe")
                .email("john.doe@email.com")
                .firstName("John")
                .lastName("Doe")
                .status(UserStatus.ACTIVE)
                .build();
        
        Pageable pageable = PageRequest.of(0, 20);
        List<UserResponse> users = Arrays.asList(searchUser);
        Page<UserResponse> page = new PageImpl<>(users);
        
        when(userService.searchUsers(eq("john"), any(Pageable.class))).thenReturn(page);
        
        mockMvc.perform(get("/api/v1/users/search")
                .param("q", "john")
                .param("page", "0")
                .param("size", "20"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].username").value("john_doe"));
        
        verify(userService, times(1)).searchUsers(eq("john"), any(Pageable.class));
    }
    
    @Test
    void deleteUser_ReturnsNoContent() throws Exception {
        doNothing().when(userService).deleteUser(userId);
        
        mockMvc.perform(delete("/api/v1/users/{id}", userId))
                .andExpect(status().isNoContent());
        
        verify(userService, times(1)).deleteUser(userId);
    }
    
    @Test
    void deleteUser_NotFound_Returns404() throws Exception {
        doThrow(new ResourceNotFoundException("User not found with id: " + userId))
                .when(userService).deleteUser(userId);
        
        mockMvc.perform(delete("/api/v1/users/{id}", userId))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message", containsString("User not found")));
        
        verify(userService, times(1)).deleteUser(userId);
    }
    
    @Test
    void validateUser_ReturnsTrue() throws Exception {
        when(userService.validateUserExists(userId)).thenReturn(true);
        
        mockMvc.perform(get("/api/v1/users/{id}/validate", userId))
                .andExpect(status().isOk())
                .andExpect(content().string("true"));
        
        verify(userService, times(1)).validateUserExists(userId);
    }
    
    @Test
    void validateUser_ReturnsFalse() throws Exception {
        when(userService.validateUserExists(userId)).thenReturn(false);
        
        mockMvc.perform(get("/api/v1/users/{id}/validate", userId))
                .andExpect(status().isOk())
                .andExpect(content().string("false"));
        
        verify(userService, times(1)).validateUserExists(userId);
    }
    
    @Test
    @WithMockUser(username = "user", roles = "USER")
    void accessWithUserRole_ReturnsOk() throws Exception {
        when(userService.getUserById(userId)).thenReturn(userResponse);
        
        mockMvc.perform(get("/api/v1/users/{id}", userId))
                .andExpect(status().isOk());
    }
}