package com.example.onionstore.domain.refund.repository.dto;

import com.example.onionstore.domain.refund.entity.RefundInitiator;
import com.example.onionstore.domain.refund.entity.RefundReasonType;
import com.example.onionstore.domain.refund.entity.RefundStatus;

import java.time.LocalDateTime;

// 관리자 환불 상세 응답 조립에 사용하는 조회 결과
public record AdminRefundDetailProjection(
        Long orderId,
        String orderNumber,
        long orderAmount,
        Long refundId,
        RefundStatus status,
        RefundInitiator initiator,
        RefundReasonType reasonType,
        String reason,
        long requestedAmount,
        LocalDateTime requestedAt,
        LocalDateTime reviewedAt,
        String rejectionReason,
        String customerName,
        String phoneNumber,
        String email,
        String reviewerName,
        String portoneCancellationId
) {
}
