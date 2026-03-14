package com.example.transaction_api.controller;
import com.example.transaction_api.dto.AuthenticationRequest;
import com.example.transaction_api.dto.AuthenticationResponse;
import com.example.transaction_api.security.AuthenticationService;
import com.example.transaction_api.dto.RegisterRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
@Tag(name = "Authentication", description = "Authentication endpoints for user login and registration")
public class AuthenticationController {
    private final AuthenticationService authenticationService;

    @PostMapping("/register")
    @Operation(summary = "Register a new user", description = "Creates a new user account")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "User registered successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid input parameters"),
        @ApiResponse(responseCode = "409", description = "Username or email already exists")
    })
    public ResponseEntity<AuthenticationResponse> register(
            @Valid @RequestBody RegisterRequest request) {
        log.info("POST /api/v1/auth/register - Registering new user: {}", request.getUsername());
        AuthenticationResponse response = authenticationService.register(request);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @PostMapping("/login")
    @Operation(summary = "Authenticate user", description = "Authenticates a user and returns JWT token")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Authentication successful"),
        @ApiResponse(responseCode = "401", description = "Invalid credentials")
    })
    public ResponseEntity<AuthenticationResponse> authenticate(
            @Valid @RequestBody AuthenticationRequest request) {
        log.info("POST /api/v1/auth/login - Authenticating user: {}", request.getUsername());
        AuthenticationResponse response = authenticationService.authenticate(request);
        return ResponseEntity.ok(response);
    }
    
    @PostMapping("/refresh")
    @Operation(summary = "Refresh access token", description = "Refreshes the access token using refresh token")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Token refreshed successfully"),
        @ApiResponse(responseCode = "401", description = "Invalid refresh token")
    })
    public ResponseEntity<AuthenticationResponse> refreshToken(
            @RequestHeader("Authorization") String authorizationHeader) {
        String refreshToken = authorizationHeader.substring(7); // Remove "Bearer " prefix
        log.info("POST /api/v1/auth/refresh - Refreshing token");
        AuthenticationResponse response = authenticationService.refreshToken(refreshToken);
        return ResponseEntity.ok(response);
    }
    
    @PostMapping("/logout")
    @Operation(summary = "Logout user", description = "Logs out the current user")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Logged out successfully")
    })
    public ResponseEntity<String> logout() {
        log.info("POST /api/v1/auth/logout - Logging out user");
        // In a stateless JWT setup, logout is handled client-side by removing the token
        // You could implement a token blacklist here if needed
        return ResponseEntity.ok("Logged out successfully");
    }
    
}
