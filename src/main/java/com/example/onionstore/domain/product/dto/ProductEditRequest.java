package com.example.onionstore.domain.product.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;

public record ProductEditRequest (
        @NotBlank
        String name,

        @NotBlank
        String description,

        @Min(1)
        Long price,

        @Min(1)
        Integer stock,

        @NotBlank
        String status

){
}
