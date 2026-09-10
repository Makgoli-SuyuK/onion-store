package com.example.onionstore.domain.user.controller;

import com.example.onionstore.domain.user.dto.UserMeResponse;
import com.example.onionstore.domain.user.dto.UserProfileUpdateRequest;
import com.example.onionstore.domain.user.dto.PasswordChangeRequest;
import com.example.onionstore.domain.user.service.UserService;
import jakarta.validation.Valid;
import com.example.onionstore.global.dto.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {
    private final UserService userService;

    @GetMapping("/me")
    public ResponseEntity<ApiResponse<UserMeResponse>> getMe(@AuthenticationPrincipal Jwt jwt) {
        UserMeResponse response = userService.getMe(Long.valueOf(jwt.getSubject()));
        return ResponseEntity.ok(ApiResponse.success("내 정보 조회에 성공했습니다.", response));
    }

    @PatchMapping("/me")
    public ResponseEntity<ApiResponse<UserMeResponse>> updateProfile(
            @AuthenticationPrincipal Jwt jwt,
            @Valid @RequestBody UserProfileUpdateRequest request) {
        UserMeResponse response = userService.updateProfile(Long.valueOf(jwt.getSubject()), request);
        return ResponseEntity.ok(ApiResponse.success("내 정보 수정에 성공했습니다.", response));
    }

    @PatchMapping("/me/password")
    public ResponseEntity<ApiResponse<Void>> changePassword(
            @AuthenticationPrincipal Jwt jwt,
            @RequestBody PasswordChangeRequest request) {
        userService.changePassword(Long.valueOf(jwt.getSubject()),
                request.currentPassword(), request.newPassword());
        return ResponseEntity.ok(ApiResponse.success("비밀번호 변경에 성공했습니다.", null));
    }
}
