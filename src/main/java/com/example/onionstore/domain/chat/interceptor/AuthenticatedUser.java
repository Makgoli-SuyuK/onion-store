package com.example.onionstore.domain.chat.interceptor;

import com.example.onionstore.domain.user.entity.User;
import lombok.Getter;

import java.security.Principal;

@Getter
public class AuthenticatedUser implements Principal {

    private final User user;

    public AuthenticatedUser(User user) {
        this.user = user;
    }

    @Override
    public String getName() {
        return String.valueOf(user.getId());
    }

    public static User fromPrincipal(Principal principal) {
        return ((AuthenticatedUser) principal).getUser();
    }
}
