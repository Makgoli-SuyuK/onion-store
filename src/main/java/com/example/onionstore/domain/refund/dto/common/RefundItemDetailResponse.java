package com.example.onionstore.domain.refund.dto.common;

// 환불 대상 주문 항목과 요청 수량 정보
public record RefundItemDetailResponse(
        Long orderItemId,
        String productName,
        int orderedQuantity,
        int requestedQuantity
) {
}
