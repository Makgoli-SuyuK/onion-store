package com.example.onionstore.domain.refund.dto.response;

import com.example.onionstore.domain.refund.entity.RefundStatus;

import java.time.LocalDateTime;

public record CustomerRefundSummaryResponse(
        Long refundId,
        RefundStatus status,
        long requestedAmount,
        LocalDateTime requestedAt,
        LocalDateTime reviewedAt,
        String rejectionReason
) {
}