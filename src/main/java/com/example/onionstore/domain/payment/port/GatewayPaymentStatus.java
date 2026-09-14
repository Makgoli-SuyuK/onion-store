package com.example.onionstore.domain.payment.port;

public enum GatewayPaymentStatus {
    READY,
    PENDING,
    PAID,
    FAILED,
    CANCELLED,
    PARTIAL_CANCELLED,
    NOT_FOUND,
    UNKNOWN;

}