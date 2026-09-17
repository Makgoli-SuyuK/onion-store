package com.example.onionstore.domain.user.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import com.example.onionstore.domain.user.validation.ValidPassword;

public record SignupRequest(
        @NotBlank @Email @Size(max = 50) String email,
        @ValidPassword String password,
        @NotBlank @Size(max = 30) String name,
        @NotBlank @Size(max = 20) String phoneNumber
) {
    @Override
    public String toString() {
        return "SignupRequest[REDACTED]";
    }
}
