package com.example.transaction_api.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "transactions", indexes = {
    @jakarta.persistence.Index(name = "idx_user_id", columnList = "userId"),
    @jakarta.persistence.Index(name = "idx_date", columnList = "transactionDate"),
    @jakarta.persistence.Index(name = "idx_category", columnList = "category")
})
@EntityListeners(AuditingEntityListener.class)
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor 
public class Transaction {
    public enum TransactionStatus{
        PENDING, COMPLETED, FAILED, CANCELLED, REFUNDED
    }

    public enum TransactionCategory{
        GROCERIES, DINING, SHOPPING, TRANSPORTATION, UTILITIES, ENTERTAINMENT, HEALTHCARE, EDUCATION, TRAVEL, INCOME, TRANSFER, RENT, INSURANCE, OTHER
    }

      @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler", "transactions"})
    private User user;
    
    @NotNull(message = "Amount is required")
    @DecimalMin(value = "0.01", message = "Amount must be greater than 0")
    @DecimalMax(value = "1000000.00", message = "Amount cannot exceed 1,000,000")
    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal amount;
    
    @NotBlank(message = "Currency is required")
    @Pattern(regexp = "^(ZAR|USD|EUR|GBP|AUD|CAD|JPY|CNY)$", 
             message = "Currency must be a valid ISO code (ZAR, USD, EUR, GBP, etc.)")
    @Column(nullable = false, length = 3)
    private String currency;
    
    @NotNull(message = "Transaction date is required")
    @PastOrPresent(message = "Transaction date cannot be in the future")
    @Column(name = "transaction_date", nullable = false)
    private LocalDateTime transactionDate;
    
    @NotBlank(message = "Description is required")
    @Size(min = 3, max = 200, message = "Description must be between 3 and 200 characters")
    @Column(nullable = false, length = 200)
    private String description;
    
    @NotNull(message = "Payment method is required")
    @Enumerated(EnumType.STRING)
    @Column(name = "payment_method", nullable = false, length = 20)
    private PaymentMethod paymentMethod;
    
    @NotNull(message = "Transaction status is required")
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private TransactionStatus status;
    
    @NotNull(message = "Category is required")
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private TransactionCategory category;
    
    @CreatedDate
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
    
    @LastModifiedDate
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}
