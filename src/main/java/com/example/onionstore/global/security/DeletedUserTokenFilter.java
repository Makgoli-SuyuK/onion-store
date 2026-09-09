package com.example.onionstore.global.security;

import com.example.onionstore.domain.user.entity.UserStatus;
import com.example.onionstore.domain.user.repository.UserRepository;
import com.example.onionstore.global.exception.ErrorCode;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@Component
@RequiredArgsConstructor
public class DeletedUserTokenFilter extends OncePerRequestFilter {
    private final UserRepository userRepository;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        var authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication instanceof JwtAuthenticationToken jwtAuthentication
                && isDeleted(jwtAuthentication.getToken().getSubject())) {
            SecurityContextHolder.clearContext();
            response.setStatus(ErrorCode.UNAUTHORIZED.getStatus().value());
            response.setContentType("application/json;charset=UTF-8");
            response.getWriter().write("{\"success\":false,\"code\":\"AUTH_001\",\"message\":\"인증이 필요합니다.\",\"data\":null}");
            return;
        }
        filterChain.doFilter(request, response);
    }

    private boolean isDeleted(String subject) {
        try {
            return userRepository.findById(Long.valueOf(subject))
                    .map(user -> user.getStatus() == UserStatus.DELETED)
                    .orElse(true);
        } catch (NumberFormatException exception) {
            return true;
        }
    }
}
