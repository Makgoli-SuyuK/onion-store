package com.example.onionstore.domain.refund.dto.common;

import com.example.onionstore.domain.refund.repository.dto.OrderItemSummaryRow;

import java.util.List;

// 환불 상세 조회용 주문 요약 정보
public record RefundOrderInfoResponse(
        String orderNumber,
        String productSummaryName,
        int totalOrderQuantity,
        long orderAmount
) {
    public static RefundOrderInfoResponse from(
            String orderNumber,
            long orderAmount,
            List<OrderItemSummaryRow> rows
    ) {
        OrderItemSummaryRow firstItem = rows.get(0);
        String productSummaryName = rows.size() == 1
                ? firstItem.productName()
                : firstItem.productName() + " 외 " + (rows.size() - 1) + "건";

        int totalOrderQuantity = rows.stream()
                .mapToInt(OrderItemSummaryRow::orderedQuantity)
                .sum();

        return new RefundOrderInfoResponse(
                orderNumber,
                productSummaryName,
                totalOrderQuantity,
                orderAmount
        );
    }
}
