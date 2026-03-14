package com.example.transaction_api.controller;

import com.example.transaction_api.security.JwtService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.example.transaction_api.dto.AggregatedTransactionResponse;
import com.example.transaction_api.dto.TransactionRequest;
import com.example.transaction_api.exception.TransactionNotFoundException;
import com.example.transaction_api.model.PaymentMethod;
import com.example.transaction_api.model.Transaction;
import com.example.transaction_api.model.Transaction.TransactionCategory;
import com.example.transaction_api.model.Transaction.TransactionStatus;
import com.example.transaction_api.model.User;
import com.example.transaction_api.model.UserStatus;
import com.example.transaction_api.service.TransactionService;

@SpringBootTest
@AutoConfigureMockMvc
@WithMockUser(username = "john_doe", roles = "USER")
public class TransactionControllerTest {    
    @Autowired
    private MockMvc mockMvc;
    
    @MockitoBean 
    private TransactionService transactionService;
    
    @MockitoBean 
    private JwtService jwtService;
    
    private ObjectMapper objectMapper;
    private User user;
    private Transaction transaction;
    private TransactionRequest request;
    private LocalDateTime now;
    private String userId;
    private String transactionId;
    
    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        
        now = LocalDateTime.now();
        userId = "user123";
        transactionId = UUID.randomUUID().toString();
        
        // Create test user
        user = User.builder()
                .id(userId)
                .username("john_doe")
                .email("john.doe@email.com")
                .firstName("John")
                .lastName("Doe")
                .status(UserStatus.ACTIVE)
                .build();
        
        // Create test transaction
        transaction = Transaction.builder()
                .id(transactionId)
                .user(user)
                .amount(new BigDecimal("100.00"))
                .currency("ZAR")
                .transactionDate(now)
                .description("Test transaction")
                .paymentMethod(PaymentMethod.CREDIT_CARD)
                .category(TransactionCategory.SHOPPING)
                .status(TransactionStatus.COMPLETED)
                .createdAt(now)
                .updatedAt(now)
                .build();
        
