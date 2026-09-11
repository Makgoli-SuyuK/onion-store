package com.example.onionstore.domain.chat.dto.response;

import lombok.Getter;



import java.time.LocalDateTime;

@Getter
public class ChatMessageBroadcastResponse {
    private final String type;
    private final Long roomId;
    private final Long messageId;
    private final Long senderId;
    private final String senderName;
    private final String message;
    private final LocalDateTime sentAt;

    public ChatMessageBroadcastResponse(Long roomId, Long messageId, Long senderId,
                                        String senderName, String message, LocalDateTime sentAt){
        this.type = "TALK";
        this.roomId = roomId;
        this.messageId = messageId;
        this.senderId = senderId;
        this.senderName = senderName;
        this.message = message;
        this.sentAt = sentAt;
    }


}
