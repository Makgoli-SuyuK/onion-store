package com.example.onionstore.domain.cart.dto.request;

import jakarta.validation.constraints.Positive;

public record UpdateCartItemQuantityRequest(
        @Positive int quantity
) {
}
