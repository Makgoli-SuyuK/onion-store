package com.example.onionstore.domain.cart.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;


public record AddCartItemRequest(
        @NotNull Long productId,
        @Positive int quantity

) {
}
