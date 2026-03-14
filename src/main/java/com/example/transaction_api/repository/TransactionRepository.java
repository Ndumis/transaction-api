package com.example.transaction_api.repository;

import com.example.transaction_api.model.Transaction;
import com.example.transaction_api.model.Transaction.TransactionCategory;
import com.example.transaction_api.model.Transaction.TransactionStatus;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, String> {
    
    Page<Transaction> findByUserId(String userId, Pageable pageable);
    
    List<Transaction> findByUserIdAndTransactionDateBetween(
        String userId, 
        LocalDateTime startDate, 
        LocalDateTime endDate
    );
    
    List<Transaction> findByUserIdAndCategory(
        String userId, 
        TransactionCategory category
    );
    
    List<Transaction> findByUserIdAndStatus(
        String userId, 
        TransactionStatus status
    );
    
    @Query("SELECT new map(" +
           " t.category as category, " +
           " SUM(t.amount) as totalAmount, " +
           " COUNT(t) as transactionCount, " +
           " CAST(AVG(t.amount) AS BigDecimal) as averageAmount) " +
           "FROM Transaction t " +
           "WHERE t.user.id = :userId " +
           "AND t.transactionDate BETWEEN :startDate AND :endDate " +
           "GROUP BY t.category")
    List<Map<String, Object>> aggregateByCategory(
        @Param("userId") String userId,
        @Param("startDate") LocalDateTime startDate,
        @Param("endDate") LocalDateTime endDate
    );
    
    @Query("SELECT new map(" +
           " t.paymentMethod as paymentMethod, " +
           " SUM(t.amount) as totalAmount, " +
           " COUNT(t) as transactionCount, " +
           " CAST(AVG(t.amount) AS BigDecimal) as averageAmount) " +
           "FROM Transaction t " +
           "WHERE t.user.id = :userId " +
           "AND t.transactionDate BETWEEN :startDate AND :endDate " +
           "GROUP BY t.paymentMethod")
    List<Map<String, Object>> aggregateByPaymentMethod(
        @Param("userId") String userId,
        @Param("startDate") LocalDateTime startDate,
        @Param("endDate") LocalDateTime endDate
    );
    
    @Query("SELECT new map(" +
           " SUM(t.amount) as totalAmount, " +
           " COUNT(t) as totalCount, " +
           " MIN(t.amount) as minAmount, " +
           " MAX(t.amount) as maxAmount, " +
           " CAST(AVG(t.amount) AS BigDecimal) as averageAmount) " + 
           "FROM Transaction t " +
           "WHERE t.user.id = :userId " +
           "AND t.transactionDate BETWEEN :startDate AND :endDate")
    Map<String, Object> getSummaryStatistics(
        @Param("userId") String userId,
        @Param("startDate") LocalDateTime startDate,
        @Param("endDate") LocalDateTime endDate
    );
    
    @Query("SELECT new map(" +
           " t.status as status, " +
           " COUNT(t) as transactionCount) " +
           "FROM Transaction t " +
           "WHERE t.user.id = :userId " +
           "AND t.transactionDate BETWEEN :startDate AND :endDate " +
           "GROUP BY t.status")
    List<Map<String, Object>> getStatusBreakdown(
        @Param("userId") String userId,
        @Param("startDate") LocalDateTime startDate,
        @Param("endDate") LocalDateTime endDate
    );
    
    boolean existsByUserIdAndId(String userId, String transactionId);
    
    long countByUserIdAndTransactionDateBetween(
        String userId, 
        LocalDateTime startDate, 
        LocalDateTime endDate
    );
}