package com.example.onionstore.domain.payment.dto;

public record PaymentStateChangeResponse(
        boolean changed,
        GetPaymentInfoResponse payment
) {
}