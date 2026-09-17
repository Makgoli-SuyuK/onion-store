package com.example.onionstore.domain.user.dto;

import jakarta.validation.constraints.Size;

public record UserProfileUpdateRequest(
        @Size(max = 30) String name,
        @Size(max = 20) String phoneNumber
) {
}
