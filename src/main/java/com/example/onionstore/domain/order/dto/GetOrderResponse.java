package com.example.onionstore.domain.order.dto;

import com.example.onionstore.domain.order.entity.Order;
import com.example.onionstore.domain.order.entity.OrderStatus;
import com.example.onionstore.domain.payment.entity.Payment;
import com.example.onionstore.domain.payment.entity.PaymentStatus;

import java.time.LocalDateTime;
import java.util.List;

public record GetOrderResponse(
        String orderNumber,
        long totalPrice,
        LocalDateTime createdAt,
        LocalDateTime paidAt,
        OrderStatus orderStatus,
        PaymentStatus paymentStatus,
        List<GetOrderItemResponse> orderItems
) {
    public static GetOrderResponse from(Order order, Payment payment, List<GetOrderItemResponse> orderItems) {
        return new GetOrderResponse(
                order.getOrderNumber(),
                order.getTotalPrice(),
                order.getCreatedAt(),
                payment.getPaidAt(),
                order.getStatus(),
                payment.getStatus(),
                orderItems
        );
    }
}
