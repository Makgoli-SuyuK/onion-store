package com.example.onionstore.domain.payment.dto;

import com.example.onionstore.domain.payment.entity.Payment;
import com.example.onionstore.domain.payment.entity.PaymentStatus;

import java.time.LocalDateTime;

public record GetPaymentInfoResponse(
        LocalDateTime paidAt,
        PaymentStatus status
) {
    public static GetPaymentInfoResponse from(Payment payment) {
        return new GetPaymentInfoResponse(
                payment.getPaidAt(),
                payment.getStatus()
        );
    }
}
