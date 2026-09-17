package com.example.onionstore.domain.order.dto;

import com.example.onionstore.domain.order.entity.OrderStatus;

import java.time.LocalDateTime;
import java.util.List;

public record OrderCreateResponse(
        Long orderId,
        String orderNumber,
        long totalPrice,
        LocalDateTime createdAt,
        OrderStatus status,
        String portonePaymentId,
        List<GetOrderListItemResponse> items
) {
}
