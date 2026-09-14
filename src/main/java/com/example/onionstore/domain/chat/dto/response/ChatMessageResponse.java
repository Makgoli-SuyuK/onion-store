package com.example.onionstore.domain.chat.dto.response;

import com.example.onionstore.domain.chat.entity.ChatMessage;
import lombok.AllArgsConstructor;
import lombok.Getter;


import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
public class ChatMessageResponse {
 private Long messageId;
 private Long senderId;
 private String senderName;
 private String message;
 private LocalDateTime sentAt;

    public ChatMessageResponse(ChatMessage message) {
        this.messageId = message.getId();
        this.senderId = message.getSender().getId();
        this.senderName = message.getSender().getName();
        this.message = message.getMessage();
        this.sentAt = message.getCreatedAt();
    }
}
