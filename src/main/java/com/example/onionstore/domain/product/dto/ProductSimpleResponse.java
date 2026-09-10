package com.example.onionstore.domain.product.dto;

import com.example.onionstore.domain.product.entity.Product;

public record ProductSimpleResponse(
        long id,
        String categoryName,
        String name,
        long price,
        long likeCount
) {
    public static ProductSimpleResponse from(Product product) {
        return new ProductSimpleResponse(
                product.getId(),
                product.getCategory().getName(),
                product.getName(),
                product.getPrice(),
                product.getLikeCount()
        );
    }
}
