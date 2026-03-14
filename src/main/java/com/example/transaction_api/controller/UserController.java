package com.example.transaction_api.controller;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.transaction_api.dto.UserResponse;
import com.example.transaction_api.model.User;
import com.example.transaction_api.model.UserStatus;
import com.example.transaction_api.service.UserService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
@Tag(name = "User Management", description = "Endpoints for managing users")
public class UserController {    
    private final UserService userService;

    @PostMapping
    @Operation(summary = "Create a new user", description = "Creates a new user with the provided details")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "User created successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid input parameters"),
        @ApiResponse(responseCode = "409", description = "User already exists"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<User> createUser(@Valid @RequestBody User user) {
        log.info("POST /api/v1/users - Creating new user: {}", user.getUsername());
        User created = userService.createUser(user);
        return new ResponseEntity<>(created, HttpStatus.CREATED);
    }
    
    @GetMapping("/{id}")
    @Operation(summary = "Get user by ID", description = "Returns a single user")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "User found"),
        @ApiResponse(responseCode = "404", description = "User not found"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<UserResponse > getUserById(
            @Parameter(description = "User ID", required = true) @PathVariable String id) {
        log.info("GET /api/v1/users/{} - Fetching user", id);
        UserResponse  user = userService.getUserById(id);

        if(user == null) {
            log.warn("User with ID {} not found", id);
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        return ResponseEntity.ok(user);
    }

    @GetMapping("/username/{username}")
    @Operation(summary = "Get user by username", description = "Returns a user by username")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "User found"),
        @ApiResponse(responseCode = "404", description = "User not found"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<UserResponse > getUserByUsername(@PathVariable String username) {
        log.info("GET /api/v1/users/username/{} - Fetching user", username);
        UserResponse  user = userService.getUserByUsername(username);

        if(user == null) {
            log.warn("User with username {} not found", username);
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        return ResponseEntity.ok(user);
    }
    
    @GetMapping
    @Operation(summary = "Get all users", description = "Returns paginated list of all users")
    public ResponseEntity<Page<UserResponse >> getAllUsers(
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        log.info("GET /api/v1/users - Fetching all users, page: {}, size: {}", 
                 pageable.getPageNumber(), pageable.getPageSize());
        Page<UserResponse > users = userService.getAllUsers(pageable);
        return ResponseEntity.ok(users);
    }
    
    @GetMapping("/status/{status}")
    @Operation(summary = "Get users by status", description = "Returns users filtered by status")
    public ResponseEntity<Page<UserResponse >> getUsersByStatus(
            @PathVariable UserStatus status,
            @PageableDefault(size = 20) Pageable pageable) {
        log.info("GET /api/v1/users/status/{} - Fetching users by status", status);
        Page<UserResponse > users = userService.getUsersByStatus(status, pageable);
        return ResponseEntity.ok(users);
    }
    
    @GetMapping("/search")
    @Operation(summary = "Search users", description = "Search users by name or email")
    public ResponseEntity<Page<UserResponse >> searchUsers(
            @RequestParam String q,
            @PageableDefault(size = 20) Pageable pageable) {
        log.info("GET /api/v1/users/search - Searching users with query: {}", q);
        Page<UserResponse > users = userService.searchUsers(q, pageable);
        return ResponseEntity.ok(users);
    }
    
    @PutMapping("/{id}")
    @Operation(summary = "Update user", description = "Updates an existing user")
    public ResponseEntity<UserResponse> updateUser(
            @PathVariable String id,
            @Valid @RequestBody User userDetails) {
        log.info("PUT /api/v1/users/{} - Updating user", id);
        UserResponse updated = userService.updateUser(id, userDetails);
        return ResponseEntity.ok(updated);
    }
    
    @DeleteMapping("/{id}")
    @Operation(summary = "Delete user", description = "Deletes a user")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "204", description = "User deleted successfully"),
        @ApiResponse(responseCode = "404", description = "User not found")
    })
    public ResponseEntity<Void> deleteUser(@PathVariable String id) {
        log.info("DELETE /api/v1/users/{} - Deleting user", id);
        userService.deleteUser(id);
        return ResponseEntity.noContent().build();
    }
    
    @GetMapping("/{id}/validate")
    @Operation(summary = "Validate user", description = "Checks if a user exists")
    public ResponseEntity<Boolean> validateUser(@PathVariable String id) {
        boolean exists = userService.validateUserExists(id);
        return ResponseEntity.ok(exists);
    }

}
