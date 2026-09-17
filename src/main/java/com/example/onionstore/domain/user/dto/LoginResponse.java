package com.example.onionstore.domain.user.dto;

import com.example.onionstore.domain.user.entity.Role;

public record LoginResponse(String accessToken, String tokenType, Long userId, Role role) {
}
