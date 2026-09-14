package com.example.onionstore.domain.refund.repository.dto;

public record OrderItemSummaryRow(
        String productName,
        int orderedQuantity
) {
}
