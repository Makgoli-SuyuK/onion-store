package com.example.onionstore.domain.order.dto;

import com.example.onionstore.domain.order.entity.Order;
import com.example.onionstore.domain.order.entity.OrderStatus;

import java.time.LocalDateTime;

public record CancelOrderResponse(
        Long orderId,
        OrderStatus status,
        LocalDateTime cancelledAt
) {
    public static CancelOrderResponse from(Order order) {
        return new CancelOrderResponse(
                order.getId(),
                order.getStatus(),
                order.getUpdatedAt()
        );
    }
}
