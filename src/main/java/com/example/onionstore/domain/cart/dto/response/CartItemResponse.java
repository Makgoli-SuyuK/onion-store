package com.example.onionstore.domain.cart.dto.response;

import com.example.onionstore.domain.cart.entity.CartItem;
import com.example.onionstore.domain.product.entity.Product;

public record CartItemResponse(
        Long cartItemId,
        Long productId,
        String productName,
        long productPrice,
        long quantity,
        long subtotal
) {
    public static CartItemResponse from(CartItem cartItem) {
        Product product = cartItem.getProduct();

        return new CartItemResponse(
                cartItem.getId(),
                product.getId(),
                product.getName(),
                product.getPrice(),
                cartItem.getQuantity(),
                product.getPrice() * cartItem.getQuantity()
        );
    }
}
