package com.example.onionstore.domain.refund.dto.admin.response;

import com.example.onionstore.domain.refund.entity.Refund;
import com.example.onionstore.domain.refund.entity.RefundStatus;

import java.time.LocalDateTime;

// 관리자 환불 승인·거절 처리 결과 응답
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
