package com.example.onionstore.domain.user.dto;

import com.example.onionstore.domain.user.validation.ValidPassword;

public record PasswordChangeRequest(
        String currentPassword,
        @ValidPassword String newPassword
) {
}
