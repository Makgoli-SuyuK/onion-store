package com.example.onionstore.domain.payment.port;

public interface PaymentGateway {
    PaymentGatewayResponse getPayment(String portonePaymentId);

    void cancelPaymentForAmountMismatch(String portonePaymentId, String reason);

    PaymentCancellationResponse requestPartialCancellation(
            String portonePaymentId,
            long amount,
            String reason
    );
}
