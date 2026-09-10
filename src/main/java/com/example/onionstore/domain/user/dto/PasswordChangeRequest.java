package com.example.onionstore.domain.user.dto;

public record PasswordChangeRequest(
        String currentPassword,
        String newPassword
) {
}
