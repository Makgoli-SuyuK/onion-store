package com.example.onionstore.domain.chat.dto.response;


import com.example.onionstore.domain.chat.entity.ChatRoomStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@AllArgsConstructor

public class ChatRoomStatusUpdateResponse {
    private Long roomId;
    private ChatRoomStatus status;
    private LocalDateTime updateAt;

}
