package com.example.onionstore.domain.refund.dto.response;

import com.example.onionstore.domain.refund.repository.dto.OrderItemSummaryRow;

import java.util.List;

public record RefundOrderSummaryResponse(
        String orderNumber,
        String productSummaryName,
        int totalOrderQuantity,
        long orderAmount
) {
    public static RefundOrderSummaryResponse from(
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

        return new RefundOrderSummaryResponse(
                orderNumber,
                productSummaryName,
                totalOrderQuantity,
                orderAmount
        );
    }
}
