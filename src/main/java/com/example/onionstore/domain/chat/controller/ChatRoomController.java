package com.example.onionstore.domain.chat.controller;

import com.example.onionstore.domain.chat.dto.request.ChatRoomCreateRequest;
import com.example.onionstore.domain.chat.dto.response.ChatRoomCreateResponse;

import com.example.onionstore.domain.chat.dto.response.ChatRoomListResponse;
import com.example.onionstore.domain.chat.entity.ChatRoomStatus;

import com.example.onionstore.domain.chat.dto.response.ChatRoomDetailResponse;

import com.example.onionstore.domain.chat.service.ChatRoomService;
import com.example.onionstore.global.dto.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
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
    @GetMapping("/rooms")
    public ResponseEntity<ApiResponse<Page<ChatRoomListResponse>>> getRooms(
            @RequestHeader("X-USER-ID") Long userId,
            @RequestParam(required = false)ChatRoomStatus status,
            Pageable pageable
            ) {
        Page<ChatRoomListResponse> response = chatRoomService.getRooms(userId, status, pageable);
        return ResponseEntity.status(HttpStatus.OK).body(ApiResponse.success(response));
    }

    @GetMapping("/rooms/{roomId}")
    public ResponseEntity<ApiResponse<ChatRoomDetailResponse>> getRoom(
            @RequestHeader("X-USER-ID")Long userId,
            @PathVariable Long roomId
    ){
        ChatRoomDetailResponse response = chatRoomService.getRoom(userId, roomId);
        return ResponseEntity.status(HttpStatus.OK).body(ApiResponse.success(response));
    }

}
