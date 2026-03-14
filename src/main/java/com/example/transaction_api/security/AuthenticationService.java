package com.example.transaction_api.security;

import com.example.transaction_api.exception.DuplicateResourceException;
import com.example.transaction_api.exception.InvalidCredentialsException;
import com.example.transaction_api.dto.AuthenticationRequest;
import com.example.transaction_api.dto.AuthenticationResponse;
import com.example.transaction_api.dto.RegisterRequest;
import com.example.transaction_api.model.User;
import com.example.transaction_api.model.UserStatus;
import com.example.transaction_api.repository.UserRepository;
import com.example.transaction_api.security.JwtService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthenticationService {    
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;

     @Transactional
    public AuthenticationResponse register(RegisterRequest request) {
        log.info("Registering new user with username: {}", request.getUsername());
        
        // Check if username already exists
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new DuplicateResourceException("Username already exists: " + request.getUsername());
        }
        
        // Check if email already exists
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new DuplicateResourceException("Email already exists: " + request.getEmail());
        }
        
        // Create new user
        var user = User.builder()
                .username(request.getUsername())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .phoneNumber(request.getPhoneNumber())
                .status(UserStatus.ACTIVE)
                .build();
        
        userRepository.save(user);
        
        // Generate tokens
        var jwtToken = jwtService.generateToken(user);
        var refreshToken = jwtService.generateRefreshToken(user);
        
        log.info("User registered successfully: {}", request.getUsername());
        
        return AuthenticationResponse.builder()
                .accessToken(jwtToken)
                .refreshToken(refreshToken)
                .tokenType("Bearer")
                .expiresIn(86400000L) // 24 hours in milliseconds
                .username(user.getUsername())
                .message("User registered successfully")
                .build();
    }
    
    public AuthenticationResponse authenticate(AuthenticationRequest request) {
        log.info("Authenticating user: {}", request.getUsername());
        
        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            request.getUsername(),
                            request.getPassword()
                    )
            );
        } catch (Exception e) {
            log.error("Authentication failed for user: {}", request.getUsername());
            throw new InvalidCredentialsException("Invalid username or password");
        }
        
        var user = userRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));
        
        var jwtToken = jwtService.generateToken(user);
        var refreshToken = jwtService.generateRefreshToken(user);
        
        log.info("User authenticated successfully: {}", request.getUsername());
        
        return AuthenticationResponse.builder()
                .accessToken(jwtToken)
                .refreshToken(refreshToken)
                .tokenType("Bearer")
                .expiresIn(86400000L)
                .username(user.getUsername())
                .message("Authentication successful")
                .build();
    }
    
    public AuthenticationResponse refreshToken(String refreshToken) {
        log.info("Refreshing token");
        
        String username = jwtService.extractUsername(refreshToken);
        var user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));
        
        if (jwtService.isTokenValid(refreshToken, user)) {
            var jwtToken = jwtService.generateToken(user);
            
            return AuthenticationResponse.builder()
                    .accessToken(jwtToken)
                    .refreshToken(refreshToken)
                    .tokenType("Bearer")
                    .expiresIn(86400000L)
                    .username(user.getUsername())
                    .message("Token refreshed successfully")
                    .build();
        } else {
            throw new InvalidCredentialsException("Invalid refresh token");
        }
    }
}
