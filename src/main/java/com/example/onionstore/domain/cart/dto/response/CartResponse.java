package com.example.onionstore.domain.cart.dto.response;

import com.example.onionstore.domain.cart.entity.CartItem;

import java.util.List;

public record CartResponse(
        List<CartItemResponse> item,
        long totalPrice
) {
    public static CartResponse from(List<CartItem> cartItems) {
        List<CartItemResponse> items = cartItems.stream()
                .map(CartItemResponse::from)
                .toList();
        long totalPrice = items.stream()
                .mapToLong(CartItemResponse::subtotal)
                .sum();
        return new CartResponse(items, totalPrice);
    }
}
