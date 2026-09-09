package com.example.onionstore.domain.chat.controller;

import com.example.onionstore.domain.chat.dto.request.ChatRoomCreateRequest;
import com.example.onionstore.domain.chat.dto.response.ChatRoomCreateResponse;
import com.example.onionstore.domain.chat.service.ChatRoomService;
import com.example.onionstore.global.dto.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/chats")
@RequiredArgsConstructor

public class ChatRoomController {
    private final ChatRoomService chatRoomService;
    @PostMapping("/rooms")
    public ResponseEntity<ApiResponse<ChatRoomCreateResponse>> createRoom(
            @RequestHeader("X-USER-ID")Long userId,
            @Valid @RequestBody ChatRoomCreateRequest request
    ){
        ChatRoomCreateResponse response = chatRoomService.createRoom(userId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(response));
    }

}
