package com.example.onionstore.domain.refund.dto.customer.response;

import com.example.onionstore.domain.refund.entity.RefundStatus;

import java.time.LocalDateTime;

// 고객 환불 목록 조회용 요약 응답
public record CustomerRefundSummaryResponse(
        Long refundId,
        RefundStatus status,
        long requestedAmount,
        LocalDateTime requestedAt,
        LocalDateTime reviewedAt,
        String rejectionReason
) {
}