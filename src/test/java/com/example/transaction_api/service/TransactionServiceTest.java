package com.example.transaction_api.service;

import com.example.transaction_api.dto.AggregatedTransactionResponse;
import com.example.transaction_api.dto.TransactionRequest;
import com.example.transaction_api.exception.InvalidTransactionException;
import com.example.transaction_api.exception.ResourceNotFoundException;
import com.example.transaction_api.exception.TransactionNotFoundException;
import com.example.transaction_api.model.*;
import com.example.transaction_api.model.Transaction.TransactionCategory;
import com.example.transaction_api.model.Transaction.TransactionStatus;
import com.example.transaction_api.repository.TransactionRepository;
import com.example.transaction_api.repository.UserRepository;
import com.example.transaction_api.service.impl.CreditCardProcessor;
import com.example.transaction_api.service.impl.DebitCardProcessor;
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

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TransactionServiceTest {
    
    @Mock
    private TransactionRepository transactionRepository;
    
    @Mock
    private UserRepository userRepository;
    
    @Mock
    private List<PaymentProcessor> paymentProcessors;
    
    @InjectMocks
    private TransactionService transactionService;
    
    private User user;
    private Transaction transaction;
    private TransactionRequest request;
    private LocalDateTime now;
    private String userId;
    private String transactionId;
    
    @BeforeEach
    void setUp() {
        now = LocalDateTime.now();
        userId = UUID.randomUUID().toString();
        transactionId = UUID.randomUUID().toString();
        
        // Create test user
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
    void createTransaction_Success() {
        // Given
        CreditCardProcessor creditCardProcessor = new CreditCardProcessor();
        List<PaymentProcessor> processors = Arrays.asList(creditCardProcessor);
        
        transactionService = new TransactionService(transactionRepository, userRepository, processors);
        transactionService.initPaymentProcessors();
        
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(transactionRepository.save(any(Transaction.class))).thenReturn(transaction);
        
        // When
        Transaction result = transactionService.createTransaction(request);
        
        // Then
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(transactionId);
        assertThat(result.getUser()).isNotNull();
        assertThat(result.getUser().getId()).isEqualTo(userId);
        assertThat(result.getAmount()).isEqualByComparingTo(new BigDecimal("100.00"));
        assertThat(result.getCurrency()).isEqualTo("ZAR");
        assertThat(result.getPaymentMethod()).isEqualTo(PaymentMethod.CREDIT_CARD);
        assertThat(result.getCategory()).isEqualTo(TransactionCategory.SHOPPING);
        assertThat(result.getStatus()).isEqualTo(TransactionStatus.COMPLETED);
        
        verify(userRepository, times(1)).findById(userId);
        verify(transactionRepository, times(1)).save(any(Transaction.class));
    }
    
    @Test
    void createTransaction_UserNotFound_ThrowsException() {
        // Given
        CreditCardProcessor creditCardProcessor = new CreditCardProcessor();
        List<PaymentProcessor> processors = Arrays.asList(creditCardProcessor);
        
        transactionService = new TransactionService(transactionRepository, userRepository, processors);
        transactionService.initPaymentProcessors();
        
        when(userRepository.findById(userId)).thenReturn(Optional.empty());
        
        // When/Then
        assertThatThrownBy(() -> transactionService.createTransaction(request))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("User not found with id: " + userId);
        
        verify(userRepository, times(1)).findById(userId);
        verify(transactionRepository, never()).save(any(Transaction.class));
    }
    
    @Test
    void createTransaction_WithInvalidPaymentMethod_ThrowsException() {
        // Given
        request.setPaymentMethod("INVALID_METHOD");
        CreditCardProcessor creditCardProcessor = new CreditCardProcessor();
        List<PaymentProcessor> processors = Arrays.asList(creditCardProcessor);
        
        transactionService = new TransactionService(transactionRepository, userRepository, processors);
        transactionService.initPaymentProcessors();
        
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        
        // When/Then
        assertThatThrownBy(() -> transactionService.createTransaction(request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Invalid payment method");
    }
    
    @Test
    void createTransaction_WithUnsupportedPaymentMethod_ThrowsException() {
        // Given
        request.setPaymentMethod("CRYPTO");
        CreditCardProcessor creditCardProcessor = new CreditCardProcessor();
        List<PaymentProcessor> processors = Arrays.asList(creditCardProcessor);
        
        transactionService = new TransactionService(transactionRepository, userRepository, processors);
        transactionService.initPaymentProcessors();
        
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        
        // When/Then
        assertThatThrownBy(() -> transactionService.createTransaction(request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Invalid payment method: " + request.getPaymentMethod());
    }
    
    @Test
    void getTransactionById_Success() {
        // Given
        when(transactionRepository.findById(transactionId))
                .thenReturn(Optional.of(transaction));
        
        // When
        Transaction result = transactionService.getTransactionById(transactionId);
        
        // Then
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(transactionId);
        assertThat(result.getUser().getId()).isEqualTo(userId);
        
        verify(transactionRepository, times(1)).findById(transactionId);
    }
    
    @Test
    void getTransactionById_NotFound_ThrowsException() {
        // Given
        when(transactionRepository.findById(transactionId))
                .thenReturn(Optional.empty());
        
        // When/Then
        assertThatThrownBy(() -> transactionService.getTransactionById(transactionId))
                .isInstanceOf(TransactionNotFoundException.class)
                .hasMessageContaining("Transaction not found");
        
        verify(transactionRepository, times(1)).findById(transactionId);
    }
    
    @Test
    void getTransactionsByUserId_Success() {
        // Given
        Pageable pageable = PageRequest.of(0, 10);
        List<Transaction> transactions = Arrays.asList(transaction);
        Page<Transaction> page = new PageImpl<>(transactions);
        
        when(transactionRepository.findByUserId(userId, pageable))
                .thenReturn(page);
        
        // When
        Page<Transaction> result = transactionService.getTransactionsByUserId(userId, pageable);
        
        // Then
        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getUser().getId()).isEqualTo(userId);
        
        verify(transactionRepository, times(1)).findByUserId(userId, pageable);
    }
    
    @Test
    void aggregatedTransactionResponse_ReturnsCorrectAggregation() {
        // Given
        LocalDateTime startDate = now.minusDays(30);
        LocalDateTime endDate = now.plusDays(1);
        
        // Mock summary statistics
        Map<String, Object> summaryStats = new HashMap<>();
        summaryStats.put("totalAmount", new BigDecimal("1951.25"));
        summaryStats.put("totalCount", 2L);
        summaryStats.put("minAmount", new BigDecimal("450.50"));
        summaryStats.put("maxAmount", new BigDecimal("1500.75"));
        summaryStats.put("averageAmount", new BigDecimal("975.625"));
        
        // Mock category breakdown
        List<Map<String, Object>> categoryBreakdown = new ArrayList<>();
        Map<String, Object> groceries = new HashMap<>();
        groceries.put("category", TransactionCategory.GROCERIES);
        groceries.put("totalAmount", new BigDecimal("1500.75"));
        groceries.put("transactionCount", 1L);
        groceries.put("averageAmount", new BigDecimal("1500.75"));
        categoryBreakdown.add(groceries);
        
        Map<String, Object> dining = new HashMap<>();
        dining.put("category", TransactionCategory.DINING);
        dining.put("totalAmount", new BigDecimal("450.50"));
        dining.put("transactionCount", 1L);
        dining.put("averageAmount", new BigDecimal("450.50"));
        categoryBreakdown.add(dining);
        
        // Mock payment method breakdown
        List<Map<String, Object>> paymentBreakdown = new ArrayList<>();
        Map<String, Object> debit = new HashMap<>();
        debit.put("paymentMethod", PaymentMethod.DEBIT_CARD);
        debit.put("totalAmount", new BigDecimal("1500.75"));
        debit.put("transactionCount", 1L);
        debit.put("averageAmount", new BigDecimal("1500.75"));
        paymentBreakdown.add(debit);
        
        Map<String, Object> credit = new HashMap<>();
        credit.put("paymentMethod", PaymentMethod.CREDIT_CARD);
        credit.put("totalAmount", new BigDecimal("450.50"));
        credit.put("transactionCount", 1L);
        credit.put("averageAmount", new BigDecimal("450.50"));
        paymentBreakdown.add(credit);
        
        // Mock status breakdown
        List<Map<String, Object>> statusBreakdown = new ArrayList<>();
        Map<String, Object> completed = new HashMap<>();
        completed.put("status", TransactionStatus.COMPLETED);
        completed.put("transactionCount", 2L);
        statusBreakdown.add(completed);
        
        when(transactionRepository.getSummaryStatistics(eq(userId), any(LocalDateTime.class), any(LocalDateTime.class)))
                .thenReturn(summaryStats);
        when(transactionRepository.aggregateByCategory(eq(userId), any(LocalDateTime.class), any(LocalDateTime.class)))
                .thenReturn(categoryBreakdown);
        when(transactionRepository.aggregateByPaymentMethod(eq(userId), any(LocalDateTime.class), any(LocalDateTime.class)))
                .thenReturn(paymentBreakdown);
        when(transactionRepository.getStatusBreakdown(eq(userId), any(LocalDateTime.class), any(LocalDateTime.class)))
                .thenReturn(statusBreakdown);
        
        // When
        AggregatedTransactionResponse response = transactionService.aggregatedTransactionResponse(
                userId, startDate, endDate);
        
        // Then
        assertThat(response).isNotNull();
        assertThat(response.getUserId()).isEqualTo(userId);
        assertThat(response.getTotalAmount()).isEqualByComparingTo(new BigDecimal("1951.25"));
        assertThat(response.getTotalTransactions()).isEqualTo(2L);
        
        // Verify category summaries
        assertThat(response.getCategorySummaries()).hasSize(2);
        assertThat(response.getCategorySummaries()).containsKey("GROCERIES");
        assertThat(response.getCategorySummaries()).containsKey("DINING");
        
        AggregatedTransactionResponse.CategorySummary groceriesSummary = response.getCategorySummaries().get("GROCERIES");
        assertThat(groceriesSummary.getTotalAmount()).isEqualByComparingTo(new BigDecimal("1500.75"));
        assertThat(groceriesSummary.getCount()).isEqualTo(1L);
        assertThat(groceriesSummary.getAverageAmount()).isEqualByComparingTo(new BigDecimal("1500.75"));
        
        // Verify payment method summaries
        assertThat(response.getPaymentMethodSummaries()).hasSize(2);
        assertThat(response.getPaymentMethodSummaries()).containsKey("DEBIT_CARD");
        assertThat(response.getPaymentMethodSummaries()).containsKey("CREDIT_CARD");
        
        // Verify summary stats
        assertThat(response.getSummary()).isNotNull();
        assertThat(response.getSummary().getCompletedCount()).isEqualTo(2L);
        assertThat(response.getSummary().getMinAmount()).isEqualByComparingTo(new BigDecimal("450.50"));
        assertThat(response.getSummary().getMaxAmount()).isEqualByComparingTo(new BigDecimal("1500.75"));
        assertThat(response.getSummary().getAverageAmount()).isEqualByComparingTo(new BigDecimal("975.625"));
    }
    
    @Test
    void validateUserTransaction_ReturnsTrue_WhenTransactionBelongsToUser() {
        // Given
        when(transactionRepository.existsByUserIdAndId(userId, transactionId))
                .thenReturn(true);
        
        // When
        boolean result = transactionService.validateUserTransaction(userId, transactionId);
        
        // Then
        assertThat(result).isTrue();
        verify(transactionRepository, times(1)).existsByUserIdAndId(userId, transactionId);
    }
    
    @Test
    void validateUserTransaction_ReturnsFalse_WhenTransactionDoesNotBelongToUser() {
        // Given
        String wrongUserId = UUID.randomUUID().toString();
        when(transactionRepository.existsByUserIdAndId(wrongUserId, transactionId))
                .thenReturn(false);
        
        // When
        boolean result = transactionService.validateUserTransaction(wrongUserId, transactionId);
        
        // Then
        assertThat(result).isFalse();
        verify(transactionRepository, times(1)).existsByUserIdAndId(wrongUserId, transactionId);
    }
    
    @Test
    void getTransactionCountByUserIdAndDateRange_ReturnsCount() {
        // Given
        LocalDateTime startDate = now.minusDays(30);
        LocalDateTime endDate = now;
        long expectedCount = 5L;
        
        when(transactionRepository.countByUserIdAndTransactionDateBetween(userId, startDate, endDate))
                .thenReturn(expectedCount);
        
        // When
        long result = transactionService.getTransactionCountByUserIdAndDateRange(userId, startDate, endDate);
        
        // Then
        assertThat(result).isEqualTo(expectedCount);
        verify(transactionRepository, times(1))
                .countByUserIdAndTransactionDateBetween(userId, startDate, endDate);
    }
    
    @Test
    void initPaymentProcessors_InitializesMap() {
        // Given
        CreditCardProcessor creditCardProcessor = new CreditCardProcessor();
        DebitCardProcessor debitCardProcessor = new DebitCardProcessor();
        List<PaymentProcessor> processors = Arrays.asList(creditCardProcessor, debitCardProcessor);
        
        transactionService = new TransactionService(transactionRepository, userRepository, processors);
        
        // When
        transactionService.initPaymentProcessors();
        
        // Then - verify by creating a transaction with each processor
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(transactionRepository.save(any(Transaction.class))).thenReturn(transaction);
        
        // Test credit card
        request.setPaymentMethod("CREDIT_CARD");
        Transaction creditResult = transactionService.createTransaction(request);
        assertThat(creditResult).isNotNull();
        
        // Test debit card
        request.setPaymentMethod("DEBIT_CARD");
        Transaction debitResult = transactionService.createTransaction(request);
        assertThat(debitResult).isNotNull();
    }
}