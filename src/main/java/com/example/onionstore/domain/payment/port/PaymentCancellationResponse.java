package com.example.onionstore.domain.payment.port;

public record PaymentCancellationResponse(
        String cancellationId,
        GatewayCancellationStatus status
) {
}
