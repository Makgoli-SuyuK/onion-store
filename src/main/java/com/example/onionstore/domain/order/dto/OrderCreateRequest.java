package com.example.onionstore.domain.order.dto;

import java.util.List;

public record OrderCreateRequest(
        List<Long> cartItemIds
) {
    public OrderCreateRequest {
        if (cartItemIds == null) {
            cartItemIds = List.of();
        }
    }
}
