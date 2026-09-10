package com.example.onionstore.domain.order.dto;

import com.example.onionstore.domain.order.entity.OrderItem;

public record GetOrderListItemResponse(
        String productName,
        long productPrice,
        int quantity
) {
    public static GetOrderListItemResponse from(OrderItem orderItem) {
        return new GetOrderListItemResponse(
                orderItem.getProductName(),
                orderItem.getProductPrice(),
                orderItem.getQuantity()
        );
    }
}
