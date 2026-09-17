package com.example.onionstore.domain.refund.repository.dto;

public record CompletedRefundQuantityRow(
        Long orderItemId,
        Long completedQuantity
) {
}