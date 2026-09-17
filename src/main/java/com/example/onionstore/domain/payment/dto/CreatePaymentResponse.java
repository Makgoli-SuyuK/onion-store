package com.example.onionstore.domain.payment.dto;

import com.example.onionstore.domain.payment.entity.Payment;
import com.example.onionstore.domain.payment.entity.PaymentStatus;

public record CreatePaymentResponse(
        Long paymentId,
        String portonePaymentId,
        long amount,
        PaymentStatus status
) {
    public static CreatePaymentResponse from(Payment payment) {
        return new CreatePaymentResponse(
                payment.getId(),
                payment.getPortonePaymentId(),
                payment.getAmount(),
                payment.getStatus()
        );
    }
}
