package com.example.transaction_api.service;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.example.transaction_api.dto.UserResponse;
import com.example.transaction_api.exception.DuplicateResourceException;
import com.example.transaction_api.exception.ResourceNotFoundException;
import com.example.transaction_api.model.User;
import com.example.transaction_api.model.UserStatus;
import com.example.transaction_api.repository.UserRepository;

import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {    
    private final UserRepository userRepository;
    
    @Transactional
    public User createUser(User user) {
        log.info("Creating new user with username: {}", user.getUsername());
        
        // Check for duplicates
        if (userRepository.existsByUsername(user.getUsername())) {
            throw new DuplicateResourceException("Username already exists: " + user.getUsername());
        }
        
        if (userRepository.existsByEmail(user.getEmail())) {
            throw new DuplicateResourceException("Email already exists: " + user.getEmail());
        }
        
        return userRepository.save(user);
    }

    @Transactional(readOnly = true)
    public UserResponse  getUserById(String id) {
        return mapToResponse(userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + id)));
    }
    
    @Transactional(readOnly = true)
    public UserResponse getUserByUsername(String username) {
        return mapToResponse(userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with username: " + username)));
    }
    
    @Transactional(readOnly = true)
    public UserResponse getUserByEmail(String email) {
        return mapToResponse(userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + email)));
    }
    
    @Transactional(readOnly = true)
    public Page<UserResponse> getAllUsers(Pageable pageable) {
        return userRepository.findAll(pageable).map(this::mapToResponse);
    }
    
    @Transactional(readOnly = true)
    public Page<UserResponse> getUsersByStatus(UserStatus status, Pageable pageable) {
        return userRepository.findByStatus(status, pageable).map(this::mapToResponse);
    }

    @Transactional(readOnly = true)
    public Page<UserResponse> searchUsers(String searchTerm, Pageable pageable) {
        return userRepository.searchUsers(searchTerm, pageable).map(this::mapToResponse);
    }

    @Transactional
    public UserResponse updateUser(String id, User userDetails) {
        log.info("Updating user with id: {}", id);
        
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + id));
        
        // Update fields
        if (userDetails.getFirstName() != null) {
            user.setFirstName(userDetails.getFirstName());
        }
        
        if (userDetails.getLastName() != null) {
            user.setLastName(userDetails.getLastName());
        }
        
        if (userDetails.getPhoneNumber() != null) {
            user.setPhoneNumber(userDetails.getPhoneNumber());
        }
        
        if (userDetails.getStatus() != null) {
            user.setStatus(userDetails.getStatus());
        }
        
        // Email update requires duplicate check
        if (userDetails.getEmail() != null && !userDetails.getEmail().equals(user.getEmail())) {
            if (userRepository.existsByEmail(userDetails.getEmail())) {
                throw new DuplicateResourceException("Email already exists: " + userDetails.getEmail());
            }
            user.setEmail(userDetails.getEmail());
        }
        
        return mapToResponse(userRepository.save(user));
    }
    
    @Transactional
    public void deleteUser(String id) {
        log.info("Deleting user with id: {}", id);
        
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + id));
        userRepository.delete(user);
    }
    
    @Transactional(readOnly = true)
    public List<Object[]> getUserStatistics() {
        return userRepository.getUserCountByStatus();
    }
    
    @Transactional(readOnly = true)
    public boolean validateUserExists(String userId) {
        return userRepository.existsById(userId);
    }       
      
    private UserResponse mapToResponse(User user) {
        return UserResponse.builder()
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .phoneNumber(user.getPhoneNumber())
                .status(user.getStatus())
                .createdAt(user.getCreatedAt())
                .updatedAt(user.getUpdatedAt())
                .build();
    }
}
