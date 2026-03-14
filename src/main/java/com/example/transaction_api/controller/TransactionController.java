package com.example.transaction_api.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.time.LocalDateTime;

import com.example.transaction_api.dto.AggregatedTransactionResponse;
import com.example.transaction_api.dto.TransactionRequest;
import com.example.transaction_api.model.Transaction;
import com.example.transaction_api.service.TransactionService;

@Slf4j
@RestController
@RequestMapping("/api/v1/transactions")
@RequiredArgsConstructor
@Validated
@Tag(name = "Transaction Management", description = "Endpoints for managing transactions")
public class TransactionController {
    private final TransactionService transactionService;

    @PostMapping
    @Operation(summary = "Create a new transaction", description = "Creates a new transaction for a user with specified details")
    @ApiResponses(value = {
        @ApiResponse(responseCode  = "201", description  = "Transaction created successfully"),
        @ApiResponse(responseCode = "400", description  = "Invalid input request data"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<Transaction> createTransaction(@RequestBody @Valid TransactionRequest request) {        
        log.info("Received request to create transaction for user {} with amount ${}", request.getUserId(), request.getAmount());

        Transaction createdTransaction = transactionService.createTransaction(request);
        log.info("Transaction created successfully with ID: {}", createdTransaction.getId());

        return new ResponseEntity<>(createdTransaction, HttpStatus.CREATED);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get transaction by ID", description = "Retrieves a transaction by its unique ID")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Transaction retrieved successfully"),
        @ApiResponse(responseCode = "404", description = "Transaction not found"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<Transaction> getTransaction(@PathVariable String id) {
        log.info("Received request to get transaction with ID: {}", id);
        
        Transaction transaction = transactionService.getTransactionById(id);
        
        log.info("Transaction with ID {} retrieved successfully", id);
        return ResponseEntity.ok(transaction);
    }

    @GetMapping("/user/{userId}")
    @Operation(summary = "Get transactions by user ID", description = "Retrieves all transactions for a specific user")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Transactions retrieved successfully"),
        @ApiResponse(responseCode = "404", description = "No transactions found for user"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<Page<Transaction>> getTransactionsByUserId(
            @Parameter(description = "User ID", required = true) @PathVariable String userId,
            @PageableDefault(size = 20, sort = "transactionDate", direction = Sort.Direction.DESC) Pageable pageable) {
        log.info("Received request to get transactions for user ID: {}", userId);

        Page<Transaction> transactions = transactionService.getTransactionsByUserId(userId, pageable);
        if(transactions.isEmpty()) {
            log.warn("No transactions found for user ID {}", userId);
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        log.info("Retrieved {} transactions for user ID {}", transactions.getContent().size(), userId);

        return new ResponseEntity<>(transactions, HttpStatus.OK);
    }

    @GetMapping("/user/{userId}/aggregate")
    @Operation(summary = "Get aggregated transaction data", description = "Retrieves aggregated transaction data for a user within a specified date range")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Aggregated transaction data retrieved successfully"),
        @ApiResponse(responseCode = "404", description = "No transactions found for user in date range"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<AggregatedTransactionResponse> aggregateTransaction(
        @Parameter(description = "User ID", required = true) @PathVariable String userId,
        @Parameter(description = "Start date (ISO format)", required = true) @RequestParam LocalDateTime startDate,
        @Parameter(description = "End date (ISO format)", required = true) @RequestParam LocalDateTime endDate) {
        log.info("Received request to aggregate transactions for user ID: {} between {} and {}", userId, startDate, endDate);

        if(startDate.isAfter(endDate)) {
            log.error("Invalid date range: startDate {} is after endDate {}", startDate, endDate);
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }

        AggregatedTransactionResponse response = transactionService.aggregatedTransactionResponse(userId, startDate, endDate);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @GetMapping("/user/{userId}/count")
    @Operation(summary = "Get transaction count", description = "Retrieves the total count of transactions for a user within a specified date range")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Transaction count retrieved successfully"),
        @ApiResponse(responseCode = "404", description = "No transactions found for user in date range"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<Long> getTransactionCount(
        @Parameter(description = "User ID", required = true) @PathVariable String userId,
        @Parameter(description = "Start date (ISO format)", required = true) @RequestParam LocalDateTime startDate,
        @Parameter(description = "End date (ISO format)", required = true) @RequestParam LocalDateTime endDate) {
        log.info("Received request to get transaction count for user ID: {} between {} and {}", userId, startDate, endDate);

        if(startDate.isAfter(endDate)) {
            log.error("Invalid date range: startDate {} is after endDate {}", startDate, endDate);
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }

        long count = transactionService.getTransactionCountByUserIdAndDateRange(userId, startDate, endDate);
        return new ResponseEntity<>(count, HttpStatus.OK);
    }
    
    @GetMapping("/user/{userId}/validate/{transactionId}")
    @Operation(summary = "Validate transaction ownership", description = "Validates whether a transaction belongs to a specific user")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Transaction ownership validated successfully"),
        @ApiResponse(responseCode = "404", description = "Transaction not found or does not belong to user"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<Boolean> validateUserTransaction(
        @Parameter(description = "User ID", required = true) @PathVariable String userId,
        @Parameter(description = "Transaction ID", required = true) @PathVariable String transactionId) {
        log.info("Received request to validate transaction ownership for user ID: {} and transaction ID: {}", userId, transactionId);
        
        Boolean isValid = transactionService.validateUserTransaction(userId, transactionId);
        return new ResponseEntity<>(isValid, HttpStatus.OK);
    }

}
