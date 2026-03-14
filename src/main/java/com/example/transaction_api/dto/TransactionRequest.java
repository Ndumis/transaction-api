package com.example.transaction_api.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class TransactionRequest {
    @NotBlank(message = "User ID is required")
    @Pattern(regexp = "^[a-zA-Z0-9_-]+$", message = "User ID must be 8-20 alphanumeric characters")
    private String userId;

    @NotNull(message = "Amount is required")
    @DecimalMin(value = "0.01", message = "Amount must be greater than zero")
    private BigDecimal amount;

    @NotBlank(message = "Currency is required")
    @Pattern(regexp = "^(ZAR|USD|EUR|GBP|AUD|CAD|JPY|CNY)$", message = "Currency must be a valid ISO code (ZAR, USD, EUR, GBP, etc.)")
    private String currency;

    @NotNull(message = "Transaction date is required")
    @PastOrPresent(message = "Transaction date cannot be in the future")
    private LocalDateTime transactionDate;

    @NotBlank(message = "Description is required")
    @Size(max = 255, message = "Description must be at most 255 characters")
    private String description;

    @NotBlank(message = "Payment method is required")
    @Pattern(regexp = "^(CREDIT_CARD|DEBIT_CARD|BANK_TRANSFER|CASH|PAYPAL|APPLE_PAY|GOOGLE_PAY|CRYPTO)$", 
             message = "Invalid payment method")
    private String paymentMethod;

    @NotBlank(message = "Category is required")
    @Pattern(regexp = "^(GROCERIES|DINING|SHOPPING|TRANSPORTATION|UTILITIES|ENTERTAINMENT|HEALTHCARE|EDUCATION|TRAVEL|INCOME|TRANSFER|RENT|INSURANCE|OTHER)$", 
             message = "Invalid category")
    private String category;
}
