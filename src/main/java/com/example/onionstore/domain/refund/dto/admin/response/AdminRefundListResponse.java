package com.example.onionstore.domain.refund.dto.admin.response;

import com.example.onionstore.domain.refund.entity.RefundInitiator;
import com.example.onionstore.domain.refund.entity.RefundStatus;

import java.time.LocalDateTime;

// 관리자 환불 목록 조회용 응답
public record AdminRefundListResponse(
        Long refundId,
        String orderNumber,
        String customerName,
        LocalDateTime requestedAt,
        RefundStatus status,
        long orderAmount,
        long requestedAmount,
        RefundInitiator initiator
) {
}
