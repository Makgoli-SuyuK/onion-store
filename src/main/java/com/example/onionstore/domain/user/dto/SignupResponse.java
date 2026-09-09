package com.example.onionstore.domain.user.dto;

import com.example.onionstore.domain.user.entity.User;

public record SignupResponse(Long userId, String email, String name, String phoneNumber) {
    public static SignupResponse from(User user) {
        return new SignupResponse(user.getId(), user.getEmail(), user.getName(), user.getPhoneNumber());
    }
}
