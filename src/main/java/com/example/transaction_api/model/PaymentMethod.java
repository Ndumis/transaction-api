package com.example.transaction_api.model;


public enum PaymentMethod {
    CREDIT_CARD("Credit Card"),
    DEBIT_CARD("Debit Card"),
    BANK_TRANSFER("Bank Transfer"),
    CASH("Cash"),
    PAYPAL("PayPal"),
    APPLE_PAY("Apple Pay"),
    GOOGLE_PAY("Google Pay"),
    CRYPTOCURRENCY("Cryptocurrency"),
    OTHER("Other");

    private final String displayName;

    PaymentMethod(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
