package com.example.transaction_api.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonInclude;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class AggregatedTransactionResponse {
    private String userId;
    private BigDecimal totalAmount;
    private long totalTransactions;
    private Map<String, CategorySummary> categorySummaries;
    private Map<String, PaymentMethodSummary> paymentMethodSummaries;
    private DateRange dateRange;
    private Summary summary;

   @Data
    @Builder
    public static class CategorySummary {
        private String category;
        private BigDecimal totalAmount;
        private long count;
        private double percentageOfTotal;
        private BigDecimal averageAmount;
    }

    @Data
    @Builder
    public static class PaymentMethodSummary {
        private String paymentMethod;
        private BigDecimal totalAmount;
        private long count;
        private BigDecimal averageAmount;
    }

    @Data
    @Builder
    public static class DateRange {
        private LocalDateTime startDate;
        private LocalDateTime endDate;
    }

    @Data
    @Builder
    public static class Summary {
        private BigDecimal minAmount;
        private BigDecimal maxAmount;
        private BigDecimal averageAmount;
        private long completedCount;
        private long pendingCount;
        private long failedCount;
    }
}
