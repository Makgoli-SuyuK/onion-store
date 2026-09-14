package com.example.onionstore.domain.refund.dto.customer.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

// 환불할 주문 항목과 수량 요청
public record RefundItemRequest(
        @NotNull Long orderItemId,
        @Positive int quantity
) {
}