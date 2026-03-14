package com.example.transaction_api.repository;

import com.example.transaction_api.model.*;
import com.example.transaction_api.model.Transaction.TransactionCategory;
import com.example.transaction_api.model.Transaction.TransactionStatus;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.withinPercentage;

@DataJpaTest
class TransactionRepositoryTest {
    
    @Autowired
    private TestEntityManager entityManager;
    
    @Autowired
    private TransactionRepository transactionRepository;
    
    @Autowired
    private UserRepository userRepository;
    
    private String userId;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private String transaction1Id;
    private String transaction2Id;
    
    @BeforeEach
    void setUp() {
        // Clean up before each test
        transactionRepository.deleteAll();
        userRepository.deleteAll();
        entityManager.flush();
        entityManager.clear();
        
        // Create a unique user for each test
        User user = User.builder()
                .username("testuser_" + System.currentTimeMillis())
                .email("test_" + System.currentTimeMillis() + "@example.com")
                .password("password")
                .firstName("Test")
                .lastName("User")
                .status(UserStatus.ACTIVE)
                .build();
        
        entityManager.persist(user);
        entityManager.flush();
        userId = user.getId();
        
        // Create test transactions
        Transaction transaction1 = Transaction.builder()
                .user(user)
                .amount(new BigDecimal("1500.75"))
                .currency("ZAR")
                .transactionDate(LocalDateTime.of(2024, 1, 15, 10, 30))
                .description("Grocery shopping at Checkers")
                .paymentMethod(PaymentMethod.DEBIT_CARD)
                .category(TransactionCategory.GROCERIES)
                .status(TransactionStatus.COMPLETED)
                .build();
        
        Transaction transaction2 = Transaction.builder()
                .user(user)
                .amount(new BigDecimal("450.50"))
                .currency("ZAR")
                .transactionDate(LocalDateTime.of(2024, 1, 20, 12, 15))
                .description("Lunch at Spur")
                .paymentMethod(PaymentMethod.CREDIT_CARD)
                .category(TransactionCategory.DINING)
                .status(TransactionStatus.COMPLETED)
                .build();
        
        entityManager.persist(transaction1);
        entityManager.persist(transaction2);
        entityManager.flush();
        
        transaction1Id = transaction1.getId();
        transaction2Id = transaction2.getId();
        
        startDate = LocalDateTime.of(2024, 1, 1, 0, 0);
        endDate = LocalDateTime.of(2024, 1, 31, 23, 59);
    }
    
    // Helper method to safely convert to BigDecimal
    private BigDecimal toBigDecimal(Object value) {
        if (value == null) return null;
        if (value instanceof BigDecimal) return (BigDecimal) value;
        if (value instanceof Double) return BigDecimal.valueOf((Double) value);
        if (value instanceof Integer) return BigDecimal.valueOf((Integer) value);
        if (value instanceof Long) return BigDecimal.valueOf((Long) value);
        if (value instanceof String) return new BigDecimal((String) value);
        throw new IllegalArgumentException("Cannot convert " + value.getClass() + " to BigDecimal");
    }
    
    // Helper method to safely convert to Long
    private Long toLong(Object value) {
        if (value == null) return null;
        if (value instanceof Long) return (Long) value;
        if (value instanceof Integer) return ((Integer) value).longValue();
        if (value instanceof BigDecimal) return ((BigDecimal) value).longValue();
        throw new IllegalArgumentException("Cannot convert " + value.getClass() + " to Long");
    }
    
    // Helper method to safely convert to TransactionCategory
    private TransactionCategory toCategory(Object value) {
        if (value == null) return null;
        if (value instanceof TransactionCategory) return (TransactionCategory) value;
        if (value instanceof String) return TransactionCategory.valueOf((String) value);
        throw new IllegalArgumentException("Cannot convert " + value.getClass() + " to TransactionCategory");
    }
    
    @Test
    void findByUserId_ReturnsTransactions() {
        Page<Transaction> transactions = transactionRepository.findByUserId(
            userId, PageRequest.of(0, 10));
        
        assertThat(transactions).isNotNull();
        assertThat(transactions.getContent()).hasSize(2);
        assertThat(transactions.getContent().get(0).getUser().getId()).isEqualTo(userId);
    }
    
