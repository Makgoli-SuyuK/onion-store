package com.example.onionstore.domain.product.dto;

import com.example.onionstore.domain.product.entity.Product;

import java.time.LocalDateTime;

public record ProductResponse(
        long id,
        String categoryName,
        String name,
        String description,
        long price,
        int stock,
        long likeCount,
        String status,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
    public static ProductResponse from(Product product) {
        return new ProductResponse(
                product.getId(),
                product.getCategory().getName(),
                product.getName(),
                product.getDescription(),
                product.getPrice(),
                product.getStock(),
                product.getLikeCount(),
                product.getStatus().name(),
                product.getCreatedAt(),
                product.getUpdatedAt()
        );
    }

    public static ProductResponse from(ProductDto dto, Integer stock, Long likeCount) {
        return new ProductResponse(
                dto.id(),
                dto.category(),
                dto.name(),
                dto.description(),
                dto.price(),
                stock,
                likeCount,
                dto.status(),
                dto.createdAt(),
                dto.updatedAt()
        );
    }
}
