package com.example.transaction_api.service.impl;

import org.springframework.stereotype.Component;

import com.example.transaction_api.dto.TransactionRequest;
import com.example.transaction_api.model.*;
import com.example.transaction_api.service.PaymentProcessor;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class DebitCardProcessor implements PaymentProcessor {
    @Override
    public Transaction processPayment(TransactionRequest request, User user) {
        log.info("Processing debit card payment of ${}", request.getAmount());
        // Simulate debit card payment processing logic
        return Transaction.builder()
                .user(user)
                .amount(request.getAmount())
                .currency(request.getCurrency())
                .transactionDate(request.getTransactionDate())
                .description(request.getDescription())
                .paymentMethod(com.example.transaction_api.model.PaymentMethod.DEBIT_CARD)
                .category(Transaction.TransactionCategory.valueOf(request.getCategory()))
                .status(Transaction.TransactionStatus.COMPLETED)
                .build();
    }

    @Override 
    public PaymentMethod getPaymentMethod() {
        log.info("Getting payment method for Debit Card Processor");
        return PaymentMethod.DEBIT_CARD;
    }

    @Override
    public boolean validatePaymentDetails(TransactionRequest request) {
        log.info("Validating debit card payment details for user {}", request.getUserId());

        if(request.getAmount() == null || request.getAmount().compareTo(new java.math.BigDecimal("0.01")) < 0) {
            log.error("Invalid amount: {}", request.getAmount());
            return false;
        }
        if(request.getCurrency() == null || !request.getCurrency().matches("^[A-Z]{3}$")) {
            log.error("Invalid currency: {}", request.getCurrency());
            return false;
        }
        if(request.getTransactionDate() == null || request.getTransactionDate().isAfter(java.time.LocalDateTime.now())) {
            log.error("Invalid transaction date: {}", request.getTransactionDate());
            return false;
        }
        if(request.getDescription() == null || request.getDescription().length() > 255) {
            log.error("Invalid description: {}", request.getDescription());
            return false;
        }
        if(request.getCategory() == null || !request.getCategory().matches("^(GROCERIES|DINING|SHOPPING|TRANSPORTATION|UTILITIES|ENTERTAINMENT|HEALTHCARE|EDUCATION|TRAVEL|INCOME|TRANSFER|RENT|INSURANCE|OTHER)$")) {
            log.error("Invalid category: {}", request.getCategory());
            return false;
        }
        return true;

    }

    @Override
    public String getProcessorName() {
        log.info("Getting processor name for Debit Card Processor");
        return "Debit Card Processor";
    }
    
}
