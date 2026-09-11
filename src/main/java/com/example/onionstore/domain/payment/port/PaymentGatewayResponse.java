package com.example.onionstore.domain.payment.port;

public record PaymentGatewayResponse(
        String portonePaymentId,
        Long totalAmount,
        boolean paid
) {
}
