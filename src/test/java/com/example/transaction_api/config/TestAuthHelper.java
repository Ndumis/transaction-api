package com.example.transaction_api.config;

import com.example.transaction_api.dto.AuthenticationRequest;
import com.example.transaction_api.dto.AuthenticationResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class TestAuthHelper {
    
    private final MockMvc mockMvc;
    private final ObjectMapper objectMapper;
    
    public TestAuthHelper(MockMvc mockMvc, ObjectMapper objectMapper) {
        this.mockMvc = mockMvc;
        this.objectMapper = objectMapper;
    }
    
    public String getAuthToken(String username, String password) throws Exception {
        AuthenticationRequest authRequest = new AuthenticationRequest();
        authRequest.setUsername(username);
        authRequest.setPassword(password);
        
        MvcResult result = mockMvc.perform(post("/api/v1/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(authRequest)))
                .andExpect(status().isOk())
                .andReturn();
        
        AuthenticationResponse authResponse = objectMapper.readValue(
            result.getResponse().getContentAsString(), 
            AuthenticationResponse.class
        );
        
        return authResponse.getAccessToken();
    }
}