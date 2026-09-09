package com.example.onionstore.domain.order.dto;

import com.example.onionstore.domain.order.entity.OrderItem;

public record GetOrderItemResponse(
        Long orderItemId,
        Long productId,
        String productName,
        long productPrice,
        int quantity
) {
    public static GetOrderItemResponse from(OrderItem orderItem) {
        return new GetOrderItemResponse(
                orderItem.getId(),
                orderItem.getProduct().getId(),
                orderItem.getProductName(),
                orderItem.getProductPrice(),
                orderItem.getQuantity()
        );
    }
}
