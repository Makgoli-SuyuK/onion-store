package com.example.onionstore.domain.user.dto;

import com.example.onionstore.domain.user.entity.Role;
import com.example.onionstore.domain.user.entity.User;

public record UserMeResponse(Long userId, String email, String name, String phoneNumber, Role role) {
    public static UserMeResponse from(User user) {
        return new UserMeResponse(user.getId(), user.getEmail(), user.getName(), user.getPhoneNumber(), user.getRole());
    }
}
