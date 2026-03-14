// src/test/java/com/example/transaction_api/config/TestSecurityBeansConfig.java
package com.example.transaction_api.config;

import com.example.transaction_api.security.JwtService;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;

import static org.mockito.Mockito.mock;

@TestConfiguration
public class TestSecurityBeansConfig {
    
    @Bean
    @Primary
    public JwtService jwtService() {
        return mock(JwtService.class);
    }
    
    @Bean
    @Primary
    public UserDetailsService userDetailsService() {
        return new InMemoryUserDetailsManager(
            User.withUsername("john_doe")
                .password("password")
                .roles("USER")
                .build(),
            User.withUsername("admin")
                .password("password")
                .roles("ADMIN")
                .build()
        );
    }
    
    @Bean
    @Primary
    public AuthenticationProvider authenticationProvider() {
        return mock(AuthenticationProvider.class);
    }
}