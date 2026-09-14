package com.example.onionstore.domain.refund.dto.response;

public record RefundItemDetailResponse(
        Long orderItemId,
        String productName,
        int orderedQuantity,
        int requestedQuantity
) {
}
