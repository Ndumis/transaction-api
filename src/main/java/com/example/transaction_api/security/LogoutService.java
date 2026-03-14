package com.example.transaction_api.security;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.logout.LogoutHandler;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class LogoutService implements LogoutHandler{
    @Override
    public void logout(
            HttpServletRequest request,
            HttpServletResponse response,
            Authentication authentication
    ) {
        final String authHeader = request.getHeader("Authorization");
        final String jwt;
        
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return;
        }
        
        jwt = authHeader.substring(7);
        
        // In a production system, you might want to invalidate the token
        // by adding it to a blacklist or revoking it in a token store
        log.info("Logging out user with token: {}", jwt.substring(0, Math.min(10, jwt.length())) + "...");
        
        // Clear the security context
        SecurityContextHolder.clearContext();
        log.info("User logged out successfully");
    }
}