    @Test
    void findByUserIdAndTransactionDateBetween_ReturnsTransactions() {
        List<Transaction> transactions = transactionRepository
            .findByUserIdAndTransactionDateBetween(userId, startDate, endDate);
        
        assertThat(transactions).hasSize(2);
        assertThat(transactions).allMatch(t -> 
            t.getUser().getId().equals(userId) &&
            !t.getTransactionDate().isBefore(startDate) &&
            !t.getTransactionDate().isAfter(endDate)
        );
    }
    
    @Test
    void findByUserIdAndCategory_ReturnsTransactions() {
        List<Transaction> transactions = transactionRepository
            .findByUserIdAndCategory(userId, TransactionCategory.GROCERIES);
        
        assertThat(transactions).hasSize(1);
        assertThat(transactions.get(0).getUser().getId()).isEqualTo(userId);
        assertThat(transactions.get(0).getCategory()).isEqualTo(TransactionCategory.GROCERIES);
    }
    
    @Test
    void aggregateByCategory_ReturnsAggregation() {
        List<Map<String, Object>> aggregation = transactionRepository
            .aggregateByCategory(userId, startDate, endDate);
        
        assertThat(aggregation).hasSize(2);
        
        // Find GROCERIES category
        Map<String, Object> groceriesAgg = aggregation.stream()
            .filter(m -> toCategory(m.get("category")) == TransactionCategory.GROCERIES)
            .findFirst()
            .orElseThrow(() -> new AssertionError("GROCERIES category not found"));
        
        assertThat(toBigDecimal(groceriesAgg.get("totalAmount")))
            .isEqualByComparingTo(new BigDecimal("1500.75"));
        assertThat(toLong(groceriesAgg.get("transactionCount"))).isEqualTo(1L);
        assertThat(toBigDecimal(groceriesAgg.get("averageAmount")))
            .isEqualByComparingTo(new BigDecimal("1500.75"));
        
        // Find DINING category
        Map<String, Object> diningAgg = aggregation.stream()
            .filter(m -> toCategory(m.get("category")) == TransactionCategory.DINING)
            .findFirst()
            .orElseThrow(() -> new AssertionError("DINING category not found"));
        
        assertThat(toBigDecimal(diningAgg.get("totalAmount")))
            .isEqualByComparingTo(new BigDecimal("450.50"));
        assertThat(toLong(diningAgg.get("transactionCount"))).isEqualTo(1L);
        assertThat(toBigDecimal(diningAgg.get("averageAmount")))
            .isEqualByComparingTo(new BigDecimal("450.50"));
    }
    
    @Test
    void aggregateByPaymentMethod_ReturnsAggregation() {
        List<Map<String, Object>> aggregation = transactionRepository
            .aggregateByPaymentMethod(userId, startDate, endDate);
        
        assertThat(aggregation).hasSize(2);
        
        // Find DEBIT_CARD
        Map<String, Object> debitAgg = aggregation.stream()
            .filter(m -> {
                Object pm = m.get("paymentMethod");
                if (pm instanceof PaymentMethod) {
                    return ((PaymentMethod) pm) == PaymentMethod.DEBIT_CARD;
                }
                return PaymentMethod.DEBIT_CARD.name().equals(pm);
            })
            .findFirst()
            .orElseThrow(() -> new AssertionError("DEBIT_CARD not found"));
        
        assertThat(toBigDecimal(debitAgg.get("totalAmount")))
            .isEqualByComparingTo(new BigDecimal("1500.75"));
        assertThat(toLong(debitAgg.get("transactionCount"))).isEqualTo(1L);
        assertThat(toBigDecimal(debitAgg.get("averageAmount")))
            .isEqualByComparingTo(new BigDecimal("1500.75"));
        
        // Find CREDIT_CARD
        Map<String, Object> creditAgg = aggregation.stream()
            .filter(m -> {
                Object pm = m.get("paymentMethod");
                if (pm instanceof PaymentMethod) {
                    return ((PaymentMethod) pm) == PaymentMethod.CREDIT_CARD;
                }
                return PaymentMethod.CREDIT_CARD.name().equals(pm);
            })
            .findFirst()
            .orElseThrow(() -> new AssertionError("CREDIT_CARD not found"));
        
        assertThat(toBigDecimal(creditAgg.get("totalAmount")))
            .isEqualByComparingTo(new BigDecimal("450.50"));
        assertThat(toLong(creditAgg.get("transactionCount"))).isEqualTo(1L);
        assertThat(toBigDecimal(creditAgg.get("averageAmount")))
            .isEqualByComparingTo(new BigDecimal("450.50"));
    }
    