        // Create request
        request = new TransactionRequest();
        request.setUserId(userId);
        request.setAmount(new BigDecimal("100.00"));
        request.setCurrency("ZAR");
        request.setTransactionDate(now);
        request.setDescription("Test transaction");
        request.setPaymentMethod("CREDIT_CARD");
        request.setCategory("SHOPPING");
    }
    
    @Test
    void createTransaction_ReturnsCreated() throws Exception {
        when(transactionService.createTransaction(any(TransactionRequest.class)))
                .thenReturn(transaction);
        
        mockMvc.perform(post("/api/v1/transactions")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(transactionId))
                .andExpect(jsonPath("$.user.id").value(userId))
                .andExpect(jsonPath("$.user.username").value("john_doe"))
                .andExpect(jsonPath("$.amount").value(100.00))
                .andExpect(jsonPath("$.currency").value("ZAR"))
                .andExpect(jsonPath("$.paymentMethod").value("CREDIT_CARD"))
                .andExpect(jsonPath("$.category").value("SHOPPING"))
                .andExpect(jsonPath("$.status").value("COMPLETED"));
    }
    
    @Test
    void createTransaction_WithInvalidInput_ReturnsBadRequest() throws Exception {
        request.setAmount(null); // Invalid - amount is required
        
        mockMvc.perform(post("/api/v1/transactions")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }
    
    @Test
    void createTransaction_WithInvalidCurrency_ReturnsBadRequest() throws Exception {
        request.setCurrency("INVALID"); // Invalid currency
        
        mockMvc.perform(post("/api/v1/transactions")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }
    
    @Test
    void getTransaction_ReturnsOk() throws Exception {
        when(transactionService.getTransactionById(transactionId))
                .thenReturn(transaction);
        
        mockMvc.perform(get("/api/v1/transactions/{id}", transactionId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(transactionId))
                .andExpect(jsonPath("$.user.id").value(userId))
                .andExpect(jsonPath("$.user.username").value("john_doe"));
    }
    
    @Test
    void getUserTransactions_ReturnsOk() throws Exception {
        Pageable pageable = PageRequest.of(0, 20);
        Page<Transaction> page = new PageImpl<>(Arrays.asList(transaction));
        
        when(transactionService.getTransactionsByUserId(eq(userId), any(Pageable.class)))
                .thenReturn(page);
        
        mockMvc.perform(get("/api/v1/transactions/user/{userId}", userId)
                .param("page", "0")
                .param("size", "20"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].id").value(transactionId))
                .andExpect(jsonPath("$.content[0].user.id").value(userId))
                .andExpect(jsonPath("$.content[0].user.username").value("john_doe"));
    }
    
    @Test
    void getUserTransactions_WithPagination_ReturnsOk() throws Exception {
        Pageable pageable = PageRequest.of(1, 5);
        Page<Transaction> page = new PageImpl<>(Arrays.asList(transaction));
        
        when(transactionService.getTransactionsByUserId(eq(userId), any(Pageable.class)))
                .thenReturn(page);
        
        mockMvc.perform(get("/api/v1/transactions/user/{userId}", userId)
                .param("page", "1")
                .param("size", "5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].id").value(transactionId));
    }
    
    @Test
    void aggregateTransactions_ReturnsOk() throws Exception {
        LocalDateTime startDate = LocalDateTime.now().minusDays(30);
        LocalDateTime endDate = LocalDateTime.now();
        
        // Create category summaries
        Map<String, AggregatedTransactionResponse.CategorySummary> categorySummaries = new HashMap<>();
        categorySummaries.put("SHOPPING", 
            AggregatedTransactionResponse.CategorySummary.builder()
                .category("SHOPPING")
                .totalAmount(new BigDecimal("100.00"))
                .count(1)
                .percentageOfTotal(100.0)
                .averageAmount(new BigDecimal("100.00"))
                .build());
        
        // Create payment method summaries
        Map<String, AggregatedTransactionResponse.PaymentMethodSummary> paymentMethodSummaries = new HashMap<>();
        paymentMethodSummaries.put("CREDIT_CARD",
            AggregatedTransactionResponse.PaymentMethodSummary.builder()
                .paymentMethod("CREDIT_CARD")
                .totalAmount(new BigDecimal("100.00"))
                .count(1)
                .averageAmount(new BigDecimal("100.00"))
                .build());
        
        AggregatedTransactionResponse response = AggregatedTransactionResponse.builder()
                .userId(userId)
                .totalAmount(new BigDecimal("100.00"))
                .totalTransactions(1)
                .categorySummaries(categorySummaries)
                .paymentMethodSummaries(paymentMethodSummaries)
                .dateRange(AggregatedTransactionResponse.DateRange.builder()
                    .startDate(startDate)
                    .endDate(endDate)
                    .build())
                .summary(AggregatedTransactionResponse.Summary.builder()
                    .minAmount(new BigDecimal("100.00"))
                    .maxAmount(new BigDecimal("100.00"))
                    .averageAmount(new BigDecimal("100.00"))
                    .completedCount(1)
                    .pendingCount(0)
                    .failedCount(0)
                    .build())
                .build();
        
        when(transactionService.aggregatedTransactionResponse(
                eq(userId), any(LocalDateTime.class), any(LocalDateTime.class)))
                .thenReturn(response);
        
        mockMvc.perform(get("/api/v1/transactions/user/{userId}/aggregate", userId)
                .param("startDate", startDate.toString())
                .param("endDate", endDate.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userId").value(userId))
                .andExpect(jsonPath("$.totalAmount").value(100.00))
                .andExpect(jsonPath("$.totalTransactions").value(1))
                .andExpect(jsonPath("$.categorySummaries.SHOPPING").exists())
                .andExpect(jsonPath("$.paymentMethodSummaries.CREDIT_CARD").exists());
    }
    
    @Test
    void aggregateTransactions_WithInvalidDateRange_ReturnsBadRequest() throws Exception {
        LocalDateTime startDate = LocalDateTime.now();
        LocalDateTime endDate = LocalDateTime.now().minusDays(30); // End date before start date
        
        mockMvc.perform(get("/api/v1/transactions/user/{userId}/aggregate", userId)
                .param("startDate", startDate.toString())
                .param("endDate", endDate.toString()))
                .andExpect(status().isBadRequest());
    }
    
    @Test
    void countTransactions_ReturnsOk() throws Exception {
        LocalDateTime startDate = LocalDateTime.now().minusDays(30);
        LocalDateTime endDate = LocalDateTime.now();
        long expectedCount = 5L;
        
        when(transactionService.getTransactionCountByUserIdAndDateRange(
                eq(userId), any(LocalDateTime.class), any(LocalDateTime.class)))
                .thenReturn(expectedCount);
        
        mockMvc.perform(get("/api/v1/transactions/user/{userId}/count", userId)
                .param("startDate", startDate.toString())
                .param("endDate", endDate.toString()))
                .andExpect(status().isOk())
                .andExpect(content().string(String.valueOf(expectedCount)));
    }
    
    @Test
    void validateUserTransaction_ReturnsTrue() throws Exception {
        when(transactionService.validateUserTransaction(userId, transactionId))
                .thenReturn(true);
        
        mockMvc.perform(get("/api/v1/transactions/user/{userId}/validate/{transactionId}", userId, transactionId))
                .andExpect(status().isOk())
                .andExpect(content().string("true"));
    }
    
    @Test
    void validateUserTransaction_ReturnsFalse() throws Exception {
        when(transactionService.validateUserTransaction(userId, transactionId))
                .thenReturn(false);
        
        mockMvc.perform(get("/api/v1/transactions/user/{userId}/validate/{transactionId}", userId, transactionId))
                .andExpect(status().isOk())
                .andExpect(content().string("false"));
    }
}