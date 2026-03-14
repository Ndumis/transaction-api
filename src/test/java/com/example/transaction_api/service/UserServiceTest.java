package com.example.transaction_api.service;

import com.example.transaction_api.dto.UserResponse;
import com.example.transaction_api.exception.DuplicateResourceException;
import com.example.transaction_api.exception.ResourceNotFoundException;
import com.example.transaction_api.model.User;
import com.example.transaction_api.model.UserStatus;
import com.example.transaction_api.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {
    
    @Mock
    private UserRepository userRepository;
    
    @InjectMocks
    private UserService userService;
    
    private User user;
    private String userId;
    private LocalDateTime now;
    
    @BeforeEach
    void setUp() {
        now = LocalDateTime.now();
        userId = UUID.randomUUID().toString();
        
        user = User.builder()
                .id(userId)
                .username("john_doe")
                .email("john.doe@email.com")
                .password("encodedPassword")
                .firstName("John")
                .lastName("Doe")
                .phoneNumber("+27123456789")
                .status(UserStatus.ACTIVE)
                .createdAt(now)
                .updatedAt(now)
                .build();
    }
    
    @Test
    void createUser_Success() {
        when(userRepository.existsByUsername(user.getUsername())).thenReturn(false);
        when(userRepository.existsByEmail(user.getEmail())).thenReturn(false);
        when(userRepository.save(any(User.class))).thenReturn(user);
        
        User result = userService.createUser(user);
        
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(userId);
        assertThat(result.getUsername()).isEqualTo("john_doe");
        assertThat(result.getEmail()).isEqualTo("john.doe@email.com");
        
        verify(userRepository, times(1)).existsByUsername(user.getUsername());
        verify(userRepository, times(1)).existsByEmail(user.getEmail());
        verify(userRepository, times(1)).save(any(User.class));
    }
    
    @Test
    void createUser_WithDuplicateUsername_ThrowsException() {
        when(userRepository.existsByUsername(user.getUsername())).thenReturn(true);
        
        assertThatThrownBy(() -> userService.createUser(user))
                .isInstanceOf(DuplicateResourceException.class)
                .hasMessageContaining("Username already exists");
        
        verify(userRepository, times(1)).existsByUsername(user.getUsername());
        verify(userRepository, never()).existsByEmail(anyString());
        verify(userRepository, never()).save(any(User.class));
    }
    
    @Test
    void createUser_WithDuplicateEmail_ThrowsException() {
        when(userRepository.existsByUsername(user.getUsername())).thenReturn(false);
        when(userRepository.existsByEmail(user.getEmail())).thenReturn(true);
        
        assertThatThrownBy(() -> userService.createUser(user))
                .isInstanceOf(DuplicateResourceException.class)
                .hasMessageContaining("Email already exists");
        
        verify(userRepository, times(1)).existsByUsername(user.getUsername());
        verify(userRepository, times(1)).existsByEmail(user.getEmail());
        verify(userRepository, never()).save(any(User.class));
    }
    
    @Test
    void getUserById_Success() {
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        
        UserResponse result = userService.getUserById(userId);
        
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(userId);
        assertThat(result.getUsername()).isEqualTo("john_doe");
        
        verify(userRepository, times(1)).findById(userId);
    }
    
    @Test
    void getUserById_NotFound_ThrowsException() {
        when(userRepository.findById(userId)).thenReturn(Optional.empty());
        
        assertThatThrownBy(() -> userService.getUserById(userId))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("User not found with id");
        
        verify(userRepository, times(1)).findById(userId);
    }
    
    @Test
    void getUserByUsername_Success() {
        when(userRepository.findByUsername("john_doe")).thenReturn(Optional.of(user));
        
        UserResponse result = userService.getUserByUsername("john_doe");
        
        assertThat(result).isNotNull();
        assertThat(result.getUsername()).isEqualTo("john_doe");
        
        verify(userRepository, times(1)).findByUsername("john_doe");
    }
    
    @Test
    void getUserByUsername_NotFound_ThrowsException() {
        when(userRepository.findByUsername("john_doe")).thenReturn(Optional.empty());
        
        assertThatThrownBy(() -> userService.getUserByUsername("john_doe"))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("User not found with username");
        
        verify(userRepository, times(1)).findByUsername("john_doe");
    }
    
    @Test
    void getUserByEmail_Success() {
        when(userRepository.findByEmail("john.doe@email.com")).thenReturn(Optional.of(user));
        
        UserResponse result = userService.getUserByEmail("john.doe@email.com");
        
        assertThat(result).isNotNull();
        assertThat(result.getEmail()).isEqualTo("john.doe@email.com");
        
        verify(userRepository, times(1)).findByEmail("john.doe@email.com");
    }
    
    @Test
    void getUserByEmail_NotFound_ThrowsException() {
        when(userRepository.findByEmail("john.doe@email.com")).thenReturn(Optional.empty());
        
        assertThatThrownBy(() -> userService.getUserByEmail("john.doe@email.com"))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("User not found with email");
        
        verify(userRepository, times(1)).findByEmail("john.doe@email.com");
    }
    
    @Test
    void getAllUsers_Success() {
        Pageable pageable = PageRequest.of(0, 10);
        List<User> users = Arrays.asList(user);
        Page<User> page = new PageImpl<>(users);
        
        when(userRepository.findAll(pageable)).thenReturn(page);
        
        Page<UserResponse> result = userService.getAllUsers(pageable);
        
        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getUsername()).isEqualTo("john_doe");
        
        verify(userRepository, times(1)).findAll(pageable);
    }
    
    @Test
    void getUsersByStatus_Success() {
        Pageable pageable = PageRequest.of(0, 10);
        List<User> users = Arrays.asList(user);
        Page<User> page = new PageImpl<>(users);
        
        when(userRepository.findByStatus(UserStatus.ACTIVE, pageable)).thenReturn(page);
        
        Page<UserResponse> result = userService.getUsersByStatus(UserStatus.ACTIVE, pageable);
        
        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getStatus()).isEqualTo(UserStatus.ACTIVE);
        
        verify(userRepository, times(1)).findByStatus(UserStatus.ACTIVE, pageable);
    }
    
    @Test
    void searchUsers_Success() {
        Pageable pageable = PageRequest.of(0, 10);
        List<User> users = Arrays.asList(user);
        Page<User> page = new PageImpl<>(users);
        
        when(userRepository.searchUsers("john", pageable)).thenReturn(page);
        
        Page<UserResponse> result = userService.searchUsers("john", pageable);
        
        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(1);
        
        verify(userRepository, times(1)).searchUsers("john", pageable);
    }
    
    @Test
    void updateUser_Success() {
        User updatedDetails = User.builder()
                .firstName("Jonathan")
                .lastName("Updated")
                .phoneNumber("+27999999999")
                .status(UserStatus.ACTIVE)
                .build();
        
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(userRepository.save(any(User.class))).thenReturn(user);
        
        UserResponse result = userService.updateUser(userId, updatedDetails);
        
        assertThat(result).isNotNull();
        assertThat(result.getFirstName()).isEqualTo("Jonathan");
        assertThat(result.getLastName()).isEqualTo("Updated");
        assertThat(result.getPhoneNumber()).isEqualTo("+27999999999");
        
        verify(userRepository, times(1)).findById(userId);
        verify(userRepository, times(1)).save(any(User.class));
    }
    
    @Test
    void updateUser_WithEmailChange_Success() {
        User updatedDetails = User.builder()
                .email("new.email@email.com")
                .firstName("John")
                .lastName("Doe")
                .build();
        
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(userRepository.existsByEmail("new.email@email.com")).thenReturn(false);
        when(userRepository.save(any(User.class))).thenReturn(user);
        
        UserResponse result = userService.updateUser(userId, updatedDetails);
        
        assertThat(result).isNotNull();
        assertThat(result.getEmail()).isEqualTo("new.email@email.com");
        
        verify(userRepository, times(1)).findById(userId);
        verify(userRepository, times(1)).existsByEmail("new.email@email.com");
        verify(userRepository, times(1)).save(any(User.class));
    }
    
    @Test
    void updateUser_WithDuplicateEmail_ThrowsException() {
        User updatedDetails = User.builder()
                .email("existing@email.com")
                .build();
        
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(userRepository.existsByEmail("existing@email.com")).thenReturn(true);
        
        assertThatThrownBy(() -> userService.updateUser(userId, updatedDetails))
                .isInstanceOf(DuplicateResourceException.class)
                .hasMessageContaining("Email already exists");
        
        verify(userRepository, times(1)).findById(userId);
        verify(userRepository, times(1)).existsByEmail("existing@email.com");
        verify(userRepository, never()).save(any(User.class));
    }
    
    @Test
    void deleteUser_Success() {
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        doNothing().when(userRepository).delete(user);
        
        userService.deleteUser(userId);
        
        verify(userRepository, times(1)).findById(userId);
        verify(userRepository, times(1)).delete(user);
    }
    
    @Test
    void deleteUser_NotFound_ThrowsException() {
        when(userRepository.findById(userId)).thenReturn(Optional.empty());
        
        assertThatThrownBy(() -> userService.deleteUser(userId))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("User not found");
        
        verify(userRepository, times(1)).findById(userId);
        verify(userRepository, never()).delete(any(User.class));
    }
    
    @Test
    void getUserStatistics_Success() {
        List<Object[]> mockStats = Arrays.asList(
            new Object[]{UserStatus.ACTIVE, 2L},
            new Object[]{UserStatus.INACTIVE, 1L}
        );
        
        when(userRepository.getUserCountByStatus()).thenReturn(mockStats);
        
        List<Object[]> result = userService.getUserStatistics();
        
        assertThat(result).isNotNull();
        assertThat(result).hasSize(2);
        
        verify(userRepository, times(1)).getUserCountByStatus();
    }
    
    @Test
    void validateUserExists_ReturnsTrue_WhenExists() {
        when(userRepository.existsById(userId)).thenReturn(true);
        
        boolean result = userService.validateUserExists(userId);
        
        assertThat(result).isTrue();
        verify(userRepository, times(1)).existsById(userId);
    }
    
    @Test
    void validateUserExists_ReturnsFalse_WhenNotExists() {
        when(userRepository.existsById(userId)).thenReturn(false);
        
        boolean result = userService.validateUserExists(userId);
        
        assertThat(result).isFalse();
        verify(userRepository, times(1)).existsById(userId);
    }
}