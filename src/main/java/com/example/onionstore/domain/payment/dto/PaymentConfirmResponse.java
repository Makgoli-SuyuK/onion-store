package com.example.onionstore.domain.payment.dto;

import com.example.onionstore.domain.order.entity.OrderStatus;
import com.example.onionstore.domain.payment.entity.PaymentStatus;

public record PaymentConfirmResponse(
        Long orderId,
        String portonePaymentId,
        PaymentStatus paymentStatus,
        OrderStatus orderStatus
) {
    public static PaymentConfirmResponse success(Long orderId, String portonePaymentId) {
        return new PaymentConfirmResponse(
                orderId,
                portonePaymentId,
                PaymentStatus.SUCCESS,
                OrderStatus.PAID
        );
    }
}
