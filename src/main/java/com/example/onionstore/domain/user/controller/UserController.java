package com.example.onionstore.domain.user.controller;

import com.example.onionstore.domain.user.dto.UserMeResponse;
import com.example.onionstore.domain.user.service.AuthService;
import com.example.onionstore.global.dto.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {
    private final AuthService authService;

    @GetMapping("/me")
    public ResponseEntity<ApiResponse<UserMeResponse>> getMe(@AuthenticationPrincipal Jwt jwt) {
        UserMeResponse response = authService.getMe(Long.valueOf(jwt.getSubject()));
        return ResponseEntity.ok(ApiResponse.success("내 정보 조회에 성공했습니다.", response));
    }
}
