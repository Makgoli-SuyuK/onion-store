package com.example.onionstore.domain.chat.controller;

import com.example.onionstore.domain.chat.dto.response.ChatMessageListResponse;
import com.example.onionstore.domain.chat.service.ChatMessageService;
import com.example.onionstore.global.dto.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;


@RestController
@RequiredArgsConstructor
@RequestMapping("/api/chats")
public class ChatMessageController {

    private final ChatMessageService chatMessageService;

    @GetMapping("/rooms/{roomId}/messages")
    public ResponseEntity<ApiResponse<ChatMessageListResponse>> getMessages(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable Long roomId,
            @RequestParam(required = false) Long lastMessageId,
            @RequestParam(defaultValue = "30") int size
    ){
        Long userId = Long.valueOf(jwt.getSubject());
        ChatMessageListResponse response = chatMessageService.getMessages(userId, roomId, lastMessageId, size);
        return ResponseEntity.ok(ApiResponse.success(response));

    }


}
