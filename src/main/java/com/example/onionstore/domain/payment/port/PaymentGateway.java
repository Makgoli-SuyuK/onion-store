package com.example.onionstore.domain.payment.port;

public interface PaymentGateway {
    PaymentGatewayResponse getPayment(String portonePaymentId);
}
