package com.example.onionstore.domain.refund.dto.response;

import com.example.onionstore.domain.refund.entity.Refund;
import com.example.onionstore.domain.refund.entity.RefundStatus;

import java.time.LocalDateTime;

public record RefundReviewResponse(
        Long refundId,
        RefundStatus status,
        LocalDateTime reviewedAt,
        String rejectionReason
) {
    public static RefundReviewResponse from(Refund refund) {
        return new RefundReviewResponse(
                refund.getId(),
                refund.getStatus(),
                refund.getReviewedAt(),
                refund.getRejectionReason()
        );
    }
}
