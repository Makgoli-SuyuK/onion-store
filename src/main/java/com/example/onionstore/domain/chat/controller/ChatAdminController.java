package com.example.onionstore.domain.chat.controller;

import com.example.onionstore.domain.chat.dto.request.ChatRoomStatusUpdateRequest;
import com.example.onionstore.domain.chat.dto.response.ChatRoomStatusUpdateResponse;
import com.example.onionstore.domain.chat.service.ChatRoomService;
import com.example.onionstore.global.dto.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/admin/chats")
public class ChatAdminController {

    private final ChatRoomService chatRoomService;

    @PatchMapping("/rooms/{roomId}/status")
    public ResponseEntity<ApiResponse<ChatRoomStatusUpdateResponse>> updateStatus(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable Long roomId,
            @Valid @RequestBody ChatRoomStatusUpdateRequest request
            ){
        Long userId = Long.valueOf(jwt.getSubject());
        ChatRoomStatusUpdateResponse response =
                chatRoomService.updateStatus(userId, roomId, request.getStatus());
        return ResponseEntity.ok(ApiResponse.success(response));
    }
}