    @Test
    void getSummaryStatistics_ReturnsStats() {
        Map<String, Object> stats = transactionRepository
            .getSummaryStatistics(userId, startDate, endDate);
        
        assertThat(stats).isNotNull();
        assertThat(stats).containsKeys("totalAmount", "totalCount", "minAmount", "maxAmount", "averageAmount");
        
        // Test exact values for non-average fields
        assertThat(toBigDecimal(stats.get("totalAmount")))
            .isEqualByComparingTo(new BigDecimal("1951.25"));
        assertThat(toLong(stats.get("totalCount"))).isEqualTo(2L);
        assertThat(toBigDecimal(stats.get("minAmount")))
            .isEqualByComparingTo(new BigDecimal("450.50"));
        assertThat(toBigDecimal(stats.get("maxAmount")))
            .isEqualByComparingTo(new BigDecimal("1500.75"));
        
        // Test average with tolerance due to floating-point precision
        BigDecimal actualAvg = toBigDecimal(stats.get("averageAmount"));
        BigDecimal expectedAvg = new BigDecimal("975.625");
        
        // Calculate difference
        BigDecimal difference = actualAvg.subtract(expectedAvg).abs();
        
        // Assert with tolerance
        assertThat(difference)
            .withFailMessage("Expected average %s but got %s, difference %s", 
                            expectedAvg, actualAvg, difference)
            .isLessThan(new BigDecimal("0.01"));
    }
    
    @Test
    void getStatusBreakdown_ReturnsBreakdown() {
        List<Map<String, Object>> breakdown = transactionRepository
            .getStatusBreakdown(userId, startDate, endDate);
        
        assertThat(breakdown).hasSize(1);
        
        Map<String, Object> statusData = breakdown.get(0);
        Object statusObj = statusData.get("status");
        
        // Handle both enum and string
        if (statusObj instanceof TransactionStatus) {
            assertThat(statusObj).isEqualTo(TransactionStatus.COMPLETED);
        } else {
            assertThat(statusObj).isEqualTo("COMPLETED");
        }
        
        assertThat(toLong(statusData.get("transactionCount"))).isEqualTo(2L);
    }
    
    @Test
    void existsByUserIdAndId_ReturnsTrue_WhenExists() {
        boolean exists = transactionRepository.existsByUserIdAndId(userId, transaction1Id);
        assertThat(exists).isTrue();
    }
    
    @Test
    void existsByUserIdAndId_ReturnsFalse_WhenNotExists() {
        String nonExistentId = UUID.randomUUID().toString();
        boolean exists = transactionRepository.existsByUserIdAndId(userId, nonExistentId);
        assertThat(exists).isFalse();
    }
    
    @Test
    void existsByUserIdAndId_ReturnsFalse_WhenWrongUser() {
        // Create another user
        User otherUser = User.builder()
                .username("otheruser_" + System.currentTimeMillis())
                .email("other_" + System.currentTimeMillis() + "@example.com")
                .password("password")
                .firstName("Other")
                .lastName("User")
                .status(UserStatus.ACTIVE)
                .build();
        
        entityManager.persist(otherUser);
        entityManager.flush();
        
        boolean exists = transactionRepository.existsByUserIdAndId(otherUser.getId(), transaction1Id);
        assertThat(exists).isFalse();
    }
    
    @Test
    void countByUserIdAndTransactionDateBetween_ReturnsCount() {
        long count = transactionRepository
            .countByUserIdAndTransactionDateBetween(userId, startDate, endDate);
        
        assertThat(count).isEqualTo(2L);
    }
    
    @Test
    void findById_ReturnsTransactionWithUser() {
        var found = transactionRepository.findById(transaction1Id);
        
        assertThat(found).isPresent();
        assertThat(found.get().getUser()).isNotNull();
        assertThat(found.get().getUser().getId()).isEqualTo(userId);
        assertThat(found.get().getDescription()).isEqualTo("Grocery shopping at Checkers");
    }
}