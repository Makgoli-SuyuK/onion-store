package com.example.onionstore.domain.order.dto;

import com.example.onionstore.domain.order.entity.OrderStatus;

public record ChangeOrderStatusRequest(
        OrderStatus status
) {
}
