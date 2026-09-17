package com.example.onionstore.domain.user.controller;

import com.example.onionstore.domain.user.dto.SignupRequest;
import com.example.onionstore.domain.user.dto.SignupResponse;
import com.example.onionstore.domain.user.dto.LoginRequest;
import com.example.onionstore.domain.user.dto.LoginResponse;
import com.example.onionstore.domain.user.service.AuthService;
import com.example.onionstore.global.security.JwtTokenProvider;
import com.example.onionstore.global.dto.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {
    private final AuthService authService;
    private final JwtTokenProvider jwtTokenProvider;

    @PostMapping("/signup")
    public ResponseEntity<ApiResponse<SignupResponse>> signup(@Valid @RequestBody SignupRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("회원가입에 성공했습니다.", authService.signup(request)));
    }

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<LoginResponse>> login(@Valid @RequestBody LoginRequest request) {
        var user = authService.login(request.email(), request.password());
        LoginResponse response = new LoginResponse(jwtTokenProvider.createAccessToken(user), "Bearer",
                user.getId(), user.getRole());
        return ResponseEntity.ok(ApiResponse.success("로그인에 성공했습니다.", response));
    }
}
