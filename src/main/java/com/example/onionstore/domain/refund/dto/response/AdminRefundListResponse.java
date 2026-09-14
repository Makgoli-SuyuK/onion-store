package com.example.onionstore.domain.refund.dto.response;

import com.example.onionstore.domain.refund.entity.RefundInitiator;
import com.example.onionstore.domain.refund.entity.RefundStatus;

import java.time.LocalDateTime;

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
