package com.example.onionstore.domain.cart.dto.response;

import com.example.onionstore.domain.cart.entity.Cart;

import java.util.List;

public record CartResponse(
        Long cartId,
        List<CartItemResponse> items,
        long totalPrice
) {
    public static CartResponse from(Cart cart, List<CartItemResponse> items) {
        long totalPrice = items.stream()
                .mapToLong(item -> item.price() * item.quantity())
                .sum();
        return new CartResponse(cart.getId(), items, totalPrice);
    }
}
