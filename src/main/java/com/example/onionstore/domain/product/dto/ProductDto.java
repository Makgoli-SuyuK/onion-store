package com.example.onionstore.domain.product.dto;

import com.example.onionstore.domain.product.entity.Product;

import java.time.LocalDateTime;

public record ProductDto(
        long id,
        String category,
        String name,
        String description,
        long price,
        String status,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
    public static ProductDto from(Product product) {
        return new ProductDto(
                product.getId(),
                product.getCategory().getName(),
                product.getName(),
                product.getDescription(),
                product.getPrice(),
                product.getStatus().name(),
                product.getCreatedAt(),
                product.getUpdatedAt()
        );
    }
}
