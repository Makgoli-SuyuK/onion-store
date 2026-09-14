package com.example.onionstore.domain.refund.repository.dto;

// 환불 상세 조회에 사용하는 주문 상품명과 수량
public record OrderItemSummaryRow(
        String productName,
        int orderedQuantity
) {
}
