package com.example.onionstore.domain.user.dto;

import com.example.onionstore.domain.user.validation.ValidPassword;
import jakarta.validation.constraints.NotBlank;

public record PasswordChangeRequest(
        @NotBlank(message = "비밀번호를 입력해주세요.") String currentPassword,
        @ValidPassword String newPassword
) {
}
