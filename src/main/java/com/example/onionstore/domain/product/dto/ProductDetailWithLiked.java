package com.example.onionstore.domain.product.dto;

public record ProductDetailWithLiked(
        ProductResponse productInfo,
        boolean liked
) {
    public static ProductDetailWithLiked from(ProductResponse productInfo, boolean liked) {
        return new ProductDetailWithLiked(productInfo, liked);
    }
}
