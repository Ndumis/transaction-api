package com.example.transaction_api.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.example.transaction_api.dto.AggregatedTransactionResponse;
import com.example.transaction_api.dto.TransactionRequest;
import com.example.transaction_api.exception.ResourceNotFoundException;
import com.example.transaction_api.exception.TransactionNotFoundException;
import com.example.transaction_api.model.PaymentMethod;
import com.example.transaction_api.model.Transaction;
import com.example.transaction_api.model.Transaction.TransactionCategory;
import com.example.transaction_api.model.Transaction.TransactionStatus;
import com.example.transaction_api.model.User;
import com.example.transaction_api.repository.TransactionRepository;
import com.example.transaction_api.repository.UserRepository;

import jakarta.annotation.PostConstruct;

import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class TransactionService {
    private final TransactionRepository transactionRepository;
    private final UserRepository userRepository;
    private final List<PaymentProcessor> paymentProcessors;
    private Map<PaymentMethod, PaymentProcessor> paymentProcessorMap;
    
    @PostConstruct
    public void initPaymentProcessors() {
        log.info("Initializing payment processor map");
        paymentProcessorMap = paymentProcessors.stream()
                .collect(Collectors.toMap(
                    PaymentProcessor::getPaymentMethod, processor -> processor
                ));

       log.info("Initialized payment processor map with {} processors", paymentProcessorMap.size());         
    }

    @Transactional
    public Transaction createTransaction(TransactionRequest request) {
        log.info("Creating transaction for user {} with amount ${}", request.getUserId(), request.getAmount());

        User user = userRepository.findById(request.getUserId())
            .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + request.getUserId()));

        PaymentMethod paymentMethod;
        try {
            paymentMethod = PaymentMethod.valueOf(request.getPaymentMethod());
        } catch (IllegalArgumentException e) {
            log.error("Invalid payment method: {}", request.getPaymentMethod());
            throw new IllegalArgumentException("Invalid payment method: " + request.getPaymentMethod());
        }

        PaymentProcessor processor = paymentProcessorMap.get(paymentMethod);
        if(processor == null) {
            log.error("Unsupported payment method: {}", request.getPaymentMethod());
            throw new IllegalArgumentException("Unsupported payment method: " + request.getPaymentMethod());
        }

        if(!processor.validatePaymentDetails(request)) {
            log.error("Invalid payment details for user {}", request.getUserId());
            throw new IllegalArgumentException("Invalid payment details");
        }

        Transaction transaction = processor.processPayment(request, user);
        transaction.setUser(user); 

        Transaction savedTransaction = transactionRepository.save(transaction);
        log.info("Transaction created successfully with ID: {}", savedTransaction.getId());
        
        return savedTransaction;
    }

    @Transactional(readOnly = true)
    public Transaction getTransactionById(String id) {
        log.info("Retrieving transaction with ID: {}", id);
        return transactionRepository.findById(id)
            .orElseThrow(() -> {
                log.error("Transaction not found with ID: {}", id);
                return new TransactionNotFoundException("Transaction not found with ID: " + id);
            });
    }

    @Transactional(readOnly = true)
    public Page<Transaction> getTransactionsByUserId(String userId, Pageable pageable) {
        log.info("Retrieving transactions for user {} with page {} and size {}", userId, pageable.getPageNumber(), pageable.getPageSize());
        return transactionRepository.findByUserId(userId, pageable);
    }

    @Transactional(readOnly = true)
    public AggregatedTransactionResponse aggregatedTransactionResponse(String userId, LocalDateTime startDate, LocalDateTime endDate) {
        log.info("Aggregating transactions for user {} between {} and {}", userId, startDate, endDate);
        
        //Get summary statistics from the repository
        Map<String, Object> summary = transactionRepository.getSummaryStatistics(userId, startDate, endDate);

        //Get Category breakdown from the repository
        List<Map<String, Object>> categoryBreakdown = transactionRepository.aggregateByCategory(userId, startDate, endDate);

        //Get Payment method breakdown from the repository
        List<Map<String, Object>> paymentMethodBreakdown = transactionRepository.aggregateByPaymentMethod(userId, startDate, endDate);

        //Get status breakdown from the repository
        List<Map<String, Object>> statusBreakdown = transactionRepository.getStatusBreakdown(userId, startDate, endDate);

        BigDecimal totalAmount = (BigDecimal) summary.getOrDefault("totalAmount", BigDecimal.ZERO);
        Long totalCount = (Long) summary.getOrDefault("totalCount", 0L);

        //Build category summary
        Map<String, AggregatedTransactionResponse.CategorySummary> categorySummary = new HashMap<>();
        for(Map<String, Object> categoryData : categoryBreakdown) {
            String category;
            Object categoryObj = categoryData.get("category");
            if (categoryObj instanceof TransactionCategory) {
                category = ((TransactionCategory) categoryObj).name();
            } else {
                category = (String) categoryObj;
            }

            BigDecimal amount = (BigDecimal) categoryData.get("totalAmount");
            Long count = (Long) categoryData.get("transactionCount");
            BigDecimal avgAmount = (BigDecimal) categoryData.get("averageAmount");

            double percentage = totalAmount.compareTo(BigDecimal.ZERO) > 0 
            ? amount.divide(totalAmount, 4, RoundingMode.HALF_UP).doubleValue() * 100 : 0.0;

            categorySummary.put(category,
                AggregatedTransactionResponse.CategorySummary.builder()
                    .category(category)
                    .totalAmount(amount)
                    .count(count)
                    .percentageOfTotal(percentage)
                    .averageAmount(avgAmount)
                    .build());
        }

        //Build payment method summary
        Map<String, AggregatedTransactionResponse.PaymentMethodSummary> paymentMethodSummary = new HashMap<>();
        for(Map<String, Object> paymentMethodData : paymentMethodBreakdown) {
            String paymentMethod;
            Object pmObj = paymentMethodData.get("paymentMethod");
            if (pmObj instanceof PaymentMethod) {
                paymentMethod = ((PaymentMethod) pmObj).name();
            } else {
                paymentMethod = (String) pmObj;
            }

            BigDecimal amount = (BigDecimal) paymentMethodData.get("totalAmount");
            Long count = (Long) paymentMethodData.get("transactionCount");
            BigDecimal avgAmount = (BigDecimal) paymentMethodData.get("averageAmount");

            paymentMethodSummary.put(paymentMethod,
                AggregatedTransactionResponse.PaymentMethodSummary.builder()
                    .paymentMethod(paymentMethod)
                    .totalAmount(amount)
                    .count(count)
                    .averageAmount(avgAmount)
                    .build());
        }

        //Build status summary
        long completedCount = 0, pendingCount = 0, failedCount = 0;
        for(Map<String, Object> statusData : statusBreakdown) {
            String status;
            Object statusObj = statusData.get("status");
            if (statusObj instanceof TransactionStatus) {
                status = ((TransactionStatus) statusObj).name();
            } else {
                status = (String) statusObj;
            }
            
            Long count = (Long) statusData.get("transactionCount");

            switch(status) {
                case "COMPLETED":
                    completedCount = count;
                    break;
                case "PENDING":
                    pendingCount = count;
                    break;
                case "FAILED":
                case "CANCELLED":
                    failedCount = count;
                    break;
                default:
                    break;
            }
        }

        return AggregatedTransactionResponse.builder()
                .userId(userId)
                .totalAmount(totalAmount)
                .totalTransactions(totalCount)
                .categorySummaries(categorySummary)
                .paymentMethodSummaries(paymentMethodSummary)
                .dateRange(AggregatedTransactionResponse.DateRange.builder()
                    .startDate(startDate)
                    .endDate(endDate)
                    .build())
                .summary(AggregatedTransactionResponse.Summary.builder()
                    .minAmount((BigDecimal) summary.getOrDefault("minAmount", BigDecimal.ZERO))
                    .maxAmount((BigDecimal) summary.getOrDefault("maxAmount", BigDecimal.ZERO))
                    .averageAmount((BigDecimal) summary.getOrDefault("averageAmount", BigDecimal.ZERO))
                    .completedCount(completedCount)
                    .pendingCount(pendingCount)
                    .failedCount(failedCount)
                    .build())
                .build();   

    }

    @Transactional(readOnly = true)
    public boolean validateUserTransaction(String userId, String transactionId) {
        log.info("Validating transaction {} for user {}", transactionId, userId);
        return transactionRepository.existsByUserIdAndId(userId, transactionId);
    }

    @Transactional(readOnly = true)
    public long getTransactionCountByUserIdAndDateRange(String userId, LocalDateTime startDate, LocalDateTime endDate) {
        log.info("Counting transactions for user {} between {} and {}", userId, startDate, endDate);
        return transactionRepository.countByUserIdAndTransactionDateBetween(userId, startDate, endDate);
    }
}
