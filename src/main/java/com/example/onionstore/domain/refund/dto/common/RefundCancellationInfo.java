package com.example.onionstore.domain.refund.dto.common;

public record RefundCancellationInfo(
        Long refundId,
        Long orderId,
        String portonePaymentId,
        long refundAmount,
        String reason
) {
}
