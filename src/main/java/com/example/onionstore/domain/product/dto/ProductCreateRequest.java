package com.example.onionstore.domain.product.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;

public record ProductCreateRequest(
        @NotBlank
        String category,

        @NotBlank
        String productName,

        @NotBlank
        String description,

        @Min(1)
        long price,

        @Min(1)
        int stock
) {
}
