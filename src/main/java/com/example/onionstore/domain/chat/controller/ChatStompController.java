package com.example.onionstore.domain.chat.controller;

import com.example.onionstore.domain.chat.dto.request.ChatMessageSendRequest;
import com.example.onionstore.domain.chat.dto.response.ChatMessageBroadcastResponse;
import com.example.onionstore.domain.chat.interceptor.AuthenticatedUser;
import com.example.onionstore.domain.chat.service.ChatMessageService;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

import java.security.Principal;

@Controller
@RequiredArgsConstructor
public class ChatStompController {

    private final ChatMessageService chatMessageService;
    private final SimpMessagingTemplate messagingTemplate;

    @MessageMapping("/chats/rooms/{roomId}/messages")
    public void sendMessage(
            @DestinationVariable Long roomId,
            ChatMessageSendRequest request,
            Principal principal
    ){
    Long userId = ((AuthenticatedUser) principal).getUserId();

        ChatMessageBroadcastResponse response =
                chatMessageService.sendMessage(userId, roomId, request.getMessage());

        messagingTemplate.convertAndSend("/sub/chats/rooms/" + roomId, response);
    }
}
