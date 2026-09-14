package com.example.onionstore.domain.order.dto;

import com.example.onionstore.domain.order.entity.Order;
import com.example.onionstore.domain.order.entity.OrderStatus;

import java.time.LocalDateTime;

public record ChangeOrderStatusResponse(
        Long orderId,
        OrderStatus previousStatus,
        OrderStatus status,
        LocalDateTime updatedAt
) {
    public static ChangeOrderStatusResponse from(Order order, OrderStatus previousStatus) {
        return new ChangeOrderStatusResponse(
                order.getId(),
                previousStatus,
                order.getStatus(),
                order.getUpdatedAt()
        );
    }
}
