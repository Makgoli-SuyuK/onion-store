package com.example.onionstore.domain.order.dto;

import com.example.onionstore.domain.order.entity.Order;
import com.example.onionstore.domain.payment.dto.GetPaymentInfoResponse;

import java.time.LocalDateTime;
import java.util.List;

public record GetOrderListResponse(
        Long orderId,
        String orderNumber,
        List<GetOrderListItemResponse> items,
        long totalPrice,
        LocalDateTime createdAt,
        LocalDateTime paidAt
) {
    public static GetOrderListResponse from(Order order, List<GetOrderListItemResponse> items, GetPaymentInfoResponse info) {
        return new GetOrderListResponse(
                order.getId(),
                order.getOrderNumber(),
                items,
                order.getTotalPrice(),
                order.getCreatedAt(),
                info.paidAt()
        );
    }
}
