package com.example.onionstore.domain.refund.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record RefundItemRequest(
        @NotNull Long orderItemId,
        @Positive int quantity
) {
}