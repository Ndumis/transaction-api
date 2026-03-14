package com.example.transaction_api.dto;

import com.example.transaction_api.model.UserStatus;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class UserResponse {
    private String id;
    private String username;
    private String email;
    private String firstName;
    private String lastName;
    private String phoneNumber;
    private UserStatus status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}