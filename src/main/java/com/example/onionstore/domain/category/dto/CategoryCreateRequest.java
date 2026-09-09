package com.example.onionstore.domain.category.dto;

import jakarta.validation.constraints.NotBlank;

public record CategoryCreateRequest (
        @NotBlank
        String name
) {
}
