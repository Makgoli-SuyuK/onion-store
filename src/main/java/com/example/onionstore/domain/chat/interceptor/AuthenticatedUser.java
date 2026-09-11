package com.example.onionstore.domain.chat.interceptor;

import com.example.onionstore.domain.user.entity.Role;

import lombok.Getter;

import java.security.Principal;

@Getter
public class AuthenticatedUser implements Principal {

    private final Long userId;
    private final Role role;

    public AuthenticatedUser(Long userId, Role role) {
        this.userId = userId;
        this.role = role;
    }

    @Override
    public String getName() {
        return String.valueOf(userId);
    }
}