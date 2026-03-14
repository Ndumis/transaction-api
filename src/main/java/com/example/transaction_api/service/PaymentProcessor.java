package com.example.transaction_api.service;

import com.example.transaction_api.dto.TransactionRequest;
import com.example.transaction_api.model.PaymentMethod;
import com.example.transaction_api.model.*;

public interface PaymentProcessor {
    Transaction processPayment(TransactionRequest request, User user);
    PaymentMethod getPaymentMethod();
    boolean validatePaymentDetails(TransactionRequest request);
    String getProcessorName();
}
