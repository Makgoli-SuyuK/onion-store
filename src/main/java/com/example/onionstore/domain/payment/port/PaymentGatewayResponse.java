package com.example.onionstore.domain.payment.port;

public record PaymentGatewayResponse(
        String portonePaymentId,
        GatewayPaymentStatus status,
        Long totalAmount
) {
    public boolean isPaid() {
        return status == GatewayPaymentStatus.PAID;
    }
}