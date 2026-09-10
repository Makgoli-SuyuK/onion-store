package com.example.onionstore.domain.product.repository.dto;

import jakarta.annotation.Nullable;

public record ProductSearchConditions(
        @Nullable
        String category,
        @Nullable
        String name,
        @Nullable
        Long priceStart,
        @Nullable
        Long priceEnd,
        @Nullable
        Integer likeCount,

        @Nullable
        String sortBy,
        @Nullable
        String sortOrder,

        int page,
        int limit
) {
}
