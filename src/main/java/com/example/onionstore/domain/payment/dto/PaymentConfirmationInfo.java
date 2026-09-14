package com.example.onionstore.domain.payment.dto;

import com.example.onionstore.domain.payment.entity.Payment;

public record PaymentConfirmationInfo(
        Long orderId,
        String portonePaymentId,
        long amount
) {
    public static PaymentConfirmationInfo from(Payment payment) {
        return new PaymentConfirmationInfo(
                payment.getOrder().getId(),
                payment.getPortonePaymentId(),
                payment.getAmount()
        );
    }
}
