package com.example.onionstore.domain.refund.service.internal;

import com.example.onionstore.domain.refund.entity.Refund;

/**
 * 트랜잭션에서 확정한 환불 정보를
 * PortOne 취소 실행 단계로 전달하는 내부 데이터.
 */
public record RefundExecutionInfo(
        Long refundId,
        String portonePaymentId,
        long amount,
        String reason
) {
    public static RefundExecutionInfo from(Refund refund) {
        return new RefundExecutionInfo(
                refund.getId(),
                refund.getPayment().getPortonePaymentId(),
                refund.getAmount(),
                refund.getReason()
        );
    }

    public String idempotencyKey() {
        return "onion-store-refund-" + refundId;
    }

    // PortOne 취소 이력에서 로컬 환불 건을 다시 찾기 위한 식별자를 포함한다.
    public String gatewayReason() {
        return "[refund:" + refundId + "] " + reason;
    }
}